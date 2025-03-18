package com.piledrive.app_gong_fu_timer_compose.ui.util

import com.piledrive.app_gong_fu_timer_compose.data.TimerPhase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


fun previewBooleanFlow(
	value: Boolean = false
): StateFlow<Boolean> {
	return MutableStateFlow(value)
}

fun previewIntFlow(
	value: Int = 0
): StateFlow<Int> {
	return MutableStateFlow(value)
}

fun previewLongFlow(
	value: Long = 0L
): StateFlow<Long> {
	return MutableStateFlow(value)
}

fun previewTimerPhaseFlow(
	value: TimerPhase = TimerPhase.INITIAL
): StateFlow<TimerPhase> {
	return MutableStateFlow(value)
}
