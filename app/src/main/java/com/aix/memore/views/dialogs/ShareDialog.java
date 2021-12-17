package com.aix.memore.views.dialogs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aix.memore.R;
import com.aix.memore.databinding.FragmentShareDialogBinding;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.GalleryViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public class ShareDialog extends DialogFragment {

    private FragmentShareDialogBinding binding;
    private GalleryViewModel galleryViewModel;
    private Memore memore;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentShareDialogBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        galleryViewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);
        galleryViewModel.getBio().observe(getViewLifecycleOwner(), new Observer<Memore>() {
            @Override
            public void onChanged(Memore memore1) {
                memore = memore1;
            }
        });
        binding.buttonLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        binding.buttonShareNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateDynamicLink(memore.getMemore_id());
            }
        });




    }

    private void generateDynamicLink(String memore_id) {

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.memore.com/?highlight="+memore_id))
                .setDomainUriPrefix("https://memore.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.aix.memore")
                        .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle("Example of a Dynamic Link")
                                .setDescription("This link works whether the app is installed or not!")
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            ErrorLog.WriteDebugLog("MEMORE SHORT LINK "+shortLink);
                            ErrorLog.WriteDebugLog("FLOW CHART LINK "+flowchartLink);

                            funcShareIntent(shortLink);

                        } else {
                            ErrorLog.WriteDebugLog("ERROR GENERATING DYNAMIC LIN "+ task.getException());
                            ErrorLog.WriteErrorLog(task.getException());
                        }
                    }
                });
    }

    private void funcShareIntent(Uri shortLink) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        if(memore!=null) {
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Click the link to view " + memore.getBio_first_name() + " " + memore.getBio_last_name() + "'s" + " Memore:" + "\n" + shortLink);
        }else{
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Click the link to view Memore:" + "\n" + shortLink);
        }
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }
}