package com.project.rootsync.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun WeatherAnimation(
    weatherType: WeatherAnimationType,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    val animationFile = when (weatherType) {
        WeatherAnimationType.SUNNY -> "lottie/sunny.json"
        WeatherAnimationType.CLOUDY -> "lottie/cloudy.json"
        WeatherAnimationType.RAINY -> "lottie/rainy.json"
        WeatherAnimationType.STORMY -> "lottie/stormy.json"
        WeatherAnimationType.SNOWY -> "lottie/snowy.json"
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(animationFile))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(size)
    )
}

enum class WeatherAnimationType {
    SUNNY,
    CLOUDY,
    RAINY,
    STORMY,
    SNOWY
}
