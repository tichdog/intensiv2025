package com.example.intensiv;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BottomMenu extends BottomSheetDialogFragment {
    private Context activityContext;
    private RootData data;


    public BottomMenu() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.down_menu, container, false);

        // Находим контейнер для карточек
        LinearLayout locationsContainer = view.findViewById(R.id.locations_container);

        try {
            loadPointsData();
            List<PointsData> points_a = data.getPoints();
            List<PointArrayItem> points = points_a.get(0).getPointsarray();

            // Создаем карточки для каждой локации
            for (PointArrayItem point : points) {
                View locationCard = inflater.inflate(R.layout.item_location_card, locationsContainer, false);

                TextView title = locationCard.findViewById(R.id.location_title);
                TextView coords = locationCard.findViewById(R.id.location_coords);

                if(point.getComplete()) {
                    locationCard.setBackgroundResource(R.drawable.back_history_active);
                }
                title.setText(point.getTitle());
                coords.setText(String.format("Координаты: %.6f, %.6f", point.getLat(), point.getLng()));

                locationsContainer.addView(locationCard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Обработчик кнопки закрытия
        Button btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
    }

    public void setActivityContext(Context context) {
        this.activityContext = context;
    }

    private void loadPointsData() {
        try {
            FileInputStream fis = activityContext.openFileInput("points1.json");
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            data = new Gson().fromJson(isr, RootData.class);
            isr.close();
        } catch (FileNotFoundException e) {
            try {
                InputStream is = activityContext.getAssets().open("points1.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String json = new String(buffer, StandardCharsets.UTF_8);
                data = new Gson().fromJson(json, RootData.class);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}