package com.aix.memore.views.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentLifeCareBinding;
import com.aix.memore.utilities.ErrorLog;


public class LifeCareFragment extends Fragment {

    private FragmentLifeCareBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLifeCareBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonBuyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.aix.mshop"));
                try{
                    startActivity(intent);
                }
                catch(Exception e){
                    ErrorLog.WriteErrorLog(e);
                }
            }
        });
    }
}