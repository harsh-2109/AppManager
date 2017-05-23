package com.deeppandya.appmanager.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.deeppandya.appmanager.R;
import com.deeppandya.appmanager.activities.PermissionsActivity;
import com.deeppandya.appmanager.enums.AppCategory;
import com.deeppandya.appmanager.enums.AppSortType;
import com.deeppandya.appmanager.enums.AppType;
import com.deeppandya.appmanager.listeners.GetAppsView;
import com.deeppandya.appmanager.model.AppModel;
import com.deeppandya.appmanager.util.CommonFunctions;
import com.deeppandya.appmanager.managers.PersistanceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by d_pandya on 3/7/17.
 */

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private final GetAppsView getAppsView;
    private View view;
    private Context context;
    private AppCategory appCategory;
    private AppSortType appSortType;
    private List<AppModel> selectedItems;
    private List<AppModel> appList;
    private List<AppModel> appModelBackUpList;

    public AppAdapter(View view, Context context, GetAppsView getAppsView) {
        this.view = view;
        this.context = context;
        this.getAppsView = getAppsView;
        selectedItems = new ArrayList<>();
        appModelBackUpList = new ArrayList<AppModel>();
    }

    public void setAppCategory(AppCategory appCategory) {
        this.appCategory = appCategory;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView txtAppName;
        LinearLayout appLayout;
        TextView txtAppDesc;
        ImageButton appProperties;
        Button btnUninstall, btnBackup, btnPermission, btnPackage;

        public ViewHolder(View view) {
            super(view);
            txtAppName = (TextView) view.findViewById(R.id.app_name);
            appIcon = (ImageView) view.findViewById(R.id.app_icon);
            appLayout = (LinearLayout) view.findViewById(R.id.app_layout);
            txtAppDesc = (TextView) view.findViewById(R.id.app_desc);
            appProperties = (ImageButton) view.findViewById(R.id.app_properties);

            btnUninstall = (Button) view.findViewById(R.id.btnUninstall);
            btnBackup = (Button) view.findViewById(R.id.btnBackup);
            btnPermission = (Button) view.findViewById(R.id.btnPermission);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_row_layout, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.appIcon.setImageDrawable(appList.get(position).getAppIcon());
        holder.txtAppName.setText(appList.get(position).getAppName());

        if (appSortType == AppSortType.BYDATE) {
            holder.txtAppDesc.setText(appList.get(position).getFormattedDate());
        } else {
            holder.txtAppDesc.setText(appList.get(position).getSize());
        }


        if (holder.appProperties != null) {
            holder.appProperties.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(v, appList.get(position));
                }
            });
        }

        if (appCategory == AppCategory.UNINSTALL) {
            if (selectedItems.contains(appList.get(position))) {
                holder.appLayout.setActivated(true);
                holder.btnUninstall.setVisibility(View.GONE);
                holder.appProperties.setVisibility(View.GONE);
            } else {
                holder.appLayout.setActivated(false);
                holder.btnUninstall.setVisibility(View.VISIBLE);
                holder.appProperties.setVisibility(View.VISIBLE);
                holder.btnUninstall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CommonFunctions.uninstallApp(context, appList.get(position));
                    }
                });
            }
        } else if (appCategory == AppCategory.BACKUP) {
            if (selectedItems.contains(appList.get(position))) {
                holder.appLayout.setActivated(true);
                holder.btnBackup.setVisibility(View.GONE);
                holder.appProperties.setVisibility(View.GONE);
            } else {
                holder.appLayout.setActivated(false);
                holder.btnBackup.setVisibility(View.VISIBLE);
                holder.appProperties.setVisibility(View.VISIBLE);
                holder.btnBackup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appModelBackUpList.clear();
                        appModelBackUpList.add(appList.get(position));
                        getAppsView.createAppBackup();
                    }
                });
            }
        } else if (appCategory == AppCategory.PERMISSIONS) {
            holder.btnPermission.setVisibility(View.VISIBLE);
            if (appList.get(position).getPermissions() != null && appList.get(position).getPermissions().length > 0) {
                holder.btnPermission.setText(context.getResources().getString(R.string.permission));
                holder.btnPermission.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //showPermissions(appList.get(position));

                        String[] permissions=new String[appList.get(position).getPermissions().length];
                        for(int i=0;i<appList.get(position).getPermissions().length;i++){
                            permissions[i]=(appList.get(position).getPermissions()[i]).toString();
                        }

                        Intent intent=new Intent(context, PermissionsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("permissions", permissions);
                        intent.putExtra("appName", appList.get(position).getAppName());
                        intent.putExtra("packageName", appList.get(position).getPackageName());
                        context.startActivity(intent);
                    }
                });
            } else {
                holder.btnPermission.setText(context.getResources().getString(R.string.no_permission));
                holder.btnPermission.setOnClickListener(null);
            }

            if (appList.get(position).getPermissions()!=null && appList.get(position).getPermissions().length > 0)
                holder.txtAppDesc.setText(String.format(context.getResources().getString(R.string.number_of_permissions), appList.get(position).getPermissions().length));
            else
                holder.txtAppDesc.setText(String.format(context.getResources().getString(R.string.number_of_permissions), 0));

        } else if (appCategory == AppCategory.PACKAGE) {
            holder.txtAppDesc.setText(appList.get(position).getPackageName());
        }
    }

    private void showPermissions(final AppModel appModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(appModel.getAppName() + " " + context.getResources().getString(R.string.permission));
        builder.setItems(appModel.getPermissions(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                try {
                    PermissionInfo pinfo = context.getPackageManager().getPermissionInfo(appModel.getPermissions()[item].toString(), PackageManager.GET_META_DATA);
                    showPermissionDesc(appModel.getPermissions()[item].toString(), pinfo.loadLabel(context.getPackageManager()).toString());
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        AlertDialog alert = builder.create();

        ListView listView = alert.getListView();
        listView.setDivider(new ColorDrawable(context.getResources().getColor(R.color.colorAccent))); // set color
        listView.setDividerHeight(2); // set height

        alert.show();
    }

    private void showPermissionDesc(String permission, String permissionDesc) {
        new MaterialDialog.Builder(context)
                .title(permission)
                .content(permissionDesc)
                .positiveText(R.string.ok)
                .show();

    }

    @Override
    public int getItemCount() {
        return appList != null ? appList.size() : 0;
    }

    public void toggleSelection(int pos) {
        if (selectedItems.contains(appList.get(pos))) {
            selectedItems.remove(appList.get(pos));
        } else {
            selectedItems.add(appList.get(pos));
        }

        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<AppModel> getSelectedItems() {
        return selectedItems;
    }

    public void setAppList(List<AppModel> appList) {
        this.appSortType = PersistanceManager.getSortType(context);
        this.appList = appList;
    }

    void showPopup(View view, final AppModel appModel) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.open:
                        CommonFunctions.openApp(context, appModel);
                        return true;
                    case R.id.share:
                        CommonFunctions.shareApp(((Activity) context), appModel.getAppName(), appModel.getPackageName());
                        return true;
                    case R.id.play:
                        CommonFunctions.openAppInPlayStore(context, appModel);
                        return true;
                    case R.id.app_properties:
                        CommonFunctions.openAppProperties(context, appModel.getPackageName());
                        return true;
                }
                return false;
            }
        });

        popupMenu.inflate(R.menu.app_options);
        if (appModel.getAppType() == AppType.USERAPP) {
            popupMenu.getMenu().findItem(R.id.share).setVisible(true);
            popupMenu.getMenu().findItem(R.id.play).setVisible(true);
            popupMenu.getMenu().findItem(R.id.open).setVisible(true);
        } else {
            popupMenu.getMenu().findItem(R.id.share).setVisible(false);
            popupMenu.getMenu().findItem(R.id.play).setVisible(false);
            popupMenu.getMenu().findItem(R.id.open).setVisible(false);
        }
        popupMenu.show();
    }

    public List<AppModel> getAppModelBackUpList() {
        return appModelBackUpList;
    }
}
