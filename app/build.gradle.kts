plugins {
    id("org.jetbrains.kotlin.android")
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.graytsar.batterynotification"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.graytsar.batterynotification"
        minSdk = 23
        targetSdk = 34
        versionCode = 9
        versionName = "1.0.7"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables.useSupportLibrary = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.android.support.constraint:constraint-layout:2.0.4")

    //waveView
    implementation("com.github.scwang90:MultiWaveHeader:master-SNAPSHOT")

    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.work:work-gcm:2.9.0")

    //hilt
    implementation("androidx.hilt:hilt-common:1.2.0")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    //room
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

}
