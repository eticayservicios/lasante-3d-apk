package com.lasante.tvkiosk.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.lasante.tvkiosk.data.CatalogRepository
import com.lasante.tvkiosk.media.AudioPlayer
import com.lasante.tvkiosk.ui.screens.intro.IntroRoute
import com.lasante.tvkiosk.ui.screens.products.ProductsRoute
import com.lasante.tvkiosk.ui.screens.treatments.TreatmentsRoute

private fun NavHostController.navigateToIntroHome() {
    if (!popBackStack(Routes.INTRO, inclusive = false)) {
        navigate(Routes.INTRO) {
            popUpTo(Routes.INTRO) { inclusive = true }
            launchSingleTop = true
        }
    }
}

@Composable
fun LaSanteNavHost(
    navController: NavHostController,
    catalogRepository: CatalogRepository,
    audioPlayer: AudioPlayer,
    windowSizeClass: WindowSizeClass,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val showIntro = navBackStackEntry?.destination?.route == Routes.INTRO

    Box(modifier = Modifier.fillMaxSize()) {
        // Intro permanece montada (vitrina 3D + catálogo cacheados) al navegar a tratamientos/productos.
        IntroRoute(
            catalogRepository = catalogRepository,
            audioPlayer = audioPlayer,
            windowSizeClass = windowSizeClass,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (showIntro) 2f else 0f)
                .graphicsLayer { alpha = if (showIntro) 1f else 0f },
            contentActive = showIntro,
            onNavigateToTreatments = { unitId ->
                navController.navigate("${Routes.TREATMENTS}/$unitId")
            },
            onNavigateToStarProducts = { unitId ->
                navController.navigate(
                    "${Routes.PRODUCTS}/$unitId/${Args.STAR_PRODUCTS_ID}",
                )
            },
        )

        NavHost(
            navController = navController,
            startDestination = Routes.INTRO,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (showIntro) 1f else 2f),
        ) {
            composable(Routes.INTRO) {
                // Placeholder: Intro se renderiza debajo para conservar la escena 3D.
            }

            composable(
                route = "${Routes.TREATMENTS}/{${Args.UNIT_ID}}",
                arguments = listOf(navArgument(Args.UNIT_ID) { type = NavType.StringType }),
            ) { backStackEntry ->
                val unitId = backStackEntry.arguments?.getString(Args.UNIT_ID).orEmpty()
                TreatmentsRoute(
                    catalogRepository = catalogRepository,
                    unitId = unitId,
                    windowSizeClass = windowSizeClass,
                    onBack = { navController.popBackStack() },
                    onTreatmentSelected = { treatmentId ->
                        navController.navigate("${Routes.PRODUCTS}/$unitId/$treatmentId")
                    },
                )
            }

            composable(
                route = "${Routes.PRODUCTS}/{${Args.UNIT_ID}}/{${Args.TREATMENT_ID}}",
                arguments = listOf(
                    navArgument(Args.UNIT_ID) { type = NavType.StringType },
                    navArgument(Args.TREATMENT_ID) { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val unitId = backStackEntry.arguments?.getString(Args.UNIT_ID).orEmpty()
                val treatmentId = backStackEntry.arguments?.getString(Args.TREATMENT_ID).orEmpty()
                ProductsRoute(
                    catalogRepository = catalogRepository,
                    unitId = unitId,
                    treatmentId = treatmentId,
                    windowSizeClass = windowSizeClass,
                    onBack = { navController.popBackStack() },
                    onHome = { navController.navigateToIntroHome() },
                )
            }
        }
    }
}
