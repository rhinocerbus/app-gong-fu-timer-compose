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
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class MainViewModel @Inject constructor(
	private val repo: SampleRepo
) : ViewModel() {

	companion object {
		const val STEEP_TIME_INTERVA_MSL = 5000
	}

	private val _steepCountState = MutableStateFlow<Long>(0)
	val steepCountState: StateFlow<Long> = _steepCountState

	private val _steepingRoundRunningState = MutableStateFlow<Boolean>(false)
	val steepingRoundRunningState: StateFlow<Boolean> = _steepingRoundRunningState

	private val _startingSteepTimeMsState = MutableStateFlow<Long>(20)
	val startingSteepTimeMsState: StateFlow<Long> = _startingSteepTimeMsState

	private val _displayTimeMsState = MutableStateFlow<Long>(0)
	val displayTimeMsState: StateFlow<Long> = _displayTimeMsState

	private var targetSteepTimeMs = -1L

	suspend fun reloadContent() {
	}

	private var timerJob: Job? = null
	fun startSteepingRound() {
		if (targetSteepTimeMs <0 -1) {
			targetSteepTimeMs = _startingSteepTimeMsState.value
		}
		timerJob?.cancel()
		timerJob = viewModelScope.launch {
			_steepingRoundRunningState.value = true
			tickerFlow(
				durationMs = targetSteepTimeMs,
				tickRateMs = 1000L
			) {
				_steepingRoundRunningState.value = false
				_steepCountState.value += 1
			}
				.onEach { sec ->
					withContext(Dispatchers.Main) {
						_displayTimeMsState.value = sec
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
				delay(tickRateMs)
				if(runtimeMs >= durationMs) {
					onFinished()
					close()
				}
			}
		}.flowOn(Dispatchers.Default)
			.distinctUntilChanged()
	}
}
