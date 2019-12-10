package tsynik.xposed.mod.gapps;

import android.os.Build;
import android.util.Log;

import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

// src: https://raw.githubusercontent.com/Lawiusz/xposed_lockscreen_visualizer/master/app/src/main/java/pl/lawiusz/lockscreenvisualizerxposed/PermGrant.java

class PermGrant {
    private static final String TAG = "PermGrant";
    private static final String PERM_ENABLE_KEYGUARD_FLAGS = "com.amazon.permission.ENABLE_KEYGUARD_FLAGS";

    private static final String CLASS_PACKAGE_MANAGER_SERVICE
            = "com.android.server.pm.PackageManagerService";
    private static final String CLASS_PACKAGE_PARSER_PACKAGE
            = "android.content.pm.PackageParser.Package";

    static void init(ClassLoader loader){
        if (Build.VERSION.SDK_INT < 23) { // Build.VERSION_CODES.M
            initLP(loader);
        } else {
            initMM(loader);
        }
    }

    private static void initLP(final ClassLoader loader) {
        try {
            final Class<?> pmServiceClass = XposedHelpers.findClass(CLASS_PACKAGE_MANAGER_SERVICE,
                    loader);
            XposedHelpers.findAndHookMethod(pmServiceClass, "grantPermissionsLPw",
                    CLASS_PACKAGE_PARSER_PACKAGE, boolean.class, String.class, new XC_MethodHook() {
                        @SuppressWarnings("unchecked")
                        @Override
                        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param)
                                throws Throwable {
                            final String pkgName = (String) XposedHelpers
                                    .getObjectField(param.args[0], "packageName");
                            if (pkgName.contentEquals(SearchFixer.FIX_PACKAGE)) {
                                Object extras = XposedHelpers
                                        .getObjectField(param.args[0], "mExtras");
                                Set<String> grantedPerms = (Set<String>) XposedHelpers
                                        .getObjectField(extras, "grantedPermissions");
                                Object settings = XposedHelpers
                                        .getObjectField(param.thisObject, "mSettings");
                                Object permissions = XposedHelpers
                                        .getObjectField(settings, "mPermissions");

                                if (!grantedPerms.contains(PERM_ENABLE_KEYGUARD_FLAGS)) {
                                    final Object pEnableHome = XposedHelpers
                                            .callMethod(permissions, "get",
                                            PERM_ENABLE_KEYGUARD_FLAGS);
                                    grantedPerms.add(PERM_ENABLE_KEYGUARD_FLAGS);
                                    int[] gpGids = (int[]) XposedHelpers
                                            .getObjectField(extras, "gids");
                                    int[] bpGids = (int[]) XposedHelpers
                                            .getObjectField(pEnableHome, "gids");
                                    //noinspection UnusedAssignment
                                    gpGids = (int[]) XposedHelpers
                                            .callStaticMethod(param.thisObject.getClass(),
                                            "appendInts", gpGids, bpGids);
                                }

                            }
                        }
                    });
        } catch (Throwable t) {
        // unused
        }
    }

    private static void initMM(ClassLoader loader) {
        try {
            final Class<?> pmServiceClass = XposedHelpers.findClass(CLASS_PACKAGE_MANAGER_SERVICE,
                    loader);
            XposedHelpers.findAndHookMethod(pmServiceClass, "grantPermissionsLPw",
                    CLASS_PACKAGE_PARSER_PACKAGE, boolean.class, String.class, new XC_MethodHook() {
                        @SuppressWarnings("unchecked")
                        @Override
                        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param)
                                throws Throwable {
                            final String pkgName = (String) XposedHelpers
                                    .getObjectField(param.args[0], "packageName");
                            if (pkgName.contentEquals(SearchFixer.FIX_PACKAGE)) {
                                final Object extras = XposedHelpers
                                        .getObjectField(param.args[0], "mExtras");
                                final Object ps = XposedHelpers
                                        .callMethod(extras, "getPermissionsState");
                                final List<String> grantedPerms =
                                        (List<String>) XposedHelpers
                                                .getObjectField(param.args[0],
                                                        "requestedPermissions");
                                final Object settings = XposedHelpers
                                        .getObjectField(param.thisObject, "mSettings");
                                final Object permissions = XposedHelpers
                                        .getObjectField(settings, "mPermissions");

                                if (!grantedPerms.contains(PERM_ENABLE_KEYGUARD_FLAGS)) {
                                    final Object pEnableHome = XposedHelpers
                                            .callMethod(permissions, "get", PERM_ENABLE_KEYGUARD_FLAGS);
                                    XposedHelpers.callMethod(ps, "grantInstallPermission",
                                            pEnableHome);
                                }
                            }
                        }
                    });
        } catch (Throwable t) {
        // unused
        }
    }
}
