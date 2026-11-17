package com.example.ui.vocab

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.VocabEntity
import com.example.data.repository.SrsRating
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.AmberTertiaryContainer
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.CorrectGreenBg
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NearAmber
import com.example.ui.theme.NearAmberBg
import com.example.ui.theme.WrongRed
import com.example.ui.theme.WrongRedBg
import com.example.viewmodel.VocabFilter
import com.example.viewmodel.VocabTabSection
import com.example.viewmodel.VocabUiState
import com.example.viewmodel.VocabViewModel

@Composable
fun VocabScreen(
    viewModel: VocabViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var wordPendingDelete by remember { mutableStateOf<VocabEntity?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SrsStatsBanner(uiState = uiState)
            }

            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = uiState.section == VocabTabSection.FLASHCARD_SRS,
                        onClick = { viewModel.selectSection(VocabTabSection.FLASHCARD_SRS) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Style,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("vocab_tab_flashcard")
                    ) {
                        Text("Flashcard SRS (${uiState.dueWords.size})")
                    }
                    SegmentedButton(
                        selected = uiState.section == VocabTabSection.NOTEBOOK,
                        onClick = { viewModel.selectSection(VocabTabSection.NOTEBOOK) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Book,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("vocab_tab_notebook")
                    ) {
                        Text("Sổ tay (${uiState.allWords.size})")
                    }
                }
            }

            if (uiState.section == VocabTabSection.FLASHCARD_SRS) {
                item {
                    FlashcardReviewSection(
                        uiState = uiState,
                        onFlip = { viewModel.flipCard() },
                        onSpeak = { text -> viewModel.speakWord(text) },
                        onRate = { rating -> viewModel.rateCurrentCard(rating) },
                        onTogglePracticeAll = { enabled -> viewModel.togglePracticeAllMode(enabled) }
                    )
                }
            } else {
                item {
                    NotebookSearchAndFilterBar(
                        searchQuery = uiState.searchQuery,
                        selectedFilter = uiState.filter,
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        onFilterSelect = { viewModel.selectFilter(it) },
                        onAddClick = { viewModel.openAddWordDialog() }
                    )
                }

                if (uiState.filteredWords.isEmpty()) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Chưa tìm thấy từ vựng phù hợp",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Thử từ khóa khác hoặc bấm \"Thêm từ mới\" để lưu vào Sổ tay.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.filteredWords, key = { it.id }) { vocab ->
                        VocabListItemCard(
                            vocab = vocab,
                            onSpeak = { viewModel.speakWord(vocab.word) },
                            onEdit = { viewModel.openEditWordDialog(vocab) },
                            onDelete = { wordPendingDelete = vocab }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        if (uiState.section == VocabTabSection.NOTEBOOK) {
            FloatingActionButton(
                onClick = { viewModel.openAddWordDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_vocab_fab")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Thêm từ vựng mới"
                )
            }
        }
    }

    if (uiState.isAddEditSheetOpen) {
        AddEditVocabDialog(
            editingWord = uiState.editingWord,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { id, word, pos, phonetic, meaning, example ->
                viewModel.saveWord(id, word, pos, phonetic, meaning, example)
            }
        )
    }

    wordPendingDelete?.let { wordToDelete ->
        AlertDialog(
            onDismissRequest = { wordPendingDelete = null },
            title = { Text("Xóa từ vựng?") },
            text = { Text("Bạn có chắc muốn xóa từ \"${wordToDelete.word}\" khỏi Sổ tay không?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWord(wordToDelete.id)
                        wordPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { wordPendingDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun SrsStatsBanner(uiState: VocabUiState) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(
                label = "Cần ôn hôm nay",
                value = "${uiState.dueWords.size}",
                containerColor = AmberTertiaryContainer,
                accentColor = AmberTertiary,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = "Đã thuộc",
                value = "${uiState.masteredCount}",
                containerColor = CorrectGreenBg,
                accentColor = CorrectGreen,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = "Đã ôn phiên này",
                value = "${uiState.reviewedTodayCount}",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    containerColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FlashcardReviewSection(
    uiState: VocabUiState,
    onFlip: () -> Unit,
    onSpeak: (String) -> Unit,
    onRate: (SrsRating) -> Unit,
    onTogglePracticeAll: (Boolean) -> Unit
) {
    val card = uiState.currentFlashcard
    if (card == null) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_srs_complete),
                    contentDescription = stringResource(R.string.srs_complete_desc),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
                Text(
                    text = "Tuyệt vời! Bạn đã ôn hết từ cần nhớ hôm nay",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Thuật toán Lặp lại ngắt quãng (SRS) sẽ tự động nhắc lại các từ đúng thời điểm trước khi bạn quên.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (uiState.allWords.isNotEmpty()) {
                    Button(
                        onClick = { onTogglePracticeAll(true) },
                        modifier = Modifier.testTag("practice_all_cards_btn")
                    ) {
                        Text("Ôn lại toàn bộ ${uiState.allWords.size} từ")
                    }
                }
            }
        }
        return
    }

    val rotation by animateFloatAsState(
        targetValue = if (uiState.isCardFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 360),
        label = "flashcardFlip"
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (uiState.practiceAllMode) {
                    "Ôn tự do • Thẻ ${uiState.currentCardIndex + 1}/${uiState.activeDeck.size}"
                } else {
                    "Cần ôn hôm nay: còn ${uiState.dueWords.size} từ"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            if (uiState.practiceAllMode) {
                TextButton(onClick = { onTogglePracticeAll(false) }) {
                    Text("Quay lại lịch SRS")
                }
            }
        }

        // Interactive 3D Flashcard
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = if (rotation <= 90f) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                }
            ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable(onClick = onFlip)
                .testTag("srs_flashcard")
        ) {
            if (rotation <= 90f) {
                // Front side: Word + POS + Phonetic + TTS button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = card.partOfSpeech,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = card.intervalLabel,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = card.word,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        if (card.phonetic.isNotBlank()) {
                            Text(
                                text = card.phonetic,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = JetBrainsMonoFamily
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { onSpeak(card.word) },
                            modifier = Modifier.testTag("flashcard_speak_word_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Nghe phát âm",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nghe phát âm")
                        }
                    }

                    Row(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flip,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Chạm vào thẻ để xem nghĩa & ví dụ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Back side (mirrored back upright)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                        .padding(22.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = card.word,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = card.meaningVi,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        if (card.exampleSentence.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "\"${card.exampleSentence}\"",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontStyle = FontStyle.Italic
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    TextButton(
                                        onClick = { onSpeak(card.exampleSentence) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                            contentDescription = "Nghe câu ví dụ",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Nghe câu ví dụ")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3 SRS Rating Buttons
        Text(
            text = "Đánh giá mức độ ghi nhớ (SRS):",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SrsRatingButton(
                title = "Quên / Khó",
                intervalHint = "1 phút",
                containerColor = WrongRedBg,
                contentColor = WrongRed,
                onClick = { onRate(SrsRating.FORGOT) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("srs_rate_forgot")
            )
            SrsRatingButton(
                title = "Mang máng",
                intervalHint = "1 ngày",
                containerColor = NearAmberBg,
                contentColor = NearAmber,
                onClick = { onRate(SrsRating.VAGUE) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("srs_rate_vague")
            )
            SrsRatingButton(
                title = "Thuộc lòng",
                intervalHint = "3-7 ngày",
                containerColor = CorrectGreenBg,
                contentColor = CorrectGreen,
                onClick = { onRate(SrsRating.MASTERED) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("srs_rate_mastered")
            )
        }
    }
}

@Composable
private fun SrsRatingButton(
    title: String,
    intervalHint: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = intervalHint,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun NotebookSearchAndFilterBar(
    searchQuery: String,
    selectedFilter: VocabFilter,
    onSearchChange: (String) -> Unit,
    onFilterSelect: (VocabFilter) -> Unit,
    onAddClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Tìm từ tiếng Anh, nghĩa tiếng Việt...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Xóa tìm kiếm")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vocab_search_input")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == VocabFilter.ALL,
                        onClick = { onFilterSelect(VocabFilter.ALL) },
                        label = { Text("Tất cả") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == VocabFilter.DUE,
                        onClick = { onFilterSelect(VocabFilter.DUE) },
                        label = { Text("Cần ôn") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == VocabFilter.MASTERED,
                        onClick = { onFilterSelect(VocabFilter.MASTERED) },
                        label = { Text("Đã thuộc") }
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onAddClick,
                modifier = Modifier.testTag("add_word_inline_btn")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Thêm từ")
            }
        }
    }
}

@Composable
private fun VocabListItemCard(
    vocab: VocabEntity,
    onSpeak: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDue = vocab.nextReviewAt <= System.currentTimeMillis()
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vocab_card_${vocab.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = vocab.word,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = vocab.partOfSpeech,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (vocab.phonetic.isNotBlank()) {
                        Text(
                            text = vocab.phonetic,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(onClick = onSpeak) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Nghe từ ${vocab.word}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Chỉnh sửa ${vocab.word}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Xóa ${vocab.word}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = vocab.meaningVi,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            if (vocab.exampleSentence.isNotBlank()) {
                Text(
                    text = "Ví dụ: ${vocab.exampleSentence}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isDue) Icons.Rounded.Schedule else Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (isDue) AmberTertiary else CorrectGreen
                )
                Text(
                    text = if (isDue) "Sẵn sàng ôn tập (${vocab.intervalLabel})" else "Hẹn ôn lại sau ${vocab.intervalLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDue) AmberTertiary else CorrectGreen
                )
            }
        }
    }
}

@Composable
private fun AddEditVocabDialog(
    editingWord: VocabEntity?,
    onDismiss: () -> Unit,
    onSave: (id: Long, word: String, pos: String, phonetic: String, meaning: String, example: String) -> Unit
) {
    var word by remember(editingWord) { mutableStateOf(editingWord?.word.orEmpty()) }
    var pos by remember(editingWord) { mutableStateOf(editingWord?.partOfSpeech ?: "noun") }
    var phonetic by remember(editingWord) { mutableStateOf(editingWord?.phonetic.orEmpty()) }
    var meaning by remember(editingWord) { mutableStateOf(editingWord?.meaningVi.orEmpty()) }
    var example by remember(editingWord) { mutableStateOf(editingWord?.exampleSentence.orEmpty()) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val posOptions = listOf("noun", "verb", "adj", "adv", "phrase")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (editingWord == null) "Thêm từ vựng mới" else "Chỉnh sửa từ vựng")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it; validationError = null },
                    label = { Text("Từ tiếng Anh *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_word_input")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(posOptions) { option ->
                        FilterChip(
                            selected = pos == option,
                            onClick = { pos = option },
                            label = { Text(option) }
                        )
                    }
                }

                OutlinedTextField(
                    value = phonetic,
                    onValueChange = { phonetic = it },
                    label = { Text("Phiên âm IPA (VD: /ˈstʌdi/)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = meaning,
                    onValueChange = { meaning = it; validationError = null },
                    label = { Text("Nghĩa tiếng Việt *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_meaning_input")
                )

                OutlinedTextField(
                    value = example,
                    onValueChange = { example = it },
                    label = { Text("Câu ví dụ tiếng Anh") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (word.isBlank() || meaning.isBlank()) {
                        validationError = "Vui lòng nhập đầy đủ Từ tiếng Anh và Nghĩa tiếng Việt."
                    } else {
                        onSave(editingWord?.id ?: 0L, word, pos, phonetic, meaning, example)
                    }
                },
                modifier = Modifier.testTag("dialog_save_word_btn")
            ) {
                Text("Lưu từ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
