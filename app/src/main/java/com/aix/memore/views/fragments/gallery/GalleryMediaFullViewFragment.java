package com.aix.memore.views.fragments.gallery;

import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.MediaController;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentGalleryMediaFullViewBinding;
import com.aix.memore.models.Gallery;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.views.adapters.GalleryFullViewAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;

import java.util.List;

public class GalleryMediaFullViewFragment extends Fragment {

    private FragmentGalleryMediaFullViewBinding binding;
    private GalleryFullViewAdapter galleryFullViewAdapter;
    private List<Gallery> galleryList;
    private GalleryViewModel galleryViewModel;
    private MediaController mediaController;
    private SimpleExoPlayer simpleExoPlayer;
    private int position = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGalleryMediaFullViewBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            mediaController = new MediaController(requireContext());
            simpleExoPlayer = new SimpleExoPlayer.Builder(requireContext()).build();
            binding.playerView.setPlayer(simpleExoPlayer);
            galleryViewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);
            galleryList = galleryViewModel.getSelectedGalleryList().getValue();
            initGalleryView();


        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }

    }

    private void initGalleryView(){
        galleryFullViewAdapter = new GalleryFullViewAdapter(requireContext(), galleryList);
        binding.gallery.setAdapter(galleryFullViewAdapter);

        galleryViewModel.getSelectedMediaPosition().observe(requireActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                position = integer;
            }
        });

        //set selected image
        binding.gallery.setSelection(position);
        showMedia(galleryList.get(position));


        binding.gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ErrorLog.WriteDebugLog("Path " + galleryList.get(i).getPath());
//                ErrorLog.WriteDebugLog("Type " + requireActivity().getContentResolver().getType(Uri.parse(galleryList.get(i).getPath())));
                showMedia(galleryList.get(i));
            }
        });
    }

    private void showMedia(Gallery gallery) {
        if(gallery.getType() == 1){
            binding.playerView.setVisibility(View.GONE);
            binding.imageView.setVisibility(View.VISIBLE);
            Glide.with(requireContext()).load(Uri.parse(gallery.getPath()))
                    .fitCenter()
                    .error(R.drawable.ic_baseline_photo_24).into((binding.imageView));
        }else {
            binding.playerView.setVisibility(View.VISIBLE);
            binding.imageView.setVisibility(View.GONE);
            initExoPlayer(gallery.getPath());
        }
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
        player.setRepeatMode(player.REPEAT_MODE_OFF);

        if(!player.isPlaying()) {
            player.prepare();
            player.play();
        }

    }

    private void initExoPlayer(String URL){
        binding.playerView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 0);
            }
        });
        binding.playerView.setClipToOutline(true);
        playVideo(simpleExoPlayer,URL);
    }


    @Override
    public void onPause() {
        super.onPause();
        if(simpleExoPlayer != null) {
            simpleExoPlayer.stop();
        }
    }
}