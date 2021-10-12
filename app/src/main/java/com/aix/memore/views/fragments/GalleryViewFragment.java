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
import com.aix.memore.databinding.FragmentGalleryViewBinding;
import com.aix.memore.interfaces.GalleryViewInterface;
import com.aix.memore.models.Album;
import com.aix.memore.models.Gallery;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.views.adapters.AlbumFirebaseAdapter;
import com.aix.memore.views.adapters.GalleryFirebaseAdapter;
import com.aix.memore.views.dialogs.UploadDialog;

import java.util.List;


public class GalleryViewFragment extends Fragment implements GalleryViewInterface {

    private FragmentGalleryViewBinding binding;
    private GalleryViewModel galleryViewModel;
    private GalleryFirebaseAdapter galleryFirebaseAdapter;
    private HighlightViewModel highlightViewModel;
    private String album_id = "";
    private UploadDialog uploadDialog;
    private NavController navController;

    public GalleryViewFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGalleryViewBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            galleryViewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);
            highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);
            galleryFirebaseAdapter = new GalleryFirebaseAdapter
                    (galleryViewModel.getGalleryViewRecyclerOptions(highlightViewModel.getScannedValue().getValue(),
                            galleryViewModel.getSelectedAlbum().getValue().getAlbum_id()), requireContext(),this);
            binding.recyclerviewGalleryView.setAdapter(galleryFirebaseAdapter);
            binding.textViewAlbumTitle.setText(galleryViewModel.getSelectedAlbum().getValue().getTitle());
            uploadDialog = new UploadDialog();
            navController = Navigation.findNavController(view);

            //temporary fix for recyclerview
            binding.recyclerviewGalleryView.setItemAnimator(null);

            binding.buttonUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chooseImage();
                }
            });

            galleryViewModel.isUploaded().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if(aBoolean){
                        if (uploadDialog.isVisible()){
                            uploadDialog.dismiss();
                        }
                    }
                }
            });

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if(galleryFirebaseAdapter!=null) {
            galleryFirebaseAdapter.startListening();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if(galleryFirebaseAdapter!=null) {
            galleryFirebaseAdapter.stopListening();
        }
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

                                galleryViewModel.uploadToFirebaseStorage(highlightViewModel.getScannedValue().getValue(),imageUri,
                                        galleryViewModel.getSelectedAlbum().getValue().getAlbum_id());

                            }
                        } else {
                            Uri uri = data.getData();
                            // your codefor single image selection
                            ErrorLog.WriteDebugLog("DATA RECEIVED "+uri);

                            uploadDialog.show(getChildFragmentManager(),"UPLOAD_DIALOG");
                            galleryViewModel.uploadToFirebaseStorage(highlightViewModel.getScannedValue().getValue(),uri,
                                    galleryViewModel.getSelectedAlbum().getValue().getAlbum_id());

                        }
                    }
                }
            }
    );

    @Override
    public void onImageClick(List<Gallery> galleryList, int position) {
        navController.navigate(R.id.action_galleryViewFragment_to_galleryMediaFullViewFragment);
        galleryViewModel.getSelectedGalleryList().setValue(galleryList);
        galleryViewModel.getSelectedMediaPosition().setValue(position);

    }
}