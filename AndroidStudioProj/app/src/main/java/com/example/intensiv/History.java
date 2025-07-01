package com.example.intensiv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.util.List;

public class History extends AppCompatActivity {

    private BottomNavigationView btNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        setOurTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

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
            InputStreamReader reader = new InputStreamReader(getAssets().open("points.json"));
            RootData data = new Gson().fromJson(reader, RootData.class);
            List<PointsData> points_a = data.getPoints();
            List<PointArrayItem> points = points_a.get(0).getPointsarray();
            if (points != null) {
                for (PointArrayItem point : points) {
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

    private View createStoryCard(PointArrayItem point, ViewGroup parent) {
        Context context = parent.getContext();
        View card = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false);

        TextView textView = card.findViewById(R.id.title_text);
        TextView opisanie = card.findViewById(R.id.opis);
        ImageButton img = card.findViewById(R.id.history_open);

        textView.setText(point.getTitle());
        opisanie.setText(point.getShort());
        // Загрузка изображения
        int resId = context.getResources().getIdentifier(
                point.getShow(),
                "drawable",
                context.getPackageName()
        );
        if (resId != 0) {
            img.setImageResource(resId);
        }

        // Передача данных в HistoryDescription
        img.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryDescription.class);
            intent.putExtra("title", point.getTitle());
            intent.putExtra("description", point.getDescription());
            intent.putExtra("image", point.getShow());  // Передаем имя файла изображения
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

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
                startActivity(new Intent(this, Settings.class));
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
}
