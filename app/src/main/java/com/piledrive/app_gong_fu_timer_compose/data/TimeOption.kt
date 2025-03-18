package com.piledrive.app_gong_fu_timer_compose.data

import com.piledrive.lib_compose_components.ui.dropdown.state.DropdownOption


class TimeOption(
	override val id: Long,
	override val textValue: String?,
	val timeValueMs: Long
) : DropdownOption<Long>