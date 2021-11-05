package com.aix.memore.views.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentCreateMemoreBinding;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.DateHelper;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.MemoreViewModel;
import com.bumptech.glide.Glide;

import java.util.Calendar;


public class CreateMemoreFragment extends Fragment {

    private FragmentCreateMemoreBinding binding;
    private MemoreViewModel memoreViewModel;
    private NavController navController;
    private Memore memore;
    private String first_name, middle_name, last_name, video_highlight, birth_date, death_date, profile_pic;
    private DatePickerDialog datePickerDialog;



    public CreateMemoreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       binding = FragmentCreateMemoreBinding.inflate(inflater,container,false);
       return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        memoreViewModel = new ViewModelProvider(requireActivity()).get(MemoreViewModel.class);
        navController = Navigation.findNavController(view);
        memore = new Memore();


        binding.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getMemoreDetails()) {
                    navController.navigate(R.id.action_createMemoreFragment_to_QRGeneratorFragment);
                }
            }
        });

        initVideoChooser();

        binding.editTextBday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initDatePicker(binding.editTextBday);

            }
        });

        binding.editTextDeathDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initDatePicker(binding.editTextDeathDate);

            }
        });

        binding.imageViewProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

    }

    private void initDatePicker(EditText editText) {
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        // date picker dialog
        datePickerDialog = new DatePickerDialog(requireContext(),R.style.DatePickerTheme,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        editText.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private boolean getMemoreDetails(){
        first_name = String.valueOf(binding.editTextFirstName.getText());
        middle_name = String.valueOf(binding.editTextMiddleName.getText());
        last_name = String.valueOf(binding.editTextLastName.getText());
        birth_date = String.valueOf(binding.editTextBday.getText());
        death_date = String.valueOf(binding.editTextDeathDate.getText());

        if(!isEmptyFields(first_name, last_name,video_highlight, birth_date,death_date)){
            memore.setBio_first_name(first_name);
            memore.setBio_middle_name(middle_name);
            memore.setBio_last_name(last_name);
            memore.setVideo_highlight(video_highlight);
            memore.setBio_birth_date(DateHelper.stringToDate(birth_date));
            memore.setBio_profile_pic(profile_pic);

            memoreViewModel.getMemoreMutableLiveData().setValue(memore);
            return true;
        }

        return false;


    }

    private void initVideoChooser(){

        binding.buttonAddHighlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseVideo();
            }
        });

    }

    private void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setData(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        chooseVideoActivityResult.launch(intent);

    }

    private ActivityResultLauncher<Intent> chooseVideoActivityResult = registerForActivityResult(
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

                                //public wall
//                                galleryViewModel.uploadToFirebaseStorageToPublicWall(highlightViewModel.getScannedValue().getValue(),imageUri);
                            }
                        } else {
                            Uri uri = data.getData();
                            // your codefor single image selection
                            ErrorLog.WriteDebugLog("DATA RECEIVED "+uri);
                            video_highlight = String.valueOf(uri);

//                            uploadDialog.show(getChildFragmentManager(),"UPLOAD_DIALOG");

                            //public wall
//                            galleryViewModel.uploadToFirebaseStorageToPublicWall(highlightViewModel.getScannedValue().getValue(),uri);



                        }
                    }
                }
            }
    );

    private boolean isEmptyFields(String firstName,String lastName,String video_highlight, String birth_date, String death_date){

        if (TextUtils.isEmpty(firstName)) {
            Toast.makeText(requireContext(), "Empty first name", Toast.LENGTH_LONG).show();
            ErrorLog.WriteDebugLog("empty first name");
            return true;
        }if (TextUtils.isEmpty(lastName)) {
            Toast.makeText(requireContext(), "Empty last name", Toast.LENGTH_LONG).show();
            ErrorLog.WriteDebugLog("empty last name");
            return true;
        }if (TextUtils.isEmpty(video_highlight)) {
            Toast.makeText(requireContext(), "Empty video highlight", Toast.LENGTH_LONG).show();
            ErrorLog.WriteDebugLog("empty last name");
            return true;
        }if (TextUtils.isEmpty(birth_date)) {
            Toast.makeText(requireContext(), "Empty Birth Date", Toast.LENGTH_LONG).show();
            ErrorLog.WriteDebugLog("empty last name");
            return true;
        }else{
            return false;
        }

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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

                                profile_pic = String.valueOf(imageUri);

                            }
                        } else {
                            Uri uri = data.getData();
                            // your codefor single image selection
                            ErrorLog.WriteDebugLog("DATA RECEIVED "+uri);
                            profile_pic = String.valueOf(uri);

                            Glide.with(requireContext()).load(uri)
                                    .fitCenter()
                                    .error(R.drawable.ic_baseline_photo_24).into((binding.imageViewProfilePic));

                        }




                    }
                }
            }
    );
}