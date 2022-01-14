package com.aix.memore.utilities;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class InstallationSharedPref {

    public static final String SHARED_PREFS = "installation_shared";
    public static final String APP_VERSION = "app_version";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public InstallationSharedPref(Context context) {
        this.context = context;
        this.sharedPreferences = this.context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public void saveVersion (String object){
        editor.putString(APP_VERSION ,object);
        editor.commit();
    }

    public String getVersion (){
        return sharedPreferences.getString(APP_VERSION,"");
    }

    public Boolean isNewlyInstalled(){
        return getVersion().isEmpty();
    }
}
