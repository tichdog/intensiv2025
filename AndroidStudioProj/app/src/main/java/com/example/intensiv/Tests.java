package com.example.intensiv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;


import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Tests extends AppCompatActivity {

    private BottomNavigationView btNav;
    TestResponse response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        setOurTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tests);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btNav = findViewById(R.id.bottom_nav_1);
        setupBottomNavigation();
        btNav.setSelectedItemId(R.id.nav_tests);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        loadTestData();

        LinearLayout container = findViewById(R.id.storiesContainer);

        // Добавляем карточки
        for (Test test : response.getTest()) {
            View storyCard = createTestCard(container, test); // 50% прогресс
            container.addView(storyCard);
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    // Создание одной карточки истории
    private View createTestCard(ViewGroup parent, Test test) {
        Context context = parent.getContext();

        View card = LayoutInflater.from(context)
                .inflate(R.layout.item_test, parent, false);


        // Настройка элементов карточки (без изменений)
        TextView textView = card.findViewById(R.id.title_text);
        TextView progressBar = card.findViewById(R.id.percentage_text);
        TextView opisanie = card.findViewById(R.id.opis);
        ImageView img_complete = card.findViewById(R.id.completeTest);
        ImageView img_background = card.findViewById(R.id.background);
//        ImageButton img = card.findViewById(R.id.history_open);
//        img.setOnClickListener(v -> {overridePendingTransition(0, 0);
//            startActivity(new Intent(this, HistoryDescription.class));});
        textView.setText(test.getTitle());
        progressBar.setText(String.valueOf(test.getCompletionPercentage()) + "%");
        int number_question = test.getQuestions().size();
        opisanie.setText("Тест из " + number_question + " вопросов");
        int resId = context.getResources().getIdentifier(
                test.getStatusImage(),
                "drawable",
                context.getPackageName()
        );
        if (resId != 0) {
            img_complete.setImageResource(resId);
        }
        int resId1 = context.getResources().getIdentifier(
                test.getBackgroundImage(),
                "drawable",
                context.getPackageName()
        );
        if (resId1 != 0) {
            img_background.setImageResource(resId1);
        }

        img_background.setOnClickListener(v -> {
            Intent intent1 = new Intent(this, TestRun.class);
            intent1.putExtra("ID_test", test.getId());
            finish();
            startActivity(intent1);
        });
        // Убираем фиксированную ширину, теперь карточки растягиваются на всю ширину

        return card;
    }

    private void loadTestData() {
        try {
            FileInputStream fis = openFileInput("tests.json");
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            response = new Gson().fromJson(isr, TestResponse.class);
            isr.close();
        } catch (FileNotFoundException e) {
            // Если файл не найден, можно загрузить данные из assets
            try {
                InputStream is = getAssets().open("tests.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String json = new String(buffer, StandardCharsets.UTF_8);
                response = new Gson().fromJson(json, TestResponse.class);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setupBottomNavigation() {
        btNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_map) {
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_history) {
                finish();
                startActivity(new Intent(this, History.class));
                return true;
            } else if (id == R.id.nav_tests) {
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
}

