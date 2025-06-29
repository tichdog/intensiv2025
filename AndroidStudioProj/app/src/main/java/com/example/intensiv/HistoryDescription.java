package com.example.intensiv;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HistoryDescription extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        setOurTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_description);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Получение данных из Intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String imageName = getIntent().getStringExtra("image");


        // Установка данных
        TextView titleView = findViewById(R.id.title_text);
        TextView descriptionView = findViewById(R.id.opis);
        ImageView imageView = findViewById(R.id.image_view);  // Добавьте ImageView в ваш layout

        titleView.setText(title);
        descriptionView.setText(description);

        if (imageName != null) {
            int resId = getResources().getIdentifier(
                    imageName,
                    "drawable",
                    getPackageName()
            );
            if (resId != 0) {
                imageView.setImageResource(resId);
            }
        }
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
}
