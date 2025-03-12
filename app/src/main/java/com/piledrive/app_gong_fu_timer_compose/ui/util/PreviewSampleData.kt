package com.piledrive.app_gong_fu_timer_compose.ui.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


fun previewMainContentFlow(
): StateFlow<Int> {
	return MutableStateFlow(0)
}
