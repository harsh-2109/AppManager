package com.deeppandya.appmanager.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.deeppandya.appmanager.R;
import com.deeppandya.appmanager.activities.MainActivity;
import com.deeppandya.appmanager.adapter.AppAdapter;
import com.deeppandya.appmanager.asynctask.AppListLoader;
import com.deeppandya.appmanager.asynctask.GetAppsAsyncTask;
import com.deeppandya.appmanager.enums.AppCategory;
import com.deeppandya.appmanager.enums.AppType;
import com.deeppandya.appmanager.listeners.GetAppsListener;
import com.deeppandya.appmanager.managers.FirebaseManager;
import com.deeppandya.appmanager.managers.PersistanceManager;
import com.deeppandya.appmanager.model.AppModel;
import com.deeppandya.appmanager.util.CommonFunctions;
import com.deeppandya.appmanager.util.DividerItemDecoration;
import com.deeppandya.appmanager.util.FileListSorter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserAppFragment extends Fragment{

    private RecyclerView recyclerView;
    private AppAdapter mAdapter;
    private List<AppModel> apps;

    public static final int ID_LOADER_APP_LIST = 0;

    private View rootView;
    private AppCategory appCategory;

    public UserAppFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView= inflater.inflate(R.layout.fragment_user_app, container, false);

        if(getArguments()!=null && getArguments().get("category")!=null){
            appCategory=(AppCategory)getArguments().get("category");
        }

        apps=new ArrayList<>();

        setHintLayout();

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        mAdapter = new AppAdapter(getActivity().findViewById(android.R.id.content), getActivity());

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter.setAppCategory(appCategory);

        recyclerView.setAdapter(mAdapter);

        getApps();

        return rootView;
    }

    private void getApps() {
        GetAppsListener getAppsListener=new GetAppsListener() {
            @Override
            public void beforeGetApps() {
                recyclerView.setVisibility(View.GONE);
                rootView.findViewById(R.id.mainProgress).setVisibility(View.VISIBLE);
            }

            @Override
            public void afterGetApps(List<AppModel> appModels) {
                apps.clear();
                apps=appModels;

                recyclerView.setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.mainProgress).setVisibility(View.GONE);

                setAppAdapter(true,false,"");
            }

            @Override
            public void onError() {
                rootView.findViewById(R.id.mainProgress).setVisibility(View.GONE);
            }
        };

        GetAppsAsyncTask getAppsAsyncTask=new GetAppsAsyncTask(getActivity(),getAppsListener);
        getAppsAsyncTask.execute();

    }

    private void setHintLayout() {
        TextView txtHint=(TextView)rootView.findViewById(R.id.txtHint);
        final CardView hintLayout=(CardView)rootView.findViewById(R.id.hint_layout);
        if(appCategory==AppCategory.UNINSTALL){
            txtHint.setText(getResources().getString(R.string.long_press_hint));
        }else if(appCategory==AppCategory.BACKUP){
            txtHint.setText(String.format(getResources().getString(R.string.backup_can_be_found),CommonFunctions.getBackupDir()));
        }else{
            hintLayout.setVisibility(View.GONE);
        }

        ImageView btnClose=(ImageView)rootView.findViewById(R.id.btnHintCancel);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hintLayout.setVisibility(View.GONE);
            }
        });

    }

    private void setAppAdapter(boolean isUserApps, boolean isSystemApps,String query) {

        if(apps!=null && apps.size()>0){

            List<AppModel> tempApps=new ArrayList<>();

            for(AppModel appModel:apps){

                if(appModel.getAppName().toLowerCase().contains(query.toLowerCase())){
                    if(isUserApps && appModel.getAppType()== AppType.USERAPP){
                        tempApps.add(appModel);
                    }else if(isSystemApps && appModel.getAppType()==AppType.SYSTEMAPP){
                        tempApps.add(appModel);
                    }else if(!isUserApps && !isSystemApps){
                        tempApps.add(appModel);
                    }
                }
            }

            Collections.sort(tempApps, new FileListSorter(PersistanceManager.getSortType(getActivity()), PersistanceManager.getSortOrder(getActivity())));

            if(mAdapter!=null){
                mAdapter.setAppList(tempApps);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}