import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

// Load local properties securely
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.hdaf.eduapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hdaf.eduapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "com.hdaf.eduapp.HiltTestRunner"
        
        // Vector drawable support for older APIs
        vectorDrawables.useSupportLibrary = true
        
        // Multi-dex enabled for large app
        multiDexEnabled = true
        
        // Room schema export for migrations
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.generateKotlin", "true")
        }
        
        // Build config fields from local.properties (SECURE)
        // API keys should be loaded from local.properties, NOT hardcoded
        buildConfigField("String", "SUPABASE_URL", 
            "\"${localProperties.getProperty("SUPABASE_URL", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", 
            "\"${localProperties.getProperty("SUPABASE_ANON_KEY", "")}\"")
        buildConfigField("String", "GEMINI_API_KEY", 
            "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\"")
        
        // Resource configurations for supported languages
        resourceConfigurations += listOf("en", "hi", "mr")
    }
    
    signingConfigs {
        create("release") {
            // Load from local.properties or environment variables
            storeFile = file(localProperties.getProperty("KEYSTORE_FILE", "keystore.jks"))
            storePassword = localProperties.getProperty("KEYSTORE_PASSWORD", "")
            keyAlias = localProperties.getProperty("KEY_ALIAS", "")
            keyPassword = localProperties.getProperty("KEY_PASSWORD", "")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            // Debug-specific config
            buildConfigField("Boolean", "ENABLE_LOGGING", "true")
            buildConfigField("Boolean", "ENABLE_CRASH_REPORTING", "false")
        }
        
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            signingConfig = signingConfigs.getByName("release")
            
            // Release-specific config
            buildConfigField("Boolean", "ENABLE_LOGGING", "false")
            buildConfigField("Boolean", "ENABLE_CRASH_REPORTING", "true")
        }
        
        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            isDebuggable = true
            
            buildConfigField("Boolean", "ENABLE_LOGGING", "true")
            buildConfigField("Boolean", "ENABLE_CRASH_REPORTING", "true")
        }
    }
    
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            isDefault = true  // Set as default variant for IDE
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.eduapp.com/\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://api.eduapp.com/\"")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview"
        )
    }
    
    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/9/previous-compilation-data.bin"
        }
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
    
    lint {
        abortOnError = false
        checkReleaseBuilds = true
        warningsAsErrors = false
    }
}

// Kapt configuration for annotation processing
kapt {
    correctErrorTypes = true
    useBuildCache = true
}

dependencies {
    // Core library desugaring for Java 8+ APIs on older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    // ==================== Core AndroidX ====================
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.material)
    implementation(libs.swiperefreshlayout)
    implementation(libs.viewpager2)
    implementation(libs.splashscreen)
    implementation(libs.dotsindicator)
    
    // ==================== Lifecycle ====================
    implementation(libs.bundles.lifecycle)
    
    // ==================== Navigation ====================
    implementation(libs.bundles.navigation)
    
    // ==================== Hilt Dependency Injection ====================
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation)
    implementation(libs.hilt.work)
    kapt(libs.hilt.work.compiler)
    
    // ==================== Room Database ====================
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)
    
    // ==================== Network ====================
    implementation(libs.bundles.network)
    
    // ==================== Firebase ====================
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    
    // ==================== Media (ExoPlayer) ====================
    implementation(libs.bundles.exoplayer)
    
    // ==================== Security ====================
    implementation(libs.security.crypto)
    implementation(libs.tink)
    implementation(libs.biometric)
    
    // ==================== Image Loading ====================
    implementation(libs.bundles.coil)
    
    // ==================== AI ====================
    implementation(libs.gemini)
    
    // ==================== Coroutines ====================
    implementation(libs.bundles.coroutines)
    
    // ==================== WorkManager ====================
    implementation(libs.work.runtime.ktx)
    
    // ==================== DataStore ====================
    implementation(libs.datastore.preferences)
    
    // ==================== Paging ====================
    implementation(libs.paging.runtime.ktx)
    
    // ==================== Logging ====================
    implementation(libs.timber)
    
    // ==================== Debug Tools ====================
    debugImplementation(libs.leakcanary)
    
    // ==================== Unit Testing ====================
    testImplementation(libs.bundles.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.room.testing)
    
    // ==================== Instrumented Testing ====================
    androidTestImplementation(libs.bundles.android.testing)
    androidTestImplementation(libs.navigation.testing)
    androidTestImplementation(libs.work.testing)
    kaptAndroidTest(libs.hilt.compiler)
}
