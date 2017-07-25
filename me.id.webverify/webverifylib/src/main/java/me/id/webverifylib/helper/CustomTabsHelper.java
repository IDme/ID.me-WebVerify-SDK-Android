package me.id.webverifylib.helper;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import me.id.webverifylib.R;

/**
 * Created by mirland on 13/03/17.
 */
public class CustomTabsHelper {
  private static final String STABLE_PACKAGE = "com.android.chrome";
  private static final String BETA_PACKAGE = "com.chrome.beta";
  private static final String DEV_PACKAGE = "com.chrome.dev";
  private static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
  private static final String ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService";

  public static CustomTabsIntent getCustomTabIntent(Context context, String url) {
    //noinspection deprecation
    CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
        .setToolbarColor(context.getResources().getColor(R.color.idme_blue_dark))
        .enableUrlBarHiding()
        .setShowTitle(false)
        .build();
    String packageName = CustomTabsHelper.getBestPackageNameToUse(context, url);
    if (packageName != null) {
      customTabsIntent.intent.setPackage(packageName);
    }
    customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
        | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    return customTabsIntent;
  }

  private static String getBestPackageNameToUse(Context context, String url) {
    PackageManager packageManager = context.getPackageManager();
    // Get default View intent handler.
    Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    ResolveInfo defaultViewHandlerInfo = packageManager.resolveActivity(activityIntent, 0);
    String defaultViewHandlerPackageName = null;
    if (defaultViewHandlerInfo != null) {
      defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;
    }

    // Get all apps that can handle VIEW intents.
    List<ResolveInfo> resolvedActivityList = packageManager.queryIntentActivities(activityIntent, 0);
    List<String> packagesSupportingCustomTabs = new ArrayList<>();
    for (ResolveInfo info : resolvedActivityList) {
      Intent serviceIntent = new Intent();
      serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
      serviceIntent.setPackage(info.activityInfo.packageName);
      if (packageManager.resolveService(serviceIntent, 0) != null) {
        packagesSupportingCustomTabs.add(info.activityInfo.packageName);
      }
    }

    if (packagesSupportingCustomTabs.isEmpty()) {
      return null;
    } else if (packagesSupportingCustomTabs.size() == 1) {
      return packagesSupportingCustomTabs.get(0);
    } else if (!TextUtils.isEmpty(defaultViewHandlerPackageName)
        && !hasSpecializedHandlerIntents(context, activityIntent)
        && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)) {
      return defaultViewHandlerPackageName;
    } else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
      return STABLE_PACKAGE;
    } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
      return BETA_PACKAGE;
    } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
      return DEV_PACKAGE;
    } else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) {
      return LOCAL_PACKAGE;
    }
    return null;
  }

  private static boolean hasSpecializedHandlerIntents(Context context, Intent intent) {
    try {
      PackageManager packageManager = context.getPackageManager();
      List<ResolveInfo> handlers = packageManager.queryIntentActivities(
          intent,
          PackageManager.GET_RESOLVED_FILTER
      );
      if (handlers == null || handlers.size() == 0) {
        return false;
      }
      for (ResolveInfo resolveInfo : handlers) {
        IntentFilter filter = resolveInfo.filter;
        if (filter == null) {
          continue;
        }
        if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) {
          continue;
        }
        if (resolveInfo.activityInfo == null) {
          continue;
        }
        return true;
      }
    } catch (RuntimeException ignored) {
      // The os will define the resolver application
    }
    return false;
  }
}
