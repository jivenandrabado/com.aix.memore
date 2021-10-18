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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentGalleryBinding;
import com.aix.memore.databinding.FragmentKnowMoreBinding;
import com.aix.memore.interfaces.GalleryInterface;
import com.aix.memore.models.Album;
import com.aix.memore.models.Bio;
import com.aix.memore.models.Gallery;
import com.aix.memore.models.Media;
import com.aix.memore.utilities.DateHelper;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.views.adapters.AlbumFirebaseAdapter;
import com.aix.memore.views.dialogs.UploadDialog;
import com.bumptech.glide.Glide;

import java.util.List;

public class GalleryFragment extends Fragment implements GalleryInterface {

    private FragmentGalleryBinding binding;
    private GalleryViewModel galleryViewModel;
    private AlbumFirebaseAdapter albumFirebaseAdapter;
    private HighlightViewModel highlightViewModel;
    private NavController navController;
    private UploadDialog uploadDialog;
    private Boolean isEdit = false;
    public GalleryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentGalleryBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            galleryViewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);
            highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);
            navController = Navigation.findNavController(view);
            albumFirebaseAdapter = new AlbumFirebaseAdapter(galleryViewModel.getGalleryRecyclerOptions(highlightViewModel.getScannedValue().getValue()),this);
            binding.recyclerviewGallery.setAdapter(albumFirebaseAdapter);
            galleryViewModel.getDefaultGallery(highlightViewModel.getScannedValue().getValue());
            galleryViewModel.addSnapshotListenerForBio(highlightViewModel.getScannedValue().getValue());
            uploadDialog = new UploadDialog();

            //temporary fix for recyclerview
            binding.recyclerviewGallery.setItemAnimator(null);

            galleryViewModel.getBio().observe(getViewLifecycleOwner(), new Observer<Bio>() {
                @Override
                public void onChanged(Bio bio) {
                    String full_name = bio.bio_first_name + " " + bio.bio_middle_name + " " + bio.bio_last_name;
                    String death_date = DateHelper.formatDate(bio.bio_birth_date.toDate()) + " - " + DateHelper.formatDate(bio.bio_death_date.toDate());
                    Glide.with(requireContext()).load(Uri.parse(bio.bio_profile_pic))
                            .error(R.drawable.ic_baseline_photo_24).into((binding.imageViewBioProfilePic));
                    binding.textViewName.setText(full_name);
                    binding.textViewDeathDate.setText(death_date);
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

            binding.buttonUploadMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chooseImage();
                }
            });

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        // Add the new menu items
        if(!isEdit) {
            inflater.inflate(R.menu.manage_menu, menu);
        }else{
            ErrorLog.WriteDebugLog("EDIT MENU");
            inflater.inflate(R.menu.delete_menu,menu);
        }
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.newAlbumFragment:
                navController.navigate(R.id.action_galleryFragment_to_newAlbumFragment);
                break;

//            case R.id.upload:
//                chooseImage();
//                break;
            case R.id.edit:
                isEdit = true;
                albumFirebaseAdapter.setEdit(isEdit);
                requireActivity().invalidateOptionsMenu();
                break;

            case R.id.cancel:
                isEdit = false;
                albumFirebaseAdapter.setEdit(isEdit);
                requireActivity().invalidateOptionsMenu();

            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        ErrorLog.WriteDebugLog("ON RESUME GALLERY FRAGMENT");
        if(albumFirebaseAdapter!=null) {
            albumFirebaseAdapter.startListening();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        ErrorLog.WriteDebugLog("ON PAUSE GALLERY FRAGMENT");
        if(albumFirebaseAdapter!=null) {
            albumFirebaseAdapter.stopListening();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        galleryViewModel.detachMediaSnapshotListener();
        galleryViewModel.detachBioSnapshotListener();
    }

    @Override
    public void onAlbumSelect(Album album) {
        navController.navigate(R.id.action_galleryFragment_to_galleryViewFragment);
        galleryViewModel.getSelectedAlbum().setValue(album);
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

                                //public wall
                                galleryViewModel.uploadToFirebaseStorageToPublicWall(highlightViewModel.getScannedValue().getValue(),imageUri);
                            }
                        } else {
                            Uri uri = data.getData();
                            // your codefor single image selection
                            ErrorLog.WriteDebugLog("DATA RECEIVED "+uri);

                            uploadDialog.show(getChildFragmentManager(),"UPLOAD_DIALOG");

                            //public wall
                            galleryViewModel.uploadToFirebaseStorageToPublicWall(highlightViewModel.getScannedValue().getValue(),uri);



                        }
                    }
                }
            }
    );


}