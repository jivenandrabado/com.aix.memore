package com.aix.memore.views.fragments.album;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentGalleryBinding;
import com.aix.memore.interfaces.GalleryInterface;
import com.aix.memore.models.Album;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.DateHelper;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.view_models.UserViewModel;
import com.aix.memore.views.adapters.AlbumFirebaseAdapter;
import com.aix.memore.views.dialogs.PasswordDialog;
import com.aix.memore.views.dialogs.ProgressDialogFragment;
import com.aix.memore.views.dialogs.UploadDialog;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment implements GalleryInterface {

    private FragmentGalleryBinding binding;
    private GalleryViewModel galleryViewModel;
    private AlbumFirebaseAdapter albumFirebaseAdapter;
    private HighlightViewModel highlightViewModel;
    private NavController navController;
    private UploadDialog uploadDialog;
    private Boolean isDeleteAlbum = false;
    private List<Album> albumList;
    private UserViewModel userViewModel;
    ProgressDialogFragment progressDialogFragment;
    private String owner_id;
    private PasswordDialog passwordDialog;
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
            userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
            navController = Navigation.findNavController(view);
            uploadDialog = new UploadDialog();
            albumList = new ArrayList<>();
            passwordDialog = new PasswordDialog();
            owner_id = highlightViewModel.getScannedValue().getValue();
            initAlbumRecyclerview();
            getCurrentUser();
            addListenerForUploadDialog();
            initUpload();
            initDelete();

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        // Add the new menu items
        if(!isDeleteAlbum) {
            inflater.inflate(R.menu.menu_album, menu);
        }else{
            ErrorLog.WriteDebugLog("EDIT MENU");
            inflater.inflate(R.menu.menu_delete,menu);
        }
        super.onCreateOptionsMenu(menu, inflater);

    }

    private void addListenerForUploadDialog(){
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

    }

    private void initUpload(){
        binding.buttonUploadMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

    }

    private void getCurrentUser(){
        galleryViewModel.getBio().observe(getViewLifecycleOwner(), new Observer<Memore>() {
            @Override
            public void onChanged(Memore memore) {
                String full_name = memore.bio_first_name + " " + memore.bio_middle_name + " " + memore.bio_last_name;
                String date = null;
                if(memore.bio_death_date!=null) {
                    if (!memore.bio_death_date.toString().isEmpty()) {
                        date = DateHelper.formatDate(memore.bio_birth_date) + " - " + DateHelper.formatDate(memore.bio_death_date);
                    }
                } else {
                    date = DateHelper.formatDate(memore.bio_birth_date);
                }
                if(memore.getBio_profile_pic()!=null) {

                    if (!memore.getBio_profile_pic().isEmpty()) {
                        Glide.with(requireContext()).load(Uri.parse(memore.bio_profile_pic))
                                .error(R.drawable.ic_baseline_photo_24).into((binding.imageViewBioProfilePic));
                    }
                }
                binding.textViewName.setText(full_name);
                binding.textViewDeathDate.setText(date);
            }
        });
    }

    private void initAlbumRecyclerview(){
        albumFirebaseAdapter = new AlbumFirebaseAdapter(galleryViewModel.getGalleryRecyclerOptions(highlightViewModel.getScannedValue().getValue()),this);
        binding.recyclerviewGallery.setAdapter(albumFirebaseAdapter);
        galleryViewModel.getDefaultGallery(highlightViewModel.getScannedValue().getValue());
        galleryViewModel.addSnapshotListenerForBio(owner_id);
        //temporary fix for recyclerview
        binding.recyclerviewGallery.setItemAnimator(null);
    }

    private void initDelete(){
        userViewModel.isAuthorized().setValue(false);
        userViewModel.isAuthorized().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    passwordDialog.dismiss();
                    progressDialogFragment = new ProgressDialogFragment();
                    progressDialogFragment.show(getChildFragmentManager(),"PROGRESS DIALOG FOR DELETE");
                    galleryViewModel.deleteAlbums(albumList,owner_id);
                    ErrorLog.WriteDebugLog("DELETE ALBUMS");

                }
            }
        });

        galleryViewModel.isDeleted().observe(requireActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    if (progressDialogFragment != null) {
                        if (progressDialogFragment.isVisible()) {
                            progressDialogFragment.dismiss();
                            ErrorLog.WriteDebugLog("ALBUMS DELETED");
                            //change options menu
                            isDeleteAlbum = false;
                            albumFirebaseAdapter.setEdit(isDeleteAlbum);
                            requireActivity().invalidateOptionsMenu();
                        }
                    }
                }
            }
        });
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
            case R.id.deleteAlbum:
                isDeleteAlbum = true;
                albumFirebaseAdapter.setEdit(isDeleteAlbum);
                requireActivity().invalidateOptionsMenu();
                break;

            case R.id.cancel:
                isDeleteAlbum = false;
                albumFirebaseAdapter.setEdit(isDeleteAlbum);
                requireActivity().invalidateOptionsMenu();
                break;

            case R.id.delete:
                passwordDialog.show(getChildFragmentManager(),"PASSWORD DIALOG FOR DELETE");
                break;

            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(albumFirebaseAdapter!=null) {
            albumFirebaseAdapter.startListening();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
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

    @Override
    public void onAlbumSelectDelete(List<Album> albumList) {
        this.albumList = albumList;
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
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