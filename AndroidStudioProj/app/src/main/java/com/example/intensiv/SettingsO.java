package com.example.intensiv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsO extends AppCompatActivity {
    private BottomNavigationView btNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        setOurTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Инициализация RadioGroup
        btNav = findViewById(R.id.bottom_nav_1);
        setupBottomNavigation();
        btNav.setSelectedItemId(R.id.nav_settings);
        RadioGroup themeRadioGroup = findViewById(R.id.themeRadioGroup);
        RadioButton lightTheme = findViewById(R.id.lightTheme);
        RadioButton darkTheme = findViewById(R.id.darkTheme);

        // Установка текущей темы
        String currentTheme = getCurrentTheme();
        if (currentTheme.equals("dark")) {
            darkTheme.setChecked(true);
        } else {
            lightTheme.setChecked(true);
        }

        // Обработчик изменения темы
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            MainActivity.themeChanged = true;
            if (checkedId == R.id.darkTheme) {
                setThemeOur("dark");
            } else {
                setThemeOur("light");
            }
            recreateActivity();
        });

        // Инициализация кнопок глав
        setupChapterButtons();
    }

    private void setupChapterButtons() {
        Button[] chapterButtons = {
                findViewById(R.id.chapter1_btn),
                findViewById(R.id.chapter2_btn),
                findViewById(R.id.chapter3_btn),
                findViewById(R.id.chapter4_btn),
                findViewById(R.id.chapter5_btn),
                findViewById(R.id.chapter6_btn)
        };

        for (int i = 0; i < chapterButtons.length; i++) {
            final int chapterNumber = i + 1;
            chapterButtons[i].setOnClickListener(v -> {
                Intent intent = new Intent(SettingsO.this, NovelActivity.class);
                intent.putExtra("chapter", chapterNumber);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        Button btn = findViewById(R.id.user_strory);
        btn.setOnClickListener(v -> {
            startActivity(new Intent(SettingsO.this, CreateRouteActivity.class));
            finish();
        });
        Button btn1 = findViewById(R.id.user_strory_show);
        btn1.setOnClickListener(v -> {
            startActivity(new Intent(SettingsO.this, ShowAllHistories.class));
            finish();
        });
    }

    private void recreateActivity() {
        Intent intent = new Intent(this, SettingsO.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    // Остальные методы остаются без изменений
    private String getCurrentTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString("app_theme", "light");
    }

    private void setThemeOur(String theme) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("app_theme", theme)
                .apply();
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

    private void setupBottomNavigation() {
        btNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_map) {
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_history) {
                finish();
                overridePendingTransition(0, 0);
                startActivity(new Intent(this, History.class));
                return true;

            } else if (id == R.id.nav_tests) {
                finish();
                startActivity(new Intent(this, Tests.class));
                return true;
            } else if (id == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(0, 0);
        return true;
    }
}