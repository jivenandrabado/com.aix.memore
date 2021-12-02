package com.aix.memore.views.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.aix.memore.R;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.AppConfigViewModel;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.views.dialogs.AppUpdateDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private AppConfigViewModel appConfigViewModel;
    private HighlightViewModel highlightViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setTheme(R.style.Theme_Memore);
            setContentView(R.layout.activity_main);
            initHighlightObserver();
            initNavigation();
            initAppConfig();
            initDynamicLinks();


        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
            Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
        }

    }

    private void initHighlightObserver() {

        highlightViewModel = new ViewModelProvider(this).get(HighlightViewModel.class);
        highlightViewModel.memoreFound().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean != null) {
                    if (aBoolean) {
                        navController.navigate(R.id.HighlightFragment);
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid QR Code,", Toast.LENGTH_SHORT).show();
                    }
                    highlightViewModel.memoreFound().setValue(null);
                }
            }
        });
    }

    private void initDynamicLinks() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }

                        if(deepLink != null){
                            String deepLinkQueryParameter = deepLink.getQueryParameter("highlight");
                            highlightViewModel.getHighlightFromQR(deepLinkQueryParameter);

                            ErrorLog.WriteDebugLog("DEEPLINK PARAM "+deepLinkQueryParameter);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ErrorLog.WriteDebugLog("getDynamicLink:onFailure"+e);
                        ErrorLog.WriteErrorLog(e);
                    }
                });
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