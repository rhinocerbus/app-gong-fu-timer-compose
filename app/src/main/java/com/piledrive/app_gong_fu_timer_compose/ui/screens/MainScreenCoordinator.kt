package com.piledrive.app_gong_fu_timer_compose.ui.screens

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.piledrive.app_gong_fu_timer_compose.data.TimeOption
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewBooleanFlow
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewIntFlow
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewLongFlow
import com.piledrive.app_gong_fu_timer_compose.util.tickerFlowWithCountdownCallbacksOnly
import com.piledrive.lib_compose_components.ui.dropdown.readonly.ReadOnlyDropdownCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface MainScreenCoordinatorImpl {
	val startTimeDropdownCoordinator: ReadOnlyDropdownCoordinator
	val additionalTimeDropdownCoordinator: ReadOnlyDropdownCoordinator
	val timerRunningState: StateFlow<Boolean>
	val steepCountState: StateFlow<Int>
	val targetSteepTimeMsState: StateFlow<Long>
	val steepRoundProgressMsState: StateFlow<Long>
	val onStartRound: () -> Unit
	val onCancelRound: () -> Unit
	val onReset: () -> Unit
}

class MainScreenCoordinator(
	private val viewModelScope: CoroutineScope,
	private val countdownTimeMs: Long,
	private val startTimeOptions: List<TimeOption>,
	private val additionalTimeOptions: List<TimeOption>,
	private val doTimerDoneHaptics: () -> Unit
) : MainScreenCoordinatorImpl {
	override val startTimeDropdownCoordinator = ReadOnlyDropdownCoordinator(
		selectedOptionState = mutableStateOf(startTimeOptions.firstOrNull { it.default }),
		dropdownOptionsState = mutableStateOf(startTimeOptions),
		externalOnOptionSelected = { option ->
			/*
				at the moment at least, this seems to be the main sticking point in favor of having the nested coordinators - not
					wanting to expose changing the target time based on state externally
			 */
			if (_steepCountState.value == 0) {
				_targetSteepTimeMsState.value =
					startTimeOptions.firstOrNull { it.id == option?.id }?.timeValueMs
						?: throw (IllegalStateException("unable to find specified time option"))
			}
		}
	)

	override val additionalTimeDropdownCoordinator = ReadOnlyDropdownCoordinator(
		selectedOptionState = mutableStateOf(additionalTimeOptions.firstOrNull { it.default }),
		dropdownOptionsState = mutableStateOf(additionalTimeOptions),
	)

	private val defaultStartTimeMs = startTimeOptions.firstOrNull { it.default }?.timeValueMs
		?: throw (IllegalArgumentException("no default start time option defined"))
	private val defaultAdditionalTimeMs = startTimeOptions.firstOrNull { it.default }?.timeValueMs
		?: throw (IllegalArgumentException("no default additional time option defined"))


	override val timerRunningState: StateFlow<Boolean>
		get() = _timerRunningState
	override val steepCountState: StateFlow<Int>
		get() = _steepCountState
	override val targetSteepTimeMsState: StateFlow<Long>
		get() = _targetSteepTimeMsState
	override val steepRoundProgressMsState: StateFlow<Long>
		get() = _steepRoundProgressMsState

	private val _timerRunningState = MutableStateFlow<Boolean>(false)
	private val _steepCountState = MutableStateFlow<Int>(0)
	private val _targetSteepTimeMsState = MutableStateFlow<Long>(defaultStartTimeMs)
	private val _steepRoundProgressMsState = MutableStateFlow<Long>(0)

	override val onStartRound: () -> Unit = {
		startRound()
	}
	override val onCancelRound: () -> Unit = {
		cancelRound()
	}
	override val onReset: () -> Unit = {
		reset()
	}

	private var timerJob: Job? = null
	private fun startRound() {
		if (_targetSteepTimeMsState.value <= 0) {
			_targetSteepTimeMsState.value =
				startTimeOptions.firstOrNull { it.id == startTimeDropdownCoordinator.selectedOptionState.value?.id }?.timeValueMs
					?: defaultStartTimeMs
		}
		timerJob?.cancel()
		timerJob = viewModelScope.launch {
			tickerFlowWithCountdownCallbacksOnly(
				initialDelayMs = countdownTimeMs,
				durationMs = _targetSteepTimeMsState.value,
				onStarted = {
					_timerRunningState.value = true
					// had this in onDelayCompleted but would cause problems with cancelling during the countdown and decrementing
					// now we don't need onDelayCompleted at all
					_steepCountState.value += 1
				},
				onFinished = {
					_timerRunningState.value = false
					var prevTime = _targetSteepTimeMsState.value
					prevTime += additionalTimeOptions.firstOrNull { it.id == additionalTimeDropdownCoordinator.selectedOptionState.value?.id }?.timeValueMs
						?: throw (IllegalStateException("missing additional per-round time selection mid-session"))
					_targetSteepTimeMsState.value = prevTime
					doTimerDoneHaptics()
				},
				onTick = { progress ->
					Log.d("VM", "prg: $progress")
					_steepRoundProgressMsState.value = progress
				}
			).collect { }
		}
	}

	private fun cancelRound() {
		if (!_timerRunningState.value) return
		timerJob?.cancel()
		_timerRunningState.value = false
		_steepCountState.value -= 1
		_steepRoundProgressMsState.value = 0L
	}

	private fun reset() {
		timerJob?.cancel()
		_timerRunningState.value = false
		_steepCountState.value = 0
		_targetSteepTimeMsState.value = -1L
		_steepRoundProgressMsState.value = -1L
		startTimeDropdownCoordinator.onOptionSelected(startTimeOptions.firstOrNull { it.default })
		additionalTimeDropdownCoordinator.onOptionSelected(additionalTimeOptions.firstOrNull { it.default })
	}
}

val stubMainScreenCoordinator = object : MainScreenCoordinatorImpl {
	override val startTimeDropdownCoordinator: ReadOnlyDropdownCoordinator = ReadOnlyDropdownCoordinator()
	override val additionalTimeDropdownCoordinator: ReadOnlyDropdownCoordinator = ReadOnlyDropdownCoordinator()
	override val timerRunningState: StateFlow<Boolean> = previewBooleanFlow(false)
	override val steepCountState: StateFlow<Int> = previewIntFlow(1)
	override val targetSteepTimeMsState: StateFlow<Long> = previewLongFlow(8000L)
	override val steepRoundProgressMsState: StateFlow<Long> = previewLongFlow(20000L)
	override val onStartRound: () -> Unit = {}
	override val onCancelRound: () -> Unit = {}
	override val onReset: () -> Unit = {}
}