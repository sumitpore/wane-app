package com.wane.app.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wane.app.R
import com.wane.app.ui.components.PageIndicator
import com.wane.app.ui.components.WaneButton
import com.wane.app.ui.theme.BackgroundDeep
import com.wane.app.ui.theme.BackgroundDeepEnd
import com.wane.app.ui.theme.WaneTheme

private const val PAGE_COUNT = 3

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })

    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page != uiState.currentPage) {
                viewModel.onEvent(
                    if (page > uiState.currentPage) OnboardingUiEvent.NextPage
                    else OnboardingUiEvent.PreviousPage,
                )
            }
        }
    }

    BackHandler(enabled = uiState.currentPage > 0) {
        viewModel.onEvent(OnboardingUiEvent.PreviousPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDeep, BackgroundDeepEnd),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> WelcomeStep()
                1 -> DurationStep(
                    selectedDuration = uiState.selectedDuration,
                    onDurationChange = { viewModel.onEvent(OnboardingUiEvent.SetDuration(it)) },
                )
                2 -> AutoLockStep(
                    autoLockEnabled = uiState.autoLockEnabled,
                    onToggle = { viewModel.onEvent(OnboardingUiEvent.ToggleAutoLock(it)) },
                )
            }
        }

        PageIndicator(
            pageCount = PAGE_COUNT,
            currentPage = uiState.currentPage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
        )

        val buttonAlpha by animateFloatAsState(
            targetValue = 1f,
            animationSpec = spring(stiffness = 100f, dampingRatio = 0.85f),
            label = "button_alpha",
        )

        WaneButton(
            text = when (uiState.currentPage) {
                0 -> stringResource(R.string.begin)
                1 -> stringResource(R.string.next)
                else -> stringResource(R.string.start)
            },
            onClick = {
                if (uiState.currentPage < PAGE_COUNT - 1) {
                    viewModel.onEvent(OnboardingUiEvent.NextPage)
                } else {
                    viewModel.onEvent(OnboardingUiEvent.CompleteOnboarding)
                    onOnboardingComplete()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 40.dp, vertical = 40.dp)
                .graphicsLayer { alpha = buttonAlpha },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun OnboardingScreenPreview() {
    WaneTheme {
        OnboardingScreen(onOnboardingComplete = {})
    }
}
