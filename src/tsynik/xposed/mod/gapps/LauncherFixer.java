package tsynik.xposed.mod.gapps;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import java.util.List;
import java.util.Set;

import tsynik.xposed.mod.gapps.BuildConfig;

@SuppressWarnings("WeakerAccess")
public class LauncherFixer implements IXposedHookLoadPackage {

	private static final String TAG = "LauncherFixer";
    static final String FIX_PACKAGE = "com.google.android.leanbacklauncher";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam)
            throws Throwable {

        if (lpparam.packageName.equals("com.google.android.leanbacklauncher")) {
			// FC
			Class<?> MainActivity = XposedHelpers.findClass("com.google.android.leanbacklauncher.MainActivity", lpparam.classLoader);
			XposedHelpers.findAndHookMethod(MainActivity, "onStart", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					// this.mAppWidgetHost = null
					Activity activity = (Activity) param.thisObject;
					if (BuildConfig.DEBUG) Log.i(TAG, "### mAppWidgetHost ### onStart");
					XposedHelpers.setObjectField(activity, "mAppWidgetHost", null);
		    	}
			});
			// FC
			Class<?> WallpaperInstaller = XposedHelpers.findClass("com.google.android.leanbacklauncher.wallpaper.WallpaperInstaller", lpparam.classLoader);
			XposedHelpers.findAndHookMethod(WallpaperInstaller, "installWallpaper", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(null);
		    	}
			});
			// Search Orb
			Class<?> SearchOrbView = XposedHelpers.findClass("com.google.android.leanbacklauncher.SearchOrbView", lpparam.classLoader);
			XposedHelpers.findAndHookMethod(SearchOrbView, "useWahlbergUx", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(true);
		    	}
			});
        }

		if (lpparam.packageName.equals("com.amazon.tv.launcher")) {
			// force frozenMode in Amazon Launcher
			XposedHelpers.findAndHookMethod("com.amazon.tv.GlobalSettings", lpparam.classLoader, "getFrozenMode", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable
				{
					if (BuildConfig.DEBUG) Log.i(TAG, "### GlobalSettings ### override getFrozenMode to true");
					// FREEZE KFTV
					param.setResult(true);
				}
			});
		}

		if (lpparam.packageName.equals("android")) {
			// Use the leanback / user launcher instead of the Amazon launcher
			XposedHelpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "chooseBestActivity", Intent.class, String.class, int.class, List.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					@SuppressWarnings("unchecked")
					List<ResolveInfo> query = (List<ResolveInfo>)param.args[3];
					Intent intent = (Intent)param.args[0];
					Set<String> categories = intent.getCategories();
					Bundle extras = intent.getExtras();
					boolean loadSettings = false;
					// DEBUG FireOS 6.2.6.8
					// 0 : com.amazon.tv.launcher.ui.HomeActivity_vNext priority: 950
					// 1 : com.google.android.leanbacklauncher.MainActivity priority: 2
					// 2 : com.amazon.firehomestarter.HomeStarterActivity priority: 1
					// 3 : com.amazon.tv.leanbacklauncher.MainActivity priority: 0
					// 4 : com.amazon.tv.settings.v2.system.FallbackHome priority: -1000
					if (Intent.ACTION_MAIN.equals(intent.getAction())
						&& categories != null
						&& categories.size() == 1
						&& categories.contains(Intent.CATEGORY_HOME)) {
						// check if we load Settings
						if (extras != null && extras.containsKey("navigate_node") && extras.get("navigate_node").equals("l_settings")) {
							loadSettings = true;
						}
						// find launcher index
						int index = 0;
						for (int i=0; i < query.size(); i++) {
							// if (BuildConfig.DEBUG) Log.d(TAG, i + " : " + query.get(i).activityInfo.name + " priority: " + query.get(i).priority);
							if (query.get(i).activityInfo.name.contains("com.google.android.leanbacklauncher")) {
								index = i;
								if (BuildConfig.DEBUG) Log.d(TAG, "### L ### found leanbacklauncher at index " + index);
								break;
							}
							if (query.get(i).priority == 0) { // allow override with user launcher
								index = i;
								if (BuildConfig.DEBUG) Log.d(TAG, "### L ### found user launcher at index " + index);
								break;
							}
						}
						// if user or leanback launcher found and 1st one is Amazon Launcher
						// swap them so the user one is used instead
						if (index > 0
							&& query.get(0).activityInfo.name.contains("com.amazon.tv.launcher.ui.HomeActivity")
							&& !loadSettings)
						{
							ResolveInfo userLauncher = query.get(index);
							query.set(index, query.get(0));
							query.set(0, userLauncher);
						}
					}
				}
			});
		}
    }
}
