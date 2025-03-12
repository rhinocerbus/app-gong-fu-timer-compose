package com.piledrive.app_gong_fu_timer_compose.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.piledrive.app_gong_fu_timer_compose.ui.nav.NavRoute
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewBooleanFlow
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewIntFlow
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewLongFlow
import com.piledrive.app_gong_fu_timer_compose.viewmodel.MainViewModel
import kotlinx.coroutines.flow.StateFlow

object MainScreen : NavRoute {
	override val routeValue: String = "home"

	@Composable
	fun draw(
		viewModel: MainViewModel,
	) {
		drawContent(
			viewModel.startingSteepTimeMsState,
			viewModel.steepCountState,
			viewModel.steepingRoundRunningState,
			viewModel.steepRoundProgressMsState,
			viewModel.targetSteepTimeMsState,
			onStartRound = {
				viewModel.startSteepingRound()
			}
		)
	}

	@Composable
	fun drawContent(
		initialSteepTimeState: StateFlow<Long>,
		steepRoundState: StateFlow<Int>,
		steepRunningState: StateFlow<Boolean>,
		steepRoundProgressState: StateFlow<Long>,
		targetSteepTimeState: StateFlow<Long>,
		onStartRound: () -> Unit
	) {
		val initialSteep = initialSteepTimeState.collectAsState().value
		val steepRound = steepRoundState.collectAsState().value
		val steepRunning = steepRunningState.collectAsState().value
		val steepProgress = steepRoundProgressState.collectAsState().value
		val targetTime = targetSteepTimeState.collectAsState().value

		Scaffold(
			topBar = {
			},
			content = { innerPadding ->
				Column(modifier = Modifier.padding(innerPadding)) {
					Text("Initial steep time: $initialSteep")
					Text("Steep running: $steepRunning")
					Text("Current round: $steepRound")
					Text("Target steep time: $targetTime")
					Text("Steep progress: $steepProgress")
					Button(
						onClick = { onStartRound() }
					) {
						Text("start next round")
					}
				}
			}
		)
	}
}

@Preview
@Composable
fun MainPreview() {
	MainScreen.drawContent(
		previewLongFlow(),
		previewIntFlow(),
		previewBooleanFlow(),
		previewLongFlow(),
		previewLongFlow(),
		onStartRound = {}
	)
}