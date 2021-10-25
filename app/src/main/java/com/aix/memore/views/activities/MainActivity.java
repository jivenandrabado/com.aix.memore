package com.aix.memore.views.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.aix.memore.R;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.AppConfigViewModel;
import com.aix.memore.views.dialogs.AppUpdateDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private AppConfigViewModel appConfigViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Memore);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_baseline_photo_24);
        initNavigation();
        initAppConfig();


    }


    private void initNavigation(){
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
//                if(destination.getId() == R.id.knowMoreFragment){
//                    Objects.requireNonNull(getSupportActionBar()).hide();
//                }else{
//                    Objects.requireNonNull(getSupportActionBar()).show();
//                }
            }
        });
//        bottomNavigationView = findViewById(R.id.bottomNavigationView);
//        NavigationUI.setupWithNavController(bottomNavigationView, navController);
//        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController.navigateUp();
        return super.onSupportNavigateUp();
    }

    private void initAppConfig(){
        appConfigViewModel = new ViewModelProvider(this).get(AppConfigViewModel.class);
        appConfigViewModel.initAppConfig(getApplicationContext());
        AppUpdateDialog appUpdateDialog = new AppUpdateDialog();


        appConfigViewModel.isForceUpdate().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean){
                    appUpdateDialog.show(getSupportFragmentManager(),"APP UPDATE DIALOG");
                    ErrorLog.WriteDebugLog("FORCE UPDATE NOW");
                }else{
                    ErrorLog.WriteDebugLog("FORCE UPDATE LATER");
                    if(appUpdateDialog.isVisible()){
                        appUpdateDialog.dismiss();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(appConfigViewModel != null){
            appConfigViewModel.detachAppConfigListener();
        }
    }
}