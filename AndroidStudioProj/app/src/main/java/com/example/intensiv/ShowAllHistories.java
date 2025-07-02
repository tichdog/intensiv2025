package com.example.intensiv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.io.File;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ShowAllHistories extends AppCompatActivity {

    private BottomNavigationView btNav;
    RootData routesRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        setOurTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_all_histories);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btNav = findViewById(R.id.bottom_nav_1);
        setupBottomNavigation();
        btNav.setSelectedItemId(R.id.nav_history);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LinearLayout container = findViewById(R.id.storiesContainer);

        try {
            // Загрузка данных из JSON
            loadPointsData();
            List<PointsData> points_a = routesRoot.getPoints();

            if (points_a != null) {
                for (PointsData point : points_a) {
                    View storyCard = createStoryCard(point, container);
                    container.addView(storyCard);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(0, 0);

            }
        });

    }

    private View createStoryCard(PointsData point, ViewGroup parent) {
        Context context = parent.getContext();
        View card = LayoutInflater.from(context).inflate(R.layout.itme_all_histories, parent, false);

        TextView textView = card.findViewById(R.id.title_text);
        TextView opisanie = card.findViewById(R.id.id_text);

        textView.setText(point.getName());
        opisanie.setText(String.valueOf(point.getId()));
        // Загрузка изображения
//        int resId = context.getResources().getIdentifier(
//                point.getShow(),
//                "drawable",
//                context.getPackageName()
//        );
//        if (resId != 0) {
//            img.setImageResource(resId);
//        }
//
//        // Передача данных в HistoryDescription
//        img.setOnClickListener(v -> {
//            Intent intent = new Intent(this, HistoryDescription.class);
//            intent.putExtra("title", point.getTitle());
//            intent.putExtra("description", point.getDescription());
//            intent.putExtra("image", point.getShow());  // Передаем имя файла изображения
//            startActivity(intent);
//            overridePendingTransition(0, 0);
//            finish();
//        });

        return card;
    }

    private void setupBottomNavigation() {
        btNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_map) {
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_history) {
                return true;
            } else if (id == R.id.nav_tests) {
                finish();
                overridePendingTransition(0, 0);
                startActivity(new Intent(this, Tests.class));
                return true;
            } else if (id == R.id.nav_settings) {
                finish();
                overridePendingTransition(0, 0);
                startActivity(new Intent(this, SettingsO.class));
                return true;
            }
            return false;
        });
    }

    private void setOurTheme() {
        String theme = android.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .getString("app_theme", "light");

        if (theme.equals("dark")) {
            setTheme(R.style.Theme_App_Dark);
        } else {
            setTheme(R.style.Theme_App_Light);
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(0, 0);
        return true;
    }

    private void loadPointsData() {
        try {
            FileInputStream fis = openFileInput("points1.json");
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            routesRoot = new Gson().fromJson(isr, RootData.class);

            isr.close();
        } catch (FileNotFoundException e) {
            try {
                InputStream is = getAssets().open("points1.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String json = new String(buffer, StandardCharsets.UTF_8);
                routesRoot = new Gson().fromJson(json, RootData.class);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
