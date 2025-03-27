package com.piledrive.app_gong_fu_timer_compose.data

import com.piledrive.lib_compose_components.ui.dropdown.data.DropdownOption


class TimeOption(
	override val textValue: String?,
	val timeValueMs: Long,
	val default: Boolean = false
) : DropdownOption {
	override val id: Long = timeValueMs
}