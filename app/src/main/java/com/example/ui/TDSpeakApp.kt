package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.games.GamesScreen
import com.example.ui.speaking.SpeakingScreen
import com.example.ui.vocab.VocabScreen
import com.example.viewmodel.GamesViewModel
import com.example.viewmodel.SpeakingViewModel
import com.example.viewmodel.VocabViewModel

enum class MainTab(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    SPEAKING(
        titleRes = R.string.tab_speaking,
        selectedIcon = Icons.Rounded.RecordVoiceOver,
        unselectedIcon = Icons.Outlined.RecordVoiceOver,
        testTag = "nav_tab_speaking"
    ),
    VOCAB(
        titleRes = R.string.tab_vocab,
        selectedIcon = Icons.Rounded.AutoStories,
        unselectedIcon = Icons.Outlined.AutoStories,
        testTag = "nav_tab_vocab"
    ),
    GAMES(
        titleRes = R.string.tab_games,
        selectedIcon = Icons.Rounded.SportsEsports,
        unselectedIcon = Icons.Outlined.SportsEsports,
        testTag = "nav_tab_games"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TDSpeakApp(
    speakingViewModel: SpeakingViewModel,
    vocabViewModel: VocabViewModel,
    gamesViewModel: GamesViewModel
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.SPEAKING) }
    val vocabUiState by vocabViewModel.uiState.collectAsStateWithLifecycle()
    val dueCount = vocabUiState.dueWords.size

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "TD",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "TDSpeak",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    actions = {
                        if (dueCount > 0) {
                            Surface(
                                onClick = { selectedTab = MainTab.VOCAB },
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "Ôn SRS: $dueCount",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar {
                        MainTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { selectedTab = tab },
                                icon = {
                                    TabIconWithBadge(
                                        tab = tab,
                                        selected = selected,
                                        dueCount = dueCount
                                    )
                                },
                                label = { Text(stringResource(tab.titleRes)) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail {
                        MainTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { selectedTab = tab },
                                icon = {
                                    TabIconWithBadge(
                                        tab = tab,
                                        selected = selected,
                                        dueCount = dueCount
                                    )
                                },
                                label = { Text(stringResource(tab.titleRes)) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        MainTab.SPEAKING -> SpeakingScreen(
                            viewModel = speakingViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        MainTab.VOCAB -> VocabScreen(
                            viewModel = vocabViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        MainTab.GAMES -> GamesScreen(
                            viewModel = gamesViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabIconWithBadge(
    tab: MainTab,
    selected: Boolean,
    dueCount: Int
) {
    val iconVector = if (selected) tab.selectedIcon else tab.unselectedIcon
    val label = stringResource(tab.titleRes)
    if (tab == MainTab.VOCAB && dueCount > 0) {
        BadgedBox(
            badge = {
                Badge { Text(if (dueCount > 99) "99+" else "$dueCount") }
            }
        ) {
            Icon(imageVector = iconVector, contentDescription = label)
        }
    } else {
        Icon(imageVector = iconVector, contentDescription = label)
    }
}
