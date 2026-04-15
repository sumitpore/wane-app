package com.wane.app.ui.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.wane.app.R
import com.wane.app.ui.theme.AccentPrimary
import com.wane.app.ui.theme.BodyText
import com.wane.app.ui.theme.SurfaceGlass
import com.wane.app.ui.theme.TextSecondary
import com.wane.app.ui.theme.WaneTheme
import com.wane.app.ui.theme.WaneTypography
import com.wane.app.util.NotificationListenerUtils

private val GrantedGreen = Color(0xFF4CAF50)

@Composable
fun NotificationStep(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isEnabled by remember {
        mutableStateOf(NotificationListenerUtils.isNotificationListenerEnabled(context))
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isEnabled = NotificationListenerUtils.isNotificationListenerEnabled(context)
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.notification_title),
            style = WaneTypography.headlineLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        val uriHandler = LocalUriHandler.current
        val fullText = stringResource(R.string.notification_description)
        val linkDisplay = "github.com/sumitpore/wane-app"
        val linkUrl = stringResource(R.string.open_source_url)
        val linkStart = fullText.indexOf(linkDisplay)
        val annotatedDescription =
            buildAnnotatedString {
                append(fullText)
                addStyle(SpanStyle(color = BodyText), 0, fullText.length)
                if (linkStart >= 0) {
                    addStyle(
                        SpanStyle(color = AccentPrimary, textDecoration = TextDecoration.Underline),
                        linkStart,
                        linkStart + linkDisplay.length,
                    )
                    addStringAnnotation("URL", linkUrl, linkStart, linkStart + linkDisplay.length)
                }
            }

        ClickableText(
            text = annotatedDescription,
            style = WaneTypography.bodyLarge.copy(textAlign = TextAlign.Center),
            modifier = Modifier.padding(horizontal = 40.dp),
            onClick = { offset ->
                annotatedDescription
                    .getStringAnnotations("URL", offset, offset)
                    .firstOrNull()
                    ?.let { uriHandler.openUri(it.item) }
            },
        )

        Spacer(modifier = Modifier.height(48.dp))

        AnimatedContent(
            targetState = isEnabled,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "notification-permission-state",
        ) { granted ->
            if (granted) {
                val scale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
                    label = "checkmark-scale",
                )
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .scale(scale)
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(GrantedGreen),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Permission granted",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
            } else {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceGlass)
                            .clickable {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            }.padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.enable_notification_access),
                        style = WaneTypography.labelLarge,
                        color = AccentPrimary,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun NotificationStepPreview() {
    WaneTheme {
        NotificationStep()
    }
}
