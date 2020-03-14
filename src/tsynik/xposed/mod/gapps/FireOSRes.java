package tsynik.xposed.mod.gapps;

import android.app.AndroidAppHelper;

import android.content.Context;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;
import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import tsynik.xposed.mod.gapps.BuildConfig;

public class FireOSRes implements IXposedHookZygoteInit
{
	private String MODULE_PATH;
	private int DRAWABLE_MENU_ITEMS;
	private static final String TAG = "FireOSRes";

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable
	{
		// System Wide Resources
		MODULE_PATH = startupParam.modulePath;
		XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, null);
		// XResources.setSystemWideReplacement("android", "bool", "config_unplugTurnsOnScreen", false);
		XResources.setSystemWideReplacement("amazon.fireos", "string", "temperatureobserver_thermalWarningText", modRes.getString(R.string.temperatureobserver_thermalWarningText));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "thermal_warning_device_hot", modRes.getString(R.string.thermal_warning_device_hot));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "thermal_warning_cool_down", modRes.getString(R.string.thermal_warning_cool_down));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "i18n_pn_generic", modRes.getString(R.string.i18n_pn_generic));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "i18n_pn_short", modRes.getString(R.string.i18n_pn_short));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "i18n_pn_medium", modRes.getString(R.string.i18n_pn_medium));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "i18n_pn_long", modRes.getString(R.string.i18n_pn_long));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "i18n_pn_full", modRes.getString(R.string.i18n_pn_full));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "i18n_pn_generational", modRes.getString(R.string.i18n_pn_generational));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "def_datamonitoring_limit_header", modRes.getString(R.string.def_datamonitoring_limit_header));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "def_datamonitoring_warning_reached", modRes.getString(R.string.def_datamonitoring_warning_reached));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "amazonshutdownmessage_power_off_message", modRes.getString(R.string.amazonshutdownmessage_power_off_message));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "def_cod_notif_title", modRes.getString(R.string.def_cod_notif_title));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "def_cod_notif_description", modRes.getString(R.string.def_cod_notif_description));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "amazonbootmessage_upgrading_title", modRes.getString(R.string.amazonbootmessage_upgrading_title));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "amazonbootmessage_upgrading_installing_text", modRes.getString(R.string.amazonbootmessage_upgrading_installing_text));
		XResources.setSystemWideReplacement("amazon.fireos", "string", "amazonbootmessage_upgrading_update_text", modRes.getString(R.string.amazonbootmessage_upgrading_update_text));
		// Disable Metrics collection
		XResources.setSystemWideReplacement("amazon.fireos", "bool", "config_amazonusagestats_record_boot_metric", false);
		XResources.setSystemWideReplacement("amazon.fireos", "bool", "config_amazonusagestats_record_usage_metrics", false);
		XResources.setSystemWideReplacement("amazon.fireos", "bool", "config_amazonusagestats_record_network_metrics", false);
		XResources.setSystemWideReplacement("amazon.fireos", "bool", "config_amazonusagestats_record_user_presence_metrics", false);
		XResources.setSystemWideReplacement("amazon.fireos", "bool", "config_amazonusagestats_process_screen_events", false);
		XResources.setSystemWideReplacement("amazon.fireos", "bool", "config_amazonusagestats_process_keyguard_events", false);
		XResources.setSystemWideReplacement("amazon.fireos", "bool", "config_amazonusagestats_process_dream_events", false);
		XResources.setSystemWideReplacement("amazon.fireos", "bool", "config_amazonusagestats_process_hdmi_events", false);
		XResources.setSystemWideReplacement("amazon.fireos", "bool", "config_amazonusagestats_amazon_app_collection", false);
		XResources.setSystemWideReplacement("amazon.fireos", "bool", "config_amazonusagestats_sideloaded_app_collection", false);
		XResources.setSystemWideReplacement("amazon.fireos", "bool", "config_amazonusagestats_usage_collection_setting", false);
		XResources.setSystemWideReplacement("amazon.fireos", "bool", "config_amazonusagestats_process_user_present", false);
		XResources.setSystemWideReplacement("amazon.fireos", "bool", "config_amazonusagestats_process_device_mode", false);
		// Launcher OOM Adjust
		XResources.setSystemWideReplacement("amazon.fireos", "array", "config_amazonoomadjpolicy_homeAdjProcNames", new String[]{"com.google.android.leanbacklauncher", "com.amazon.tv.launcher", "com.amazon.ags.app"});
		// Hook Power Off Layout amazonshutdownmessage_activity_powering_off
		// <LinearLayout
		//	android:gravity="center_horizontal"
		//	android:orientation="vertical"
		//	android:background="#ff000000"
		//	android:paddingLeft="@android:dimen/config_inCallNotificationVolume"
		//	android:paddingRight="@android:dimen/config_inCallNotificationVolume"
		//	android:layout_width="fill_parent"
		//	android:layout_height="fill_parent"
		//	xmlns:android="http://schemas.android.com/apk/res/android">
		//	<TextView
		//		android:textAppearance="?android:textAppearanceSmall"
		//		android:textColor="#ffffffff"
		//		android:layout_gravity="center_horizontal"
		//		android:id="@id/power_off_message"
		//		android:paddingTop="@android:dimen/config_inCallNotificationVolume"
		//		android:paddingBottom="@android:dimen/config_inCallNotificationVolume"
		//		android:layout_width="wrap_content"
		//		android:layout_height="wrap_content"
		//		android:fontFamily="helvetica_lt_75_bold" />
		// </LinearLayout>
		XResources.hookSystemWideLayout("amazon.fireos", "layout", "amazonshutdownmessage_activity_powering_off", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				if (BuildConfig.DEBUG) Log.d(TAG, "### initZygote for amazonshutdownmessage_activity_powering_off");
				liparam.view.setBackgroundColor(Color.parseColor("#BF010C12"));
				// Context context = (Context) AndroidAppHelper.currentApplication();
				// int width = (context.getResources().getDisplayMetrics().widthPixels / 2);
				// int height = (context.getResources().getDisplayMetrics().heightPixels / 2);
				// if (BuildConfig.DEBUG) Log.d(TAG, "### W : H ### " + width + " : " + height);
				TextView poweroff = (TextView) liparam.view.findViewById(liparam.res.getIdentifier("power_off_message", "id", "amazon.fireos"));
				poweroff.setTextColor(Color.parseColor("#FFA724"));
				poweroff.setTextSize(22.0f);
				poweroff.setPadding(60, 60, 60, 60);
				poweroff.setTypeface(Typeface.create("helvetica_ne_lt_45_lt",Typeface.NORMAL)); // Amazon Ember Light
			}
		});
	}
}
