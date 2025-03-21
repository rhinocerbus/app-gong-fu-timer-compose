package com.piledrive.app_gong_fu_timer_compose.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piledrive.app_gong_fu_timer_compose.data.TimerPhase
import com.piledrive.app_gong_fu_timer_compose.repo.TimerRepo
import com.piledrive.app_gong_fu_timer_compose.ui.screens.MainScreenCoordinator
import com.piledrive.lib_compose_components.ui.dropdown.readonly.ReadOnlyDropdownCoordinator
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

	val coordinator: MainScreenCoordinator = object : MainScreenCoordinator {
		override val startTimeDropdownCoordinator: ReadOnlyDropdownCoordinator
			get() = _startTimeDropdownCoordinator
		override val additionalTimeDropdownCoordinator: ReadOnlyDropdownCoordinator
			get() = _additionalTimeDropdownCoordinator
		override val timerPhaseState: StateFlow<TimerPhase>
			get() = _timerPhaseState
		override val steepCountState: StateFlow<Int>
			get() = _steepCountState
		override val targetSteepTimeMsState: StateFlow<Long>
			get() = _targetSteepTimeMsState
		override val steepRoundProgressMsState: StateFlow<Long>
			get() = _steepRoundProgressMsState
		override val keepScreenOnState: StateFlow<Boolean>
			get() = _keepScreenOnState
		override val onStartRound: () -> Unit = { startSteepingRound() }
		override val onCancelRound: () -> Unit = { cancelRound() }
		override val onReset: () -> Unit = { reset() }
	}

	private val _timerPhaseState = MutableStateFlow<TimerPhase>(TimerPhase.INITIAL)
	private val _steepCountState = MutableStateFlow<Int>(0)
	private val _targetSteepTimeMsState = MutableStateFlow<Long>(repo.defaultInitialRoundTimeMs)
	private val _steepRoundProgressMsState = MutableStateFlow<Long>(0)

	private var targetSteepTimeMs = -1L

	private var timerJob: Job? = null
	fun startSteepingRound() {
		if (targetSteepTimeMs <= -1) {
			targetSteepTimeMs =
				repo.startTimeOptions.firstOrNull { it.id == _startTimeDropdownCoordinator.selectedOptionState.value?.id }?.timeValueMs
					?: repo.defaultInitialRoundTimeMs
		}
		timerJob?.cancel()
		timerJob = viewModelScope.launch {
			repo.startCallbackOnlyTimer(
				delayMs = repo.defaultCountdownMs,
				durationMs = targetSteepTimeMs,
				onStarted = {
					_timerPhaseState.value = TimerPhase.COUNTDOWN
					_keepScreenOnState.value = true
				},
				onDelayCompleted = {
					_timerPhaseState.value = TimerPhase.RUNNING
					// incr. on start so first press shows as first round in progress, 1-based counting in ui
					// was originally done at timer start, now after the countdown feels right
					_steepCountState.value += 1
				},
				onFinished = {
					_timerPhaseState.value = TimerPhase.IDLE
					targetSteepTimeMs += repo.additionalTimeOptions.firstOrNull { it.id == _additionalTimeDropdownCoordinator.selectedOptionState.value?.id }?.timeValueMs
						?: repo.defaultInitialRoundTimeMs
					_targetSteepTimeMsState.value = targetSteepTimeMs
					_keepScreenOnState.value = false
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
		timerJob?.cancel()
		_timerPhaseState.value = TimerPhase.IDLE
		_steepCountState.value -= 1
		_steepRoundProgressMsState.value = 0L
		_keepScreenOnState.value = false
	}

	fun reset() {
		timerJob?.cancel()
		_timerPhaseState.value = TimerPhase.INITIAL
		_steepCountState.value = 0
		_targetSteepTimeMsState.value = repo.defaultInitialRoundTimeMs
		_steepRoundProgressMsState.value = 0L
		_startTimeDropdownCoordinator.onOptionSelected(repo.startTimeOptions.firstOrNull { it.timeValueMs == repo.defaultInitialRoundTimeMs })
		_additionalTimeDropdownCoordinator.onOptionSelected(repo.additionalTimeOptions.firstOrNull { it.timeValueMs == repo.defaultAdditionalRoundTimeMs })
		_keepScreenOnState.value = false
	}

	/////////////////////////////////////////////////
	//  endregion


	//  region Dropdown state/options
	/////////////////////////////////////////////////

	private val _startTimeDropdownCoordinator = ReadOnlyDropdownCoordinator(
		selectedOptionState = mutableStateOf(repo.startTimeOptions.firstOrNull { it.timeValueMs == repo.defaultInitialRoundTimeMs }),
		dropdownOptionsState = mutableStateOf(repo.startTimeOptions),
		externalOnOptionSelected = { option ->
			if (_steepCountState.value == 0) {
				_targetSteepTimeMsState.value =
					repo.startTimeOptions.firstOrNull { it.id == option?.id }?.timeValueMs ?: repo.defaultInitialRoundTimeMs
			}
		}
	)

	private val _additionalTimeDropdownCoordinator = ReadOnlyDropdownCoordinator(
		selectedOptionState = mutableStateOf(repo.additionalTimeOptions.firstOrNull { it.timeValueMs == repo.defaultAdditionalRoundTimeMs }),
		dropdownOptionsState = mutableStateOf(repo.additionalTimeOptions),
	)

	/////////////////////////////////////////////////
	//  endregion


	//  region Screen-On state
	/////////////////////////////////////////////////

	// could just go back to having an "isRunning" state now that the countdown ui state is based on negative progress, and roll this into that
	private val _keepScreenOnState = MutableStateFlow<Boolean>(false)

	/////////////////////////////////////////////////
	//  endregion
}
