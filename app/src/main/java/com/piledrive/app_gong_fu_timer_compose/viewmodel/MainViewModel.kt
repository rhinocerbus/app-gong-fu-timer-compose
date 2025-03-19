package com.piledrive.app_gong_fu_timer_compose.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piledrive.app_gong_fu_timer_compose.data.TimerPhase
import com.piledrive.app_gong_fu_timer_compose.repo.TimerRepo
import com.piledrive.lib_compose_components.ui.dropdown.state.ReadOnlyDropdownCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val repo: TimerRepo
) : ViewModel() {


	private val _timerPhaseState = MutableStateFlow<TimerPhase>(TimerPhase.INITIAL)
	val timerPhaseState: StateFlow<TimerPhase> = _timerPhaseState

	private val _steepCountState = MutableStateFlow<Int>(0)
	val steepCountState: StateFlow<Int> = _steepCountState

	private val _targetSteepTimeMsState = MutableStateFlow<Long>(repo.DEFAULT_INITIAL_STEEP_TIME_MS)
	val targetSteepTimeMsState: StateFlow<Long> = _targetSteepTimeMsState

	private val _steepRoundProgressMsState = MutableStateFlow<Long>(0)
	val steepRoundProgressMsState: StateFlow<Long> = _steepRoundProgressMsState

	private var targetSteepTimeMs = -1L

	private var timerJob: Job? = null
	fun startSteepingRound() {
		if (targetSteepTimeMs <= -1) {
			targetSteepTimeMs =
				repo.startTimeOptions.firstOrNull { it.id == startTimeDropdownCoordinator.selectedOptionState.value?.id }?.timeValueMs
					?: repo.DEFAULT_INITIAL_STEEP_TIME_MS
		}
		timerJob?.cancel()
		timerJob = viewModelScope.launch {

			repo.startCallbackOnlyTimer(
				delayMs = repo.DEFAULT_INITIAL_DELAY_MS,
				durationMs = targetSteepTimeMs,
				onStarted = {
					_timerPhaseState.value = TimerPhase.COUNTDOWN
				},
				onDelayCompleted = {
					_timerPhaseState.value = TimerPhase.RUNNING
					// incr. on start so first press shows as first round in progress, 1-based counting in ui
					// was originally done at timer start, now after the countdown feels right
					_steepCountState.value += 1
				},
				onFinished = {
					_timerPhaseState.value = TimerPhase.IDLE
					targetSteepTimeMs += repo.additionalTimeOptions.firstOrNull { it.id == additionalTimeDropdownCoordinator.selectedOptionState.value?.id }?.timeValueMs
						?: repo.DEFAULT_INITIAL_STEEP_TIME_MS
					_targetSteepTimeMsState.value = targetSteepTimeMs
				},
				onTick = { progress ->
					Log.d("VM", "prg: $progress")
					_steepRoundProgressMsState.value = progress
				}
			).collect { }
		}
	}


	//  region Cancel/reset rounds
	/////////////////////////////////////////////////

	fun cancelRound() {
		if (_timerPhaseState.value != TimerPhase.RUNNING) return
		_timerPhaseState.value = TimerPhase.IDLE
		_steepCountState.value -= 1
		_steepRoundProgressMsState.value = 0L
	}

	fun reset() {
		_timerPhaseState.value = TimerPhase.INITIAL
		_steepCountState.value = 0
		_targetSteepTimeMsState.value = repo.DEFAULT_INITIAL_STEEP_TIME_MS
		_steepRoundProgressMsState.value = 0L
		startTimeDropdownCoordinator.onSelectedOptionChanged(repo.startTimeOptions.firstOrNull { it.timeValueMs == repo.DEFAULT_INITIAL_STEEP_TIME_MS })
		additionalTimeDropdownCoordinator.onSelectedOptionChanged(repo.additionalTimeOptions.firstOrNull { it.timeValueMs == repo.DEFAULT_ADDITIONAL_STEEP_TIME_MS })
		timerJob?.cancel()
	}

	/////////////////////////////////////////////////
	//  endregion


	//  region Dropdown state/options
	/////////////////////////////////////////////////

	val startTimeDropdownCoordinator = ReadOnlyDropdownCoordinator<Long>(
		selectedOptionState = mutableStateOf(repo.startTimeOptions.firstOrNull { it.timeValueMs == repo.DEFAULT_INITIAL_STEEP_TIME_MS }),
		dropdownOptionsState = mutableStateOf(repo.startTimeOptions),
		externalOnSelectedOptionChanged = { option ->
			if (_steepCountState.value == 0) {
				_targetSteepTimeMsState.value =
					repo.startTimeOptions.firstOrNull { it.id == option?.id }?.timeValueMs ?: repo.DEFAULT_INITIAL_STEEP_TIME_MS
			}
		}
	)

	val additionalTimeDropdownCoordinator = ReadOnlyDropdownCoordinator<Long>(
		selectedOptionState = mutableStateOf(repo.additionalTimeOptions.firstOrNull { it.timeValueMs == repo.DEFAULT_ADDITIONAL_STEEP_TIME_MS }),
		dropdownOptionsState = mutableStateOf(repo.additionalTimeOptions),
	)

	/////////////////////////////////////////////////
	//  endregion

}
