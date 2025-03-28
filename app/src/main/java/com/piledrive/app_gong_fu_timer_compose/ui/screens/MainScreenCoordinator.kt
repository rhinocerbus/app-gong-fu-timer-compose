package com.piledrive.app_gong_fu_timer_compose.ui.screens

import androidx.compose.runtime.mutableStateOf
import com.piledrive.app_gong_fu_timer_compose.data.TimeOption
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewIntFlow
import com.piledrive.lib_compose_components.ui.coordinators.TimerCoordinator
import com.piledrive.lib_compose_components.ui.dropdown.readonly.ReadOnlyDropdownCoordinatorGeneric
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface MainScreenCoordinatorImpl {
	// change to typed, use timeoption directly, get rid of filtering to get curent seelction and jus tht ethe coordinator states
	val startTimeDropdownCoordinator: ReadOnlyDropdownCoordinatorGeneric<TimeOption>
	val additionalTimeDropdownCoordinator: ReadOnlyDropdownCoordinatorGeneric<TimeOption>
	val steepTimerCoordinator: TimerCoordinator
	val steepCountState: StateFlow<Int>
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
	override val startTimeDropdownCoordinator = ReadOnlyDropdownCoordinatorGeneric<TimeOption>(
		selectedOptionState = mutableStateOf(startTimeOptions.firstOrNull { it.default }),
		dropdownOptionsState = mutableStateOf(startTimeOptions),
		externalOnOptionSelected = { option ->
			/*
				at the moment at least, this seems to be the main sticking point in favor of having the nested coordinators - not
					wanting to expose changing the target time based on state externally
			 */
			if (_steepCountState.value == 0) {
				val updTime = option?.timeValueMs ?: throw (IllegalStateException("unable to find specified time option"))
				steepTimerCoordinator.updateTimerDuration(updTime)
			}
		}
	)

	override val additionalTimeDropdownCoordinator = ReadOnlyDropdownCoordinatorGeneric<TimeOption>(
		selectedOptionState = mutableStateOf(additionalTimeOptions.firstOrNull { it.default }),
		dropdownOptionsState = mutableStateOf(additionalTimeOptions),
	)

	override val steepTimerCoordinator: TimerCoordinator = TimerCoordinator(
		viewModelScope,
		countdownTimeMs,
		startTimeOptions.firstOrNull { it.default }?.timeValueMs ?: throw (IllegalArgumentException("no default starting time")),
		onTimerStarted = {
			// had this in onDelayCompleted but would cause problems with cancelling during the countdown and decrementing
			// now we don't need onDelayCompleted at all
			_steepCountState.value += 1
		},
		onTimerFinished = {
			incrementTime()
			doTimerDoneHaptics()
		}
	)

	private val defaultStartTimeMs = startTimeOptions.firstOrNull { it.default }?.timeValueMs
		?: throw (IllegalArgumentException("no default start time option defined"))
	private val defaultAdditionalTimeMs = startTimeOptions.firstOrNull { it.default }?.timeValueMs
		?: throw (IllegalArgumentException("no default additional time option defined"))


	override val steepCountState: StateFlow<Int>
		get() = _steepCountState

	private val _steepCountState = MutableStateFlow<Int>(0)

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
		if (steepTimerCoordinator.timerDurationMsState.value <= 0) {
			val updTime = startTimeDropdownCoordinator.selectedOptionState.value?.timeValueMs ?: defaultStartTimeMs
			steepTimerCoordinator.updateTimerDuration(updTime)
		}
		steepTimerCoordinator.startTimer()
	}

	private fun incrementTime() {
		var prevTime = steepTimerCoordinator.timerDurationMsState.value
		prevTime += additionalTimeOptions.firstOrNull { it.id == additionalTimeDropdownCoordinator.selectedOptionState.value?.id }?.timeValueMs
			?: throw (IllegalStateException("missing additional per-round time selection mid-session"))
		steepTimerCoordinator.updateTimerDuration(prevTime)
	}

	private fun cancelRound() {
		if (!steepTimerCoordinator.timerRunningState.value) return
		steepTimerCoordinator.cancel()
		_steepCountState.value -= 1
	}

	private fun reset() {
		timerJob?.cancel()
		steepTimerCoordinator.cancel()
		_steepCountState.value = 0
		startTimeDropdownCoordinator.onOptionSelected(startTimeOptions.firstOrNull { it.default })
		additionalTimeDropdownCoordinator.onOptionSelected(additionalTimeOptions.firstOrNull { it.default })
	}
}

val stubMainScreenCoordinator = object : MainScreenCoordinatorImpl {
	override val startTimeDropdownCoordinator: ReadOnlyDropdownCoordinatorGeneric<TimeOption> =
		ReadOnlyDropdownCoordinatorGeneric<TimeOption>()
	override val additionalTimeDropdownCoordinator: ReadOnlyDropdownCoordinatorGeneric<TimeOption> =
		ReadOnlyDropdownCoordinatorGeneric<TimeOption>()
	override val steepTimerCoordinator: TimerCoordinator = TimerCoordinator()
	override val steepCountState: StateFlow<Int> = previewIntFlow(1)
	override val onStartRound: () -> Unit = {}
	override val onCancelRound: () -> Unit = {}
	override val onReset: () -> Unit = {}
}