package com.piledrive.app_gong_fu_timer_compose.ui.screens

import androidx.compose.runtime.mutableStateOf
import com.piledrive.app_gong_fu_timer_compose.data.TimeOption
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewBooleanFlow
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewIntFlow
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewLongFlow
import com.piledrive.lib_compose_components.ui.coordinators.TimerCoordinator
import com.piledrive.lib_compose_components.ui.dropdown.readonly.ReadOnlyDropdownCoordinator
import com.piledrive.lib_compose_components.ui.dropdown.readonly.ReadOnlyDropdownCoordinatorGeneric
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface MainScreenCoordinatorImpl {
	val startTimeDropdownCoordinator: ReadOnlyDropdownCoordinator
	val additionalTimeDropdownCoordinator: ReadOnlyDropdownCoordinator
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

	override val steepTimerCoordinator: TimerCoordinator = TimerCoordinator(
		viewModelScope,
		3000L,
		onTimerStarted = {
			// had this in onDelayCompleted but would cause problems with cancelling during the countdown and decrementing
			// now we don't need onDelayCompleted at all
			_steepCountState.value += 1
		},
		onTimerFinished = {

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
		}
		steepTimerCoordinator.startTimer()
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
	override val startTimeDropdownCoordinator: ReadOnlyDropdownCoordinator = ReadOnlyDropdownCoordinator()
	override val additionalTimeDropdownCoordinator: ReadOnlyDropdownCoordinator = ReadOnlyDropdownCoordinator()
	override val steepTimerCoordinator: TimerCoordinator = TimerCoordinator()
	override val steepCountState: StateFlow<Int> = previewIntFlow(1)
	override val onStartRound: () -> Unit = {}
	override val onCancelRound: () -> Unit = {}
	override val onReset: () -> Unit = {}
}