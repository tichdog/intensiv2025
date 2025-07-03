package com.example.intensiv;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CreateRouteActivity extends AppCompatActivity implements InputListener {
    private BottomNavigationView btNav;

    private MapView mapView;
    private RootData routesRoot;
    private PointsData currentRoute;
    private int currentPointId = 1;
    private int currentRouteId = 1;


    //private TextView routeIdEditText;
    private EditText routeNameEditText;
    private Button saveRouteButton;
    private RecyclerView pointsRecyclerView;
    private long lastTapTime = 0;

    private PointsAdapter pointsAdapter;
    private AlertDialog currentPointDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        setOurTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btNav = findViewById(R.id.bottom_nav_1);
        setupBottomNavigation();
        btNav.setSelectedItemId(R.id.nav_settings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        loadPointsData();

        if (routesRoot != null && routesRoot.getPoints() != null && !routesRoot.getPoints().isEmpty()) {
            currentRouteId = routesRoot.getPoints().stream()
                    .mapToInt(PointsData::getId)
                    .max()
                    .orElse(0) + 1;
        } else {
            currentRouteId = 1;
        }

        // Инициализация MapView
        mapView = findViewById(R.id.mapView);
        mapView.getMap().addInputListener(this);

        // Инициализация UI элементов
        //routeIdEditText = findViewById(R.id.routeIdEditText);
        routeNameEditText = findViewById(R.id.routeNameEditText);
        saveRouteButton = findViewById(R.id.saveRouteButton);
        pointsRecyclerView = findViewById(R.id.pointsRecyclerView);

        // Инициализация данных
        //routesRoot = new RootData();
        createNewRoute();

        // Настройка RecyclerView
        pointsAdapter = new PointsAdapter();
        pointsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pointsRecyclerView.setAdapter(pointsAdapter);


        mapView.getMap().move(
                new CameraPosition(new Point(57.766909, 40.927924), 15, 0, 0),
                new Animation(Animation.Type.SMOOTH, 0.3f),
                null
        );

        // Обработчики событий
        saveRouteButton.setOnClickListener(v -> saveRoutes());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(0, 0);
                startActivity(new Intent(CreateRouteActivity.this, SettingsO.class));


            }
        });
    }

    private void createNewRoute() {
        currentRoute = new PointsData(currentRouteId, "Новый маршрут");
        routesRoot.addRoute(currentRoute);
        //routeIdEditText.setText("ID маршрута: " + currentRouteId);
        routeNameEditText.setText(currentRoute.getName());
    }

    @Override
    public void onMapTap(@NonNull Map map, @NonNull Point point) {
        if (System.currentTimeMillis() - lastTapTime > 300) { // Защита от быстрых повторных нажатий
            lastTapTime = System.currentTimeMillis();
            // Ваш код добавления точки

            PointArrayItem newPoint = new PointArrayItem(
                    currentPointId,
                    "Точка " + currentPointId,
                    point.getLatitude(),
                    point.getLongitude()
            );
            showPointDialog(newPoint);
        }
    }

    @Override
    public void onMapLongTap(@NonNull Map map, @NonNull Point point) {

    }

    private void showPointDialog(PointArrayItem point) {
        currentEditingPoint = point;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_point_edit, null);

        EditText titleEditText = dialogView.findViewById(R.id.titleEditText);
        EditText shrtEditText = dialogView.findViewById(R.id.shrtEditText);
        EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);

        titleEditText.setText(currentEditingPoint.getTitle());
        shrtEditText.setText(currentEditingPoint.getShort());
        descriptionEditText.setText(currentEditingPoint.getDescription());


        // существующие поля
        Button addPhotoButton = dialogView.findViewById(R.id.addPhotoButton);
        ImageView photoPreview = dialogView.findViewById(R.id.photoPreview);

        // если есть изображение, показываем его
        if (currentEditingPoint.getShow() != null && !currentEditingPoint.getShow().isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentEditingPoint.getShow());
            photoPreview.setImageBitmap(bitmap);
        }

        addPhotoButton.setOnClickListener(v -> {
            // Запускаем Intent для выбора изображения
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            // Записываем в текущую точку информацию для правильного пересоздания диалога
            currentEditingPoint.setTitle(titleEditText.getText().toString());
            currentEditingPoint.setShrt(shrtEditText.getText().toString());
            currentEditingPoint.setDescription(descriptionEditText.getText().toString());
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                    REQUEST_IMAGE_PICK + currentEditingPoint.getId());
        });

        if (currentPointDialog != null && currentPointDialog.isShowing()) {
            currentPointDialog.dismiss();
        }

        currentPointDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Редактирование точки")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    currentEditingPoint.setTitle(titleEditText.getText().toString());
                    currentEditingPoint.setShrt(shrtEditText.getText().toString());
                    currentEditingPoint.setDescription(descriptionEditText.getText().toString());

                    boolean f1 = false;
                    for (PointArrayItem pointer : currentRoute.getPointsarray()) {
                        if (pointer.getId() == currentEditingPoint.getId()) {
                            f1 = true;
                        }
                    }
                    if (!f1) {
                        currentRoute.addPoint(currentEditingPoint);
                        addPointMarker(currentEditingPoint);
                        currentPointId++;
                    }
                    pointsAdapter.notifyDataSetChanged();

                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void addPointMarker(PointArrayItem point) {
        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
        PlacemarkMapObject marker = mapObjects.addPlacemark(new Point(point.getLat(), point.getLng()));
        marker.setIcon(ImageProvider.fromResource(this, R.drawable.point_map));
        marker.setUserData(point);
    }

    private void saveRoutes() {
        try {
            int id = currentRouteId;
            String name = routeNameEditText.getText().toString();
            currentRoute.setId(id);
            currentRoute.setName(name);
            saveMarshrutToFile();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Некорректный ID маршрута", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveMarshrutToFile() {
        try {
            String json = new Gson().toJson(routesRoot);
            FileOutputStream fos = openFileOutput("points1.json", Context.MODE_PRIVATE);
            fos.write(json.getBytes(StandardCharsets.UTF_8));
            fos.close();
            Toast.makeText(this, "Маршрут сохранен", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setOurTheme() {
        String theme = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("app_theme", "light");

        setTheme(theme.equals("dark") ? R.style.Theme_App_Dark : R.style.Theme_App_Light);
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
                overridePendingTransition(0, 0);
                startActivity(new Intent(this, Tests.class));
                return true;
            } else if (id == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }

    private void loadPointsData() {
        try {
            FileInputStream fis = openFileInput("points1.json");
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            routesRoot = new Gson().fromJson(isr, RootData.class);

            isr.close();
        } catch (FileNotFoundException e) {
            try {
                InputStream is = getAssets().open("points1.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String json = new String(buffer, StandardCharsets.UTF_8);
                routesRoot = new Gson().fromJson(json, RootData.class);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        MapKitFactory.getInstance().onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    private class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_point, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) { //обновление точки будет тут
            PointArrayItem point = currentRoute.getPointsarray().get(position);
            holder.titleTextView.setText(point.getTitle());
            holder.positionTextView.setText(String.format("Ш: %.6f, Д: %.6f", point.getLat(), point.getLng()));

            holder.itemView.setOnClickListener(v -> {
                mapView.getMap().move(
                        new CameraPosition(new Point(point.getLat(), point.getLng()), 16, 0, 0),
                        new Animation(Animation.Type.SMOOTH, 0.3f),
                        null
                );
                showPointDialog(point);
            });
        }

        @Override
        public int getItemCount() {
            return currentRoute.getPointsarray().size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView positionTextView;

            ViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.pointTitle);
                positionTextView = itemView.findViewById(R.id.pointPosition);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(new Intent(this, SettingsO.class));
        return true;
    }

    private static final int REQUEST_IMAGE_PICK = 1000;

    private PointArrayItem currentEditingPoint;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            // Проверяем, что у нас есть текущая редактируемая точка
            if (currentEditingPoint != null) {
                try {
                    // Сохраняем изображение и обновляем точку
                    String imagePath = saveImageToInternalStorage(imageUri, "point_" + currentRoute.getId() + currentEditingPoint.getId());
                    currentEditingPoint.setShow(imagePath);


                    if (currentPointDialog != null && currentPointDialog.isShowing()) {
                        currentPointDialog.dismiss();
                    }
                    // Обновляем адаптер
                    pointsAdapter.notifyDataSetChanged();
                    // Закрываем диалог и открываем заново с обновленным изображением
                    recreatePointDialog();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void recreatePointDialog() {
        // Закрываем текущий диалог
        if (currentEditingPoint != null) {
            // Создаем копию точки, так как оригинал уже изменен
            PointArrayItem pointCopy = new PointArrayItem(
                    currentEditingPoint.getId(),
                    currentEditingPoint.getTitle(),
                    currentEditingPoint.getLat(),
                    currentEditingPoint.getLng()
            );
            pointCopy.setShow(currentEditingPoint.getShow());
            pointCopy.setShrt(currentEditingPoint.getShort());
            pointCopy.setDescription(currentEditingPoint.getDescription());

            // Показываем диалог снова
            showPointDialog(pointCopy);
        }
    }


    private String saveImageToInternalStorage(Uri imageUri, String fileName) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        File directory = getDir("point_images", MODE_PRIVATE);
        File imageFile = new File(directory, fileName + ".jpg");

        FileOutputStream fos = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, fos);
        fos.close();

        return imageFile.getAbsolutePath();
    }
}