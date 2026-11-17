package com.example.ui.speaking

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.audio.PronunciationResult
import com.example.audio.SpeechRecognitionState
import com.example.audio.WordMatchStatus
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.CorrectGreenBg
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NearAmber
import com.example.ui.theme.NearAmberBg
import com.example.ui.theme.WrongRed
import com.example.ui.theme.WrongRedBg
import com.example.viewmodel.SpeakingMode
import com.example.viewmodel.SpeakingUiState
import com.example.viewmodel.SpeakingViewModel

@Composable
fun SpeakingScreen(
    viewModel: SpeakingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showPermissionNotice by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showPermissionNotice = false
            pendingTarget?.let { (id, sentence) ->
                viewModel.startRecordingForTarget(id, sentence)
            }
        } else {
            showPermissionNotice = true
        }
        pendingTarget = null
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.loadDocumentFromUri(it) }
    }

    fun requestMicAndStart(targetId: String, sentence: String) {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            showPermissionNotice = false
            viewModel.startRecordingForTarget(targetId, sentence)
        } else {
            pendingTarget = targetId to sentence
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SpeakingHeroHeader()
            }

            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speaking_mode_switcher")
                ) {
                    SegmentedButton(
                        selected = uiState.mode == SpeakingMode.TOPICS,
                        onClick = { viewModel.selectMode(SpeakingMode.TOPICS) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("mode_topics_btn")
                    ) {
                        Text("Chủ đề 15p đầu giờ")
                    }
                    SegmentedButton(
                        selected = uiState.mode == SpeakingMode.CUSTOM_DOCUMENT,
                        onClick = { viewModel.selectMode(SpeakingMode.CUSTOM_DOCUMENT) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Description,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("mode_custom_doc_btn")
                    ) {
                        Text("Tài liệu tự do")
                    }
                }
            }

            if (showPermissionNotice) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.mic_permission_needed),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            FilledTonalButton(
                                onClick = {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            ) {
                                Text(stringResource(R.string.mic_permission_grant))
                            }
                        }
                    }
                }
            }

            if (uiState.mode == SpeakingMode.TOPICS) {
                item {
                    TopicSelectorSection(
                        uiState = uiState,
                        onSelectTopic = { viewModel.selectTopic(it) },
                        onSelectPrompt = { viewModel.selectPrompt(it) }
                    )
                }

                item {
                    val prompt = uiState.selectedPrompt
                    TopicPracticeCard(
                        prompt = prompt,
                        speechState = uiState.speechState,
                        isActiveTarget = uiState.activeTargetId == prompt.id,
                        evaluation = uiState.resultsByTargetId[prompt.id],
                        onListenNormal = { viewModel.speakText(prompt.sampleAnswerEn, slow = false) },
                        onListenSlow = { viewModel.speakText(prompt.sampleAnswerEn, slow = true) },
                        onListenQuestion = { viewModel.speakText(prompt.questionEn, slow = false) },
                        onToggleMic = { requestMicAndStart(prompt.id, prompt.sampleAnswerEn) },
                        onClearResult = { viewModel.clearResultForTarget(prompt.id) }
                    )
                }
            } else {
                item {
                    CustomPassageInputCard(
                        rawText = uiState.customRawText,
                        errorText = uiState.documentLoadError,
                        onTextChange = { viewModel.updateCustomRawText(it) },
                        onSplitClick = { viewModel.splitCustomPassage() },
                        onUploadClick = {
                            documentPickerLauncher.launch(arrayOf("text/plain", "text/*"))
                        }
                    )
                }

                item {
                    Text(
                        text = "Danh sách câu luyện đọc (${uiState.customSentences.size} câu)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                itemsIndexed(
                    items = uiState.customSentences,
                    key = { _, item -> item.id }
                ) { idx, item ->
                    CustomSentencePracticeCard(
                        index = idx + 1,
                        sentence = item.text,
                        speechState = uiState.speechState,
                        isActiveTarget = uiState.activeTargetId == item.id,
                        evaluation = uiState.resultsByTargetId[item.id],
                        onListen = { viewModel.speakText(item.text, slow = false) },
                        onListenSlow = { viewModel.speakText(item.text, slow = true) },
                        onToggleMic = { requestMicAndStart(item.id, item.text) },
                        onClearResult = { viewModel.clearResultForTarget(item.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SpeakingHeroHeader() {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_speaking_hero),
                contentDescription = stringResource(R.string.speaking_hero_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .clip(MaterialTheme.shapes.large)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xD91E3A8A),
                                Color(0x991E40AF),
                                Color(0x331E40AF)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = Color(0x33FFFFFF),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "AI Pronunciation Coach",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "Luyện nói chuẩn & Tự tin",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Text(
                    text = "Nghe phát âm bản xứ, đọc theo và nhận chấm điểm từng từ trên thang 100.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE0E7FF)
                )
            }
        }
    }
}

@Composable
private fun TopicSelectorSection(
    uiState: SpeakingUiState,
    onSelectTopic: (String) -> Unit,
    onSelectPrompt: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Chọn chủ đề giao tiếp",
            style = MaterialTheme.typography.titleMedium
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.topics, key = { it.id }) { topic ->
                val selected = topic.id == uiState.selectedTopicId
                FilterChip(
                    selected = selected,
                    onClick = { onSelectTopic(topic.id) },
                    label = { Text(topic.titleVi) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("topic_chip_${topic.id}")
                )
            }
        }

        val topic = uiState.selectedTopic
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${topic.titleEn} • ${topic.subtitleVi}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(topic.prompts, key = { _, p -> p.id }) { index, prompt ->
                val isSelected = prompt.id == uiState.selectedPromptId
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectPrompt(prompt.id) },
                    label = { Text("Câu ${index + 1}") },
                    modifier = Modifier.testTag("prompt_chip_${prompt.id}")
                )
            }
        }
    }
}

@Composable
private fun TopicPracticeCard(
    prompt: com.example.data.speaking.SpeakingPromptItem,
    speechState: SpeechRecognitionState,
    isActiveTarget: Boolean,
    evaluation: PronunciationResult?,
    onListenNormal: () -> Unit,
    onListenSlow: () -> Unit,
    onListenQuestion: () -> Unit,
    onToggleMic: () -> Unit,
    onClearResult: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Question Box
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CÂU HỎI GỢI Ý",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        AssistChip(
                            onClick = onListenQuestion,
                            label = { Text("Nghe câu hỏi") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = "Nghe câu hỏi",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("listen_question_btn")
                        )
                    }
                    Text(
                        text = prompt.questionEn,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = prompt.questionVi,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Sample Answer Section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "CÂU TRẢ LỜI MẪU",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = prompt.sampleAnswerEn,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = prompt.sampleAnswerIpa,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Nghĩa: ${prompt.sampleAnswerVi}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Audio & Mic Controls
            PronunciationActionBar(
                isListening = isActiveTarget && speechState is SpeechRecognitionState.Listening,
                isProcessing = isActiveTarget && speechState is SpeechRecognitionState.Processing,
                onListenNormal = onListenNormal,
                onListenSlow = onListenSlow,
                onToggleMic = onToggleMic
            )

            if (isActiveTarget) {
                SpeechStatusBanner(speechState = speechState)
            }

            AnimatedVisibility(visible = evaluation != null) {
                evaluation?.let {
                    PronunciationResultCard(
                        result = it,
                        onRetry = onClearResult
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomPassageInputCard(
    rawText: String,
    errorText: String?,
    onTextChange: (String) -> Unit,
    onSplitClick: () -> Unit,
    onUploadClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Dán đoạn văn hoặc tải tệp (.txt)",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Hệ thống sẽ tự động tách thành từng câu ngắn để bạn nghe mẫu và chấm điểm phát âm từng câu.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = rawText,
                onValueChange = onTextChange,
                label = { Text("Đoạn văn bản tiếng Anh") },
                placeholder = { Text("Paste an English paragraph here...") },
                minLines = 4,
                maxLines = 7,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_passage_input")
            )
            if (errorText != null) {
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onUploadClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("upload_doc_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.UploadFile,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tải tệp .txt")
                }
                Button(
                    onClick = onSplitClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("split_sentences_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tách câu luyện đọc")
                }
            }
        }
    }
}

@Composable
private fun CustomSentencePracticeCard(
    index: Int,
    sentence: String,
    speechState: SpeechRecognitionState,
    isActiveTarget: Boolean,
    evaluation: PronunciationResult?,
    onListen: () -> Unit,
    onListenSlow: () -> Unit,
    onToggleMic: () -> Unit,
    onClearResult: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = sentence,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.weight(1f)
                )
            }

            PronunciationActionBar(
                isListening = isActiveTarget && speechState is SpeechRecognitionState.Listening,
                isProcessing = isActiveTarget && speechState is SpeechRecognitionState.Processing,
                onListenNormal = onListen,
                onListenSlow = onListenSlow,
                onToggleMic = onToggleMic
            )

            if (isActiveTarget) {
                SpeechStatusBanner(speechState = speechState)
            }

            AnimatedVisibility(visible = evaluation != null) {
                evaluation?.let {
                    PronunciationResultCard(result = it, onRetry = onClearResult)
                }
            }
        }
    }
}

@Composable
private fun PronunciationActionBar(
    isListening: Boolean,
    isProcessing: Boolean,
    onListenNormal: () -> Unit,
    onListenSlow: () -> Unit,
    onToggleMic: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val micBg by animateColorAsState(
        targetValue = if (isListening) WrongRed else MaterialTheme.colorScheme.primary,
        label = "micBg"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = onListenNormal,
            modifier = Modifier.testTag("tts_normal_btn")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = "Nghe mẫu",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Nghe mẫu")
        }

        OutlinedButton(
            onClick = onListenSlow,
            modifier = Modifier.testTag("tts_slow_btn")
        ) {
            Icon(
                imageVector = Icons.Rounded.SlowMotionVideo,
                contentDescription = "Nghe chậm",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Chậm")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onToggleMic,
            enabled = !isProcessing,
            colors = ButtonDefaults.buttonColors(containerColor = micBg),
            modifier = Modifier
                .scale(pulseScale)
                .testTag("mic_record_btn")
        ) {
            Icon(
                imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                contentDescription = if (isListening) "Dừng ghi âm" else "Bắt đầu đọc",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                when {
                    isListening -> "Đang nghe..."
                    isProcessing -> "Đang chấm..."
                    else -> "Đọc & Chấm"
                }
            )
        }
    }
}

@Composable
private fun SpeechStatusBanner(speechState: SpeechRecognitionState) {
    when (speechState) {
        is SpeechRecognitionState.Listening -> {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Micro đang thu âm tiếng Anh...",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (speechState.partialText.isNotBlank()) {
                            Text(
                                text = "\"${speechState.partialText}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (speechState.rmsDb / 10f).coerceIn(0.1f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }
        is SpeechRecognitionState.Processing -> {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        is SpeechRecognitionState.Error -> {
            Surface(
                color = WrongRedBg,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = speechState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = WrongRed,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        else -> Unit
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PronunciationResultCard(
    result: PronunciationResult,
    onRetry: () -> Unit
) {
    val scoreColor = when {
        result.score >= 80 -> CorrectGreen
        result.score >= 55 -> NearAmber
        else -> WrongRed
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pronunciation_result_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.feedbackTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = scoreColor
                    )
                    Text(
                        text = "Bạn đã đọc: \"${result.spokenTranscript}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = scoreColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.5.dp, scoreColor)
                ) {
                    Text(
                        text = "${result.score}/100",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = scoreColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Word-by-word green/red highlights
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                result.wordEvaluations.forEach { eval ->
                    val (bg, fg) = when (eval.status) {
                        WordMatchStatus.CORRECT -> CorrectGreenBg to CorrectGreen
                        WordMatchStatus.NEAR_MATCH -> NearAmberBg to NearAmber
                        WordMatchStatus.MISSED_OR_WRONG -> WrongRedBg to WrongRed
                    }
                    Surface(
                        color = bg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = eval.displayWord,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = fg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = result.feedbackDetail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xanh: Đúng • Vàng: Gần đúng • Đỏ: Sai/Thiếu",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AssistChip(
                    onClick = onRetry,
                    label = { Text("Xóa kết quả") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}
