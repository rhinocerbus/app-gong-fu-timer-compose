package com.piledrive.template.ui.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


fun previewMainContentFlow(
): StateFlow<Int> {
	return MutableStateFlow(0)
}
