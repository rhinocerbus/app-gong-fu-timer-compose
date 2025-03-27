package com.piledrive.app_gong_fu_timer_compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piledrive.app_gong_fu_timer_compose.repo.TimerRepo
import com.piledrive.app_gong_fu_timer_compose.ui.screens.MainScreenCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val repo: TimerRepo
) : ViewModel() {

	val coordinator: MainScreenCoordinator = MainScreenCoordinator(
		viewModelScope,
		TimerRepo.defaultCountdownMs,
		repo.startTimeOptions,
		repo.additionalTimeOptions,
	)
}
