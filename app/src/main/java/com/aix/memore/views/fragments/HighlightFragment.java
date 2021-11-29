package com.aix.memore.views.fragments;

import android.content.Intent;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentHighlightBinding;
import com.aix.memore.interfaces.HighlightInterface;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.DateHelper;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.view_models.UserViewModel;
import com.aix.memore.views.dialogs.PasswordDialog;
import com.aix.memore.views.dialogs.ProgressDialogFragment;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;

public class HighlightFragment extends Fragment implements HighlightInterface {

    private FragmentHighlightBinding binding;
    private HighlightViewModel highlightViewModel;
    private MediaController mediaController;
    private HighlightInterface highlightInterface;
    private ProgressDialogFragment progressDialogFragment;
    private NavController navController;
    private SimpleExoPlayer simpleExoPlayer;
    private GalleryViewModel galleryViewModel;
    private PasswordDialog passwordDialog;
    private UserViewModel userViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

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
            passwordDialog = new PasswordDialog();
            userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

            galleryViewModel.getBio().observe(getViewLifecycleOwner(), new Observer<Memore>() {
                @Override
                public void onChanged(Memore memore) {
                    String date = null;
                    String full_name = memore.bio_first_name+ " "+ memore.bio_middle_name+ " "+ memore.bio_last_name;
                    ErrorLog.WriteDebugLog("FULL NAME "+ full_name);
                    if(memore.bio_death_date!=null) {
                        if (!memore.bio_death_date.toString().isEmpty()) {
                            date = DateHelper.formatDate(memore.bio_birth_date) + " - " + DateHelper.formatDate(memore.bio_death_date);
                        }
                    } else {
                        date = DateHelper.formatDate(memore.bio_birth_date);
                    }

                    if(memore.getBio_profile_pic()!=null) {
                        if (!memore.bio_profile_pic.isEmpty()) {
                            Glide.with(requireContext()).load(Uri.parse(memore.bio_profile_pic))
                                    .error(R.drawable.ic_baseline_photo_24).into((binding.imageViewHightlightBioProfilePic));
                        }
                    }else{
                        Glide.with(requireContext()).load(R.drawable.ic_baseline_photo_24)
                                .error(R.drawable.ic_baseline_photo_24).into((binding.imageViewHightlightBioProfilePic));
                    }

                    binding.textViewHighlightName.setText(full_name);
                    binding.textViewHighlightDeathDate.setText(date);
                    binding.buttonKnowMore.setText(memore.bio_first_name+"'s Life");
                }
            });

            highlightViewModel.getScannedValue().observe(requireActivity(),onHighlightFoundObserver);

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

            initPasswordListener();

        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    private void initPasswordListener() {
        userViewModel.isAuthorized().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    passwordDialog.dismiss();
                    navController.navigate(R.id.action_HighlightFragment_to_editMemoreFragment);
                    userViewModel.isAuthorized().setValue(false);
                }
            }
        });
    }

    private Observer<String> onHighlightFoundObserver = new Observer<String>() {
        @Override
        public void onChanged(String o) {
            highlightViewModel.getHighlight(highlightInterface,o);
            galleryViewModel.addSnapshotListenerForBio(o);
        }
    };


    @Override
    public void onHighlightFound(Memore memore) {

        if(memore!=null) {
            if(memore.is_video) {
                initProgressDialog();
                initExoPlayer(memore.getVideo_highlight());
                ErrorLog.WriteDebugLog("On Highlight Found");
                progressDialogFragment.dismiss();
                enableVideoView();
            }else{
                enableImageView();
                Glide.with(requireContext()).load(memore.getVideo_highlight())
                        .centerInside()
                        .error(R.drawable.ic_baseline_photo_24).into((binding.imageViewHighlight));
            }

        }else{
            progressDialogFragment.dismiss();
            Toast.makeText(requireContext(),"Please try again",Toast.LENGTH_SHORT).show();
        }

    }

    private void enableVideoView() {
        binding.playerView.setVisibility(View.VISIBLE);
        binding.imageViewHighlight.setVisibility(View.INVISIBLE);
    }

    private void enableImageView(){
        binding.playerView.setVisibility(View.INVISIBLE);
        binding.imageViewHighlight.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCredentialsSubmitted() {
        navController.navigate(R.id.action_HighlightFragment_to_galleryFragment);
    }


    private void initExoPlayer(String URL){
        binding.playerView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 70);
            }
        });
        binding.playerView.setClipToOutline(true);


        playVideo(simpleExoPlayer,URL);
    }

    private void playVideo(SimpleExoPlayer player,String linkToPlay){
        ErrorLog.WriteDebugLog("INIT PLAY VIDEO");
        boolean playWhenReady = true;
        int currentWindow = 0;
        long playbackPosition = 0;

        MediaItem mediaItem = MediaItem.fromUri(linkToPlay);
        player.setMediaItem(mediaItem);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.setRepeatMode(player.REPEAT_MODE_ONE);

        if(!player.isPlaying()) {
            player.prepare();
            player.play();
        }


    }

    private void initProgressDialog(){
        progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.show(getChildFragmentManager(),"highlight_progress_dialog");
    }

    @Override
    public void onPause(){
        super.onPause();
        if(simpleExoPlayer != null) {
            simpleExoPlayer.stop();
        }

        highlightViewModel.getScannedValue().removeObserver(onHighlightFoundObserver);
    }
    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        galleryViewModel.detachBioSnapshotListener();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        // Add the new menu items
        ErrorLog.WriteDebugLog("EDIT MENU");
        inflater.inflate(R.menu.menu_highlight,menu);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.editMemore:
                passwordDialog.show(getChildFragmentManager(),"PASSWORD DIALOG FRAGMENT");
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}