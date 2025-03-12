package com.piledrive.app_gong_fu_timer_compose.ui.nav

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.piledrive.app_gong_fu_timer_compose.ui.screens.MainScreen
import com.piledrive.app_gong_fu_timer_compose.viewmodel.SampleViewModel

interface NavRoute {
	val routeValue: String
}

enum class TopLevelRoutes(override val routeValue: String) : NavRoute {
	SPLASH("splash"), HOME("home")
}

enum class NavArgKeys(val key: String) { GUID("guid") }

enum class ChildRoutes(override val routeValue: String) : NavRoute {
	CONTENT_DETAILS("content/{${NavArgKeys.GUID.key}}"),
}

@Composable
fun RootNavHost() {
	val navController = rememberNavController()
	NavHost(
		modifier = Modifier.safeDrawingPadding(),
		navController = navController,
		startDestination = MainScreen.routeValue
	) {
		/*
		val podcastCallbacks = object : PodcastCallbacks {
			override val onPodcastOpen: (podcast: IPodcastData) -> Unit = { podcast ->
				val toRoute = PodcastScreen.routeValue.replace("{${NavArgKeys.GUID.key}}", podcast.guid)
				navController.navigate(toRoute)
			}
			override val onFollowToggled: (podcast: IPodcastData, status: PodcastFollowStatus) -> Unit = { _, _ -> }
			override val onShowFullDescription: (podcast: IPodcastData) -> Unit = {}
			override val onOpenShowSettings: (podcast: IPodcastData) -> Unit = {}
		}
*/

		composable(route = MainScreen.routeValue) {
			val viewModel: SampleViewModel = hiltViewModel<SampleViewModel>()
			LaunchedEffect("load_content_on_launch") {
				viewModel.reloadContent()
			}
			MainScreen.draw(
				viewModel,
			)
		}
		/*
		composable(route = PodcastScreen.routeValue) { navStackEntry ->
			val podcastGuid = navStackEntry.arguments?.getString(NavArgKeys.GUID.key)
				?: throw InvalidParameterException("no podcast guid provided for nav")
			val podcastViewModel: PodcastViewModel = hiltViewModel<PodcastViewModel>()
			//val vmOwner = LocalViewModelStoreOwner.current ?: return@composable
			//val altPodcastViewModel: PodcastViewModel = hiltViewModel<PodcastViewModel>(vmOwner)
			val playerViewModel = hiltViewModel<PlayerViewModel>()
			LaunchedEffect("load_podcast_on_nav") {
				podcastViewModel.loadPodcast(podcastGuid)
			}
			PodcastScreen.draw(
				podcastViewModel,
				playerViewModel,
				podcastGuid,
				onBack = { navController.navigateUp() },
				onOpenPodcastSettings = { podcast ->
					val toRoute = PodcastSettingsScreen.routeValue.replace("{${NavArgKeys.GUID.key}}", podcast.guid)
					navController.navigate(toRoute)
				}
			)
		}
		 */
	}
}

