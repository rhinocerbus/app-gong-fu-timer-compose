package com.piledrive.app_gong_fu_timer_compose.viewmodel

import androidx.lifecycle.ViewModel
import com.piledrive.app_gong_fu_timer_compose.repo.SampleRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SampleViewModel @Inject constructor(
	private val repo: SampleRepo
) : ViewModel() {

	private val _contentState = MutableStateFlow<Int>(0)
	val contentState: StateFlow<Int> = _contentState

	suspend fun reloadContent() {

	}

}
