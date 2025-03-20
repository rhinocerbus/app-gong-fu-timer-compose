package com.piledrive.app_gong_fu_timer_compose.ui.screens

import com.piledrive.app_gong_fu_timer_compose.data.TimerPhase
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewBooleanFlow
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewIntFlow
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewLongFlow
import com.piledrive.app_gong_fu_timer_compose.ui.util.previewTimerPhaseFlow
import com.piledrive.lib_compose_components.ui.dropdown.readonly.ReadOnlyDropdownCoordinator
import kotlinx.coroutines.flow.StateFlow

interface MainScreenCoordinator {
	val startTimeDropdownCoordinator: ReadOnlyDropdownCoordinator
	val additionalTimeDropdownCoordinator: ReadOnlyDropdownCoordinator
	val timerPhaseState: StateFlow<TimerPhase>
	val steepCountState: StateFlow<Int>
	val targetSteepTimeMsState: StateFlow<Long>
	val steepRoundProgressMsState: StateFlow<Long>
	val keepScreenOnState: StateFlow<Boolean>
	val onStartRound: () -> Unit
	val onCancelRound: () -> Unit
	val onReset: () -> Unit
}

val stubMainScreenCoordinator = object : MainScreenCoordinator {
	override val startTimeDropdownCoordinator: ReadOnlyDropdownCoordinator = ReadOnlyDropdownCoordinator()
	override val additionalTimeDropdownCoordinator: ReadOnlyDropdownCoordinator = ReadOnlyDropdownCoordinator()
	override val timerPhaseState: StateFlow<TimerPhase> = previewTimerPhaseFlow()
	override val steepCountState: StateFlow<Int> = previewIntFlow(1)
	override val targetSteepTimeMsState: StateFlow<Long> = previewLongFlow(8000L)
	override val steepRoundProgressMsState: StateFlow<Long> = previewLongFlow(20000L)
	override val keepScreenOnState: StateFlow<Boolean> = previewBooleanFlow(false)
	override val onStartRound: () -> Unit = {}
	override val onCancelRound: () -> Unit = {}
	override val onReset: () -> Unit = {}
}