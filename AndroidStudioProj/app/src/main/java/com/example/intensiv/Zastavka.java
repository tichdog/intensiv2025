package com.example.intensiv;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class Zastavka extends AppCompatActivity {
    private int clickCount = 0;
    private String[] messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                someIdTextView.setText(messages[clickCount]);
            }
        });
    }
}