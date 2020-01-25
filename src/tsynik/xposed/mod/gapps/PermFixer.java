package tsynik.xposed.mod.gapps;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("WeakerAccess")
public class PermFixer implements IXposedHookLoadPackage {

    // static final String MOD_PACKAGE = "tsynik.xposed.mod.gapps";
    static final String SRC_PACKAGE = "com.google.android.katniss";
    static final String SYS_PACKAGE = "com.android.tv.settings";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam)
            throws Throwable {

        if (lpparam.packageName.equals("android") && lpparam.processName.equals("android")) {
            PermGrant.init(lpparam.classLoader);
        }
    }
}
