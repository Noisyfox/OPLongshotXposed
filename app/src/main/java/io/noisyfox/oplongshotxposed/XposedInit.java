package io.noisyfox.oplongshotxposed;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.OutputStream;
import java.lang.reflect.Member;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by noisyfox on 2017/12/27.
 */

public class XposedInit implements IXposedHookLoadPackage {

    private static final String SCREENSHOT_FILE_NAME_TEMPLATE = "Screenshot_%s.png";
    private static final String MIME_TYPE_IMAGE_ORIG = "image/jpeg";
    private static final String MIME_TYPE_IMAGE = "image/png";

    private static final Object[][] PARAM_CONSTRUCTOR_SAVEIMAGEINBACKGROUNDTASK = {
            { // OSS based on android 7.x
                    Context.class,
                    "com.oneplus.screenshot.SaveImageInBackgroundData",
                    NotificationManager.class,
                    int.class
            },
            { // OSS based on android 8.0
                    Context.class,
                    "com.oneplus.screenshot.SaveImageInBackgroundData",
                    NotificationManager.class
            }
    };

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        final String packageName = lpparam.packageName;
        if (!"com.oneplus.screenshot".equals(packageName)) {
            return;
        }

        XposedBridge.log("[OPLongshot]Loaded app: " + lpparam.packageName);

        // Fix file name & path
        XposedHelpers.setStaticObjectField(
                XposedHelpers.findClass("com.oneplus.screenshot.SaveImageInBackgroundTask", lpparam.classLoader),
                "SCREENSHOT_FILE_NAME_TEMPLATE", SCREENSHOT_FILE_NAME_TEMPLATE);
        Member constructor = null;
        for (Object[] params : PARAM_CONSTRUCTOR_SAVEIMAGEINBACKGROUNDTASK) {
            try {
                constructor = XposedHelpers.findConstructorExact("com.oneplus.screenshot.SaveImageInBackgroundTask", lpparam.classLoader, params);
                break;
            } catch (NoSuchMethodError ignored) {
            }
        }
        if (constructor == null) {
            XposedBridge.log("[OPLongshot]Unable to hook com.oneplus.screenshot.SaveImageInBackgroundTask()!");
        } else {
            XposedBridge.hookMethod(constructor, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("[OPLongshot]new SaveImageInBackgroundTask() called!");

                    String _mImageFileName = (String) XposedHelpers.getObjectField(param.thisObject, "mImageFileName");
                    String _mImageFilePath = (String) XposedHelpers.getObjectField(param.thisObject, "mImageFilePath");
                    _mImageFileName = replaceFileName(_mImageFileName);
                    _mImageFilePath = replaceFileName(_mImageFilePath);
                    XposedHelpers.setObjectField(param.thisObject, "mImageFileName", _mImageFileName);
                    XposedHelpers.setObjectField(param.thisObject, "mImageFilePath", _mImageFilePath);
                }
            });
        }
        XposedHelpers.findAndHookMethod("com.oneplus.screenshot.util.ImageInfo", lpparam.classLoader, "getSuffix",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return ".png";
                    }
                });

        // Fix content type in Intent & Content
        XposedHelpers.setStaticObjectField(
                XposedHelpers.findClass("com.oneplus.screenshot.util.Common", lpparam.classLoader),
                "MIME_TYPE_IMAGE", MIME_TYPE_IMAGE);
        XposedHelpers.findAndHookMethod(ContentValues.class, "put",
                String.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!"mime_type".equalsIgnoreCase((String) param.args[0])) {
                            return;
                        }
                        if (!MIME_TYPE_IMAGE_ORIG.equalsIgnoreCase((String) param.args[1])) {
                            return;
                        }

                        param.args[1] = MIME_TYPE_IMAGE;
                    }
                });
        XposedHelpers.findAndHookMethod(Intent.class, "setType",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!MIME_TYPE_IMAGE_ORIG.equalsIgnoreCase((String) param.args[0])) {
                            return;
                        }

                        param.args[0] = MIME_TYPE_IMAGE;
                    }
                });
        XposedHelpers.findAndHookMethod(Intent.class, "setDataAndType",
                Uri.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!MIME_TYPE_IMAGE_ORIG.equalsIgnoreCase((String) param.args[1])) {
                            return;
                        }

                        XposedBridge.log("[OPLongshot]beforeHookedMethod Intent.setDataAndType()!");

                        param.args[1] = MIME_TYPE_IMAGE;
                    }
                });

        // Force compress as png
        XposedHelpers.findAndHookMethod(Bitmap.class, "compress",
                Bitmap.CompressFormat.class,
                int.class,
                OutputStream.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("[OPLongshot]beforeHookedMethod Bitmap.compress()!");

                        param.args[0] = Bitmap.CompressFormat.PNG;
                        param.args[1] = 100;
                    }
                });
    }

    private static String replaceFileName(String orig) {
        if (orig != null && orig.toLowerCase().endsWith(".jpg")) {
            orig = orig.substring(0, orig.length() - 3) + "png";
        }

        return orig;
    }
}
