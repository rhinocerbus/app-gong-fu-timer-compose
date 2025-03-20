package com.piledrive.app_gong_fu_timer_compose.data

enum class TimerPhase {
	INITIAL, COUNTDOWN, RUNNING, IDLE;

	val isActive: Boolean
		get() {
			return this == COUNTDOWN || this == RUNNING
		}
}