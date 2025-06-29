plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.intensiv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.intensiv"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.google.android.material:material:1.6.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.yandex.android:maps.mobile:4.16.0-full")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

