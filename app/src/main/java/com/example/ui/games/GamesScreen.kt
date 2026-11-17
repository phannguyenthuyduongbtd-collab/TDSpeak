package com.example.ui.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.AmberTertiaryContainer
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.CorrectGreenBg
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.WrongRed
import com.example.ui.theme.WrongRedBg
import com.example.viewmodel.GameType
import com.example.viewmodel.GamesUiState
import com.example.viewmodel.GamesViewModel
import com.example.viewmodel.QuizDirection

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    viewModel: GamesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val games = GameType.entries
    val selectedTabIndex = games.indexOf(uiState.selectedGame).coerceAtLeast(0)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 4 Sub-tabs for the 4 distinct vocabulary games
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 12.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    games.forEach { game ->
                        val icon = when (game) {
                            GameType.QUIZ -> Icons.Rounded.Quiz
                            GameType.FILL_BLANK -> Icons.Rounded.TextFields
                            GameType.DICTATION -> Icons.Rounded.Hearing
                            GameType.SCRAMBLE -> Icons.Rounded.Spellcheck
                        }
                        Tab(
                            selected = uiState.selectedGame == game,
                            onClick = { viewModel.selectGame(game) },
                            text = { Text(game.label, maxLines = 1) },
                            icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("game_subtab_${game.name.lowercase()}")
                        )
                    }
                }
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier.testTag("game_sound_toggle")
                ) {
                    Icon(
                        imageVector = if (uiState.isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = if (uiState.isMuted) "Bật âm thanh" else "Tắt âm thanh",
                        tint = if (uiState.isMuted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (uiState.vocabPool.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Hãy thêm ít nhất 4 từ vựng ở Tab Sổ tay để bắt đầu chơi!",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                when (uiState.selectedGame) {
                    GameType.QUIZ -> QuizGameView(uiState = uiState, viewModel = viewModel)
                    GameType.FILL_BLANK -> FillBlankGameView(uiState = uiState, viewModel = viewModel)
                    GameType.DICTATION -> DictationGameView(uiState = uiState, viewModel = viewModel)
                    GameType.SCRAMBLE -> WordScrambleGameView(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun QuizGameView(
    uiState: GamesUiState,
    viewModel: GamesViewModel
) {
    if (uiState.vocabPool.size < 4) {
        NotEnoughWordsCard(requiredCount = 4, currentCount = uiState.vocabPool.size)
        return
    }

    if (uiState.quizFinished) {
        GameCompletionCard(
            gameTitle = "Hoàn thành Quiz 4 đáp án!",
            score = uiState.quizScore,
            maxScore = uiState.quizQuestions.size * 10,
            onPlayAgain = { viewModel.restartQuiz() }
        )
        return
    }

    val question = uiState.quizQuestions.getOrNull(uiState.quizIndex) ?: return
    val answered = uiState.quizSelectedOption != null || uiState.quizTimedOut

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GameProgressHeader(
            currentRound = uiState.quizIndex + 1,
            totalRounds = uiState.quizQuestions.size,
            score = uiState.quizScore,
            extraBadge = if (uiState.quizStreak > 1) "Chuỗi ${uiState.quizStreak}🔥" else null
        )

        // Direction switcher + Timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = {
                    val next = if (uiState.quizDirection == QuizDirection.EN_TO_VI) {
                        QuizDirection.VI_TO_EN
                    } else {
                        QuizDirection.EN_TO_VI
                    }
                    viewModel.toggleQuizDirection(next)
                },
                label = {
                    Text(
                        if (uiState.quizDirection == QuizDirection.EN_TO_VI) "Chế độ: Anh → Việt" else "Chế độ: Việt → Anh"
                    )
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )

            Surface(
                color = if (uiState.quizSecondsLeft <= 5) WrongRedBg else MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = "Thời gian còn lại",
                        tint = if (uiState.quizSecondsLeft <= 5) WrongRed else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${uiState.quizSecondsLeft}s",
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                        color = if (uiState.quizSecondsLeft <= 5) WrongRed else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { uiState.quizSecondsLeft / 15f },
            color = if (uiState.quizSecondsLeft <= 5) WrongRed else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        // Question Prompt Card
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Chọn đáp án đúng nhất",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = question.promptText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (question.promptSub.isNotBlank()) {
                    Text(
                        text = question.promptSub,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (uiState.quizDirection == QuizDirection.EN_TO_VI) {
                    IconButton(onClick = { viewModel.speakText(question.target.word) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Nghe từ",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 4 Choices
        question.options.forEachIndexed { idx, optionText ->
            val isCorrectOption = idx == question.correctOptionIndex
            val isSelected = idx == uiState.quizSelectedOption

            val (containerColor, borderColor, textColor) = when {
                !answered -> Triple(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.outlineVariant,
                    MaterialTheme.colorScheme.onSurface
                )
                isCorrectOption -> Triple(CorrectGreenBg, CorrectGreen, CorrectGreen)
                isSelected -> Triple(WrongRedBg, WrongRed, WrongRed)
                else -> Triple(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.outlineVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                onClick = { if (!answered) viewModel.selectQuizOption(idx) },
                color = containerColor,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.5.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_option_$idx")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = borderColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = ('A' + idx).toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = textColor
                            )
                        }
                    }
                    Text(
                        text = optionText,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        AnimatedVisibility(visible = answered) {
            val isSuccess = uiState.quizSelectedOption == question.correctOptionIndex
            FeedbackContinueBar(
                isCorrect = isSuccess,
                message = when {
                    uiState.quizTimedOut -> "Hết giờ! Đáp án đúng: ${question.options[question.correctOptionIndex]}"
                    isSuccess -> "Chính xác! +10 điểm"
                    else -> "Chưa đúng! Đáp án là: ${question.options[question.correctOptionIndex]}"
                },
                onNext = { viewModel.nextQuizQuestion() }
            )
        }
    }
}

@Composable
private fun FillBlankGameView(
    uiState: GamesUiState,
    viewModel: GamesViewModel
) {
    if (uiState.fillFinished) {
        GameCompletionCard(
            gameTitle = "Hoàn thành Điền từ khuyết!",
            score = uiState.fillScore,
            maxScore = uiState.fillQuestions.size * 10,
            onPlayAgain = { viewModel.restartFillBlank() }
        )
        return
    }

    val q = uiState.fillQuestions.getOrNull(uiState.fillIndex) ?: return
    val submitted = uiState.fillSubmittedCorrect != null

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GameProgressHeader(
            currentRound = uiState.fillIndex + 1,
            totalRounds = uiState.fillQuestions.size,
            score = uiState.fillScore
        )

        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = AmberTertiaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Gợi ý: ${q.target.meaningVi} (${q.target.partOfSpeech})",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmberTertiary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.speakText(q.fullSentence) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Nghe cả câu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = q.maskedSentence,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = uiState.fillInput,
                    onValueChange = { viewModel.updateFillInput(it) },
                    label = { Text("Nhập từ còn thiếu") },
                    placeholder = { Text("Gõ từ tiếng Anh...") },
                    singleLine = true,
                    enabled = !submitted,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.submitFillBlank() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fill_blank_input")
                )

                if (!submitted) {
                    Button(
                        onClick = { viewModel.submitFillBlank() },
                        enabled = uiState.fillInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fill_blank_submit_btn")
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kiểm tra đáp án")
                    }
                }
            }
        }

        AnimatedVisibility(visible = submitted) {
            val isCorrect = uiState.fillSubmittedCorrect == true
            FeedbackContinueBar(
                isCorrect = isCorrect,
                message = if (isCorrect) {
                    "Chuẩn xác! \"${q.target.word}\" (${q.target.phonetic})"
                } else {
                    "Đáp án đúng là: \"${q.target.word}\" (${q.target.phonetic})"
                },
                onNext = { viewModel.nextFillBlank() }
            )
        }
    }
}

@Composable
private fun DictationGameView(
    uiState: GamesUiState,
    viewModel: GamesViewModel
) {
    if (uiState.dictationFinished) {
        GameCompletionCard(
            gameTitle = "Hoàn thành Nghe & Gõ chính tả!",
            score = uiState.dictationScore,
            maxScore = uiState.dictationWords.size * 10,
            onPlayAgain = { viewModel.restartDictation() }
        )
        return
    }

    val target = uiState.dictationWords.getOrNull(uiState.dictationIndex) ?: return
    val submitted = uiState.dictationSubmittedCorrect != null

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GameProgressHeader(
            currentRound = uiState.dictationIndex + 1,
            totalRounds = uiState.dictationWords.size,
            score = uiState.dictationScore
        )

        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Nghe phát âm và gõ lại đúng chính tả",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.playDictationAudio(slow = false) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dictation_play_normal_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nghe từ")
                    }
                    FilledTonalButton(
                        onClick = { viewModel.playDictationAudio(slow = true) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dictation_play_slow_btn")
                    ) {
                        Icon(Icons.Rounded.SlowMotionVideo, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nghe chậm")
                    }
                }

                TextButton(onClick = { viewModel.toggleDictationHint() }) {
                    Icon(Icons.Rounded.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (uiState.dictationShowHint) "Ẩn gợi ý nghĩa" else "Xem gợi ý nghĩa tiếng Việt")
                }

                AnimatedVisibility(visible = uiState.dictationShowHint) {
                    Surface(
                        color = AmberTertiaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${target.meaningVi} (${target.partOfSpeech}) • ${target.word.length} chữ cái",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberTertiary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.dictationInput,
                    onValueChange = { viewModel.updateDictationInput(it) },
                    label = { Text("Gõ từ bạn nghe được") },
                    singleLine = true,
                    enabled = !submitted,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.submitDictation() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dictation_input")
                )

                if (!submitted) {
                    Button(
                        onClick = { viewModel.submitDictation() },
                        enabled = uiState.dictationInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dictation_submit_btn")
                    ) {
                        Text("Kiểm tra chính tả")
                    }
                }
            }
        }

        AnimatedVisibility(visible = submitted) {
            val isCorrect = uiState.dictationSubmittedCorrect == true
            FeedbackContinueBar(
                isCorrect = isCorrect,
                message = if (isCorrect) {
                    "Chính xác 100%! ${target.word} — ${target.meaningVi}"
                } else {
                    "Từ đúng là: \"${target.word}\" (${target.phonetic}) — ${target.meaningVi}"
                },
                onNext = { viewModel.nextDictation() }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordScrambleGameView(
    uiState: GamesUiState,
    viewModel: GamesViewModel
) {
    if (uiState.scrambleFinished) {
        GameCompletionCard(
            gameTitle = "Hoàn thành Sắp xếp chữ cái!",
            score = uiState.scrambleScore,
            maxScore = uiState.scrambleWords.size * 10,
            onPlayAgain = { viewModel.restartScramble() }
        )
        return
    }

    val target = uiState.scrambleWords.getOrNull(uiState.scrambleIndex) ?: return
    val submitted = uiState.scrambleSubmittedCorrect != null

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GameProgressHeader(
            currentRound = uiState.scrambleIndex + 1,
            totalRounds = uiState.scrambleWords.size,
            score = uiState.scrambleScore
        )

        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = AmberTertiaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Nghĩa: ${target.meaningVi} (${target.partOfSpeech})",
                            style = MaterialTheme.typography.labelLarge,
                            color = AmberTertiary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.speakText(target.word) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Nghe phát âm gợi ý",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Answer slot box
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scramble_answer_zone")
                ) {
                    if (uiState.scramblePlacedTiles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chạm các chữ cái bên dưới để ghép thành từ đúng",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.scramblePlacedTiles.forEach { tile ->
                                LetterTileButton(
                                    char = tile.char,
                                    isPlaced = true,
                                    enabled = !submitted,
                                    onClick = { viewModel.removePlacedTile(tile) }
                                )
                            }
                        }
                    }
                }

                // Available scrambled tiles
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.scrambleAvailableTiles.forEach { tile ->
                        LetterTileButton(
                            char = tile.char,
                            isPlaced = false,
                            enabled = !submitted,
                            onClick = { viewModel.pickScrambleTile(tile) }
                        )
                    }
                }

                if (!submitted) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.reshuffleAvailableTiles() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Xáo trộn")
                        }
                        OutlinedButton(
                            onClick = { viewModel.hintOneScrambleLetter() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gợi ý")
                        }
                    }

                    Button(
                        onClick = { viewModel.submitScramble() },
                        enabled = uiState.scrambleAvailableTiles.isEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scramble_submit_btn")
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kiểm tra từ")
                    }
                }
            }
        }

        AnimatedVisibility(visible = submitted) {
            val isCorrect = uiState.scrambleSubmittedCorrect == true
            FeedbackContinueBar(
                isCorrect = isCorrect,
                message = if (isCorrect) {
                    "Tuyệt vời! \"${target.word}\" (${target.phonetic})"
                } else {
                    "Đáp án đúng là: \"${target.word}\" (${target.phonetic})"
                },
                onNext = { viewModel.nextScramble() }
            )
        }
    }
}

@Composable
private fun LetterTileButton(
    char: Char,
    isPlaced: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = if (isPlaced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (isPlaced) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isPlaced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        shadowElevation = if (isPlaced) 2.dp else 1.dp,
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = char.uppercaseChar().toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun GameProgressHeader(
    currentRound: Int,
    totalRounds: Int,
    score: Int,
    extraBadge: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Câu $currentRound / $totalRounds",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (extraBadge != null) {
                Surface(
                    color = AmberTertiaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = extraBadge,
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberTertiary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Điểm: $score",
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun FeedbackContinueBar(
    isCorrect: Boolean,
    message: String,
    onNext: () -> Unit
) {
    val bg = if (isCorrect) CorrectGreenBg else WrongRedBg
    val fg = if (isCorrect) CorrectGreen else WrongRed
    Surface(
        color = bg,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.5.dp, fg.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_feedback_bar")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = fg,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.testTag("game_next_btn")
            ) {
                Text("Tiếp")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun GameCompletionCard(
    gameTitle: String,
    score: Int,
    maxScore: Int,
    onPlayAgain: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_completion_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = AmberTertiaryContainer,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = AmberTertiary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Text(
                text = gameTitle,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = "$score / $maxScore điểm",
                style = MaterialTheme.typography.displaySmall.copy(fontFamily = JetBrainsMonoFamily),
                color = MaterialTheme.colorScheme.primary
            )
            val feedback = when {
                maxScore > 0 && score * 100 / maxScore >= 80 -> "Xuất sắc! Bạn ghi nhớ từ vựng cực kỳ chắc chắn."
                maxScore > 0 && score * 100 / maxScore >= 50 -> "Khá tốt! Ôn thêm một lượt nữa để đạt điểm tuyệt đối nhé."
                else -> "Cố lên nhé! Hãy lật lại Flashcard ở Tab 2 rồi thử sức lại."
            }
            Text(
                text = feedback,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onPlayAgain,
                modifier = Modifier.testTag("game_play_again_btn")
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Chơi lượt mới")
            }
        }
    }
}

@Composable
private fun NotEnoughWordsCard(requiredCount: Int, currentCount: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Cần tối thiểu $requiredCount từ vựng (hiện có $currentCount)",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Vui lòng thêm từ mới ở Tab Sổ tay & SRS để mở khóa trò chơi này.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
