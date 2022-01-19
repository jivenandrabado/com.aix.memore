package com.aix.memore.utilities;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    public void makeText(Context context, String message, int length){
        Toast.makeText(context.getApplicationContext(),message,length).show();
    }


    public void toastSingInSuccess(Context context){
        Toast.makeText(context.getApplicationContext(),"Log in Success",Toast.LENGTH_LONG).show();
    }

    public void toastSingInFailed(Context context){
        Toast.makeText(context.getApplicationContext(),"Log in Failed",Toast.LENGTH_LONG).show();
    }

    public void toastLogoutSuccess(Context context){
        Toast.makeText(context.getApplicationContext(),"Logout success", Toast.LENGTH_LONG).show();
    }

    public void toastNoInternetConnection(Context context){
        Toast.makeText(context.getApplicationContext(),"No internet connection!", Toast.LENGTH_LONG).show();
    }

    public void toastRegistrationSucces(Context context){
        Toast.makeText(context.getApplicationContext(),"Registration Success",Toast.LENGTH_LONG).show();
    }

    public void toastRegistrationFailed(Context context){
        Toast.makeText(context.getApplicationContext(),"Registration Failed",Toast.LENGTH_LONG).show();
    }

    public void toastProfileUpdateSuccess(Context context){
        Toast.makeText(context.getApplicationContext(),"Profile update success",Toast.LENGTH_LONG).show();
    }

    public void toastProfileUpdateFailed(Context context){
        Toast.makeText(context.getApplicationContext(),"Profile update failed",Toast.LENGTH_LONG).show();
    }
}
