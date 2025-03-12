package com.piledrive.app_gong_fu_timer_compose.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.piledrive.app_gong_fu_timer_compose.ui.nav.NavRoute
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewMainContentFlow
import com.piledrive.app_gong_fu_timer_compose.viewmodel.MainViewModel
import kotlinx.coroutines.flow.StateFlow

object MainScreen : NavRoute {
	override val routeValue: String = "home"

	@Composable
	fun draw(
		viewModel: MainViewModel,
	) {
		drawContent(
			viewModel.contentState,
		)
	}

	@Composable
	fun drawContent(
		contentState: StateFlow<Int>,
	) {
		val homeState = contentState.collectAsState().value
		Scaffold(
			topBar = {
			},
			content = { innerPadding ->
				Column(modifier = Modifier.padding(innerPadding)) {

				}
			}
		)
	}
}

@Preview
@Composable
fun MainPreview() {
	val contentState = previewMainContentFlow()
	MainScreen.drawContent(
		contentState
	)
}