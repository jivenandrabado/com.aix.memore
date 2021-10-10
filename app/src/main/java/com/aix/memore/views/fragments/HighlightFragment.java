package com.aix.memore.views.fragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentHighlightBinding;
import com.aix.memore.interfaces.HighlightInterface;
import com.aix.memore.models.Bio;
import com.aix.memore.models.Highlight;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.views.dialogs.ProgressDialogFragment;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;

import java.util.Objects;

public class HighlightFragment extends Fragment implements HighlightInterface {

    private FragmentHighlightBinding binding;
    private HighlightViewModel highlightViewModel;
    private VideoView videoView;
    private MediaController mediaController;
    private HighlightInterface highlightInterface;
    private ProgressDialogFragment progressDialogFragment;
    private NavController navController;
    private SimpleExoPlayer simpleExoPlayer;
    private GalleryViewModel galleryViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHighlightBinding.inflate(inflater,container,false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        highlightInterface = this;
        try {
            mediaController = new MediaController(requireContext());
            highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);
            navController = Navigation.findNavController(view);
            simpleExoPlayer = new SimpleExoPlayer.Builder(requireContext()).build();
            binding.playerView.setPlayer(simpleExoPlayer);
            galleryViewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);

            galleryViewModel.getBio().observe(getViewLifecycleOwner(), new Observer<Bio>() {
                @Override
                public void onChanged(Bio bio) {
                    String full_name = bio.bio_first_name+ " "+bio.bio_middle_name+ " "+bio.bio_last_name;
                    String death_date = bio.bio_birth_date+ " - "+ bio.bio_death_date.toDate();
                    Glide.with(requireContext()).load(Uri.parse(bio.bio_profile_pic))
                            .error(R.drawable.ic_baseline_photo_24).into((binding.imageViewHightlightBioProfilePic));
                    binding.textViewHighlightName.setText(full_name);
                    binding.textViewHighlightDeathDate.setText(death_date);
                }
            });

            highlightViewModel.getScannedValue().observe(requireActivity(), new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    highlightViewModel.getHighlight(highlightInterface,s);
                    galleryViewModel.addSnapshotListenerForBio(s);

                }
            });

            binding.buttonShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, "https://firebasestorage.googleapis.com/v0/b/memore-ca204.appspot.com/o/vince%20violation%202.mp4?alt=media&token=702ac0e3-0e8e-41bb-8642-65cc1261ad7c");
                    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    sharingIntent.setType("video/mp4");
                    sharingIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sharingIntent, "Share Video"));
                }
            });

            binding.buttonKnowMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navController.navigate(R.id.action_HighlightFragment_to_galleryFragment);
                }
            });

            initProgressDialog();


        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }



    @Override
    public void onHighlightFound(Highlight highlight) {
        initExoPlayer(highlight.getVideo_highlight());
        progressDialogFragment.dismiss();

    }


    private void initExoPlayer(String URL){
        playVideo(simpleExoPlayer,URL);
    }

    private void playVideo(SimpleExoPlayer player,String linkToPlay){
        boolean playWhenReady = true;
        int currentWindow = 0;
        long playbackPosition = 0;

        MediaItem mediaItem = MediaItem.fromUri(linkToPlay);
        player.setMediaItem(mediaItem);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
//        player.err(new Player.EventListener() {
//            @Override
//            public void onPlayerError(ExoPlaybackException error) {
//                Log.d(TAG, "onPlayerError: " + error.getMessage());
//            }
//        });
        player.setRepeatMode(player.REPEAT_MODE_ONE);
        player.prepare();
    }

    private void initProgressDialog(){
        progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.show(getChildFragmentManager(),"highlight_progress_dialog");
    }

    @Override
    public void onPause(){
        super.onPause();
        if(videoView != null) {
            videoView.pause();
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        if(videoView != null) {
            videoView.resume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        galleryViewModel.detachBioSnapshotListener();
    }
}