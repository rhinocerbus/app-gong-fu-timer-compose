package com.piledrive.app_gong_fu_timer_compose.util

import android.util.Log
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

/**
 * mixed flow emits and callbacks causes race conditions, see alternate strategies below
 */
fun tickerFlowWithCountdown(
	durationMs: Long,
	tickRateMs: Long,
	initialDelayMs: Long = 0,
	onStarted: () -> Unit,
	onDelayCompleted: () -> Unit = {},
	onFinished: () -> Unit
): Flow<Long> {
	return callbackFlow {
		// report as countdown (-3...-2...-1...0...+1...+2...)
		var runtimeMs = 0L - initialDelayMs
		val startTime = System.currentTimeMillis()
		var delayReported = false

		onStarted()
		trySend(runtimeMs)

		while (currentCoroutineContext().isActive) {
			runtimeMs = System.currentTimeMillis() - startTime
			if (runtimeMs >= 0 && !delayReported) {
				delayReported = true
				onDelayCompleted()
			}

			send(runtimeMs)

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

fun tickerFlowWithCountdownCallbacksOnly(
	initialDelayMs: Long = 0,
	durationMs: Long,
	tickRateMs: Long = 25L,
	onStarted: () -> Unit,
	onDelayCompleted: () -> Unit = {},
	onFinished: () -> Unit,
	onTick: (Long) -> Unit
): Flow<Unit> {
	return callbackFlow<Unit> {
		// report as countdown (-3...-2...-1...0...+1...+2...)
		var runtimeMs = 0L - initialDelayMs
		var lastTimeMs = System.currentTimeMillis()
		var delayReported = false

		onStarted()
		Log.d("TIMER", "prg: $runtimeMs")
		onTick(runtimeMs)

		while (currentCoroutineContext().isActive) {
			runtimeMs += System.currentTimeMillis() - lastTimeMs
			lastTimeMs = System.currentTimeMillis()
			if (runtimeMs >= 0 && !delayReported) {
				delayReported = true
				onDelayCompleted()
			}

			// potentially interesting sticking point - whether or not to tick when finished
			//onTick(runtimeMs)

			if (runtimeMs >= durationMs) {
				onFinished()
				close()
				return@callbackFlow
			} else {
				Log.d("TIMER", "prg: $runtimeMs")
				onTick(runtimeMs)
			}
			delay(tickRateMs)
		}
	}.flowOn(Dispatchers.Default)
		.distinctUntilChanged()
}

sealed class TimerUpdate
class ActiveChanged(val isActive: Boolean) : TimerUpdate()
class ProgressChange(val progressMs: Long) : TimerUpdate()

/**
 * "unified" because versions having callbacks lambdas mixed with callback flows caused issues.
 * trying a version using just the flow emit
 */
fun unifiedTickerFlowWithCountdown(
	initialDelayMs: Long = 0,
	durationMs: Long,
	tickRateMs: Long = 25L,
): Flow<TimerUpdate> {
	return callbackFlow {
		// report as countdown (-3...-2...-1...0...+1...+2...)
		var runtimeMs = 0L - initialDelayMs
		val startTime = System.currentTimeMillis()

		send(ActiveChanged(true))
		while (currentCoroutineContext().isActive) {
			runtimeMs = System.currentTimeMillis() - startTime
			send(ProgressChange(runtimeMs))

			if (runtimeMs >= durationMs) {
				send(ActiveChanged(false))
				close()
				return@callbackFlow
			}
			delay(tickRateMs)
		}
	}.flowOn(Dispatchers.Default)
		.distinctUntilChanged()
}