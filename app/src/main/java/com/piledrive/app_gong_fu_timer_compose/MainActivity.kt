package com.piledrive.app_gong_fu_timer_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.piledrive.app_gong_fu_timer_compose.ui.theme.SampleComposeTheme
import com.piledrive.app_gong_fu_timer_compose.viewmodel.SampleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

	private val viewModel: SampleViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			SampleComposeTheme {
				//RootNavHost(viewModel, this.lifecycleScope)
			}
		}
	}
}