plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.waterdropapp"
    compileSdk {
        version = release(36)
    }

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.waterdropapp"
        minSdk = 24
        targetSdk = 36
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
    val nav_version = "2.7.7"
    // Java language implementation
    implementation("androidx.navigation:navigation-fragment:${nav_version}")
    implementation("androidx.navigation:navigation-ui:${nav_version}")
    // Kotlin (esto es lo que habilita findNavController() y las extensiones KTX)
    implementation("androidx.navigation:navigation-fragment-ktx:${nav_version}")
    implementation("androidx.navigation:navigation-ui-ktx:${nav_version}")
    // Feature module Support
    implementation("androidx.navigation:navigation-dynamic-features-fragment:${nav_version}")
    // Testing Navigation
    testImplementation("androidx.navigation:navigation-testing:${nav_version}")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.glide)

}