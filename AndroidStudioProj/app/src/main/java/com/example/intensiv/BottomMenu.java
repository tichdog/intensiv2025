package com.example.intensiv;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomMenu extends BottomSheetDialogFragment {

    public BottomMenu() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.down_menu, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Устанавливаем стиль для затемнения фона
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
    }
}