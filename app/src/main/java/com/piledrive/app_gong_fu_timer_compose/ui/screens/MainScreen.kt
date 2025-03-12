@file:OptIn(ExperimentalMaterial3Api::class)

package com.piledrive.app_gong_fu_timer_compose.ui.screens

import android.text.TextPaint
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
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewBooleanFlow
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewIntFlow
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewLongFlow
import com.piledrive.app_gong_fu_timer_compose.viewmodel.MainViewModel
import com.piledrive.lib_compose_components.ui.appbar.TopAppBarWithOverflow
import com.piledrive.lib_compose_components.ui.dropdown.ReadOnlyDropdownTextField
import com.piledrive.lib_compose_components.ui.dropdown.state.ReadOnlyDropdownCoordinator
import com.piledrive.lib_compose_components.ui.spacer.Gap
import com.piledrive.lib_compose_components.ui.theme.custom.AppTheme
import com.piledrive.lib_compose_components.ui.util.MeasureTextWidth
import kotlinx.coroutines.flow.StateFlow

object MainScreen : NavRoute {
	override val routeValue: String = "home"

	@Composable
	fun draw(
		viewModel: MainViewModel,
	) {
		drawContent(
			viewModel.startTimeDropdownCoordinator,
			viewModel.additionalTimeDropdownCoordinator,
			viewModel.steepCountState,
			viewModel.steepingRoundRunningState,
			viewModel.steepRoundProgressMsState,
			viewModel.targetSteepTimeMsState,
			onStartRound = {
				viewModel.startSteepingRound()
			},
			onCancelRound = {
				viewModel.cancelRound()
			},
			onReset = {
				viewModel.reset()
			}
		)
	}

	@Composable
	fun drawContent(
		initialTimeCoordinator: ReadOnlyDropdownCoordinator<Long>,
		additionalTimeCoordinator: ReadOnlyDropdownCoordinator<Long>,
		steepRoundState: StateFlow<Int>,
		steepRunningState: StateFlow<Boolean>,
		steepRoundProgressState: StateFlow<Long>,
		targetSteepTimeState: StateFlow<Long>,
		onStartRound: () -> Unit,
		onCancelRound: () -> Unit,
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
						Modifier.fillMaxSize(),
						initialTimeCoordinator,
						additionalTimeCoordinator,
						steepRoundState,
						steepRunningState,
						steepRoundProgressState,
						targetSteepTimeState,
						onStartRound,
						onCancelRound
					)
				}
			}
		)
	}

	@Composable
	private fun DrawBody(
		modifier: Modifier = Modifier,
		initialTimeCoordinator: ReadOnlyDropdownCoordinator<Long>,
		additionalTimeCoordinator: ReadOnlyDropdownCoordinator<Long>,
		steepRoundState: StateFlow<Int>,
		steepRunningState: StateFlow<Boolean>,
		steepRoundProgressState: StateFlow<Long>,
		targetSteepTimeState: StateFlow<Long>,
		onStartRound: () -> Unit,
		onCancelRound: () -> Unit,
	) {
		val steepRound = steepRoundState.collectAsState().value
		val steepRunning = steepRunningState.collectAsState().value
		val steepProgress = steepRoundProgressState.collectAsState().value
		val targetTime = targetSteepTimeState.collectAsState().value

		val amountW =
			MeasureTextWidth("00000s", MaterialTheme.typography.bodySmall, TextPaint())

		Column(modifier = modifier.padding(16.dp)) {
			Row {
				Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
					Text("Initial steep time", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
					Gap(8.dp)
					ReadOnlyDropdownTextField(
						innerTextFieldModifier = Modifier.width(amountW.dp),
						coordinator = initialTimeCoordinator,
						enabled = steepRound == 0
					)
				}
				Gap(12.dp)
				Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
					Text("Added time per round", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
					Gap(8.dp)
					ReadOnlyDropdownTextField(
						innerTextFieldModifier = Modifier.width(amountW.dp),
						coordinator = additionalTimeCoordinator,
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
					CircularProgressIndicator(
						modifier = Modifier
							.fillMaxWidth(0.8f)
							.aspectRatio(1f),
						strokeWidth = 12.dp,
						progress = {
							(steepProgress.toFloat() / targetTime.toFloat()).toFloat()
						}
					)
					Column(horizontalAlignment = Alignment.CenterHorizontally) {
						Text(text = "${steepProgress/1000 + 1}", style = MaterialTheme.typography.headlineMedium)
						Gap(8.dp)
						IconButton(onClick = {
							onCancelRound()
						}) {
							Icon(Icons.Default.Clear, "cancel current steeping")
						}
						Text(text = "Cancel")
					}
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
			initialTimeCoordinator = ReadOnlyDropdownCoordinator(),
			additionalTimeCoordinator = ReadOnlyDropdownCoordinator(),
			steepRoundState = previewIntFlow(1),
			steepRunningState = previewBooleanFlow(true),
			steepRoundProgressState = previewLongFlow(8000L),
			targetSteepTimeState = previewLongFlow(20000L),
			onStartRound = {},
			onCancelRound = {},
			onReset = {}
		)
	}
}