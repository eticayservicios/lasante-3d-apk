package com.lasante.tvkiosk.app

import android.app.ActivityManager
import android.content.Context
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.lasante.tvkiosk.navigation.LaSanteNavHost
import com.lasante.tvkiosk.ui.layout.Tv1080LayoutDebug
import com.lasante.tvkiosk.ui.layout.Tv1080PreviewFrame
import com.lasante.tvkiosk.ui.layout.Tv66LayoutDebug
import com.lasante.tvkiosk.ui.layout.Tv66PreviewFrame
import com.lasante.tvkiosk.ui.screens.treatments.TreatmentsViewModel
import com.lasante.tvkiosk.ui.screens.treatments.TreatmentsViewModelFactory
import com.lasante.tvkiosk.ui.theme.LaSanteTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun LaSanteApp() {
    val app = LocalContext.current.applicationContext as LaSanteApplication
    val navController = rememberNavController()
    val activity = LocalContext.current as androidx.activity.ComponentActivity
    val windowSizeClass = calculateWindowSizeClass(activity)
    val treatmentsViewModel: TreatmentsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = TreatmentsViewModelFactory(app.catalogRepository),
    )

    LaunchedEffect(Unit) {
        // En low-RAM no competir con el arranque de Filament/GLB.
        val am = app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (am.isLowRamDevice || am.memoryClass <= 256) {
            delay(2_500)
        }
        treatmentsViewModel.warmCache()
    }

    LaSanteTheme {
        Surface {
            // Solo uno activo: tv66Force gana si ambos flags están ON.
            when {
                Tv66LayoutDebug.isForced() -> Tv66PreviewFrame {
                    LaSanteNavHost(
                        navController = navController,
                        catalogRepository = app.catalogRepository,
                        audioPlayer = app.audioPlayer,
                        windowSizeClass = windowSizeClass,
                    )
                }
                Tv1080LayoutDebug.isForced() -> Tv1080PreviewFrame {
                    LaSanteNavHost(
                        navController = navController,
                        catalogRepository = app.catalogRepository,
                        audioPlayer = app.audioPlayer,
                        windowSizeClass = windowSizeClass,
                    )
                }
                else -> LaSanteNavHost(
                    navController = navController,
                    catalogRepository = app.catalogRepository,
                    audioPlayer = app.audioPlayer,
                    windowSizeClass = windowSizeClass,
                )
            }
        }
    }
}
