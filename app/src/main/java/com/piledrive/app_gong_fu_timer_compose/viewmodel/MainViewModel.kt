package com.piledrive.app_gong_fu_timer_compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piledrive.app_gong_fu_timer_compose.repo.SampleRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val repo: SampleRepo
) : ViewModel() {

	companion object {
		const val STEEP_TIME_INTERVAL_MS = 5000L
		const val STEEP_TIME_INITIAL_MS = 20000L
	}

	private val _steepCountState = MutableStateFlow<Int>(0)
	val steepCountState: StateFlow<Int> = _steepCountState

	private val _steepingRoundRunningState = MutableStateFlow<Boolean>(false)
	val steepingRoundRunningState: StateFlow<Boolean> = _steepingRoundRunningState

	private val _startingSteepTimeMsState = MutableStateFlow<Long>(STEEP_TIME_INITIAL_MS)
	val startingSteepTimeMsState: StateFlow<Long> = _startingSteepTimeMsState

	private val _targetSteepTimeMsState = MutableStateFlow<Long>(STEEP_TIME_INITIAL_MS)
	val targetSteepTimeMsState: StateFlow<Long> = _targetSteepTimeMsState

	private val _steepRoundIntervalMsState = MutableStateFlow<Long>(STEEP_TIME_INTERVAL_MS)
	val steepRoundIntervalMsState: StateFlow<Long> = _steepRoundIntervalMsState

	private val _steepRoundProgressMsState = MutableStateFlow<Long>(0)
	val steepRoundProgressMsState: StateFlow<Long> = _steepRoundProgressMsState

	private var targetSteepTimeMs = -1L

	suspend fun reloadContent() {
	}

	private var timerJob: Job? = null
	fun startSteepingRound() {
		if (targetSteepTimeMs <= -1) {
			targetSteepTimeMs = STEEP_TIME_INITIAL_MS
		}
		timerJob?.cancel()
		timerJob = viewModelScope.launch {
			_steepingRoundRunningState.value = true
			_steepCountState.value += 1
			tickerFlow(
				durationMs = targetSteepTimeMs,
				tickRateMs = 50L
			) {
				_steepingRoundRunningState.value = false
				targetSteepTimeMs += STEEP_TIME_INTERVAL_MS
				_targetSteepTimeMsState.value = targetSteepTimeMs
			}
				.collect { progress ->
					withContext(Dispatchers.Main) {
						_steepRoundProgressMsState.value = progress
					}
				}
		}
	}

	fun tickerFlow(durationMs: Long, tickRateMs: Long, initialDelayMs: Long = 0, onFinished: () -> Unit): Flow<Long> {
		return callbackFlow {
			val startTime = System.currentTimeMillis()
			var runtimeMs = 0L
			delay(initialDelayMs)
			while (currentCoroutineContext().isActive) {
				runtimeMs = System.currentTimeMillis() - startTime
				trySend(runtimeMs)
				if(runtimeMs >= durationMs) {
					onFinished()
					close()
					return@callbackFlow
				}
				delay(tickRateMs)
			}
		}.flowOn(Dispatchers.Default)
			.distinctUntilChanged()
	}
}
