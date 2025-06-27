package com.example.intensiv;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            if (checkedId == R.id.darkTheme) {
                setThemeOur("dark");
            } else {
                setThemeOur("light");
            }
            recreate(); // Пересоздаем активность для применения темы
        });
    }

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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}