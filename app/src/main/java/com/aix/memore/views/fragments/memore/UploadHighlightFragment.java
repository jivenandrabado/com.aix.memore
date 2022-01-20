package com.aix.memore.views.fragments.memore;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentUploadHighlightBinding;
import com.aix.memore.interfaces.UploadHighlightInterface;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.MemoreViewModel;
import com.aix.memore.views.dialogs.EmptyHighlightDialog;
import com.aix.memore.views.dialogs.ShareDialog;
import com.aix.memore.views.dialogs.UploadDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;


public class UploadHighlightFragment extends Fragment implements UploadHighlightInterface {

    private FragmentUploadHighlightBinding binding;
    private NavController navController;
    private String video_highlight ="";
    private SimpleExoPlayer simpleExoPlayer;
    private MemoreViewModel memoreViewModel;
    private Memore memore;
    private boolean is_video;
    private boolean isEdit = false;
    private UploadDialog uploadDialog;
    private EmptyHighlightDialog emptyHighlightDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUploadHighlightBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        emptyHighlightDialog = new EmptyHighlightDialog(this);

        initMemore();
        initExoPlayer();
        initVideoChooser();
        initImageChooser();
        initUploadDialog();

        memoreViewModel.isEdit.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                isEdit = aBoolean;

                if(aBoolean) {
                    binding.buttonNext.setText("Save");
                    if (memore.isIs_video()) {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        playVideo(simpleExoPlayer, memore.getVideo_highlight());
                        enableVideoView();
                    } else {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        Glide.with(requireContext()).load(memore.getVideo_highlight())
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        binding.progressBar.setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                .error(R.drawable.ic_baseline_photo_24).into((binding.imageViewHighlight));

                        enableImageView();
                    }
                }else{
                    binding.buttonNext.setText("Next");
                }
            }
        });
        binding.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if (!video_highlight.isEmpty()) {
                        memore.setVideo_highlight(video_highlight);
                        memore.setIs_video(is_video);
                        if(!isEdit) {
                            navController.navigate(R.id.action_uploadHighlightFragment_to_QRGeneratorFragment);
                        }else{
                            uploadDialog.show(getChildFragmentManager(),"UPLOAD DIALOG EDIT");
                            memoreViewModel.updateHightlightToFirebase(memore, requireContext());
                        }
                    } else {
                        emptyHighlightDialog.show(getChildFragmentManager(),"EMPTY HIGHLIGHT");
//                        ErrorLog.WriteDebugLog("Pls select video highlight");
//                        Toast.makeText(requireContext(), "Please upload video or photo.", Toast.LENGTH_LONG).show();
                    }
            }
        });

        memoreViewModel.getMemoreUploadProgress().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                if(aDouble == 100){
                    if(uploadDialog!=null){
                        if(uploadDialog.isVisible()){
                            uploadDialog.dismiss();
                        }
                    }
                }
            }
        });

        memoreViewModel.memoreSaved().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Toast.makeText(requireActivity().getApplicationContext(), "Highlight upload success", Toast.LENGTH_SHORT).show();
                    memoreViewModel.memoreSaved().setValue(false);
                    navController.popBackStack(R.id.uploadHighlightFragment,true);
                }
            }
        });
    }

    private void initUploadDialog() {
        uploadDialog = new UploadDialog();
    }

    private void initImageChooser() {
        binding.buttonUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
    }

    private void initVideoChooser(){

        binding.buttonUploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseVideo();
            }
        });

    }

    private void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_PICK);
        chooseVideoActivityResult.launch(intent);

    }

    private ActivityResultLauncher<Intent> chooseVideoActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK){
                        binding.progressBar.setVisibility(View.VISIBLE);
                        Intent data = result.getData();

                        ClipData clipData = data.getClipData();
                        if (clipData != null) {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                Uri imageUri = clipData.getItemAt(i).getUri();
                                ErrorLog.WriteDebugLog("DATA RECEIVED "+imageUri);
                                video_highlight = String.valueOf(imageUri);
                                is_video = true;
                                playVideo(simpleExoPlayer,video_highlight);
                                enableVideoView();
                            }
                        } else {
                            Uri uri = data.getData();
                            ErrorLog.WriteDebugLog("DATA RECEIVED "+uri);
                            video_highlight = String.valueOf(uri);
                            is_video = true;
                            playVideo(simpleExoPlayer,video_highlight);
                            enableVideoView();

                        }
                    }
                }


            }
    );

    private void enableVideoView() {
        binding.playerView.setVisibility(View.VISIBLE);
        binding.imageViewHighlight.setVisibility(View.GONE);
    }

    private void enableImageView(){
        binding.playerView.setVisibility(View.GONE);
        binding.imageViewHighlight.setVisibility(View.VISIBLE);
    }


    private void initExoPlayer(){
        simpleExoPlayer = new SimpleExoPlayer.Builder(requireContext()).build();
        binding.playerView.setPlayer(simpleExoPlayer);
    }


    private void playVideo(SimpleExoPlayer player, String linkToPlay) {
        ErrorLog.WriteDebugLog("INIT PLAY VIDEO");
        boolean playWhenReady = true;
        int currentWindow = 0;
        long playbackPosition = 0;

        MediaItem mediaItem = MediaItem.fromUri(linkToPlay);
        player.setMediaItem(mediaItem);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.setRepeatMode(player.REPEAT_MODE_ONE);

        if (!player.isPlaying()) {
            player.prepare();
            binding.progressBar.setVisibility(View.GONE);
            player.play();
        }

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
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
                                video_highlight = String.valueOf(imageUri);
                                is_video = false;

//                                imageUriList.add(imageUri);
                                enableImageView();

                            }
                        } else {
                            Uri uri = data.getData();
                            // your codefor single image selection
                            ErrorLog.WriteDebugLog("DATA RECEIVED "+uri);

//                            uploadDialog.show(getChildFragmentManager(),"UPLOAD_DIALOG");
//                            imageUriList.add(uri);

                            enableImageView();
                            video_highlight = String.valueOf(uri);
                            is_video = false;
                            binding.progressBar.setVisibility(View.VISIBLE);
                            Glide.with(requireContext()).load(uri)
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            binding.progressBar.setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .error(R.drawable.ic_baseline_photo_24).into((binding.imageViewHighlight));

                        }
                    }
                }
            }
    );

    private void initMemore(){
        memoreViewModel = new ViewModelProvider(requireActivity()).get(MemoreViewModel.class);
        memore = memoreViewModel.getMemoreMutableLiveData().getValue();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(simpleExoPlayer != null) {
            simpleExoPlayer.stop();
        }
    }

    @Override
    public void onContinueWithoutHighlight() {
        memore.setVideo_highlight("");
        navController.navigate(R.id.action_uploadHighlightFragment_to_QRGeneratorFragment);
    }
}