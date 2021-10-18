package com.aix.memore.views.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentNewAlbumDialogBinding;
import com.aix.memore.models.Album;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.view_models.UserViewModel;
import com.aix.memore.views.dialogs.PasswordDialog;
import com.aix.memore.views.dialogs.UploadDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewAlbumFragment extends Fragment {

     private FragmentNewAlbumDialogBinding binding;
     private UserViewModel userViewModel;
     private GalleryViewModel galleryViewModel;
     private HighlightViewModel highlightViewModel;
     private String doc_id;
     private PasswordDialog passwordDialog;
     private NavController navController;
     private List<Uri> imageUriList = new ArrayList<>();
     private UploadDialog uploadDialog;

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
        uploadDialog = new UploadDialog();


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
                    album.setIs_public(false);
                    galleryViewModel.createNewAlbum(doc_id, album, imageUriList);

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

        binding.buttonAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });


    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        chooseImageActivityResult.launch(intent);


    }

    private ActivityResultLauncher<Intent> chooseImageActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();

                        ClipData clipData = data.getClipData();
                        if (clipData != null) {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                Uri imageUri = clipData.getItemAt(i).getUri();
                                // your code for multiple image selection
                                ErrorLog.WriteDebugLog("DATA RECEIVED "+imageUri);
                                imageUriList.add(imageUri);

                            }
                        } else {
                            Uri uri = data.getData();
                            // your codefor single image selection
                            ErrorLog.WriteDebugLog("DATA RECEIVED "+uri);

//                            uploadDialog.show(getChildFragmentManager(),"UPLOAD_DIALOG");
                            imageUriList.add(uri);

                        }
                    }
                }
            }
    );


}