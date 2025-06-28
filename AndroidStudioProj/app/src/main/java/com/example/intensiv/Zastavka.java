package com.example.intensiv;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class Zastavka extends AppCompatActivity {
    private int clickCount = 0;
    private String[] messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setOurTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zast_layout); // Укажите имя вашего XML-файла

        messages = getResources().getStringArray(R.array.zastavka_array);

        ConstraintLayout rootLayout = findViewById(R.id.root);
        TextView someIdTextView = findViewById(R.id.some_id);
        someIdTextView.setText(messages[clickCount]);

        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCount = clickCount + 1;
                if (clickCount == 5){
                    startMainActivity();
                }
                else {
                    someIdTextView.setText(messages[clickCount]);
                }
            }
        });
    }

    private void setOurTheme() {
        String theme = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("app_theme", "light");

        if (theme.equals("dark")) {
            setTheme(R.style.Theme_App_Dark);
        } else {
            setTheme(R.style.Theme_App_Light);
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(Zastavka.this, MainActivity.class);
        startActivity(intent);
        finish(); // Закрываем текущую активность
    }
}