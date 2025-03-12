package com.piledrive.app_gong_fu_timer_compose.ui.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


fun previewBooleanFlow(
): StateFlow<Boolean> {
	return MutableStateFlow(false)
}

fun previewIntFlow(
): StateFlow<Int> {
	return MutableStateFlow(0)
}

fun previewLongFlow(
): StateFlow<Long> {
	return MutableStateFlow(0L)
}
