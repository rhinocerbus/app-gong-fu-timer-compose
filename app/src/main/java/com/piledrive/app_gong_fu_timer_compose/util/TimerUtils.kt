package com.piledrive.app_gong_fu_timer_compose.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

fun tickerFlow(
	durationMs: Long,
	tickRateMs: Long,
	initialDelayMs: Long = 0,
	onStarted: () -> Unit,
	onFinished: () -> Unit
): Flow<Long> {
	return callbackFlow {
		var runtimeMs = 0L
		val startTime = System.currentTimeMillis()
		onStarted()
		delay(initialDelayMs)
		while (currentCoroutineContext().isActive) {
			runtimeMs = System.currentTimeMillis() - startTime
			trySend(runtimeMs)
			if (runtimeMs >= durationMs) {
				onFinished()
				close()
				return@callbackFlow
			}
			delay(tickRateMs)
		}
	}.flowOn(Dispatchers.Default)
		.distinctUntilChanged()
}

fun tickerFlowWithCountdown(
	durationMs: Long,
	tickRateMs: Long,
	initialDelayMs: Long = 0,
	onStarted: () -> Unit,
	onDelayCompleted: () -> Unit = {},
	onFinished: () -> Unit
): Flow<Long> {
	return callbackFlow {
		var runtimeMs = 0L
		val startTime = System.currentTimeMillis()
		val totalRunTime = initialDelayMs + durationMs
		var delayReported = false
		onStarted()
		while (currentCoroutineContext().isActive) {
			runtimeMs = System.currentTimeMillis() - startTime
			if (totalRunTime <= initialDelayMs) {
				// report as countdown (3...2...1...0...1...2...)
				trySend(initialDelayMs - runtimeMs)
			} else {
				if(!delayReported) {
					delayReported = true
					onDelayCompleted()
				}
				trySend(runtimeMs)
			}
			if (runtimeMs >= totalRunTime) {
				onFinished()
				close()
				return@callbackFlow
			}
			delay(tickRateMs)
		}
	}.flowOn(Dispatchers.Default)
		.distinctUntilChanged()
}