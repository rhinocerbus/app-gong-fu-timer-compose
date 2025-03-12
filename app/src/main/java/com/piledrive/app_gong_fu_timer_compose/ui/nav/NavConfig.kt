package com.piledrive.app_gong_fu_timer_compose.ui.nav

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.piledrive.app_gong_fu_timer_compose.ui.screens.MainScreen
import com.piledrive.app_gong_fu_timer_compose.viewmodel.MainViewModel

interface NavRoute {
	val routeValue: String
}

@Composable
fun RootNavHost() {
	val navController = rememberNavController()
	NavHost(
		modifier = Modifier.safeDrawingPadding(),
		navController = navController,
		startDestination = MainScreen.routeValue
	) {
		composable(route = MainScreen.routeValue) {
			val viewModel: MainViewModel = hiltViewModel<MainViewModel>()
			LaunchedEffect("load_content_on_launch") {
				//viewModel.reloadContent()
			}
			MainScreen.draw(
				viewModel,
			)
		}
	}
}

