package tsynik.xposed.mod.gapps;

import android.util.Log;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import tsynik.xposed.mod.gapps.BuildConfig;

public class MetricFixer implements IXposedHookLoadPackage
{
	private static final String TAG = "MetricFixer";

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{

		if (!lpparam.packageName.equals("android")) {

			// XposedBridge.log("### MetricFixer called for package: " + lpparam.packageName);
			Class<?> MetricsServiceConnection = XposedHelpers.findClass("com.amazon.client.metrics.MetricsServiceConnection", lpparam.classLoader);
			XposedHelpers.findAndHookMethod(MetricsServiceConnection, "getService", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					//if (BuildConfig.DEBUG) Log.i(TAG, "### MetricsApi ### override getService to null");
					param.setResult(null);
				}
			});

			Class<?> AndroidMetricsFactoryImpl = XposedHelpers.findClass("com.amazon.client.metrics.AndroidMetricsFactoryImpl", lpparam.classLoader);
			XposedHelpers.findAndHookMethod(AndroidMetricsFactoryImpl, "shouldRecordMetrics", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					//if (BuildConfig.DEBUG) Log.i(TAG, "### MetricsApi ### override shouldRecordMetrics to false");
					param.setResult(false);
				}
			});

		}
	}
}
