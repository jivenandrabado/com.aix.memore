package com.aix.memore.views.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aix.memore.databinding.DialogAlbumDetailsBinding;
import com.aix.memore.models.Album;
import com.aix.memore.models.AlbumDetails;
import com.aix.memore.utilities.GlobalFunctions;

public class AlbumDetailsDialog extends DialogFragment {

    private DialogAlbumDetailsBinding binding;
    private Album album;
    private AlbumDetails albumDetails;

    public AlbumDetailsDialog(Album album, AlbumDetails albumDetails) {
        this.album = album;
        this.albumDetails = albumDetails;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAlbumDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        super.onStart();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.textViewAlbumName.setText(album.getTitle());
        binding.textViewPrivacy.setText(album.getIs_public() ? "Public" : "Private");
        binding.textViewFileCount.setText(String.valueOf(albumDetails.getFileCount()));
        binding.textViewStorageSize.setText(GlobalFunctions.convertFileSize(albumDetails.getByteSize()));
    }
}
