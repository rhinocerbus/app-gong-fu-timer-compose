package com.piledrive.app_gong_fu_timer_compose

import com.piledrive.app_gong_fu_timer_compose.repo.TimerRepo
import com.piledrive.app_gong_fu_timer_compose.util.ActiveChanged
import com.piledrive.app_gong_fu_timer_compose.util.ProgressChange
import com.piledrive.app_gong_fu_timer_compose.util.tickerFlowWithCountdownCallbacksOnly
import com.piledrive.app_gong_fu_timer_compose.util.unifiedTickerFlowWithCountdown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TimerTests {

	/**
	 * Timer flow emitting TimerUpdate.ActiveChanged & TimerUpdate.ProgressChange
	 */
	@Test
	fun unified_timer_using_sealed_class_flow() {
		val stateCounter = CountDownLatch(2)

		val durationMs = 10000L
		val delayMs = 3000L

		var timeMs = 0L
		var isActive = false

		CoroutineScope(Dispatchers.Default).launch {
			unifiedTickerFlowWithCountdown(
				initialDelayMs = delayMs,
				durationMs = durationMs,
			).collect {
				when (it) {
					is ActiveChanged -> {
						when {
							isActive == it.isActive -> {}
							it.isActive -> {
								assert(timeMs == 0L, lazyMessage = {
									IllegalStateException("onStarted time out of order | was: ${timeMs} | expected: 0")
								})
								stateCounter.countDown()
							}

							!it.isActive -> {
								assert(timeMs > durationMs, lazyMessage = {
									IllegalStateException("onFinished time out of order | was: ${timeMs} | expected: ${durationMs}")
								})
								stateCounter.countDown()
							}

						}
						isActive = it.isActive
					}

					is ProgressChange -> {
						timeMs = it.progressMs
						when {
							timeMs >= -durationMs && timeMs <= 0 -> {
								assert(isActive, lazyMessage = {
									IllegalStateException("timer onStarted out of order | was: ${stateCounter.count} | expected: 3")
								})
							}

							timeMs in 1..<durationMs -> {
								assert(isActive, lazyMessage = {
									IllegalStateException("timer onDelayCompleted out of order | was: ${stateCounter.count} | expected: 2")
								})
							}

							else -> {
								assert(!isActive, lazyMessage = {
									IllegalStateException("timer onFinished out of order | was: ${stateCounter.count} | expected: 1")
								})
							}
						}
					}

					else -> {
					}
				}
			}
		}
		stateCounter.await(20000L, TimeUnit.MILLISECONDS)
		assert(timeMs > durationMs, lazyMessage = {
			IllegalStateException("timer expiration runtime value mismatch | was: ${timeMs} | expected: $durationMs")
		})
	}

	@Test
	fun timer_using_only_callbacks() {
		val stateCounter = CountDownLatch(3)

		var timeMs = 0L
		val durationMs = 10000L
		val delayMs = 3000L
		CoroutineScope(Dispatchers.Default).launch {
			tickerFlowWithCountdownCallbacksOnly(
				initialDelayMs = delayMs,
				durationMs = durationMs,
				onStarted = {
					assert((stateCounter.count == 3L), lazyMessage = {
						IllegalStateException("timer onStarted out of order | was: ${stateCounter.count} | expected: 3")
					})
					stateCounter.countDown()
				},
				onDelayCompleted = {
					assert((stateCounter.count == 2L), lazyMessage = {
						IllegalStateException("timer onDelayCompleted out of order | was: ${stateCounter.count} | expected: 2")
					})
					stateCounter.countDown()
				},
				onFinished = {
					assert((stateCounter.count == 1L), lazyMessage = {
						IllegalStateException("timer onFinished out of order | was: ${stateCounter.count} | expected: 1")
					})
					stateCounter.countDown()
				},
				onTick = { runtimeMs ->
					when (stateCounter.count) {
						3L -> assert((runtimeMs == 0L), lazyMessage = {
							IllegalStateException("time out of range | time: $runtimeMs | expected: 0")
						})

						2L -> assert((runtimeMs in -delayMs..0), lazyMessage = {
							IllegalStateException("time out of range | time: $runtimeMs | expected: ${-delayMs} <-> 0")
						})

						1L -> assert((runtimeMs in -delayMs..durationMs), lazyMessage = {
							IllegalStateException("time out of range | time: $runtimeMs | expected: ${-delayMs} <-> $durationMs")
						})

						0L -> assert((runtimeMs < durationMs), lazyMessage = {
							IllegalStateException("time out of range | time: $runtimeMs | expected: > $durationMs")
						})
					}
					timeMs = runtimeMs
				}
			).collect {
			}
		}
		stateCounter.await(20000L, TimeUnit.MILLISECONDS)
		// can't have both an onTick check for the finished state, AND and accurate post-finish check
		/*assert(timeMs > durationMs, lazyMessage = {
			IllegalStateException("timer expiration runtime value mismatch | was: ${timeMs} | expected: $durationMs")
		})*/
	}

	@Test
	fun addition_isCorrect2() {
		val stateCounter = CountDownLatch(3)
		val repo = TimerRepo()

		var timeMs = 0L
		val durationMs = 10000L
		val delayMs = 3000L
		CoroutineScope(Dispatchers.Default).launch {
			repo.startTimerFlow(
				durationMs = durationMs,
				onStarted = {
					assert((stateCounter.count == 3L), lazyMessage = {
						IllegalStateException("timer onStarted out of order | was: ${stateCounter.count} | expected: 3")
					})
					stateCounter.countDown()
				},
				onDelayCompleted = {
					assert((stateCounter.count == 2L), lazyMessage = {
						IllegalStateException("timer onDelayCompleted out of order | was: ${stateCounter.count} | expected: 2")
					})
					stateCounter.countDown()
				},
				onFinished = {
					assert((stateCounter.count == 1L), lazyMessage = {
						IllegalStateException("timer onFinished out of order | was: ${stateCounter.count} | expected: 1")
					})
					stateCounter.countDown()
				}
			).collect {
				timeMs = it
				when (stateCounter.count) {
					3L -> assert(it in delayMs..0)
					2L -> assert(it in delayMs..durationMs)
					1L -> assert(it < durationMs)
				}
			}
		}
		stateCounter.await(20000L, TimeUnit.MILLISECONDS)
		assert(timeMs > durationMs, lazyMessage = {
			IllegalStateException("timer expiration runtime value mismatch | was: ${timeMs} | expected: $durationMs")
		})
	}
}