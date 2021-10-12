package com.aix.memore.views.fragments;

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
import android.widget.AdapterView;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentGalleryMediaFullViewBinding;
import com.aix.memore.models.Gallery;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.views.adapters.GalleryFullViewAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class GalleryMediaFullViewFragment extends Fragment {

    private FragmentGalleryMediaFullViewBinding binding;
    private GalleryFullViewAdapter galleryFullViewAdapter;
    private List<Gallery> galleryList;
    private GalleryViewModel galleryViewModel;
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

        galleryViewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);
        galleryList = galleryViewModel.getSelectedGalleryList().getValue();
        galleryFullViewAdapter = new GalleryFullViewAdapter(requireContext(), galleryList);
        ErrorLog.WriteDebugLog("GALLERY LIST SIZE "+galleryList.size());
        binding.gallery.setAdapter(galleryFullViewAdapter);

        galleryViewModel.getSelectedMediaPosition().observe(requireActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                position = integer;
            }
        });

        //set selected image
        binding.gallery.setSelection(position);
        Glide.with(requireContext()).load(Uri.parse(galleryList.get(position).getPath()))
                .fitCenter()
                .error(R.drawable.ic_baseline_photo_24).into((binding.imageView));

        binding.gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Glide.with(requireContext()).load(Uri.parse(galleryList.get(i).getPath()))
                        .fitCenter()
                        .error(R.drawable.ic_baseline_photo_24).into((binding.imageView));
            }
        });

    }
}