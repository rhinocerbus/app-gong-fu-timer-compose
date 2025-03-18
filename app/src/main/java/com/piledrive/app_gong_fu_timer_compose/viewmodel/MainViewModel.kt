package com.piledrive.app_gong_fu_timer_compose.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piledrive.app_gong_fu_timer_compose.data.TimerPhase
import com.piledrive.app_gong_fu_timer_compose.repo.SampleRepo
import com.piledrive.app_gong_fu_timer_compose.util.tickerFlow
import com.piledrive.lib_compose_components.ui.dropdown.state.DropdownOption
import com.piledrive.lib_compose_components.ui.dropdown.state.ReadOnlyDropdownCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val repo: SampleRepo
) : ViewModel() {

	companion object {
		const val DEFAULT_ADDITIONAL_STEEP_TIME_MS = 10000L
		const val DEFAULT_INITIAL_STEEP_TIME_MS = 20000L
	}

	private val _timerPhaseState = MutableStateFlow<TimerPhase>(TimerPhase.INITIAL)
	val timerPhaseState: StateFlow<TimerPhase> = _timerPhaseState

	private val _steepCountState = MutableStateFlow<Int>(0)
	val steepCountState: StateFlow<Int> = _steepCountState

	private val _targetSteepTimeMsState = MutableStateFlow<Long>(DEFAULT_INITIAL_STEEP_TIME_MS)
	val targetSteepTimeMsState: StateFlow<Long> = _targetSteepTimeMsState

	private val _steepRoundProgressMsState = MutableStateFlow<Long>(0)
	val steepRoundProgressMsState: StateFlow<Long> = _steepRoundProgressMsState

	private var targetSteepTimeMs = -1L

	private var timerJob: Job? = null
	fun startSteepingRound() {
		if (targetSteepTimeMs <= -1) {
			targetSteepTimeMs = startTimeOptions.firstOrNull { it.id == startTimeDropdownCoordinator.selectedOptionState.value?.id }?.timeValueMs ?: DEFAULT_INITIAL_STEEP_TIME_MS
		}
		timerJob?.cancel()
		timerJob = viewModelScope.launch {
			_timerPhaseState.value = TimerPhase.RUNNING
			_steepCountState.value += 1
			tickerFlow(
				durationMs = targetSteepTimeMs,
				tickRateMs = 33L
			) {
				_timerPhaseState.value = TimerPhase.IDLE
				targetSteepTimeMs += additionalTimeOptions.firstOrNull { it.id == additionalTimeDropdownCoordinator.selectedOptionState.value?.id }?.timeValueMs ?: DEFAULT_INITIAL_STEEP_TIME_MS
				_targetSteepTimeMsState.value = targetSteepTimeMs
			}
				.collect { progress ->
					withContext(Dispatchers.Main) {
						_steepRoundProgressMsState.value = progress
					}
				}
		}
	}


	//  region Cancel/reset rounds
	/////////////////////////////////////////////////

	fun cancelRound() {
		if(_timerPhaseState.value != TimerPhase.RUNNING) return
		_timerPhaseState.value = TimerPhase.IDLE
		_steepCountState.value -= 1
		_steepRoundProgressMsState.value = 0L
	}

	fun reset() {
		_timerPhaseState.value = TimerPhase.INITIAL
		_steepCountState.value = 0
		_targetSteepTimeMsState.value = DEFAULT_INITIAL_STEEP_TIME_MS
		_steepRoundProgressMsState.value = 0L
		startTimeDropdownCoordinator.onSelectedOptionChanged(startTimeOptions.firstOrNull { it.timeValueMs == DEFAULT_INITIAL_STEEP_TIME_MS })
		additionalTimeDropdownCoordinator.onSelectedOptionChanged(additionalTimeOptions.firstOrNull { it.timeValueMs == DEFAULT_ADDITIONAL_STEEP_TIME_MS })
		timerJob?.cancel()
	}

	/////////////////////////////////////////////////
	//  endregion


	//  region Dropdown state/options
	/////////////////////////////////////////////////

	class TimeOption(
		override val id: Long,
		override val textValue: String?,
		val timeValueMs: Long
	) : DropdownOption<Long>

	private val startTimeOptions = listOf(
		TimeOption(10, "10s", timeValueMs = 10000L),
		TimeOption(15, "15s", timeValueMs = 15000L),
		TimeOption(20, "20s", timeValueMs = 20000L),
		TimeOption(25, "25s", timeValueMs = 25000L),
		TimeOption(30, "30s", timeValueMs = 30000L),
	)

	val startTimeDropdownCoordinator = ReadOnlyDropdownCoordinator<Long>(
		selectedOptionState = mutableStateOf(startTimeOptions.firstOrNull { it.timeValueMs == DEFAULT_INITIAL_STEEP_TIME_MS }),
		dropdownOptionsState = mutableStateOf(startTimeOptions),
		externalOnSelectedOptionChanged = { option ->
			if(_steepCountState.value == 0) {
				_targetSteepTimeMsState.value = startTimeOptions.firstOrNull { it.id == option?.id }?.timeValueMs ?: DEFAULT_INITIAL_STEEP_TIME_MS
			}
		}
	)

	private val additionalTimeOptions = listOf(
		TimeOption(5, "5s", timeValueMs = 5000L),
		TimeOption(10, "10s", timeValueMs = 10000L),
		TimeOption(15, "15s", timeValueMs = 15000L),
		TimeOption(20, "20s", timeValueMs = 20000L),
	)

	val additionalTimeDropdownCoordinator = ReadOnlyDropdownCoordinator<Long>(
		selectedOptionState = mutableStateOf(additionalTimeOptions.firstOrNull { it.timeValueMs == DEFAULT_ADDITIONAL_STEEP_TIME_MS }),
		dropdownOptionsState = mutableStateOf(additionalTimeOptions),

	)

	/////////////////////////////////////////////////
	//  endregion

}
