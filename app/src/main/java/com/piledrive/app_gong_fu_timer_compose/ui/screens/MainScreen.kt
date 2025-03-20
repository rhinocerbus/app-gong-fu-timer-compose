@file:OptIn(ExperimentalMaterial3Api::class)

package com.piledrive.app_gong_fu_timer_compose.ui.screens

import android.text.TextPaint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.piledrive.app_gong_fu_timer_compose.R
import com.piledrive.app_gong_fu_timer_compose.ui.nav.NavRoute
import com.piledrive.app_gong_fu_timer_compose.viewmodel.MainViewModel
import com.piledrive.lib_compose_components.ui.appbar.TopAppBarWithOverflow
import com.piledrive.lib_compose_components.ui.dropdown.readonly.ReadOnlyDropdownTextField
import com.piledrive.lib_compose_components.ui.spacer.Gap
import com.piledrive.lib_compose_components.ui.theme.custom.AppTheme
import com.piledrive.lib_compose_components.ui.util.MeasureTextWidth
import com.piledrive.lib_compose_components.ui.util.ScreenOnWatcher
import kotlin.math.absoluteValue

object MainScreen : NavRoute {
	override val routeValue: String = "home"

	@Composable
	fun draw(
		viewModel: MainViewModel,
	) {
		ScreenOnWatcher(viewModel.coordinator.keepScreenOnState)
		drawContent(
			viewModel.coordinator,
		)
	}

	@Composable
	fun drawContent(
		coordinator: MainScreenCoordinator,
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
								coordinator.onReset()
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
						Modifier.fillMaxSize(),
						coordinator,
					)
				}
			}
		)
	}

	@Composable
	private fun DrawBody(
		modifier: Modifier = Modifier,
		coordinator: MainScreenCoordinator,
	) {
		val steepRound = coordinator.steepCountState.collectAsState().value
		val steepRunning = coordinator.timerPhaseState.collectAsState().value.isActive
		val targetTime = coordinator.targetSteepTimeMsState.collectAsState().value

		val amountW =
			MeasureTextWidth("00000s", MaterialTheme.typography.bodySmall, TextPaint())

		Column(modifier = modifier.padding(16.dp)) {
			Row {
				Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
					Text("Initial steep time", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
					Gap(8.dp)
					ReadOnlyDropdownTextField(
						innerTextFieldModifier = Modifier.width(amountW.dp),
						coordinator = coordinator.startTimeDropdownCoordinator,
						enabled = steepRound == 0
					)
				}
				Gap(12.dp)
				Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
					Text("Added time per round", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
					Gap(8.dp)
					ReadOnlyDropdownTextField(
						innerTextFieldModifier = Modifier.width(amountW.dp),
						coordinator = coordinator.additionalTimeDropdownCoordinator,
						enabled = steepRound == 0
					)
				}
			}
			Gap(24.dp)
			Row {
				Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
					Text("Current round", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
					Gap(8.dp)
					Text("$steepRound", style = MaterialTheme.typography.headlineSmall)
				}
				Gap(12.dp)
				Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
					Text("Target steep time", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
					Gap(8.dp)
					Text("${targetTime / 1000L} seconds", style = MaterialTheme.typography.headlineSmall)
				}
			}

			Box(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth(),
				contentAlignment = Alignment.Center
			) {
				if (steepRunning) {
					DrawTimer(
						Modifier
							.fillMaxWidth(0.8f)
							.aspectRatio(1f),
						coordinator,
					)
				} else {
					Button(
						modifier = Modifier
							.fillMaxWidth(0.8f)
							.aspectRatio(1f),
						onClick = { coordinator.onStartRound() }
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

	@Composable
	fun DrawTimer(
		modifier: Modifier = Modifier,
		coordinator: MainScreenCoordinator,
	) {
		val timerProgress = coordinator.steepRoundProgressMsState.collectAsState().value
		val targetTime = coordinator.targetSteepTimeMsState.collectAsState().value
		//val inCountdown = timerPhaseState.collectAsState().value == TimerPhase.COUNTDOWN
		val inCountdown = timerProgress < 0

		Log.d("UI", "prg: $timerProgress")

		CircularProgressIndicator(
			modifier = modifier,
			strokeWidth = 12.dp,
			progress = {
				if (inCountdown) {
					(timerProgress.absoluteValue.toFloat() % 1000f) / 1000f
				} else {
					(timerProgress.toFloat() / targetTime.toFloat())
				}
			}
		)
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			if (inCountdown) {
				// being reported from timer as -{countdown} -> 0
				Text(text = "${timerProgress / 1000 - 1}...", style = MaterialTheme.typography.headlineMedium)
			} else {
				// cant decide if, after having the countdown, it's better to show 0 or go straight to 1
				Text(text = "${timerProgress / 1000 /* = 1*/}", style = MaterialTheme.typography.headlineMedium)
			}
			Gap(8.dp)
			IconButton(onClick = {
				coordinator.onCancelRound()
			}) {
				Icon(Icons.Default.Clear, "cancel current steeping")
			}
			Text(text = "Cancel")
		}
	}
}

@Preview
@Composable
fun MainPreview() {
	AppTheme {
		MainScreen.drawContent(
			stubMainScreenCoordinator,
		)
	}
}