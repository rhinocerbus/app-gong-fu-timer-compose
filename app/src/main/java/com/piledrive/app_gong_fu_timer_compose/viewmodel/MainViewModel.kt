package com.piledrive.app_gong_fu_timer_compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piledrive.app_gong_fu_timer_compose.repo.TimerRepo
import com.piledrive.app_gong_fu_timer_compose.ui.screens.MainScreenCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val repo: TimerRepo
) : ViewModel() {

	private val _hapticsFlow: MutableSharedFlow<Boolean> = MutableSharedFlow()
	val hapticsFlow: Flow<Boolean> = _hapticsFlow

	/*
		still deciding if shoving all of the logic into the coordinator gained anything.
		like in the other apps, i do still ike divorcing the data sources from the logic handlers, although in this more basic use
		case it's just some lists rather than flows that update dynamically.
	 */
	val coordinator: MainScreenCoordinator = MainScreenCoordinator(
		viewModelScope,
		TimerRepo.defaultCountdownMs,
		repo.startTimeOptions,
		repo.additionalTimeOptions,
		doTimerDoneHaptics = {
			viewModelScope.launch(Dispatchers.Main) {
				_hapticsFlow.emit(true)
			}
		}
	)
}
