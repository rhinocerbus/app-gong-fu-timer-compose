package com.piledrive.app_gong_fu_timer_compose.repo

import com.piledrive.app_gong_fu_timer_compose.data.TimeOption
import com.piledrive.app_gong_fu_timer_compose.util.TimerUpdate
import com.piledrive.app_gong_fu_timer_compose.util.tickerFlowWithCountdown
import com.piledrive.app_gong_fu_timer_compose.util.tickerFlowWithCountdownCallbacksOnly
import com.piledrive.app_gong_fu_timer_compose.util.unifiedTickerFlowWithCountdown
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class TimerRepo @Inject constructor(
	//private val settingsSource: LocalSettingsSource
) {
	val defaultAdditionalRoundTimeMs = 10000L
	val defaultInitialRoundTimeMs = 20000L
	val defaultCountdownMs = 3000L

	val startTimeOptions = listOf(
		TimeOption(10, "10s", timeValueMs = 10000L),
		TimeOption(15, "15s", timeValueMs = 15000L),
		TimeOption(20, "20s", timeValueMs = 20000L),
		TimeOption(25, "25s", timeValueMs = 25000L),
		TimeOption(30, "30s", timeValueMs = 30000L),
	)

	val additionalTimeOptions = listOf(
		TimeOption(5, "5s", timeValueMs = 5000L),
		TimeOption(10, "10s", timeValueMs = 10000L),
		TimeOption(15, "15s", timeValueMs = 15000L),
		TimeOption(20, "20s", timeValueMs = 20000L),
	)

	fun startTimerFlow(
		durationMs: Long,
		onStarted: () -> Unit,
		onDelayCompleted: () -> Unit,
		onFinished: () -> Unit
	): Flow<Long> {
		return tickerFlowWithCountdown(
			initialDelayMs = 3000L,
			durationMs = durationMs,
			tickRateMs = 25L,
			onStarted = onStarted,
			onDelayCompleted = onDelayCompleted,
			onFinished = onFinished
		)
	}

	fun startUnifiedTimerFlow(
		delayMs: Long,
		durationMs: Long,
	): Flow<TimerUpdate> {
		return unifiedTickerFlowWithCountdown(
			initialDelayMs = delayMs,
			durationMs = durationMs,
			tickRateMs = 25L,
		)
	}

	fun startCallbackOnlyTimer(
	delayMs: Long,
	durationMs: Long,
	onStarted: () -> Unit,
	onDelayCompleted: () -> Unit,
	onFinished: () -> Unit,
	onTick: (Long) -> Unit
	): Flow<Unit> {
		return tickerFlowWithCountdownCallbacksOnly(
			initialDelayMs = delayMs,
			durationMs = durationMs,
			tickRateMs = 25L,
			onStarted, onDelayCompleted, onFinished, onTick
		)
	}
}