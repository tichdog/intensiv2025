package com.example.intensiv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class History extends AppCompatActivity {

    private BottomNavigationView btNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        setOurTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btNav = findViewById(R.id.bottom_nav_1);
        setupBottomNavigation();
        btNav.setSelectedItemId(R.id.nav_history);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        LinearLayout container = findViewById(R.id.storiesContainer);

        // Простые данные для историй
        String[] storyTexts = {
                "Призраки Сусанинской площади...",
                "Тайны Костромского кремля...",
                "Легенды старого моста...",
                "Легенды старого моста...",
                "Легенды старого моста..."
        };

        // Добавляем карточки
        for (String text : storyTexts) {
            View storyCard = createStoryCard(text, 50, container); // 50% прогресс
            container.addView(storyCard);
        }
    }

    // Создание одной карточки истории
    private View createStoryCard(String text, int progress, ViewGroup parent) {
        Context context = parent.getContext();
        View card = LayoutInflater.from(context)
                .inflate(R.layout.item_story, parent, false);

        // Настройка элементов карточки (без изменений)
        TextView textView = card.findViewById(R.id.title_text);
        TextView progressBar = card.findViewById(R.id.percentage_text);
        TextView opisanie = card.findViewById(R.id.opis);
        ImageButton img = card.findViewById(R.id.history_open);
        img.setOnClickListener(v -> {overridePendingTransition(0, 0);
            startActivity(new Intent(this, HistoryDescription.class));});
        textView.setText(text);
        progressBar.setText(String.valueOf(progress) + "%");
        opisanie.setText("fdsffffffffffffffffffff fdsssssssssssssssss jkfhdsjkhfdskjhfjkdsh fhfdsjkhfkjdshfjsdf fdsfsdfdsfds пусто");


        // Убираем фиксированную ширину, теперь карточки растягиваются на всю ширину

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
//            } else if (id == R.id.nav_tests) {
//                startActivity(new Intent(this, TestsActivity.class));
//                return true;
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
