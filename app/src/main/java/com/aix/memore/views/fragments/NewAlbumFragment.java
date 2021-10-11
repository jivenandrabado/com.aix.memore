package com.aix.memore.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentNewAlbumDialogBinding;
import com.aix.memore.models.Album;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.view_models.UserViewModel;
import com.aix.memore.views.dialogs.PasswordDialog;

import java.util.Date;

public class NewAlbumFragment extends Fragment {

     private FragmentNewAlbumDialogBinding binding;
     private UserViewModel userViewModel;
     private GalleryViewModel galleryViewModel;
     private HighlightViewModel highlightViewModel;
     private String doc_id;
     private PasswordDialog passwordDialog;
     private NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNewAlbumDialogBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        galleryViewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);
        highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);
        passwordDialog = new PasswordDialog();
        navController = Navigation.findNavController(view);
        userViewModel.isAuthorized().setValue(false);
        galleryViewModel.isAlbumCreated().setValue(false);



        highlightViewModel.getScannedValue().observe(requireActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                doc_id = s;
            }
        });

        galleryViewModel.isAlbumCreated().observe(requireActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    navController.popBackStack(R.id.newAlbumFragment, true);
                    galleryViewModel.isAlbumCreated().setValue(false);
                }
            }
        });


        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.popBackStack(R.id.newAlbumFragment, true);
            }
        });

        userViewModel.isAuthorized().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Album album = new Album();
                    album.setTitle( String.valueOf(binding.editTextTitle.getText()).trim());
                    album.setDescription( String.valueOf(binding.editTextAlbumDescription.getText()).trim());
                    album.setDate_created(new Date());
                    galleryViewModel.createNewAlbum(doc_id, album);
                }
            }
        });

        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!String.valueOf(userViewModel.isAuthorized().getValue()).isEmpty()){
                    passwordDialog.show(getChildFragmentManager(),"PASSWORD_DIALOG");
                }
            }
        });

    }
}