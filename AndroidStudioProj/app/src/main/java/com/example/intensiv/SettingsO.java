package com.example.intensiv;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SettingsO extends AppCompatActivity {
    private BottomNavigationView btNav;
    private Button btnUpload, btnDownload;
    RootData data;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private FTPManager ftpManager;
    ArrayList<String> names_photo;

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(0, 0);
            }
        });


        btnUpload = findViewById(R.id.btnUpload);
        btnDownload = findViewById(R.id.btnDownload);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        ftpManager = new FTPManager();


        updateNamesPhoto();
        // Обработчик для кнопки загрузки НА сервер
        btnUpload.setOnClickListener(v -> {
            new FtpTask(true, names_photo).execute();
            updateNamesPhoto();
        });

        // Обработчик для кнопки скачивания С сервера
        btnDownload.setOnClickListener(v -> {
            new FtpTask(false, names_photo).execute();
            updateNamesPhoto();
        });
    }

    private void updateNamesPhoto() {
        loadPointsData();
        List<PointsData> points_a = data.getPoints();
        names_photo = new ArrayList<String>();
        for (PointsData datap : points_a) {
            for (PointArrayItem point : datap.getPointsarray()) {
                String sh = point.getShow();
                if (sh != null) {
                    names_photo.add(sh);
                }
            }
        }
    }


    private class FtpTask extends AsyncTask<Void, Integer, Boolean> {
        private boolean isUpload;
        private String errorMessage = "";
        String localFileName = "points1.json";
        private ArrayList<String> namesPhoto; // Добавляем поле для хранения массива
        private int currentProgress = 0;


        public FtpTask(boolean isUpload, ArrayList<String> namesPhoto) {
            this.isUpload = isUpload;
            this.namesPhoto = namesPhoto;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            tvStatus.setText(isUpload ? "Загрузка на сервер..." : "Скачивание с сервера...");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (!ftpManager.connect()) {
                    errorMessage = "Ошибка подключения";
                    return false;
                }
                File jsonFile = new File(getFilesDir(), localFileName);

                if (isUpload) {
                    if (!ftpManager.deleteAllImages()) {
                        Log.w("FTP", "Не все старые изображения удалены");
                    }
                    // ЗАГРУЗКА НА СЕРВЕР
                    Log.d("FILE_PATH", "Путь к файлу: " + jsonFile.getAbsolutePath());

                    if (!jsonFile.exists()) {
                        Log.e("FILE_ERROR", "Файл не найден!");
                        Toast.makeText(SettingsO.this, "Файл не найден по пути: " + jsonFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    }
                    if (!ftpManager.uploadFile(jsonFile, "/htdocs/uploads/points1.json")) {
                        return false;
                    }


                    for (String photoName : namesPhoto) {
                        File photoFile = new File(photoName);

                        if (photoFile.exists()) {
                            String fileName = photoFile.getName();
                            String remotePath = "/htdocs/uploads/images/" + fileName;
                            if (!ftpManager.uploadImage(photoFile, remotePath)) {
                                errorMessage = "Ошибка загрузки фото: " + photoName;
                                return false;
                            }
                        }
                        currentProgress++;
                        publishProgress((currentProgress * 100) / (namesPhoto.size() + 1));
                    }
                    return true;

                } else {
                    // СКАЧИВАНИЕ С СЕРВЕРА
                    // 1. Создаем временный файл
                    File tempFile = new File(getFilesDir(), "temp_" + localFileName);

                    // 2. Скачиваем во временный файл
                    boolean downloadSuccess = ftpManager.downloadFile("/htdocs/uploads/" + localFileName, tempFile);
                    tempFile.renameTo(jsonFile);

                    if (!downloadSuccess) {
                        return false;
                    }
                    namesPhoto = ftpManager.listImageFiles();
                    File imagesDir = new File(getFilesDir(), "app_point_images");
                    if (!imagesDir.exists()) {
                        imagesDir.mkdir();
                    }
                    for (String photoName : namesPhoto) {
                        File photoFile = new File(imagesDir, photoName);
                        String remotePath = "/htdocs/uploads/images/" + photoName;
                        if (!ftpManager.downloadAndCompressImage(remotePath, photoFile, SettingsO.this)) {
                            errorMessage = "Ошибка скачивания фото: " + photoName;
                            return false;
                        }
                        currentProgress++;
                        publishProgress((currentProgress * 100) / (namesPhoto.size() + 1));
                    }
                    return true;
                }
            } catch (IOException e) {
                errorMessage = e.getMessage();
                return false;
            } finally {
                try {
                    ftpManager.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        @Override
        protected void onPostExecute(Boolean success) {
            progressBar.setVisibility(View.GONE);
            if (success) {
                tvStatus.setText(isUpload ? "Файл загружен!" : "Файл скачан!");
                Toast.makeText(SettingsO.this,
                        isUpload ? "Успешная загрузка" : "Успешное скачивание",
                        Toast.LENGTH_SHORT).show();
            } else {
                tvStatus.setText("Ошибка: " + errorMessage);
                Toast.makeText(SettingsO.this,
                        "Ошибка: " + errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        }
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

    private void loadPointsData() {
        try {
            FileInputStream fis = openFileInput("points1.json");
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            data = new Gson().fromJson(isr, RootData.class);
            isr.close();
        } catch (FileNotFoundException e) {
            try {
                InputStream is = getAssets().open("points1.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String json = new String(buffer, StandardCharsets.UTF_8);
                data = new Gson().fromJson(json, RootData.class);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}