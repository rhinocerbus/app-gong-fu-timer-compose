package com.piledrive.app_gong_fu_timer_compose.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val repo: SampleRepo
) : ViewModel() {

	companion object {
		const val DEFAULT_ADDITIONAL_STEEP_TIME_MS = 10000L
		const val DEFAULT_INITIAL_STEEP_TIME_MS = 20000L
	}

	private val _steepCountState = MutableStateFlow<Int>(0)
	val steepCountState: StateFlow<Int> = _steepCountState

	private val _steepingRoundRunningState = MutableStateFlow<Boolean>(false)
	val steepingRoundRunningState: StateFlow<Boolean> = _steepingRoundRunningState

	private val _startingSteepTimeMsState = MutableStateFlow<Long>(DEFAULT_INITIAL_STEEP_TIME_MS)
	val startingSteepTimeMsState: StateFlow<Long> = _startingSteepTimeMsState

	private val _targetSteepTimeMsState = MutableStateFlow<Long>(DEFAULT_INITIAL_STEEP_TIME_MS)
	val targetSteepTimeMsState: StateFlow<Long> = _targetSteepTimeMsState

	private val _steepRoundIntervalMsState = MutableStateFlow<Long>(DEFAULT_ADDITIONAL_STEEP_TIME_MS)
	val steepRoundIntervalMsState: StateFlow<Long> = _steepRoundIntervalMsState

	private val _steepRoundProgressMsState = MutableStateFlow<Long>(0)
	val steepRoundProgressMsState: StateFlow<Long> = _steepRoundProgressMsState

	private var targetSteepTimeMs = -1L

	fun reset() {
		_steepCountState.value = 0
		_steepingRoundRunningState.value = false
		_targetSteepTimeMsState.value = DEFAULT_INITIAL_STEEP_TIME_MS
		_steepRoundProgressMsState.value = 0L
		timerJob?.cancel()
	}

	private var timerJob: Job? = null
	fun startSteepingRound() {
		if (targetSteepTimeMs <= -1) {
			targetSteepTimeMs = DEFAULT_INITIAL_STEEP_TIME_MS
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
				targetSteepTimeMs += DEFAULT_ADDITIONAL_STEEP_TIME_MS
				_targetSteepTimeMsState.value = targetSteepTimeMs
			}
				.collect { progress ->
					withContext(Dispatchers.Main) {
						_steepRoundProgressMsState.value = progress
					}
				}
		}
	}


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
		dropdownOptionsState = mutableStateOf(startTimeOptions)
	)

	private val additionalTimeOptions = listOf(
		TimeOption(5, "5s", timeValueMs = 5000L),
		TimeOption(10, "10s", timeValueMs = 10000L),
		TimeOption(15, "15s", timeValueMs = 15000L),
		TimeOption(20, "20s", timeValueMs = 20000L),
	)

	val additionalTimeDropdownCoordinator = ReadOnlyDropdownCoordinator<Long>(
		selectedOptionState = mutableStateOf(additionalTimeOptions.firstOrNull { it.timeValueMs == DEFAULT_ADDITIONAL_STEEP_TIME_MS }),
		dropdownOptionsState = mutableStateOf(additionalTimeOptions)
	)

	/////////////////////////////////////////////////
	//  endregion

}
