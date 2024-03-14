package com.android.settings.compatible;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.os.UserHandle;
import android.os.UserManager;
import android.content.Context;

import com.android.settings.utils.StringUtils;

public class CompUtils {

    public static List<AppData> getAllApps(Context context) {
        List<AppData> list = new ArrayList<>();
        try {
            LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            List<UserHandle> userHandles = userManager.getUserProfiles();
            List<LauncherActivityInfo> activityInfoList = new ArrayList<>();
            for (UserHandle userHandle : userHandles) {
                activityInfoList.addAll(launcherApps.getActivityList(null, userHandle));
            }

            for (LauncherActivityInfo info : activityInfoList) {
                AppData appData = new AppData();
                appData.setName(StringUtils.ToString(info.getLabel()));
                appData.setPackageName(StringUtils.ToString(info.getComponentName().getPackageName()));
                appData.setIcon(info.getIcon(0));
                appData.setComponentName(info.getComponentName());
                list.add(appData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static AppData getAppInfo(Context context, String packageName) {
        List<AppData> appList = getAllApps(context);
        Optional<AppData> result = appList.stream()
                .filter(appData -> appData.getPackageName().equals(packageName))
                .findFirst();
        return result.orElse(null);
    }

    public static boolean isChineseLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.equals("zh");
    }

}
