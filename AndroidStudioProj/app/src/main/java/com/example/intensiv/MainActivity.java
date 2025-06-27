package com.example.intensiv;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends FragmentActivity {
    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private BottomNavigationView btNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);



        // Настройка osmdroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(this,
                getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE));
        setContentView(R.layout.activity_main);


        btNav = findViewById(R.id.bottom_nav);
        setupBottomNavigation();
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK); // Используем стандартные тайлы OSM
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.setMultiTouchControls(true);

        // Запрос разрешений
        requestPermissionsIfNecessary(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        // Настройка отображения текущего местоположения
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);

        // Центрирование карты на начальной позиции
        GeoPoint startPoint = new GeoPoint(57.767274, 40.926936); // Сусанинская площадь
        mapView.getController().setCenter(startPoint);
        mapView.getController().setZoom(17);

        // Добавление маркеров
        addMarkers();
    }

    private void addMarkers() {
        // Маркер 1
        GeoPoint point1 = new GeoPoint(57.767789, 40.927354); // Сусанинская площадь
        Marker marker1 = new Marker(mapView);
        marker1.setPosition(point1);
        marker1.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker1.setTitle("Сусанинская площадь");
        mapView.getOverlays().add(marker1);
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

    private void applyTheme() {
        String theme = PreferenceManager.getDefaultSharedPreferences(this)
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
//            } else if (id == R.id.nav_history) {
//                startActivity(new Intent(this, HistoryActivity.class));
//                return true;
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


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}