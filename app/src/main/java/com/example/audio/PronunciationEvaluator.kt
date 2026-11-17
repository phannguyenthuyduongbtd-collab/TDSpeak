package com.example.audio

import kotlin.math.max
import kotlin.math.roundToInt

enum class WordMatchStatus {
    CORRECT,
    NEAR_MATCH,
    MISSED_OR_WRONG
}

data class WordEvaluation(
    val displayWord: String,
    val normalizedWord: String,
    val status: WordMatchStatus
)

data class PronunciationResult(
    val targetSentence: String,
    val spokenTranscript: String,
    val score: Int,
    val wordEvaluations: List<WordEvaluation>,
    val feedbackTitle: String,
    val feedbackDetail: String,
    val missedWords: List<String>
)

object PronunciationEvaluator {

    private val contractionMap = mapOf(
        "im" to listOf("i", "am"),
        "youre" to listOf("you", "are"),
        "hes" to listOf("he", "is"),
        "shes" to listOf("she", "is"),
        "its" to listOf("it", "is"),
        "were" to listOf("we", "are"),
        "theyre" to listOf("they", "are"),
        "dont" to listOf("do", "not"),
        "doesnt" to listOf("does", "not"),
        "didnt" to listOf("did", "not"),
        "cant" to listOf("can", "not"),
        "cannot" to listOf("can", "not"),
        "wont" to listOf("will", "not"),
        "isnt" to listOf("is", "not"),
        "arent" to listOf("are", "not"),
        "ive" to listOf("i", "have"),
        "ill" to listOf("i", "will"),
        "id" to listOf("i", "would")
    )

    fun evaluate(targetSentence: String, spokenTranscript: String): PronunciationResult {
        val rawTargetTokens = targetSentence
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        if (rawTargetTokens.isEmpty()) {
            return PronunciationResult(
                targetSentence = targetSentence,
                spokenTranscript = spokenTranscript,
                score = 0,
                wordEvaluations = emptyList(),
                feedbackTitle = "Chưa có câu mẫu",
                feedbackDetail = "Vui lòng chọn hoặc nhập một câu tiếng Anh để bắt đầu luyện tập.",
                missedWords = emptyList()
            )
        }

        val normalizedTargets = rawTargetTokens.map { normalizeWord(it) }
        val spokenNormalized = expandContractions(
            spokenTranscript
                .trim()
                .split(Regex("\\s+"))
                .map { normalizeWord(it) }
                .filter { it.isNotEmpty() }
        )
        val expandedTargets = normalizedTargets.flatMap { contractionMap[it] ?: listOf(it) }

        // LCS alignment on target tokens vs spoken tokens
        val matchedTargetIndices = lcsMatchedIndices(normalizedTargets, spokenNormalized)

        val evaluations = rawTargetTokens.mapIndexed { idx, rawWord ->
            val norm = normalizedTargets[idx]
            val status = when {
                idx in matchedTargetIndices -> WordMatchStatus.CORRECT
                spokenNormalized.any { spoken -> isNearMatch(norm, spoken) } -> WordMatchStatus.NEAR_MATCH
                else -> WordMatchStatus.MISSED_OR_WRONG
            }
            WordEvaluation(
                displayWord = rawWord,
                normalizedWord = norm,
                status = status
            )
        }

        val correctCount = evaluations.count { it.status == WordMatchStatus.CORRECT }
        val nearCount = evaluations.count { it.status == WordMatchStatus.NEAR_MATCH }
        val totalWords = evaluations.size.coerceAtLeast(1)

        // Score out of 100: exact matches = 1.0, near matches = 0.6, slight penalty for extreme extra filler
        val rawAccuracy = ((correctCount + nearCount * 0.6) / totalWords) * 100.0
        val extraWordCount = max(0, spokenNormalized.size - expandedTargets.size - 2)
        val extraPenalty = extraWordCount * 3.0
        val finalScore = (rawAccuracy - extraPenalty).roundToInt().coerceIn(0, 100)

        val missedWords = evaluations
            .filter { it.status == WordMatchStatus.MISSED_OR_WRONG }
            .map { it.displayWord.trim(',', '.', '!', '?', ';', ':') }

        val nearWords = evaluations
            .filter { it.status == WordMatchStatus.NEAR_MATCH }
            .map { it.displayWord.trim(',', '.', '!', '?', ';', ':') }

        val (title, detail) = buildFeedback(finalScore, correctCount, totalWords, missedWords, nearWords)

        return PronunciationResult(
            targetSentence = targetSentence,
            spokenTranscript = spokenTranscript,
            score = finalScore,
            wordEvaluations = evaluations,
            feedbackTitle = title,
            feedbackDetail = detail,
            missedWords = missedWords
        )
    }

    private fun buildFeedback(
        score: Int,
        correctCount: Int,
        totalWords: Int,
        missedWords: List<String>,
        nearWords: List<String>
    ): Pair<String, String> {
        val title = when {
            score >= 90 -> "Phát âm xuất sắc!"
            score >= 75 -> "Rất tốt, tiếp tục phát huy!"
            score >= 55 -> "Khá ổn, cố gắng thêm chút nữa!"
            else -> "Đừng nản nhé, hãy luyện chậm từng cụm!"
        }

        val sb = StringBuilder()
        sb.append("Bạn đã đọc chuẩn $correctCount/$totalWords từ. ")

        if (score >= 90 && missedWords.isEmpty()) {
            sb.append("Nhịp điệu và độ rõ âm của bạn rất tự nhiên, gần như chuẩn bản xứ!")
        } else {
            if (nearWords.isNotEmpty()) {
                sb.append("Các từ phát âm gần đúng (cần rõ âm cuối hơn): ${nearWords.take(4).joinToString(", ")}. ")
            }
            if (missedWords.isNotEmpty()) {
                sb.append("Hãy bấm nút Nghe mẫu và chú ý nhấn rõ các từ màu đỏ: ${missedWords.take(5).joinToString(", ")}.")
            }
        }
        return title to sb.toString().trim()
    }

    private fun normalizeWord(word: String): String {
        return word.lowercase().replace(Regex("[^a-z0-9]"), "")
    }

    private fun expandContractions(tokens: List<String>): List<String> {
        return tokens.flatMap { contractionMap[it] ?: listOf(it) }
    }

    private fun lcsMatchedIndices(target: List<String>, spoken: List<String>): Set<Int> {
        val m = target.size
        val n = spoken.size
        if (m == 0 || n == 0) return emptySet()

        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (isWordMatch(target[i - 1], spoken[j - 1])) {
                    dp[i - 1][j - 1] + 1
                } else {
                    max(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }

        val matched = mutableSetOf<Int>()
        var i = m
        var j = n
        while (i > 0 && j > 0) {
            if (isWordMatch(target[i - 1], spoken[j - 1])) {
                matched.add(i - 1)
                i--
                j--
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--
            } else {
                j--
            }
        }
        return matched
    }

    private fun isWordMatch(target: String, spoken: String): Boolean {
        if (target.isEmpty() || spoken.isEmpty()) return false
        if (target == spoken) return true
        val expanded = contractionMap[target]
        if (expanded != null && expanded.contains(spoken)) return true
        return false
    }

    private fun isNearMatch(a: String, b: String): Boolean {
        if (a.length < 3 || b.length < 3) return false
        val dist = levenshtein(a, b)
        val maxLen = max(a.length, b.length)
        return dist <= 1 || (maxLen >= 6 && dist <= 2)
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[a.length][b.length]
    }
}

object SentenceSplitter {
    private val abbreviations = setOf("mr.", "mrs.", "ms.", "dr.", "prof.", "e.g.", "i.e.", "etc.", "u.s.", "u.k.")

    fun splitIntoShortSentences(rawText: String): List<String> {
        val cleaned = rawText
            .replace("\r\n", "\n")
            .replace(Regex("[\\t ]+"), " ")
            .trim()
        if (cleaned.isEmpty()) return emptyList()

        val sentences = mutableListOf<String>()
        val paragraphs = cleaned.split(Regex("\\n+"))

        for (para in paragraphs) {
            val trimmedPara = para.trim()
            if (trimmedPara.isEmpty()) continue

            val current = StringBuilder()
            val tokens = trimmedPara.split(" ")
            for (token in tokens) {
                if (current.isNotEmpty()) current.append(" ")
                current.append(token)

                val lower = token.lowercase()
                val endsWithTerminal = token.endsWith(".") || token.endsWith("!") || token.endsWith("?")
                val isAbbr = abbreviations.any { lower.endsWith(it) }

                if (endsWithTerminal && !isAbbr) {
                    addChunkedSentence(current.toString().trim(), sentences)
                    current.clear()
                }
            }
            if (current.isNotEmpty()) {
                addChunkedSentence(current.toString().trim(), sentences)
            }
        }

        return sentences.filter { it.any { ch -> ch.isLetter() } }
    }

    private fun addChunkedSentence(sentence: String, out: MutableList<String>) {
        val words = sentence.split(Regex("\\s+"))
        if (words.size <= 22) {
            out.add(sentence)
            return
        }
        // Split very long sentences cleanly at ';' or ',' for 1-breath speaking practice
        val clauses = sentence.split(Regex("(?<=[;,])\\s+"))
        val buffer = StringBuilder()
        for (clause in clauses) {
            val candidate = if (buffer.isEmpty()) clause else "$buffer $clause"
            if (candidate.split(Regex("\\s+")).size > 18 && buffer.isNotEmpty()) {
                out.add(buffer.toString().trim())
                buffer.clear()
                buffer.append(clause)
            } else {
                if (buffer.isNotEmpty()) buffer.append(" ")
                buffer.append(clause)
            }
        }
        if (buffer.isNotEmpty()) {
            out.add(buffer.toString().trim())
        }
    }
}
