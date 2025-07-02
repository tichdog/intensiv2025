package com.example.intensiv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
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
    private TextView option1, option2, option3, option4;
    private TextView questionText;
    private int correctAnswer = 0;
    private ImageButton next_button;
    private boolean answerCheked = false;

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(0, 0);
                startActivity(new Intent(TestRun.this, Tests.class));
            }
        });
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
        btNav.setSelectedItemId(R.id.nav_tests);

        questionText = findViewById(R.id.some_id);

        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        next_button = findViewById(R.id.next_button);

        // Установка обработчиков для кнопок ответов
        View.OnClickListener answerClickListener = v -> {
            if (!answerCheked) {
                answerCheked = true;
                next_button.setVisibility(View.VISIBLE);
                TextView selectedButton = (TextView) v;

                boolean check = checkAnswer(selectedButton.getText().toString());

                // Для MaterialButton используем setBackgroundResource()
                if (check) {
                    selectedButton.setBackgroundResource(R.drawable.test_item_rectangle_correct);
                } else {
                    selectedButton.setBackgroundResource(R.drawable.test_item_rectangle_incorrect);
                }
            }
        };

        option1.setOnClickListener(answerClickListener);
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

        // Установка текста вопроса
        setAdaptiveTextByLengthQ(questionText, question.getText());

        // Установка вариантов ответов
        List<Option> options = question.getOptions();
        setAdaptiveTextByLengthA(option1, options.get(0).getText());
        setAdaptiveTextByLengthA(option2, options.get(1).getText());
        setAdaptiveTextByLengthA(option3, options.get(2).getText());
        setAdaptiveTextByLengthA(option4, options.get(3).getText());

        // Сброс фона кнопок к стандартному
        resetButtonBackgrounds();
    }

    private void setAdaptiveTextByLengthA(TextView textView, String text) {
        // Устанавливаем базовые параметры
        float baseSizeSp = 24f; // Базовый размер шрифта
        float minSizeSp = 10f;  // Минимальный допустимый размер

        // Определяем размер на основе длины текста
        float newSizeSp;
        if (text.length() < 20) {
            newSizeSp = baseSizeSp; // Короткий текст - базовый размер
        } else if (text.length() < 40) {
            newSizeSp = baseSizeSp - 6f; // Средний текст - немного меньше
        } else if (text.length() < 60) {
            newSizeSp = baseSizeSp - 10f; // Длинный текст - еще меньше
        } else if (text.length() < 80) {
            newSizeSp = baseSizeSp - 12f; // Длинный текст - еще меньше
        } else {
            newSizeSp = baseSizeSp - 14f; // Очень длинный текст - минимально допустимый
        }

        // Гарантируем, что размер не меньше минимального
        newSizeSp = Math.max(newSizeSp, minSizeSp);

        // Устанавливаем размер
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSizeSp);
        textView.setText(text);
        int lineHeightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                newSizeSp + 3,
                getResources().getDisplayMetrics()
        );

        textView.setLineHeight(lineHeightPx);
    }


    private void setAdaptiveTextByLengthQ(TextView textView, String text) {
        // Устанавливаем базовые параметры
        float baseSizeSp = 35f; // Базовый размер шрифта
        float minSizeSp = 10f;  // Минимальный допустимый размер

        // Определяем размер на основе длины текста
        float newSizeSp;
        if (text.length() < 20) {
            newSizeSp = baseSizeSp; // Короткий текст - базовый размер
        } else if (text.length() < 40) {
            newSizeSp = baseSizeSp - 8f; // Средний текст - немного меньше
        } else if (text.length() < 60) {
            newSizeSp = baseSizeSp - 10f; // Длинный текст - еще меньше
        } else if (text.length() < 80) {
            newSizeSp = baseSizeSp - 12f; // Длинный текст - еще меньше
        } else {
            newSizeSp = baseSizeSp - 14f; // Очень длинный текст - минимально допустимый
        }

        // Гарантируем, что размер не меньше минимального
        newSizeSp = Math.max(newSizeSp, minSizeSp);

        // Устанавливаем размер
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSizeSp);
        textView.setText(text);
        int lineHeightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                newSizeSp + 3,
                getResources().getDisplayMetrics()
        );

        textView.setLineHeight(lineHeightPx);
    }

    private void resetButtonBackgrounds() {
        option1.setBackgroundResource(R.drawable.test_item_rectangle);
        option2.setBackgroundResource(R.drawable.test_item_rectangle);
        option3.setBackgroundResource(R.drawable.test_item_rectangle);
        option4.setBackgroundResource(R.drawable.test_item_rectangle);

    }

    private boolean checkAnswer(String selectedAnswer) {
        Question currentQuestion = currentTest.getQuestions().get(currentQuestionIndex);
        for (Option option : currentQuestion.getOptions()) {
            if (option.getText().equals(selectedAnswer)) {
                if (option.isCorrect()) {
                    // Правильный ответ
                    correctAnswer++;
                    return true;
                }
            }
        }
        return false;
    }

    public void moveToNextQuestion(View view) {
        if (answerCheked) {
            answerCheked = false;
            next_button.setVisibility(View.INVISIBLE);
            currentQuestionIndex++;
            if (currentQuestionIndex < currentTest.getQuestions().size()) {
                displayQuestion(currentQuestionIndex);
            } else {
                finishTest();
            }
        }
    }

    private void finishTest() {
        // Расчет процента выполнения
        int totalQuestions = currentTest.getQuestions().size();
        int percentage = (int) ((correctAnswer / (float) totalQuestions) * 100);

        // Обновление теста
        currentTest.setCompletionPercentage(percentage);
        currentTest.setStatusImage("test_complete");
        saveTestsToFile(this);

        // Возврат к списку тестов
        Intent intent = new Intent(this, Tests.class);
        startActivity(intent);
        finish();
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
                return true;
            } else if (id == R.id.nav_settings) {
                finish();
                startActivity(new Intent(this, SettingsO.class));
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        startActivity(new Intent(this, Tests.class));
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