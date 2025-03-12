plugins {
	// core android
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.google.ksp)
	// DI
	alias(libs.plugins.hilt.android)
}

android {
	namespace = "com.piledrive.app_gong_fu_timer_compose"
	compileSdk = 35

	defaultConfig {
		applicationId = "com.piledrive.app_gong_fu_timer_compose"
		minSdk = 27
		targetSdk = 35
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		compose = true
	}
}

dependencies {
	// composite build config
	//implementation("com.piledrive.lib_retrofit_moshi:lib") // no version necessary
	//implementation("com.piledrive.lib_datastore:lib") // no version necessary

	// android/androidx/compose
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.lifecycle.viewmodel.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.ui.graphics.android)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.livedata)
	implementation(libs.androidx.ui)
	implementation(libs.androidx.ui.graphics)
	implementation(libs.androidx.ui.tooling.preview)
	implementation(libs.androidx.material3)
	implementation(libs.androidx.navigation.compose)

	// DI
	implementation(libs.hilt)
	ksp(libs.hilt.compiler)
	implementation(libs.hilt.navigation)

	// testing
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.ui.test.junit4)
}
