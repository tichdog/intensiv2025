package com.example.intensiv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CreateRouteActivity extends AppCompatActivity implements InputListener {
    private BottomNavigationView btNav;

    private MapView mapView;
    private RootData routesRoot;
    private PointsData currentRoute;
    private int currentPointId = 1;
    private int currentRouteId = 1;
    RootData data;


    private EditText routeIdEditText;
    private EditText routeNameEditText;
    private Button saveRouteButton;
    private RecyclerView pointsRecyclerView;
    private long lastTapTime = 0;

    private PointsAdapter pointsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        setOurTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btNav = findViewById(R.id.bottom_nav_1);
        setupBottomNavigation();
        btNav.setSelectedItemId(R.id.nav_settings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        loadPointsData();

        // Инициализация MapView
        mapView = findViewById(R.id.mapView);
        mapView.getMap().addInputListener(this);

        // Инициализация UI элементов
        routeIdEditText = findViewById(R.id.routeIdEditText);
        routeNameEditText = findViewById(R.id.routeNameEditText);
        saveRouteButton = findViewById(R.id.saveRouteButton);
        pointsRecyclerView = findViewById(R.id.pointsRecyclerView);

        // Инициализация данных
        routesRoot = new RootData();
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
    }

    private void createNewRoute() {
        currentRoute = new PointsData(currentRouteId++, "Новый маршрут");
        routesRoot.addRoute(currentRoute);
        routeIdEditText.setText(String.valueOf(currentRoute.getId()));
        routeNameEditText.setText(currentRoute.getName());
    }

    @Override
    public void onMapTap(@NonNull Map map, @NonNull Point point) {
        if (System.currentTimeMillis() - lastTapTime > 300) { // Защита от быстрых повторных нажатий
            lastTapTime = System.currentTimeMillis();
            // Ваш код добавления точки

            PointArrayItem newPoint = new PointArrayItem(
                    currentPointId++,
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
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_point_edit, null);

        EditText titleEditText = dialogView.findViewById(R.id.titleEditText);
        EditText showEditText = dialogView.findViewById(R.id.showEditText);
        EditText shrtEditText = dialogView.findViewById(R.id.shrtEditText);
        EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);

        titleEditText.setText(point.getTitle());
        showEditText.setText(point.getShow());
        shrtEditText.setText(point.getShort());
        descriptionEditText.setText(point.getDescription());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Редактирование точки")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    point.setTitle(titleEditText.getText().toString());
                    point.setShow(showEditText.getText().toString());
                    point.setShrt(shrtEditText.getText().toString());
                    point.setDescription(descriptionEditText.getText().toString());

                    currentRoute.addPoint(point);
                    pointsAdapter.notifyDataSetChanged();
                    addPointMarker(point);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void addPointMarker(PointArrayItem point) {
        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
        PlacemarkMapObject marker = mapObjects.addPlacemark(new Point(point.getLat(), point.getLng()));
        marker.setIcon(ImageProvider.fromResource(this, R.drawable.ic_map_light));
        marker.setUserData(point);
    }

    private void saveRoutes() {
        try {
            int id = Integer.parseInt(routeIdEditText.getText().toString());
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
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
}