package io.demj.enablesolionpixel4;

import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Main implements IXposedHookLoadPackage {

    ClassLoader classLoader;

    Class loadClass(String className) {
        return XposedHelpers.findClass(className, classLoader);
    }

    private boolean enable(String className, String methodName) {
        try {
            Class clazz = loadClass(className);
            Method method = XposedHelpers.findMethodBestMatch(clazz, methodName, String.class);
            enable(method);
            return true;
        } catch (Throwable ignored) {
//            XposedBridge.log(ignored);
        }
        return false;
    }

    private void enable(Method method) {
        XposedBridge.hookMethod(method, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d("demj", "after hook " + param.args[0] + " return " + param.getResult());
                Log.d("demj", "we wil replace it to true");
                param.setResult(Boolean.TRUE);
            }
        });

    }


    private Method findMethod(String className) {
        Class<?> clazz = loadClass(className);
        while (clazz!=null&&clazz != Object.class) {

            for (Method declaredMethod : clazz.getDeclaredMethods()) {
                Class returnTypeClass = declaredMethod.getReturnType();
                if (!(returnTypeClass == boolean.class || returnTypeClass == Boolean.class)) {
                    continue;
                }

                if (!declaredMethod.getName().toLowerCase().contains("Country")) {
                    continue;
                }

                Class[] paramTypes = declaredMethod.getParameterTypes();
                if (paramTypes.length != 1 || paramTypes[0] != String.class) {
                    continue;
                }
                return declaredMethod;
            }

            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private boolean trySearchMethodAndHook(String className) {
        try {
            Method method = findMethod(className);
            if (method != null) {
                enable(method);
                return true;
            }
            return false;
        } catch (Throwable ignored) {
//            XposedBridge.log(ignored);
        }
        return false;
    }


    private boolean enable() {
        final String osloEnableDetectorClassName = "com.google.oslo.service.OsloEnableDetector";
        final String chreSensorClassName = "com.google.oslo.service.sensors.CHRESensor";
        boolean enabled = enable(osloEnableDetectorClassName, "getInsideGreenCountry") ||
                enable(chreSensorClassName, "isAvailableInCountry");

        if (!enabled) {
            return trySearchMethodAndHook(osloEnableDetectorClassName) || trySearchMethodAndHook(chreSensorClassName);
        }
        return true;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.appInfo.packageName.startsWith("com.google.oslo")) {
            return;
        }

        classLoader = lpparam.classLoader;

        if (enable()) {
            XposedBridge.log("Success Enable Soli");
        } else {
            XposedBridge.log("Cannot Enable Soli");
        }


//        Log.d("demj", "found com.google.oslo");
//        classLoader = lpparam.classLoader;
//
//        try {
//
//
//            XposedHelpers.findAndHookMethod(loadClass("com.google.oslo.service.sensors.CHRESensor"), "isAvailableInCountry", String.class, new XC_MethodHook() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    super.afterHookedMethod(param);
//                    Log.d("demj", "after hook " + param.args[0] + " return " + param.getResult());
//                    Log.d("demj", "we wil replace it to true");
//                    param.setResult(Boolean.TRUE);
//                }
//            });
//
//        } catch (Exception e) {
//            Log.e("demj", "have problem " + e.getMessage());
//            e.printStackTrace();
//        }
    }
}
