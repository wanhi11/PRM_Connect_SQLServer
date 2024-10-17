plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.connectsqlserver"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.connectsqlserver"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
//    implementation(fileTree(mapOf(
//        "dir" to "E:\\Ky8\\PRM\\jtds1.3.1",
//        "include" to listOf("*.aar", "*.jar"),
//        "exclude" to listOf()
//    )))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.jtds)
}