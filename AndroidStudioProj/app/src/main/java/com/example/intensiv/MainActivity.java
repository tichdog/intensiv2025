package com.example.intensiv;

import static androidx.core.util.TimeUtils.formatDuration;

import com.example.intensiv.PointData;  // Ваш класс точки
import com.example.intensiv.PointsData; // Ваш класс-контейнер
import com.google.gson.Gson;            // Для парсинга JSON

import java.io.InputStreamReader;       // Для чтения файла
import java.util.Arrays;
import java.util.List;                  // Для работы со списком

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.health.connect.LocalTimeRangeFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.transport.TransportFactory;
import com.yandex.mapkit.transport.masstransit.PedestrianRouter;
import com.yandex.mapkit.transport.masstransit.Route;
import com.yandex.mapkit.transport.masstransit.Session;
import com.yandex.mapkit.transport.masstransit.TimeOptions;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleType;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.transport.masstransit.FilterVehicleTypes;
import com.yandex.mapkit.transport.TransportFactory;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.transport.TransportFactory;
import com.yandex.mapkit.transport.masstransit.MasstransitRouter;
import com.yandex.mapkit.transport.masstransit.Route;

import com.yandex.mapkit.transport.masstransit.TimeOptions;
import com.yandex.mapkit.transport.masstransit.TransitOptions;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;

import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.mapkit.transport.masstransit.MasstransitRouter;
import com.yandex.mapkit.transport.masstransit.Route;
import com.yandex.mapkit.transport.masstransit.Session;
import com.yandex.mapkit.transport.masstransit.TransitOptions;


public class MainActivity extends AppCompatActivity implements UserLocationObjectListener {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private UserLocationLayer userLocationLayer;
    private Point userLocation; // Текущее местоположение пользователя
    private MasstransitRouter masstransitRouter;

    private MapObjectCollection mapObjects;
    private MapView mapView;
    private BottomNavigationView btNav;

    public static boolean themeChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        setOurTheme();
        super.onCreate(savedInstanceState);
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
        mapView = findViewById(R.id.mapView);
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        CameraPosition position = new CameraPosition(
                new Point(57.767689, 40.926422),
                14,  // Zoom
                0,   // Азимут
                0    // Наклон
        );
        mapView.getMap().move(position, new Animation(Animation.Type.SMOOTH, 1), null);


        masstransitRouter = TransportFactory.getInstance().createMasstransitRouter();
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


    private void setupUserLocationLayer() {
        MapKit mapKit = MapKitFactory.getInstance();
        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);
        userLocationLayer.setObjectListener(this); // Устанавливаем слушатель
    }

    // Обработчик обновления местоположения
    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView) {
        this.userLocation = userLocationView.getPin().getGeometry();
        buildRouteToFirstPoint();
    }

    @Override
    public void onObjectRemoved(@NonNull UserLocationView userLocationView) {
    }

    @Override
    public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {
        this.userLocation = userLocationView.getPin().getGeometry();
        buildRouteToFirstPoint();
    }

    private void addMarkers() {
        try {
            InputStreamReader reader = new InputStreamReader(getAssets().open("points.json"));
            PointsData data = new Gson().fromJson(reader, PointsData.class);
            List<PointData> points = data.getPoints();

            if (!points.isEmpty()) {
                // Добавляем маркер для первой точки
                PointData firstPoint = points.get(0);
                Point yandexPoint = new Point(firstPoint.getLat(), firstPoint.getLng());

                PlacemarkMapObject marker = mapObjects.addPlacemark(yandexPoint);
                //marker.setIcon(ImageProvider.fromResource(R.drawable.ic_target));
                marker.setUserData(firstPoint.getTitle());

                // Строим маршрут, когда будет известно местоположение пользователя
                if (userLocation != null && userLocation.getLatitude() != 0 && userLocation.getLongitude() != 0) {
                    buildPedestrianRoute(yandexPoint);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Ошибка загрузки точек");
        }
    }


    private void buildRouteToFirstPoint() {
        try {
            InputStreamReader reader = new InputStreamReader(getAssets().open("points.json"));
            PointsData data = new Gson().fromJson(reader, PointsData.class);
            List<PointData> points = data.getPoints();

            if (!points.isEmpty() && userLocation != null && userLocation.getLatitude() != 0 && userLocation.getLongitude() != 0) {
                Point destination = new Point(points.get(0).getLat(), points.get(0).getLng());
                buildPedestrianRoute(destination);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void buildPedestrianRoute(Point destination) {
        mapObjects.clear();
        Point p1 = new Point(57.761691, 40.928960);

        // Создаем точки маршрута
        RequestPoint startPoint = new RequestPoint(
                p1,
                RequestPointType.WAYPOINT,
                null, null
        );

        RequestPoint endPoint = new RequestPoint(
                destination,
                RequestPointType.WAYPOINT,
                null, null
        );

        // Настройки для пешехода
        TransitOptions transitOptions = new TransitOptions(
                FilterVehicleTypes.NONE.value, // Фильтр транспорта (null - пешеход по умолчанию)
                new TimeOptions(null, null)
        );

        masstransitRouter.requestRoutes(
                Arrays.asList(startPoint, endPoint),
                transitOptions,
                new Session.RouteListener() {
                    @Override
                    public void onMasstransitRoutes(@NonNull List<Route> routes) {
                        if (!routes.isEmpty()) {
                            // Отображение маршрута зеленой линией
                            PolylineMapObject routeLine = mapObjects.addPolyline(routes.get(0).getGeometry());
                            routeLine.setStrokeColor(ContextCompat.getColor(
                                    MainActivity.this,
                                    R.color.incorrect
                            ));
                            routeLine.setStrokeWidth(5);

//                            // Расчет времени ходьбы
//                            long minutes = (long) (routes.get(0).getMetadata().getWeight().getTime() / 60);
//                            String duration = minutes > 60 ?
//                                    (minutes / 60) + " ч " + (minutes % 60) + " мин" :
//                                    minutes + " мин";
//
//                            showToast("Время ходьбы: " + duration);
                        }
                    }

                    @Override
                    public void onMasstransitRoutesError(@NonNull Error error) {
                        //showToast("Ошибка: " + error.getMessage());
                    }
                }
        );
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
        if (themeChanged) {
            updateTheme();
            themeChanged = false;
        }
        btNav.setSelectedItemId(R.id.nav_map);
        super.onResume();
    }


    private void updateTheme() {
        setOurTheme(); // Переустанавливаем тему

        // Обновляем BottomNavigationView
        updateBottomNavigationColors();

        // Обновляем другие элементы при необходимости
    }

    private void updateBottomNavigationColors() {
        overridePendingTransition(0, 0);
        // Получаем цвета из текущей темы
        int activeColor = getColorFromAttr(R.attr.menuItemColorActive);
        int inactiveColor = getColorFromAttr(R.attr.menuItemColor);
        int backgroundColor = getColorFromAttr(R.attr.menuColor);

        // Создаем ColorStateList для иконок и текста
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{activeColor, inactiveColor}
        );

        // Применяем новые цвета
        btNav.setItemIconTintList(colorStateList);
        btNav.setItemTextColor(colorStateList);
        btNav.setBackgroundColor(backgroundColor);
        btNav.setItemRippleColor(ColorStateList.valueOf(activeColor));
    }

    private int getColorFromAttr(int attrResId) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attrResId, typedValue, true);
        return typedValue.data;
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
            } else if (id == R.id.nav_tests) {
                startActivity(new Intent(this, Tests.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, Settings.class));
                return true;
            }
            return false;
        });
    }
}