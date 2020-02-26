package tsynik.xposed.mod.gapps;

import java.io.File;
import java.util.Map.Entry;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import tsynik.xposed.mod.gapps.BuildConfig;

public class KeyBindings implements IXposedHookZygoteInit, IXposedHookLoadPackage
{
	private static final int LONG_PRESS = 0x80000000;
	private static final String TAG = "KeyBindings";
	private static String PhoneWindowMgr;
	SparseArray<String> bindings;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable
	{
		bindings = new SparseArray<String>();
		// Add the home long press to nothing by default
		bindings.put(LONG_PRESS | KeyEvent.KEYCODE_HOME, null);
		// Google Search
		bindings.put(LONG_PRESS | KeyEvent.KEYCODE_SEARCH, "com.google.android.katniss");
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{
		// DEBUG
		StringBuilder stringBuilder = new StringBuilder();
		int size = bindings.size();
		stringBuilder.append("{ ");
		for (int i = 0; i < size; i++) {
			stringBuilder.append(bindings.keyAt(i)).append(" = ")
			   .append(bindings.valueAt(i));
			if (i < (size - 1)) {
				stringBuilder.append(", ");
			}
		}
		stringBuilder.append(" }");
		// if (BuildConfig.DEBUG) Log.d(TAG, "### bindings ### " + stringBuilder.toString());

		// Don't do the hook if the prefs were empty
		if (!lpparam.packageName.equals("android") || bindings.size() == 0)
			return;

		if (Build.VERSION.SDK_INT >= 23) // MM
			PhoneWindowMgr = "com.android.server.policy.PhoneWindowManager";
		else
			PhoneWindowMgr = "com.android.internal.policy.impl.PhoneWindowManager";

		// Add a hook for setting up action on HOME long press
		if (bindings.get(LONG_PRESS | KeyEvent.KEYCODE_HOME) == null)
		{
			XposedHelpers.findAndHookConstructor(PhoneWindowMgr, lpparam.classLoader, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable
				{
					// Set the long press home behavior: 0 - nothing, 1 - recents, 2 - assist, 3 - custom
					Log.d(TAG, "### mLongPressOnHomeBehavior ### 2 ### ");
					XposedHelpers.setIntField(param.thisObject, "mLongPressOnHomeBehavior", 2);
					// Set the double press home behavior: 0 - nothing, 1 - recents, 2 - custom
					Log.d(TAG, "### mDoubleTapOnHomeBehavior ### 1 ### ");
					XposedHelpers.setIntField(param.thisObject, "mDoubleTapOnHomeBehavior", 1);
				}
			});
		}

		// For some reason, findAndHookMethod doesn't work for this
		XposedBridge.hookAllMethods(XposedHelpers.findClass(PhoneWindowMgr, lpparam.classLoader), "interceptKeyBeforeDispatching", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable
			{
				KeyEvent event = (KeyEvent)param.args[1];
				if (event.getAction() == KeyEvent.ACTION_DOWN)
				{
					int longPress = (event.getFlags() & KeyEvent.FLAG_LONG_PRESS) != 0 ? LONG_PRESS : 0;
					String value = bindings.get(longPress | event.getKeyCode());
					if (value != null)
					{
						if (BuildConfig.DEBUG) Log.d(TAG, " ### LAUNCH ### " + value);
						Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
						mContext.startActivity(mContext.getPackageManager().getLaunchIntentForPackage(value));
						param.setResult(-1);
					}
					// Keyboard switch on LONG PRESS MENU
					if (longPress != 0 && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
						if (BuildConfig.DEBUG) Log.d(TAG, " ### MENU_LONG ### ");
						Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
						((InputMethodManager) mContext.getSystemService("input_method")).showInputMethodPicker();
					}
				}
			}
		});
	}
}
