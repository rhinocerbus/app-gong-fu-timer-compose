@file:OptIn(ExperimentalMaterial3Api::class)

package com.piledrive.app_gong_fu_timer_compose.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.piledrive.app_gong_fu_timer_compose.R
import com.piledrive.app_gong_fu_timer_compose.ui.nav.NavRoute
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewBooleanFlow
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewIntFlow
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewLongFlow
import com.piledrive.app_gong_fu_timer_compose.viewmodel.MainViewModel
import com.piledrive.lib_compose_components.ui.appbar.TopAppBarWithOverflow
import com.piledrive.lib_compose_components.ui.spacer.Gap
import com.piledrive.lib_compose_components.ui.theme.custom.AppTheme
import kotlinx.coroutines.flow.StateFlow

object MainScreen : NavRoute {
	override val routeValue: String = "home"

	@Composable
	fun draw(
		viewModel: MainViewModel,
	) {
		drawContent(
			viewModel.startingSteepTimeMsState,
			viewModel.steepRoundIntervalMsState,
			viewModel.steepCountState,
			viewModel.steepingRoundRunningState,
			viewModel.steepRoundProgressMsState,
			viewModel.targetSteepTimeMsState,
			onStartRound = {
				viewModel.startSteepingRound()
			},
			onReset = {
				viewModel.reset()
			}
		)
	}

	@Composable
	fun drawContent(
		initialSteepTimeState: StateFlow<Long>,
		steepRoundIntervalState: StateFlow<Long>,
		steepRoundState: StateFlow<Int>,
		steepRunningState: StateFlow<Boolean>,
		steepRoundProgressState: StateFlow<Long>,
		targetSteepTimeState: StateFlow<Long>,
		onStartRound: () -> Unit,
		onReset: () -> Unit,
	) {
		Scaffold(
			topBar = {
				TopAppBarWithOverflow.Draw(
					title = {
						Text(text = "Gong Fu Timer")
					},
					overflowActions = {
						DropdownMenuItem(
							onClick = {
								onReset()
							},
							text = {
								Text("Reset")
							}
						)
					}
				)
			},
			content = { innerPadding ->
				Box(modifier = Modifier.padding(innerPadding)) {
					DrawBody(
						Modifier,
						initialSteepTimeState,
						steepRoundIntervalState,
						steepRoundState,
						steepRunningState,
						steepRoundProgressState,
						targetSteepTimeState,
						onStartRound
					)
				}
			}
		)
	}

	@Composable
	private fun DrawBody(
		modifier: Modifier = Modifier,
		initialSteepTimeState: StateFlow<Long>,
		steepRoundIntervalState: StateFlow<Long>,
		steepRoundState: StateFlow<Int>,
		steepRunningState: StateFlow<Boolean>,
		steepRoundProgressState: StateFlow<Long>,
		targetSteepTimeState: StateFlow<Long>,
		onStartRound: () -> Unit,
	) {
		val initialSteep = initialSteepTimeState.collectAsState().value
		val steepRoundInterval = steepRoundIntervalState.collectAsState().value
		val steepRound = steepRoundState.collectAsState().value
		val steepRunning = steepRunningState.collectAsState().value
		val steepProgress = steepRoundProgressState.collectAsState().value
		val targetTime = targetSteepTimeState.collectAsState().value

		Column(modifier = modifier.padding(16.dp)) {
			Row {
				Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
					Text("Initial steep time: $initialSteep")
				}
				Gap(12.dp)

				Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
					Text("Additional time per round: $steepRoundInterval")
				}
			}
			Gap(12.dp)
			Row {
				Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
					Text("Current round: $steepRound")
				}
				Gap(12.dp)

				Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
					Text("Target steep time: $targetTime")
				}
			}

			Box(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth(),
				contentAlignment = Alignment.Center
			) {
				if (steepRunning) {
					CircularProgressIndicator(
						modifier = Modifier
							.fillMaxWidth(0.8f)
							.aspectRatio(1f),
						strokeWidth = 12.dp,
						progress = {
							(steepProgress.toFloat() / targetTime.toFloat()).toFloat()
						}
					)
				} else {
					Button(
						modifier = Modifier
							.fillMaxWidth(0.8f)
							.aspectRatio(1f),
						onClick = { onStartRound() }
					) {
						Icon(
							modifier = Modifier.fillMaxSize(0.75f),
							imageVector = ImageVector.vectorResource(R.drawable.baseline_emoji_food_beverage_24),
							contentDescription = "Start steep timer"
						)
					}
				}
			}
		}
	}
}

@Preview
@Composable
fun MainPreview() {
	AppTheme {
		MainScreen.drawContent(
			initialSteepTimeState = previewLongFlow(20000L),
			steepRoundIntervalState = previewLongFlow(5000L),
			steepRoundState = previewIntFlow(1),
			steepRunningState = previewBooleanFlow(true),
			steepRoundProgressState = previewLongFlow(8000L),
			targetSteepTimeState = previewLongFlow(20000L),
			onStartRound = {},
			onReset = {}
		)
	}
}