package com.aix.memore.views.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentKnowMoreBinding;
import com.aix.memore.models.Bio;
import com.aix.memore.models.Media;
import com.aix.memore.utilities.DateHelper;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.views.adapters.AlbumFirebaseAdapter;
import com.bumptech.glide.Glide;
import java.util.List;

public class KnowMoreFragment extends Fragment {
    private FragmentKnowMoreBinding binding;
    private GalleryViewModel galleryViewModel;
    private AlbumFirebaseAdapter albumFirebaseAdapter;
    private List<Media> mediaList;
    private int position = 0;
    private MutableLiveData<Integer> positionLiveData = new MutableLiveData<>();
    private HighlightViewModel highlightViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentKnowMoreBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        galleryViewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);
        highlightViewModel = new ViewModelProvider(requireActivity()).get(HighlightViewModel.class);
        highlightViewModel.getScannedValue().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                albumFirebaseAdapter = new AlbumFirebaseAdapter(galleryViewModel.getGalleryRecyclerOptions(s));
                albumFirebaseAdapter.updateOptions(galleryViewModel.getGalleryRecyclerOptions(s));
                binding.recyclerviewGallery.setAdapter(albumFirebaseAdapter);
                galleryViewModel.getDefaultGallery(s);
                galleryViewModel.addSnapshotListenerForBio(s);

            }
        });

        galleryViewModel.getBio().observe(getViewLifecycleOwner(), new Observer<Bio>() {
            @Override
            public void onChanged(Bio bio) {
                String full_name = bio.bio_first_name+ " "+bio.bio_middle_name+ " "+bio.bio_last_name;
                String death_date = bio.bio_birth_date+ " - "+ bio.bio_death_date.toDate();
                Glide.with(requireContext()).load(Uri.parse(bio.bio_profile_pic))
                        .error(R.drawable.ic_baseline_photo_24).into((binding.imageViewBioProfilePic));
                binding.textViewName.setText(full_name);
                binding.textViewDeathDate.setText(death_date);
            }
        });


        binding.imageSwitcherMemorial.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView= new ImageView(requireContext());
                return imageView;
            }
        });

        galleryViewModel.getDefaultGalleryMedia().observe(getViewLifecycleOwner(), new Observer<List<Media>>() {
            @Override
            public void onChanged(List<Media> media) {
                mediaList = media;
                Glide.with(requireContext()).load(Uri.parse(mediaList.get(position).getPath()))
                        .error(R.drawable.ic_baseline_photo_24).into((ImageView) binding.imageSwitcherMemorial.getCurrentView());
            }
        });
        positionLiveData.setValue(0);
        positionLiveData.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                ErrorLog.WriteDebugLog("Position Observer "+ position);
                //visibility
                if(position == 0){
                    binding.btnLeft.setVisibility(View.GONE);
                    ErrorLog.WriteDebugLog("DONT SHOW LEFT BTN "+ position);
                }else{
                    binding.btnLeft.setVisibility(View.VISIBLE);
                }

                if(mediaList != null) {
                    if (position == mediaList.size() - 1) {
                        binding.btnRight.setVisibility(View.GONE);
                    }else{
                        binding.btnRight.setVisibility(View.VISIBLE);
                    }
                }

            }
        });

//        initSwitchButton();

    }

    private void initSwitchButton(){

        //function
        binding.btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mediaList.isEmpty()) {
                    Glide.with(requireContext()).load(Uri.parse(mediaList.get(position+1).getPath()))
                            .error(R.drawable.ic_baseline_photo_24).into((ImageView) binding.imageSwitcherMemorial.getCurrentView());
                    position = position+1;
                    positionLiveData.setValue(position);
                }else{
                    ErrorLog.WriteDebugLog("MEDIA LIST EMPTY");
                }
            }
        });

        binding.btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mediaList.isEmpty()) {
                    Glide.with(requireContext()).load(Uri.parse(mediaList.get(position-1).getPath()))
                            .error(R.drawable.ic_baseline_photo_24).into((ImageView) binding.imageSwitcherMemorial.getCurrentView());
                    position=position-1;
                    positionLiveData.setValue(position);
                }else{
                    ErrorLog.WriteDebugLog("MEDIA LIST EMPTY");
                }
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        albumFirebaseAdapter.startListening();

    }

    @Override
    public void onPause() {
        super.onPause();
        albumFirebaseAdapter.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        galleryViewModel.detachMediaSnapshotListener();
        galleryViewModel.detachBioSnapshotListener();
    }
}