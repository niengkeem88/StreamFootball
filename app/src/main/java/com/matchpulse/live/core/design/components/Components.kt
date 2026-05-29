package com.matchpulse.live.core.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.android.gms.ads.AdSize
import com.matchpulse.live.R
import com.matchpulse.live.core.ads.AdMobManager
import com.matchpulse.live.core.ads.AdPlacement
import com.matchpulse.live.core.ads.BannerAdComposable
import com.matchpulse.live.domain.model.Competition
import com.matchpulse.live.domain.model.FootballMatch
import com.matchpulse.live.domain.model.MatchStatus
import com.matchpulse.live.domain.model.TvGuideCompetition
import com.matchpulse.live.domain.model.TvScheduleItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MatchPulseScaffold(
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background,
        content = content,
    )
}

@Composable
fun HeroHeader(title: String, subtitle: String, lastUpdated: Long? = null, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().statusBarsPadding(),
        shape = RoundedCornerShape(0.dp, 0.dp, 24.dp, 24.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f),
                        )
                    )
                )
        ) {
            // Subtle branding watermark in header
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_image),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 30.dp, y = 20.dp)
                    .alpha(0.12f),
                contentScale = ContentScale.Fit
            )

            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_image),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                    if (lastUpdated != null && lastUpdated > 0) {
                        Text(
                            text = "Last updated: ${formatTimeOnly(lastUpdated)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(enabled = enabled, onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(text)
    }
}

@Composable
fun StatusBadge(status: MatchStatus, minute: Int? = null) {
    val color = when (status) {
        MatchStatus.LIVE -> MaterialTheme.colorScheme.secondary
        MatchStatus.HALFTIME -> MaterialTheme.colorScheme.tertiary
        MatchStatus.FINISHED -> MaterialTheme.colorScheme.outline
        MatchStatus.POSTPONED, MatchStatus.CANCELLED -> MaterialTheme.colorScheme.error
        MatchStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
    }
    val text = when {
        status == MatchStatus.LIVE && minute != null -> "${minute}' LIVE"
        status == MatchStatus.SCHEDULED -> "SCHEDULED"
        else -> status.name.replace('_', ' ')
    }
    Surface(shape = RoundedCornerShape(999.dp), color = color.copy(alpha = 0.16f), border = BorderStroke(1.dp, color.copy(alpha = 0.5f))) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun MatchCard(
    match: FootballMatch,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isScheduled = match.status == MatchStatus.SCHEDULED
    OutlinedCard(
        modifier = modifier.fillMaxWidth().heightIn(min = 154.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(match.competition.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                StatusBadge(match.status, match.minute)
            }
            TeamScoreRow(match.homeTeam.name, if (isScheduled) null else match.homeScore, bold = !isScheduled && match.homeScore.orZero() >= match.awayScore.orZero(), logoUrl = match.homeTeam.logoUrl)
            TeamScoreRow(match.awayTeam.name, if (isScheduled) null else match.awayScore, bold = !isScheduled && match.awayScore.orZero() >= match.homeScore.orZero(), logoUrl = match.awayTeam.logoUrl)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val bottomText = if (isScheduled) {
                    "Starts at ${formatTimeOnly(match.kickoffTimeMillis)}"
                } else {
                    match.venue
                }
                Text(bottomText, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                TextButton(onClick = onFavorite) { Text(if (isFavorite) "Saved" else "Favorite") }
            }
            if (match.isStale) Text("Showing cached data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun TeamScoreRow(name: String, score: Int?, bold: Boolean, logoUrl: String?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = logoUrl,
                contentDescription = null,
                modifier = Modifier.size(24.dp).clip(CircleShape),
                contentScale = ContentScale.Fit,
                error = painterResource(id = R.drawable.ic_launcher_image),
                fallback = painterResource(id = R.drawable.ic_launcher_image)
            )
            Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        }
        Text(score?.toString() ?: "-", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CompetitionCard(competition: Competition, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AsyncImage(
                    model = competition.logoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                    error = painterResource(id = R.drawable.ic_launcher_image),
                    fallback = painterResource(id = R.drawable.ic_launcher_image)
                )
                Text(competition.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("${competition.region} - ${competition.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TvCompetitionCard(competition: TvGuideCompetition, onSchedule: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(competition.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${competition.region} - ${competition.category}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(competition.description, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onSchedule, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("View Schedule") }
        }
    }
}

@Composable
fun TvScheduleCard(item: TvScheduleItem, onProviderClick: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(item.matchTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(formatTime(item.kickoffTimeMillis), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            item.providers.forEach { provider ->
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column {
                        Text(provider.name, fontWeight = FontWeight.SemiBold)
                        Text(provider.type, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = { onProviderClick(provider.id) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Text("Open Official Provider", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            if (item.isStale) Text("Showing cached schedule", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    StateBox(title, message, modifier)
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    StateBox("Could not load", message, modifier, MaterialTheme.colorScheme.error, onRetry)
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun StateBox(
    title: String, 
    message: String, 
    modifier: Modifier = Modifier, 
    accent: Color = MaterialTheme.colorScheme.primary,
    onAction: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (onAction != null) {
            OutlinedButton(onClick = onAction) {
                Text("Retry")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        singleLine = true,
        placeholder = { Text(placeholder) },
        shape = RoundedCornerShape(18.dp),
    )
}

@Composable
fun SettingGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title)
        OutlinedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(4.dp)) { content() }
        }
    }
}

@Composable
fun SettingRow(title: String, subtitle: String? = null, checked: Boolean? = null, onCheckedChange: ((Boolean) -> Unit)? = null, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(enabled = onClick != null) { onClick?.invoke() }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (checked != null && onCheckedChange != null) Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun AdContainer(placement: AdPlacement, adMobManager: AdMobManager, adSize: AdSize? = null) {
    PremiumBannerContainer(placement, adMobManager, adSize = adSize)
}

@Composable
fun PremiumBannerContainer(
    placement: AdPlacement,
    adMobManager: AdMobManager,
    modifier: Modifier = Modifier,
    adSize: AdSize? = null
) {
    var visible by remember { mutableStateOf(true) }
    if (!visible) return

    val minHeight = if (adSize == AdSize.MEDIUM_RECTANGLE) 260.dp else 110.dp
    val maxHeight = if (adSize == AdSize.MEDIUM_RECTANGLE) 300.dp else 130.dp

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight, max = maxHeight),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SPONSORED",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                BannerAdComposable(
                    placement = placement,
                    adMobManager = adMobManager,
                    adSize = adSize,
                    onAdFailedToLoad = { visible = false }
                )
            }
        }
    }
}

@Composable
fun LegalDisclaimerCard(modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))) {
        Text(
            "MatchPulse Live does not host, distribute, or promote unauthorized live streams. Provider links must be configured only from legal and official sources.",
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun Spacer8() = Spacer(Modifier.height(8.dp))

@Composable
fun Spacer16() = Spacer(Modifier.height(16.dp))

fun formatTime(millis: Long): String = SimpleDateFormat("EEE, HH:mm", Locale.getDefault()).format(Date(millis))

fun formatTimeOnly(millis: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

private fun Int?.orZero(): Int = this ?: 0
