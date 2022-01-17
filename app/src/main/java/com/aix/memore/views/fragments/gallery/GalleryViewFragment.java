package com.aix.memore.views.fragments.gallery;

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
import com.aix.memore.databinding.FragmentGalleryViewBinding;
import com.aix.memore.interfaces.GalleryViewInterface;
import com.aix.memore.models.AlbumDetails;
import com.aix.memore.models.Gallery;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.view_models.UserViewModel;
import com.aix.memore.views.adapters.GalleryFirebaseAdapter;
import com.aix.memore.views.dialogs.AlbumDetailsDialog;
import com.aix.memore.views.dialogs.PasswordDialog;
import com.aix.memore.views.dialogs.ProgressDialogFragment;
import com.aix.memore.views.dialogs.UploadDialog;

import java.util.ArrayList;
import java.util.List;


public class GalleryViewFragment extends Fragment implements GalleryViewInterface {

    private FragmentGalleryViewBinding binding;
    private GalleryViewModel galleryViewModel;
    private GalleryFirebaseAdapter galleryFirebaseAdapter;
    private HighlightViewModel highlightViewModel;
    private UploadDialog uploadDialog;
    private NavController navController;
    private String owner_id, album_id;
    private Boolean isDelete;
    private UserViewModel userViewModel;
    private ProgressDialogFragment progressDialogFragment;
    private List<Gallery> galleryList = new ArrayList<>();
    private PasswordDialog passwordDialog;
    private AlbumDetailsDialog albumDetailsDialog;

    public GalleryViewFragment() {
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
        binding = FragmentGalleryViewBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            galleryViewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);
            highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);
            userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
            owner_id = highlightViewModel.getScannedValue().getValue();
            album_id = galleryViewModel.getSelectedAlbum().getValue().getAlbum_id();
            uploadDialog = new UploadDialog();
            navController = Navigation.findNavController(view);
            isDelete = false;
            passwordDialog = new PasswordDialog();


            initGalleryRecyclerview();
            uploadImage();
            initDelete();


        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

        galleryViewModel.getAlbumDetails(owner_id, album_id);

        galleryViewModel.getSelectedGalleryList().observe(getViewLifecycleOwner(), result-> {
            long byteSize = 0L;
            AlbumDetails albumDet = new AlbumDetails();
            for (Gallery galleryItem : result){
                byteSize += galleryItem.getByteSize() != null ? galleryItem.getByteSize() : 0L;
            }
            albumDet.setByteSize(byteSize);
            albumDet.setFileCount(result.size());
            ErrorLog.WriteDebugLog("albumDetailsDialog initiated");
            albumDetailsDialog = new AlbumDetailsDialog(galleryViewModel.getSelectedAlbum().getValue(), albumDet);
        });

    }

    private void uploadImage(){
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
    }

    private void initGalleryRecyclerview(){
        galleryFirebaseAdapter = new GalleryFirebaseAdapter
                (galleryViewModel.getGalleryViewRecyclerOptions(owner_id,album_id), requireContext(),this);
        galleryFirebaseAdapter.setHasStableIds(true);

        binding.recyclerviewGalleryView.setAdapter(galleryFirebaseAdapter);
        binding.textViewAlbumTitle.setText(galleryViewModel.getSelectedAlbum().getValue().getTitle());
//        if(galleryViewModel.getSelectedAlbum().getValue().getIs_public()) {
//            binding.buttonUpload.setVisibility(View.VISIBLE);
//        }else{
//            binding.buttonUpload.setVisibility(View.GONE);
//        }
        //temporary fix for recyclerview
        binding.recyclerviewGalleryView.setItemAnimator(null);
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
        galleryViewModel.detachAlbumDetailsListener();
    }

    private void chooseImage() {
        userViewModel.isAuthorized().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    passwordDialog.dismiss();
                    ErrorLog.WriteDebugLog("UPLOAD Images");
                    Intent intent = new Intent();
                    intent.setType("image/* video/*");
                    intent.setAction(Intent.ACTION_PICK);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    chooseImageActivityResult.launch(intent);
                    userViewModel.isAuthorized().setValue(false);
                }
            }
        });


        if(!galleryViewModel.getSelectedAlbum().getValue().getIs_public()) {
            passwordDialog.show(getChildFragmentManager(),"PASSWORD DIALOG FOR DELETE");
        }else{
            Intent intent = new Intent();
            intent.setType("image/* video/*");
            intent.setAction(Intent.ACTION_PICK);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            chooseImageActivityResult.launch(intent);
        }

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

    @Override
    public void onImageDelete(List<Gallery> galleryList) {
        ErrorLog.WriteDebugLog("ON IMAGE DELETE");
        this.galleryList = galleryList;

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        // Add the new menu items
        if(!isDelete) {
            inflater.inflate(R.menu.menu_gallery, menu);
        }else{
            ErrorLog.WriteDebugLog("EDIT MENU");
            inflater.inflate(R.menu.menu_delete,menu);
        }
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.deleteImage:
                isDelete = true;
                galleryFirebaseAdapter.setDelete(isDelete);
                requireActivity().invalidateOptionsMenu();
                break;

            case R.id.cancel:
                isDelete = false;
                galleryFirebaseAdapter.setDelete(isDelete);
                requireActivity().invalidateOptionsMenu();
                break;

            case R.id.delete:
                ErrorLog.WriteDebugLog("SHOW PASSWORD DIALOG");
                passwordDialog.show(getChildFragmentManager(),"PASSWORD DIALOG FOR DELETE");
                break;

            case R.id.albumDetails:
                ErrorLog.WriteDebugLog("SHOW DETAILS DIALOG");
                if(albumDetailsDialog != null)
                albumDetailsDialog.show(getChildFragmentManager(), "ALBUM_DETAILS_DIALOG");
                break;

            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initDelete(){
        userViewModel.isAuthorized().setValue(false);
        userViewModel.isAuthorized().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    passwordDialog.dismiss();
                    progressDialogFragment = new ProgressDialogFragment();
                    progressDialogFragment.show(getChildFragmentManager(),"PROGRESS DIALOG FOR DELETE IMAGES");
                    galleryViewModel.deleteImage(galleryList,owner_id, album_id);
                    ErrorLog.WriteDebugLog("DELETE Images");
                }else{
                    ErrorLog.WriteDebugLog("IS NOT AUTHORIZE");
                }
            }
        });

        galleryViewModel.isImageDeleted().observe(requireActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    if (progressDialogFragment != null) {
                        if (progressDialogFragment.isVisible()) {
                            progressDialogFragment.dismiss();
                            ErrorLog.WriteDebugLog("ALBUMS DELETED");
                            //change options menu
                            galleryFirebaseAdapter.updateOptions(galleryViewModel.getGalleryViewRecyclerOptions(owner_id, album_id));
                            isDelete = false;
                            galleryFirebaseAdapter.setDelete(isDelete);
                            requireActivity().invalidateOptionsMenu();
                        }
                    }
                }
            }
        });
    }
}