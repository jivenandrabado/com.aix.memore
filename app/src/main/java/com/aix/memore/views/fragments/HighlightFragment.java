package com.aix.memore.views.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aix.memore.databinding.FragmentHighlightBinding;
import com.aix.memore.interfaces.HighlightInterface;
import com.aix.memore.models.Highlight;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.HighlightViewModel;

public class HighlightFragment extends Fragment implements HighlightInterface {

    private FragmentHighlightBinding binding;
    private HighlightViewModel highlightViewModel;
    private VideoView videoView;
    private MediaController mediaController;
    private HighlightInterface highlightInterface;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

            highlightViewModel.getScannedValue().observe(requireActivity(), new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    highlightViewModel.getHighlight(highlightInterface,s);
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


        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }



    @Override
    public void onHighlightFound(Highlight highlight) {
        initVideoViewer(highlight);
    }

    private void initVideoViewer(Highlight highlight){
        mediaController.setAnchorView(videoView);
        videoView = binding.videoView;
        videoView.setMediaController(mediaController);
        videoView.setVideoPath(highlight.getPath());
        videoView.requestFocus();
        videoView.start();
    }
}