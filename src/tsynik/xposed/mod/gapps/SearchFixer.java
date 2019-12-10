package tsynik.xposed.mod.gapps;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("WeakerAccess")
public class SearchFixer implements IXposedHookLoadPackage {

    static final String MOD_PACKAGE = "tsynik.xposed.mod.gapps";
    static final String FIX_PACKAGE = "com.google.android.katniss";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam)
            throws Throwable {

        if (lpparam.packageName.equals("android") && lpparam.processName.equals("android")) {
            PermGrant.init(lpparam.classLoader);
        }
    }
}
