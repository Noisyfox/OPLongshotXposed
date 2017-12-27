package io.noisyfox.oplongshotxposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by noisyfox on 2017/12/27.
 */

public class XposedInit implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        String packageName = lpparam.packageName;
        if (!"com.oneplus.screenshot".equals(packageName)) {
            return;
        }

        XposedBridge.log("[OPLongshot]Loaded app: " + lpparam.packageName);
    }
}
