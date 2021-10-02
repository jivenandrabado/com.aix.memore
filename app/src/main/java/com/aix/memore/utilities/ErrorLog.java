package com.aix.memore.utilities;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class ErrorLog {
    static String logParam = "Memoree";

    public static void WriteDebugLog(String txt) {
        try{
            Log.d(logParam, txt);
        } catch (Exception e){

        }
    }

    public static void WriteDebugLog(Exception e) {
        ErrorLog.WriteErrorLog(e);
    }

    public static void WriteErrorLog(Exception e){
        try{

            Log.d(logParam, "\r\n" +
                    "Time: " + "$current" + "\r\n" +
                    "Method Name: " + e.getStackTrace()[0].getMethodName() + "\r\n" +
                    "Err Num: " + e.getStackTrace()[0].getLineNumber() + "\r\n" +
                    "Err Desc: " + e.getMessage() + "\r\n" +
                    "FileName: " + e.getStackTrace()[0].getFileName() + "\r\n" +
                    "");

            FirebaseCrashlytics.getInstance().recordException(e);
            FirebaseCrashlytics.getInstance().setUserId(String.valueOf(FirebaseAuth.getInstance().getUid()));

        } catch (Exception e2){
            FirebaseCrashlytics.getInstance().recordException(e);
            FirebaseCrashlytics.getInstance().setUserId(String.valueOf(FirebaseAuth.getInstance().getUid()));
        }
    }
}
