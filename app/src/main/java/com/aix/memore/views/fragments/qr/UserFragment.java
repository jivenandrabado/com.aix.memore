package com.aix.memore.views.fragments.qr;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentUserBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class UserFragment extends Fragment {

    private FragmentUserBinding binding;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Glide.with(requireContext()).load(Uri.parse("https://www.netcov.com/wp-content/pubfiles/2019/09/What-is-Cloud-Computing.jpeg"))
                .centerCrop()
                .error(R.drawable.ic_baseline_photo_24).into((binding.imageView3));

        navController = Navigation.findNavController(view);

        binding.buttonCreateMemore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_userFragment_to_createMemoreFragment);
            }
        });
        binding.textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_userFragment_to_loginFragment);
            }
        });
    }
}