package tsynik.xposed.mod.gapps;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import tsynik.xposed.mod.gapps.BuildConfig;

public class USBStorage implements IXposedHookLoadPackage
{
	private static final String TAG = "USBStorage";

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{
		// Amazon Settings v2 hook
		if (lpparam.packageName.equals("com.amazon.tv.settings.v2"))
		{
			findAndHookMethod("com.amazon.tv.config.DeviceInfo", lpparam.classLoader, "hasAdaptableStorageSupport", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable
				{
					if (BuildConfig.DEBUG) Log.i(TAG, "### com.amazon.tv.config.DeviceInfo ### override hasAdaptableStorageSupport to true");
					// return TRUE
					param.setResult(true);
				}
			});

			findAndHookMethod("com.amazon.tv.config.DeviceInfo", lpparam.classLoader, "hasExternalStorage", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable
				{
					if (BuildConfig.DEBUG) Log.i(TAG, "### com.amazon.tv.config.DeviceInfo ### override hasExternalStorage to true");
					// return TRUE
					param.setResult(true);
				}
			});
		}
	}
}
