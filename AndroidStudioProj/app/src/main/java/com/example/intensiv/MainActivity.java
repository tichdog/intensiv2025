package com.example.intensiv;

import com.example.intensiv.PointData;  // Ваш класс точки
import com.example.intensiv.PointsData; // Ваш класс-контейнер
import com.google.gson.Gson;            // Для парсинга JSON

import java.io.InputStreamReader;       // Для чтения файла
import java.util.List;                  // Для работы со списком

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.mapkit.mapview.MapView;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;


    private MapView mapView;
    private UserLocationLayer userLocationLayer;
    private MapObjectCollection mapObjects;
    private BottomNavigationView btNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        setOurTheme();
        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey("352f8f4a-2b58-41cc-8fc1-edf5e9e75901");
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        CameraPosition position = new CameraPosition(
                new Point(57.767689, 40.926422),
                14,  // Zoom
                0,   // Азимут
                0    // Наклон
        );
        mapView.getMap().move(position, new Animation(Animation.Type.SMOOTH, 1), null);

        setupUserLocationLayer();
        addMarkers();
        // Запрос разрешений
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
        btNav = findViewById(R.id.bottom_nav);
        setupBottomNavigation();
        btNav.setSelectedItemId(R.id.nav_map);
    }


    private void setupUserLocationLayer() {
        // Получаем экземпляр MapKit
        com.yandex.mapkit.MapKit mapKit = MapKitFactory.getInstance();

        // Создаем слой местоположения
        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);
    }

    private void addMarkers() {
        try {
            // Загрузка JSON
            InputStreamReader reader = new InputStreamReader(getAssets().open("points.json"));
            PointsData data = new Gson().fromJson(reader, PointsData.class);
            List<PointData> points = data.getPoints();

            // Добавление маркеров
            for (PointData point : points) {
                Point yandexPoint = new Point(point.getLat(), point.getLng());
                PlacemarkMapObject marker = mapObjects.addPlacemark(yandexPoint);
                marker.setUserData(point.getTitle());
                marker.addTapListener((mapObject, p) -> {
                    showToast(point.getTitle());
                    return true;
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Ошибка загрузки точек");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onResume() {
        btNav.setSelectedItemId(R.id.nav_map);
        super.onResume();
    }


    private void requestPermissionsIfNecessary(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        REQUEST_PERMISSIONS_REQUEST_CODE);
                break;
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


    private void setupBottomNavigation() {
        btNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_map) {
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, History.class));
                return true;
//            } else if (id == R.id.nav_tests) {
//                startActivity(new Intent(this, TestsActivity.class));
//                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, Settings.class));
                return true;
            }
            return false;
        });
    }
}