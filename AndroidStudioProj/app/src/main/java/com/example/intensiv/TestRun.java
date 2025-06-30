package com.example.intensiv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import com.google.gson.Gson;

import com.google.android.gms.common.api.Response;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class TestRun extends AppCompatActivity {
    private BottomNavigationView btNav;
    private TestResponse response;
    private Test currentTest;
    private int currentQuestionIndex = 0;
    private Button option1, option2, option3, option4;
    private TextView questionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        setOurTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_run);

        // Инициализация UI элементов
        initUI();

        // Загрузка данных теста
        loadTestData();

        // Получение ID теста из Intent
        int id_test = getIntent().getIntExtra("ID_test", 1);

        // Поиск нужного теста
        for (Test test : response.getTest()) {
            if (test.getId() == id_test) {
                currentTest = test;
                break;
            }
        }

        // Проверка, что тест найден
        if (currentTest != null && !currentTest.getQuestions().isEmpty()) {
            displayQuestion(currentQuestionIndex);
        }
    }

    private void initUI() {
        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Настройка нижней навигации
        btNav = findViewById(R.id.bottom_nav_1);
        setupBottomNavigation();
        btNav.setSelectedItemId(R.id.nav_settings);

        questionText = findViewById(R.id.some_id); // Нужно добавить в XML

        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);

        // Установка обработчиков для кнопок ответов
        View.OnClickListener answerClickListener = v -> {
            Button selectedButton = (Button) v;
            checkAnswer(selectedButton.getText().toString());
            moveToNextQuestion();
        };

        option1.setOnClickListener(answerClickListener);
        option2.setOnClickListener(answerClickListener);
        option3.setOnClickListener(answerClickListener);
        option4.setOnClickListener(answerClickListener);
    }

    private void displayQuestion(int questionIndex) {
        if (questionIndex >= currentTest.getQuestions().size()) {
            finishTest();
            return;
        }

        Question question = currentTest.getQuestions().get(questionIndex);

        // Установка номера вопроса и текста
        questionText.setText(question.getText());

        // Установка вариантов ответов
        List<Option> options = question.getOptions();
        option1.setText(options.get(0).getText());
        option2.setText(options.get(1).getText());
        option3.setText(options.get(2).getText());
        option4.setText(options.get(3).getText());
    }

    private void checkAnswer(String selectedAnswer) {
        Question currentQuestion = currentTest.getQuestions().get(currentQuestionIndex);
        for (Option option : currentQuestion.getOptions()) {
            if (option.getText().equals(selectedAnswer) && option.isCorrect()) {
                // Правильный ответ
                return;
            }
        }
        // Неправильный ответ
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < currentTest.getQuestions().size()) {
            displayQuestion(currentQuestionIndex);
        } else {
            finishTest();
        }
    }

    private void finishTest() {
        // Расчет процента выполнения
        int totalQuestions = currentTest.getQuestions().size();
        int correctAnswers = calculateCorrectAnswers();
        int percentage = (int) ((correctAnswers / (float) totalQuestions) * 100);

        // Обновление теста
        currentTest.setCompletionPercentage(percentage);
        saveTestsToFile(this);

        // Возврат к списку тестов
        Intent intent = new Intent(this, Tests.class);
        startActivity(intent);
        finish();
    }

    private int calculateCorrectAnswers() {
        // Здесь должна быть логика подсчета правильных ответов
        // В текущей реализации всегда возвращаем 100% для примера
        return currentTest.getQuestions().size();
    }

    // Остальные методы без изменений
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
        startActivity(new Intent(this, History.class));
        overridePendingTransition(0, 0);
        return true;
    }

    private void loadTestData() {
        try {
            FileInputStream fis = openFileInput("tests.json");
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            response = new Gson().fromJson(isr, TestResponse.class);
            isr.close();
        } catch (FileNotFoundException e) {
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

    private void saveTestsToFile(Context context) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(response);

            FileOutputStream fos = context.openFileOutput("tests.json", Context.MODE_PRIVATE);
            fos.write(json.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}