package tsynik.xposed.mod.gapps;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import java.lang.Object;
import java.util.Set;

import tsynik.xposed.mod.gapps.BuildConfig;

public class MarketFixer implements IXposedHookLoadPackage
{
	private static final String TAG = "MarketFixer";

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{
		// Market Downloads Fix
		if (lpparam.packageName.equals("com.android.providers.downloads"))
		{
			if (BuildConfig.DEBUG) Log.i(TAG, "### IN ### " + lpparam.packageName);
			Class<?> DownloadProvider = XposedHelpers.findClass("com.android.providers.downloads.DownloadProvider", lpparam.classLoader);
			XposedHelpers.findAndHookMethod(DownloadProvider, "insert", Uri.class, ContentValues.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					// is_public_api: 1, 2 (2 - Amazon AppStore)
			    	// destination: 2, 4, 6 (4 - external)
			    	// visibility: 2, 0, 1, 3 (0 - visible)
			    	// allowed_network_types: -1, 2 (-1 - all, 2 - wifi)
			    	// is_visible_in_downloads_ui: false / true
			    	ContentValues cv = (ContentValues) param.args[1];
			    	if (BuildConfig.DEBUG) parseContentValues(cv);
			    	if (cv.containsKey("notificationpackage") && cv.getAsString("notificationpackage").equals("com.android.vending")) {
			    		// fix is_public_api = boolean
						if (cv.containsKey("is_public_api") && cv.getAsString("is_public_api").equals("true")) {
							if (BuildConfig.DEBUG) Log.d(TAG, "### is_public_api ### FIX");
							// cv.remove("is_public_api");
							cv.put("is_public_api", 1);
						}
						if (cv.containsKey("otheruid")) {
							if (BuildConfig.DEBUG) Log.d(TAG, "### otheruid ### FIX");
							cv.remove("otheruid");
						}
						// fix allowed_network_types = all
						if (cv.containsKey("uri") && !cv.containsKey("allowed_network_types")) {
							if (BuildConfig.DEBUG) Log.d(TAG, "### allowed_network_types ### FIX");
							cv.put("allowed_network_types", -1);
							cv.put("allow_metered", true);
						}
			    	}
					if (BuildConfig.DEBUG) parseContentValues(cv);
					param.args[1] = cv;
		    	}
			});
		}
		// Download Complete Fix
		if (lpparam.packageName.equals("com.android.vending"))
		{
//			if (BuildConfig.DEBUG) Log.i(TAG, "### IN ### " + lpparam.packageName);
//			Class<?> DownloadBroadcastReceiver = XposedHelpers.findClass("com.google.android.finsky.download.DownloadBroadcastReceiver", lpparam.classLoader);
//			XposedHelpers.findAndHookMethod(DownloadBroadcastReceiver, "a", Context.class, Intent.class, new XC_MethodHook() {
//				@Override
//				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//					Intent intent = (Intent) param.args[1];
//					// https://stackoverflow.com/questions/5968896/listing-all-extras-of-an-intent
//					if (BuildConfig.DEBUG) Log.d(TAG, "### REC ### " + intent.toUri(0));
//		    	}
//			});
			// fix "Unable to find %s in download manager"
			// RCV referenced in DownloadBroadcastReceiver a()
			// izz for com.android.vending 18.3.27
			Class<?> RCV = XposedHelpers.findClassIfExists("izz", lpparam.classLoader);
			if (RCV != null) XposedHelpers.findAndHookMethod(RCV, "doInBackground", Object[].class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					// return code 200 (SUCCESS)
					// if (BuildConfig.DEBUG) Log.d(TAG, "### RET ### 200");
					param.setResult(200);
		    	}
			});
			// Break Market self-update routine ("Skipping DFE self-update")
			// tfk for com.android.vending 18.3.27
			Class<?> SU = XposedHelpers.findClassIfExists("tfk", lpparam.classLoader);
			Class<?> P0 = XposedHelpers.findClassIfExists("tfd", lpparam.classLoader);
			Class<?> P1 = XposedHelpers.findClassIfExists("dyh", lpparam.classLoader);
			Class<?> P2 = XposedHelpers.findClassIfExists("dva", lpparam.classLoader);
			if (SU != null && P0 != null && P1 != null && P2 != null) XposedHelpers.findAndHookMethod(SU, "a", P0, P1, P2, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					// return false
					if (BuildConfig.DEBUG) Log.d(TAG, "### RET ### false");
					param.setResult(false);
		    	}
			});
		}
	}
    public static void log(String msg) {
        XposedBridge.log(TAG + " ===> " + msg);
    }
    private static void parseContentValues(ContentValues values) {
        log("###############");
        for (String name : values.keySet()) {
            log(String.valueOf(name) + " : " + values.getAsString(name));
        }
        log("###############");
    }
}
