package com.example.intensiv;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.util.List;

public class BottomMenu extends BottomSheetDialogFragment {

    public BottomMenu() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.down_menu, container, false);

        // Находим контейнер для карточек
        LinearLayout locationsContainer = view.findViewById(R.id.locations_container);

        try {
            // Загружаем данные из JSON
            InputStreamReader reader = new InputStreamReader(getContext().getAssets().open("points.json"));
            PointsData data = new Gson().fromJson(reader, PointsData.class);
            List<PointData> points = data.getPoints();

            // Создаем карточки для каждой локации
            for (PointData point : points) {
                View locationCard = inflater.inflate(R.layout.item_location_card, locationsContainer, false);

                TextView title = locationCard.findViewById(R.id.location_title);
                TextView coords = locationCard.findViewById(R.id.location_coords);

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
}