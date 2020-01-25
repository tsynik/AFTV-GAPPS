package tsynik.xposed.mod.gapps;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.app.Notification;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import java.util.UUID;

import tsynik.xposed.mod.gapps.BuildConfig;

@SuppressWarnings("WeakerAccess")
public class RecsFixer implements IXposedHookLoadPackage {

	private static final String TAG = "RecsFixer";
	private static Bitmap icon;

	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam)
			throws Throwable {

		if (lpparam.packageName.equals("com.amazon.device.sale.service")) {

			Class<?> NotificationImageHandler = XposedHelpers.findClassIfExists("com.amazon.device.sale.service.handlers.NotificationImageHandler", lpparam.classLoader);
			// processLargeIcon(Notification notification, String pkgName, int notificationId, UUID objectID, String categories)
			if (NotificationImageHandler != null) XposedHelpers.findAndHookMethod(NotificationImageHandler, "processLargeIcon", Notification.class, String.class, int.class, UUID.class, String.class, new XC_MethodHook() {

//				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
//					icon = (Bitmap) XposedHelpers.getObjectField(param.args[0], "largeIcon");
//					if (BuildConfig.DEBUG) Log.d(TAG, "### processLargeIcon ### MR " + icon);
//					AsyncTask<Void, Void, Void> as = (AsyncTask) XposedHelpers.callStaticMethod(NotificationImageHandler, "processImages", icon, param.args[1], param.args[2], param.args[3], "largeicon" , param.args[4]);
//					return as;
//				}

				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					// Notification notification, String pkgName, int notificationId, UUID objectID, String categories
					icon = (Bitmap) XposedHelpers.getObjectField(param.args[0], "largeIcon");
					if (BuildConfig.DEBUG) Log.d(TAG, "### processLargeIcon ### thisObject " + param.thisObject);
					if (BuildConfig.DEBUG) Log.d(TAG, "### processLargeIcon ### icon " + icon);
					if (BuildConfig.DEBUG) Log.d(TAG, "### processLargeIcon ### pkgName " + param.args[1]);
					if (BuildConfig.DEBUG) Log.d(TAG, "### processLargeIcon ### notificationId " + param.args[2]);
					if (BuildConfig.DEBUG) Log.d(TAG, "### processLargeIcon ### UUID " + param.args[3]);
					if (BuildConfig.DEBUG) Log.d(TAG, "### processLargeIcon ### categories " + param.args[4]);

					// .param p1, "bmp"    # Landroid/graphics/Bitmap;
					// .param p2, "pkgName"    # Ljava/lang/String;
					// .param p3, "notificationId"    # I
					// .param p4, "objectID"    # Ljava/util/UUID;
					// .param p5, "bmpkey"    # Ljava/lang/String;
					// .param p6, "categories"    # Ljava/lang/String;
					AsyncTask<Void, Void, Void> as = (AsyncTask) XposedHelpers.callMethod(param.thisObject, "processImages", icon, param.args[1], param.args[2], param.args[3], "largeicon" , param.args[4]);
					// AsyncTask<Void, Void, Void> asyncTask = processImages(notification.largeIcon, pkgName, notificationId, objectID, SaleConstant.LARGE_ICON_IMAGE, categories);
					// Lcom/amazon/device/sale/service/handlers/NotificationImageHandler;->processImages(Landroid/graphics/Bitmap;Ljava/lang/String;ILjava/util/UUID;Ljava/lang/String;Ljava/lang/String;)Landroid/os/AsyncTask;
					param.setResult(as);
				}
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//					XposedHelpers.setObjectField(param.args[0], "largeIcon", icon);
					icon = (Bitmap) XposedHelpers.getObjectField(param.args[0], "largeIcon");
					if (BuildConfig.DEBUG) Log.d(TAG, "### processLargeIcon ### afterHookedMethod largeIcon " + icon);
				}
			});
//			Class<?> AbstractRecommendationHandler = XposedHelpers.findClass("com.amazon.device.sale.service.handlers.AbstractRecommendationHandler", lpparam.classLoader);
//			// void processNotification(StatusBarNotification sbn)
//			XposedHelpers.findAndHookMethod(AbstractRecommendationHandler, "processNotification", StatusBarNotification.class, new XC_MethodHook() {
//				@Override
//				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//					if (BuildConfig.DEBUG) Log.d(TAG, "### processNotification ### NULL ");
//					param.setResult(null);
//				}
//			});
		}
	}
}
