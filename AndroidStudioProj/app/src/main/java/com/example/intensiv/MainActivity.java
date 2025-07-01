package com.example.intensiv;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.transport.TransportFactory;
import com.yandex.mapkit.transport.masstransit.FitnessOptions;
import com.yandex.mapkit.transport.masstransit.PedestrianRouter;
import com.yandex.mapkit.transport.masstransit.Route;
import com.yandex.mapkit.transport.masstransit.RouteOptions;
import com.yandex.mapkit.transport.masstransit.Session;
import com.yandex.mapkit.transport.masstransit.TimeOptions;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private static final double MIN_DISTANCE_UPDATE = 10; // метров
    private static final double TARGET_REACHED_DISTANCE = 15; // метров

    private UserLocationLayer userLocationLayer;
    private Point userLocation;
    private PedestrianRouter pedestrianRouter;
    public static boolean themeChanged = false;
    private List<PointData> points;
    private MapObjectCollection mapObjects;
    private MapView mapView;
    private BottomNavigationView btNav;
    private Point lastRoutePoint;
    private int currentTargetIndex = 0;
    PointsData data;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private boolean initial;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        setOurTheme();
        super.onCreate(savedInstanceState);


        // Инициализация MapKit
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        boolean initial = sharedPreferences.getBoolean("needInitial", true);
        if (initial) {
            MapKitFactory.setApiKey("352f8f4a-2b58-41cc-8fc1-edf5e9e75901");
            MapKitFactory.initialize(this);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("needInitial", true);
        editor.apply();
        setContentView(R.layout.activity_main);
        initViews();
        setupLocationServices();
        setupMap();
        setupBottomNavigation();
        setupBackPressHandler();
        setupMenuButton();
        setupMyLocationButton();
    }

    private void initViews() {
        mapView = findViewById(R.id.mapView);
        btNav = findViewById(R.id.bottom_nav);
    }

    private void setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationCallback();
        requestLocationPermissions();
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    updateUserLocation(new Point(
                            location.getLatitude(),
                            location.getLongitude()
                    ), location.getAccuracy());
                }
            }
        };
    }

    private void updateUserLocation(Point newLocation, float accuracy) {
        if (accuracy > 20) return; // Игнорируем неточные данные

        this.userLocation = newLocation;
//        updateUserMarkerPosition(newLocation);
        checkTargetReached(newLocation);
        updateRouteIfNeeded(newLocation);
    }

    private void updateUserMarkerPosition(Point newLocation) {
        if (userLocationLayer != null) {
            // Просто обновляем позицию без анимации, так как UserLocationLayer
            // не предоставляет методов для анимации напрямую
            userLocationLayer.resetAnchor();
//            userLocationLayer.setAnchor(
//                    new PointF((float)(mapView.getWidth() * 0.5),
//                            (float)(mapView.getHeight() * 0.5)),
//                    new PointF((float)(mapView.getWidth() * 0.5),
//                            (float)(mapView.getHeight() * 0.83))
//            );
        }
    }


    private void checkTargetReached(Point newLocation) {
        if (currentTargetIndex >= points.size()) return;

        Point currentTarget = new Point(
                points.get(currentTargetIndex).getLat(),
                points.get(currentTargetIndex).getLng()
        );

        if (calculateDistance(newLocation, currentTarget) <= TARGET_REACHED_DISTANCE) {
            showToast("Точка достигнута!");

            //Записываем в json файл
            points.get(currentTargetIndex).setComplete(true);
            data.setPoints(points);
            savePointsToJson();

            currentTargetIndex++;
            if (currentTargetIndex < points.size()) {
                buildRouteToCurrentTarget();
            } else {
                showToast("Маршрут завершен!");
            }
        }
    }


    private void setupMyLocationButton() {
        ImageView btn = findViewById(R.id.my_location_1);
        btn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Запрос разрешений
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSIONS_REQUEST_CODE
                );
                return;
            }

            if (userLocation == null) {
                // Однократный запрос текущей позиции
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                userLocation = new Point(location.getLatitude(), location.getLongitude());
                                moveCameraToUserLocation();
                            }
                        });
            } else {
                moveCameraToUserLocation();
            }
        });
    }

    private void moveCameraToUserLocation() {
        mapView.getMap().move(
                new CameraPosition(
                        userLocation,  // Координаты пользователя
                        17,           // Уровень зума (рекомендуется 15-19)
                        0,            // Азимут (0 = север)
                        0             // Наклон камеры (0 = вид сверху)
                ),
                new Animation(Animation.Type.SMOOTH, 1),  // Плавная анимация (1 сек)
                null
        );
    }

    private void updateRouteIfNeeded(Point newLocation) {
        if (lastRoutePoint == null ||
                calculateDistance(lastRoutePoint, newLocation) > MIN_DISTANCE_UPDATE) {
            if (currentTargetIndex < points.size()) {
                buildRouteToCurrentTarget();
            }
            lastRoutePoint = newLocation;
        }
    }

    private void setupMap() {
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        // Начальная позиция камеры
        mapView.getMap().move(new CameraPosition(
                        new Point(57.767689, 40.926422), 14, 0, 0),
                new Animation(Animation.Type.SMOOTH, 1), null
        );

        pedestrianRouter = TransportFactory.getInstance().createPedestrianRouter();
        setupUserLocationLayer();
        loadPointsData();
        points = data.getPoints();
        for (PointData point : points) {
            if(point.getComplete()){
                currentTargetIndex++;
            }
        }
        if (!points.isEmpty()) {
            addAllMarkers();
        }
    }

    private void setupUserLocationLayer() {
        MapKit mapKit = MapKitFactory.getInstance();
        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(false);
    }


    private void addAllMarkers() {

        mapObjects.clear();
        PointData point = points.get(currentTargetIndex);
        Point yandexPoint = new Point(point.getLat(), point.getLng());

        PlacemarkMapObject marker = mapObjects.addPlacemark(yandexPoint);
        marker.setIcon(ImageProvider.fromResource(this, R.drawable.ic_history_light));
        marker.setUserData(point.getTitle());

    }

    private void buildRouteToCurrentTarget() {
        if (currentTargetIndex >= points.size()) return;

        Point destination = new Point(
                points.get(currentTargetIndex).getLat(),
                points.get(currentTargetIndex).getLng()
        );

        buildPedestrianRoute(destination);
    }

    private void buildPedestrianRoute(Point destination) {
        if (userLocation == null) return;

        RequestPoint startPoint = new RequestPoint(
                userLocation, RequestPointType.WAYPOINT, null, null, null);
        RequestPoint endPoint = new RequestPoint(
                destination, RequestPointType.WAYPOINT, null, null, null);

        pedestrianRouter.requestRoutes(
                Arrays.asList(startPoint, endPoint),
                new TimeOptions(null, null),
                new RouteOptions(new FitnessOptions(false, false)),
                new Session.RouteListener() {
                    @Override
                    public void onMasstransitRoutes(@NonNull List<Route> routes) {
                        if (!routes.isEmpty()) {
                            showRouteOnMap(routes.get(0));
                        }
                    }

                    @Override
                    public void onMasstransitRoutesError(@NonNull Error error) {
                        showToast("Ошибка построения маршрута");
                    }
                }
        );
    }

    private void showRouteOnMap(Route route) {
        mapObjects.clear();
        addAllMarkers(); // Восстанавливаем маркеры

        PolylineMapObject routeLine = mapObjects.addPolyline(route.getGeometry());
        routeLine.setStrokeColor(ContextCompat.getColor(this, R.color.incorrect));
        routeLine.setStrokeWidth(3);
    }

    private double calculateDistance(Point a, Point b) {
        return Math.sqrt(
                Math.pow(a.getLatitude() - b.getLatitude(), 2) +
                        Math.pow(a.getLongitude() - b.getLongitude(), 2)
        ) * 111000; // Метры
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        }
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE
            );
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            }
        }
    }

    // Остальные методы (onStart, onStop, onResume, setupBottomNavigation и т.д.)
    // остаются такими же как в вашем исходном коде, но с учетом новых полей

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        if (themeChanged) {
            updateTheme();
            themeChanged = false;
        }
        btNav.setSelectedItemId(R.id.nav_map);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
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

    private void setupBottomNavigation() {
        btNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_map) return true;
            if (id == R.id.nav_history) startActivity(new Intent(this, History.class));
            if (id == R.id.nav_tests) startActivity(new Intent(this, Tests.class));
            if (id == R.id.nav_settings) startActivity(new Intent(this, Settings.class));

            return true;
        });
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("needInitial", false);
                editor.apply();
                finish();
                setEnabled(false);
                MainActivity.super.onBackPressed();
            }
        });
    }

    private void setupMenuButton() {
        findViewById(R.id.map_menu).setOnClickListener(v ->
                {
                    BottomMenu bottomMenu = new BottomMenu();
                    bottomMenu.setActivityContext(this); // Передаем контекст
                    bottomMenu.show(getSupportFragmentManager(), "bottomMenu");
                }
        );
    }

    private void setOurTheme() {
        String theme = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("app_theme", "light");

        setTheme(theme.equals("dark") ? R.style.Theme_App_Dark : R.style.Theme_App_Light);
    }

    private void updateTheme() {
        setOurTheme();
        updateBottomNavigationColors();
    }

    private void updateBottomNavigationColors() {
        int activeColor = getColorFromAttr(R.attr.menuItemColorActive);
        int inactiveColor = getColorFromAttr(R.attr.menuItemColor);
        int backgroundColor = getColorFromAttr(R.attr.menuColor);

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{activeColor, inactiveColor}
        );

        btNav.setItemIconTintList(colorStateList);
        btNav.setItemTextColor(colorStateList);
        btNav.setBackgroundColor(backgroundColor);
    }

    private int getColorFromAttr(int attrResId) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attrResId, typedValue, true);
        return typedValue.data;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void savePointsToJson() {
        try {
            // Преобразуем обновленные данные в JSON
            String updatedJson = new Gson().toJson(data);

            // Записываем в файл
            FileOutputStream fos = openFileOutput("points1.json", Context.MODE_PRIVATE);
            fos.write(updatedJson.getBytes(StandardCharsets.UTF_8));
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
            showToast("Ошибка сохранения прогресса");
        }
    }


    private void loadPointsData() {
        try {
            FileInputStream fis = openFileInput("points1.json");
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            data = new Gson().fromJson(isr, PointsData.class);
            isr.close();
        } catch (FileNotFoundException e) {
            try {
                InputStream is = getAssets().open("points1.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String json = new String(buffer, StandardCharsets.UTF_8);
                data = new Gson().fromJson(json, PointsData.class);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}