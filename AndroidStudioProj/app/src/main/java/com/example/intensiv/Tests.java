package com.example.intensiv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.gson.Gson;



import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Tests extends AppCompatActivity {

    private BottomNavigationView btNav;
    TestResponse response;

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
        btNav.setSelectedItemId(R.id.nav_tests);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        loadTestData();

        LinearLayout container = findViewById(R.id.storiesContainer);

//        // Простые данные для историй
//        String[] storyTexts = {
//                "Призраки Сусанинской площади...",
//                "Тайны Костромского кремля...",
//                "Легенды старого моста...",
//                "Легенды старого моста...",
//                "Легенды старого моста...",
//                "Легенды старого моста...",
//        };

        // Добавляем карточки
        for (Test test : response.test) {
            View storyCard = createTestCard(container, test); // 50% прогресс
            container.addView(storyCard);
        }
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

//        img_background.setOnClickListener(v -> {
//
//                });
        // Убираем фиксированную ширину, теперь карточки растягиваются на всю ширину

        return card;
    }

    private void loadTestData() {
        try {
            InputStream is = getAssets().open("tests.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            response = new Gson().fromJson(json, TestResponse.class);
        } catch (Exception e) {
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


    class Test {
        private int id;
        private String title;
        private String description;
        private int completion_percentage;
        private String background_image;
        private String status_image;
        private List<Question> questions;

        // Геттеры и сеттеры
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getCompletionPercentage() {
            return completion_percentage;
        }

        public void setCompletionPercentage(int completion_percentage) {
            this.completion_percentage = completion_percentage;
        }

        public String getBackgroundImage() {
            return background_image;
        }

        public void setBackgroundImage(String background_image) {
            this.background_image = background_image;
        }

        public String getStatusImage() {
            return status_image;
        }

        public void setStatusImage(String status_image) {
            this.status_image = status_image;
        }

        public List<Question> getQuestions() {
            return questions;
        }

        public void setQuestions(List<Question> questions) {
            this.questions = questions;
        }
    }

    class Question {
        private int id;
        private String text;
        private List<Option> options;

        // Геттеры и сеттеры
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<Option> getOptions() {
            return options;
        }

        public void setOptions(List<Option> options) {
            this.options = options;
        }
    }

    class Option {
        private int id;
        private String text;
        private boolean is_correct;

        // Геттеры и сеттеры
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public boolean isCorrect() {
            return is_correct;
        }

        public void setCorrect(boolean is_correct) {
            this.is_correct = is_correct;
        }
    }

    class TestResponse {
        private List<Test> test;

        public List<Test> getTest() {
            return test;
        }

        public void setTest(List<Test> test) {
            this.test = test;
        }
    }
}