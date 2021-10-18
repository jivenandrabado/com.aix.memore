package com.aix.memore.views.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aix.memore.R;
import com.aix.memore.databinding.DialogProgressbarBinding;

import java.util.Objects;

public class ProgressDialogFragment extends DialogFragment {
    private DialogProgressbarBinding dialogProgressbarBinding;
    public ProgressDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dialogProgressbarBinding = DialogProgressbarBinding.inflate(inflater,container,false);
        Objects.requireNonNull(getDialog()).getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().setCancelable(false);

        return dialogProgressbarBinding.getRoot();
    }
}