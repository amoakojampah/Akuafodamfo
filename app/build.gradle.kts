plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.devtools.ksp")

    id("com.google.gms.google-services")}
android {
    namespace = "com.example.akuafodamfo"
    compileSdk = 35 // Updated to match TOML's AGP version (8.1.0 supports SDK 34)
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.example.akuafodamfo"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xskip-metadata-version-check"
        )
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.cardview)  // Add this to your TOML if missing
    implementation (libs.material.v190)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.firebase.database)
    implementation(libs.androidx.camera.core)
    implementation(libs.play.services.nearby)
    ksp(libs.hilt.compiler)
    ksp("androidx.room:room-compiler:2.5.0")
    // UI
    implementation(libs.androidx.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.recyclerview)

    // Firebase
    implementation(platform(libs.firebase.bom.v33140))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.gms.play.services.location)

    // Glide (add to TOML if missing)
    implementation (libs.glide)
   //ksp(libs.glide.compiler)  // Add if using Glide's annotation processor

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

        // CameraX
        implementation (libs.androidx.camera.core.v130)
        implementation (libs.androidx.camera.camera2.v130)
        implementation (libs.androidx.camera.lifecycle.v130)
        implementation (libs.androidx.camera.view.v130)

        // Retrofit for API calls
        implementation (libs.retrofit)
        implementation (libs.retrofit.converter.gson)
        implementation (libs.okhttp)

}