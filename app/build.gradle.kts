plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.hdaf.eduapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hdaf.eduapp"
        minSdk = 24
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ✅ OkHttp for API Requests (Google Translate API)
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // ✅ Gson for JSON Parsing (Translation API)
    implementation("com.google.code.gson:gson:2.8.9")

    // ✅ Text-to-Speech Support
    implementation("androidx.core:core-ktx:1.10.1")

    // ✅ Firebase (Optional)
    implementation(libs.firebase.inappmessaging)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
