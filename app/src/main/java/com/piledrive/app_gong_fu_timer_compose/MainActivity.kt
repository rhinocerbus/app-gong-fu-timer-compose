package com.piledrive.app_gong_fu_timer_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.piledrive.app_gong_fu_timer_compose.ui.nav.RootNavHost
import com.piledrive.lib_compose_components.ui.theme.custom.AppTheme
import com.piledrive.app_gong_fu_timer_compose.viewmodel.MainViewModel
import com.piledrive.lib_compose_components.ui.util.updateStatusBarColorCompose
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			AppTheme {
				updateStatusBarColorCompose(MaterialTheme.colorScheme.background)
				RootNavHost()
			}
		}
	}
}