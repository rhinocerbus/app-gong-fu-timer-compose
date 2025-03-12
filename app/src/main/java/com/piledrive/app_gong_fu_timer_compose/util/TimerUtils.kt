package com.piledrive.app_gong_fu_timer_compose.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

fun tickerFlow(durationMs: Long, tickRateMs: Long, initialDelayMs: Long = 0, onFinished: () -> Unit): Flow<Long> {
	return callbackFlow {
		val startTime = System.currentTimeMillis()
		var runtimeMs = 0L
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