package tsynik.xposed.mod.gapps;

import android.content.ContentValues;
import android.content.pm.Signature;
import android.content.res.XResources;
import android.net.Uri;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.lang.reflect.Method;

import tsynik.xposed.mod.gapps.BuildConfig;

public class Main implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    public void initZygote(StartupParam startupParam) throws Throwable {
        log("initZygote");
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

		try {
			Class<?> ContentProviderProxy = XposedHelpers.findClassIfExists("android.content.ContentProviderProxy", lpparam.classLoader);
			if (ContentProviderProxy != null) XposedHelpers.findAndHookMethod(ContentProviderProxy, "insert", Uri.class, ContentValues.class, new XC_MethodHook() {

			// Method m = XposedHelpers.findMethodExact(ContentProviderProxy, "insert", new Class[]{Uri.class, ContentValues.class});
			// XposedBridge.hookMethod(m, new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					ContentValues values = (ContentValues) param.args[1];
					String npackage = values.getAsString("notificationpackage");
					if (npackage != null && npackage.equals("com.android.vending")) {
						values.put("allowed_network_types", 3);
						values.put("app_item_id_amz", 0);
						values.put("header_flags_amz", "Content-Length::Content-Type::ETag");
						values.put("allow_metered", true);
						values.put("is_visible_in_downloads_ui", true);
						values.put("content_type", 1);
						values.put("is_public_api", 1);
						values.put("allow_roaming", true);
						param.args[1] = values;
					}
					log(String.valueOf(ContentProviderProxy.getName()) + ". hooked in " + lpparam.packageName);
				}
			});
			log(String.valueOf(ContentProviderProxy.getName()) + ".insert hooked");
		} catch (Throwable t3) {
			log(t3);
		}

        if (lpparam.packageName.equals("com.android.providers.downloads")) {
            try { // Fix Downloads Checks
                Class<?> DownloadProvider = XposedHelpers.findClass("com.android.providers.downloads.DownloadProvider", lpparam.classLoader);
                Method m = XposedHelpers.findMethodExact(DownloadProvider, "checkInsertPermissions", new Class[]{ContentValues.class});
                XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(0));
                log(String.valueOf(DownloadProvider.getName()) + "." + m.getName() + " hooked");
            } catch (Throwable t) {
                log(t);
            }
        }
        if (lpparam.packageName.equals("android") && lpparam.processName.equals("android")) {
			try { // No Signature Checks
				Class<?> PackageManagerService = XposedHelpers.findClass("com.android.server.pm.PackageManagerService", lpparam.classLoader);
				Method compareSignatures = XposedHelpers.findMethodExact(PackageManagerService, "compareSignatures", new Class[]{Signature[].class, Signature[].class});
				XposedBridge.hookMethod(compareSignatures, XC_MethodReplacement.returnConstant(0));
				log(String.valueOf(PackageManagerService.getName()) + "." + compareSignatures.getName() + " hooked");
			} catch (Throwable t2) {
				log(t2);
			}
        }
    }

    /* access modifiers changed from: private */
    public static void log(String msg) {
        if (BuildConfig.DEBUG) XposedBridge.log("Main ===> " + msg);
    }

    private static void log(Throwable msg) {
        if (BuildConfig.DEBUG) XposedBridge.log(msg);
    }

    private static void parseContentValues(ContentValues values) {
        log("###############");
        for (String name : values.keySet()) {
            log(String.valueOf(name) + " : " + values.getAsString(name));
        }
        log("###############");
    }
}
