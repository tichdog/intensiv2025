package com.example.intensiv;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.gson.Gson;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class NovelActivity extends AppCompatActivity {
    private StoryData storyData;
    private int currentChapterId = 1;
    private int currentDialogueIndex = 0;
    private ImageView characterLeft, characterRight;
    private TextView characterName, characterSpeech, continueHint;
    private CardView dialogCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setOurTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.novel);

        // Получаем номер главы из интента
        currentChapterId = getIntent().getIntExtra("chapter", 1);

        // Инициализация UI элементов
        characterLeft = findViewById(R.id.character_left);
        characterRight = findViewById(R.id.character_right);
        characterName = findViewById(R.id.character_name);
        characterSpeech = findViewById(R.id.character_speech);
        continueHint = findViewById(R.id.continue_hint);
        dialogCard = findViewById(R.id.dialog_card);

        // Загрузка истории из JSON
        loadStoryData();

        // Обработчик нажатия на экран
        dialogCard.setOnClickListener(v -> showNextDialogue());

        // Показать первую реплику главы
        showCurrentDialogue();

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
    private void loadStoryData() {
        try {
            InputStream is = getAssets().open("story.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            storyData = new Gson().fromJson(json, StoryData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCurrentDialogue() {
        StoryScene scene = getCurrentScene();
        if (scene == null || currentDialogueIndex >= scene.dialogue.size()) {
            finish();
            return;
        }

        Dialogue dialogue = scene.dialogue.get(currentDialogueIndex);

        // Установка персонажей
        setCharacterImage(characterLeft, dialogue.left_character);
        setCharacterImage(characterRight, dialogue.right_character);

        // Установка текста
        characterName.setText(dialogue.name);
        characterSpeech.setText(dialogue.text);
    }

    private void setCharacterImage(ImageView imageView, String imageName) {
        if (imageName == null || imageName.equals("null")) {
            imageView.setVisibility(View.INVISIBLE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            imageView.setImageResource(resId);
        }
    }

    private void showNextDialogue() {
        StoryScene scene = getCurrentScene();
        if (scene != null && currentDialogueIndex < scene.dialogue.size() - 1) {
            currentDialogueIndex++;
            showCurrentDialogue();
        } else {
            finish();
        }
    }

    private StoryScene getCurrentScene() {
        if (storyData == null) return null;
        for (StoryScene scene : storyData.scenes) {
            if (scene.id == currentChapterId) {
                return scene;
            }
        }
        return null;
    }

    // Классы для парсинга JSON
    private static class StoryData {
        List<StoryScene> scenes;
    }

    private static class StoryScene {
        int id;
        String title;
        List<Dialogue> dialogue;
    }

    private static class Dialogue {
        String left_character;
        String right_character;
        String name;
        String text;
    }
}