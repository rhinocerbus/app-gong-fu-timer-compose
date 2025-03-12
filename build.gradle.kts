// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
	// composite build config
	alias(libs.plugins.android.library) apply false
	// core android
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.kotlin.android) apply false
	alias(libs.plugins.kotlin.compose) apply false
	alias(libs.plugins.google.ksp) apply false
	// DI
	alias(libs.plugins.hilt.android) apply false
}