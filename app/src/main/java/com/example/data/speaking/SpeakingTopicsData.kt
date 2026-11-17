package com.example.data.speaking

data class SpeakingPromptItem(
    val id: String,
    val questionEn: String,
    val questionVi: String,
    val sampleAnswerEn: String,
    val sampleAnswerIpa: String,
    val sampleAnswerVi: String
)

data class SpeakingTopic(
    val id: String,
    val titleVi: String,
    val titleEn: String,
    val subtitleVi: String,
    val badge: String,
    val prompts: List<SpeakingPromptItem>
)

object SpeakingTopicsData {
    val topics: List<SpeakingTopic> = listOf(
        SpeakingTopic(
            id = "intro",
            titleVi = "Giới thiệu bản thân",
            titleEn = "Self-Introduction",
            subtitleVi = "15 phút đầu giờ • Làm quen & mục tiêu",
            badge = "Cơ bản",
            prompts = listOf(
                SpeakingPromptItem(
                    id = "intro_1",
                    questionEn = "Can you introduce yourself and tell us what grade you are in?",
                    questionVi = "Bạn có thể giới thiệu bản thân và cho biết bạn đang học lớp mấy không?",
                    sampleAnswerEn = "Hello everyone, my name is Minh and I am a high school student in grade ten.",
                    sampleAnswerIpa = "/həˈləʊ ˈevriwʌn, maɪ neɪm ɪz mɪn ænd aɪ æm ə ˈhaɪ skuːl ˈstjuːdnt ɪn ɡreɪd ten/",
                    sampleAnswerVi = "Xin chào mọi người, mình tên là Minh và mình là học sinh trung học phổ thông lớp 10."
                ),
                SpeakingPromptItem(
                    id = "intro_2",
                    questionEn = "What are your biggest strengths as a student?",
                    questionVi = "Điểm mạnh lớn nhất của bạn khi là một học sinh là gì?",
                    sampleAnswerEn = "I am hardworking and always willing to help my classmates with group projects.",
                    sampleAnswerIpa = "/aɪ æm ˈhɑːdwɜːkɪŋ ænd ˈɔːlweɪz ˈwɪlɪŋ tuː help maɪ ˈklɑːsmeɪts wɪð ɡruːp ˈprɒdʒekts/",
                    sampleAnswerVi = "Mình chăm chỉ và luôn sẵn sàng giúp đỡ các bạn cùng lớp trong các dự án nhóm."
                ),
                SpeakingPromptItem(
                    id = "intro_3",
                    questionEn = "Why do you want to improve your English speaking skills?",
                    questionVi = "Tại sao bạn muốn cải thiện kỹ năng nói tiếng Anh?",
                    sampleAnswerEn = "Speaking English fluently opens great opportunities to study abroad and connect with the world.",
                    sampleAnswerIpa = "/ˈspiːkɪŋ ˈɪŋɡlɪʃ ˈfluːəntli ˈəʊpənz ɡreɪt ˌɒpəˈtjuːnətiz tuː ˈstʌdi əˈbrɔːd ænd kəˈnekt wɪð ðə wɜːld/",
                    sampleAnswerVi = "Nói tiếng Anh trôi chảy mở ra nhiều cơ hội tuyệt vời để du học và kết nối với thế giới."
                )
            )
        ),
        SpeakingTopic(
            id = "school",
            titleVi = "Trường học",
            titleEn = "School Life",
            subtitleVi = "Môn học, thầy cô & hoạt động lớp",
            badge = "Quen thuộc",
            prompts = listOf(
                SpeakingPromptItem(
                    id = "school_1",
                    questionEn = "What is your favorite subject at school and why?",
                    questionVi = "Môn học yêu thích của bạn ở trường là gì và tại sao?",
                    sampleAnswerEn = "My favorite subject is English because it allows me to explore new cultures.",
                    sampleAnswerIpa = "/maɪ ˈfeɪvərɪt ˈsʌbdʒɪkt ɪz ˈɪŋɡlɪʃ bɪˈkɒz ɪt əˈlaʊz miː tuː ɪkˈsplɔː njuː ˈkʌltʃəz/",
                    sampleAnswerVi = "Môn học yêu thích của mình là tiếng Anh vì nó giúp mình khám phá những nền văn hóa mới."
                ),
                SpeakingPromptItem(
                    id = "school_2",
                    questionEn = "How do you usually prepare for an important exam?",
                    questionVi = "Bạn thường chuẩn bị cho một kỳ thi quan trọng như thế nào?",
                    sampleAnswerEn = "I make a study schedule and review key vocabulary with flashcards every evening.",
                    sampleAnswerIpa = "/aɪ meɪk ə ˈstʌdi ˈʃedjuːl ænd rɪˈvjuː kiː vəˈkæbjələri wɪð ˈflæʃkɑːdz ˈevri ˈiːvnɪŋ/",
                    sampleAnswerVi = "Mình lập thời gian biểu học tập và ôn tập từ vựng trọng tâm bằng flashcard mỗi tối."
                ),
                SpeakingPromptItem(
                    id = "school_3",
                    questionEn = "What school club would you like to join this semester?",
                    questionVi = "Bạn muốn tham gia câu lạc bộ nào ở trường trong học kỳ này?",
                    sampleAnswerEn = "I want to join the debate club to build confidence and critical thinking.",
                    sampleAnswerIpa = "/aɪ wɒnt tuː dʒɔɪn ðə dɪˈbeɪt klʌb tuː bɪld ˈkɒnfɪdəns ænd ˈkrɪtɪkl ˈθɪŋkɪŋ/",
                    sampleAnswerVi = "Mình muốn tham gia câu lạc bộ tranh biện để rèn sự tự tin và tư duy phản biện."
                )
            )
        ),
        SpeakingTopic(
            id = "hobbies",
            titleVi = "Sở thích",
            titleEn = "Hobbies & Interests",
            subtitleVi = "Giải trí, âm nhạc, thể thao & đọc sách",
            badge = "Giao tiếp",
            prompts = listOf(
                SpeakingPromptItem(
                    id = "hobby_1",
                    questionEn = "What do you enjoy doing in your free time after school?",
                    questionVi = "Bạn thích làm gì vào thời gian rảnh sau giờ học?",
                    sampleAnswerEn = "In my free time, I love listening to podcasts and playing badminton with friends.",
                    sampleAnswerIpa = "/ɪn maɪ friː taɪm, aɪ lʌv ˈlɪsnɪŋ tuː ˈpɒdkɑːsts ænd ˈpleɪɪŋ ˈbædmɪntən wɪð frendz/",
                    sampleAnswerVi = "Vào thời gian rảnh, mình thích nghe podcast và chơi cầu lông cùng bạn bè."
                ),
                SpeakingPromptItem(
                    id = "hobby_2",
                    questionEn = "Do you prefer reading books or watching movies in English?",
                    questionVi = "Bạn thích đọc sách hay xem phim bằng tiếng Anh hơn?",
                    sampleAnswerEn = "I prefer watching short movies because I can learn natural pronunciation and daily expressions.",
                    sampleAnswerIpa = "/aɪ prɪˈfɜː ˈwɒtʃɪŋ ʃɔːt ˈmuːviz bɪˈkɒz aɪ kæn lɜːn ˈnætʃrəl prəˌnʌnsiˈeɪʃn ænd ˈdeɪli ɪkˈspreʃnz/",
                    sampleAnswerVi = "Mình thích xem phim ngắn hơn vì có thể học phát âm tự nhiên và các cách diễn đạt hằng ngày."
                ),
                SpeakingPromptItem(
                    id = "hobby_3",
                    questionEn = "How does your hobby help you reduce stress?",
                    questionVi = "Sở thích của bạn giúp giảm căng thẳng như thế nào?",
                    sampleAnswerEn = "Drawing and listening to calm music help me relax after a busy week.",
                    sampleAnswerIpa = "/ˈdrɔːɪŋ ænd ˈlɪsnɪŋ tuː kɑːm ˈmjuːzɪk help miː rɪˈlæks ˈɑːftər ə ˈbɪzi wiːk/",
                    sampleAnswerVi = "Vẽ tranh và nghe nhạc nhẹ giúp mình thư giãn sau một tuần bận rộn."
                )
            )
        ),
        SpeakingTopic(
            id = "environment",
            titleVi = "Bảo vệ môi trường",
            titleEn = "Environment",
            subtitleVi = "Sống xanh, tái chế & biến đổi khí hậu",
            badge = "Trọng tâm",
            prompts = listOf(
                SpeakingPromptItem(
                    id = "env_1",
                    questionEn = "What small actions can students take to protect the environment?",
                    questionVi = "Học sinh có thể làm những hành động nhỏ nào để bảo vệ môi trường?",
                    sampleAnswerEn = "Students can bring reusable water bottles and recycle paper waste in every classroom.",
                    sampleAnswerIpa = "/ˈstjuːdnts kæn brɪŋ ˌriːˈjuːzəbl ˈwɔːtə ˈbɒtlz ænd ˌriːˈsaɪkl ˈpeɪpə weɪst ɪn ˈevri ˈklɑːsruːm/",
                    sampleAnswerVi = "Học sinh có thể mang bình nước dùng nhiều lần và tái chế giấy vụn ở mỗi lớp học."
                ),
                SpeakingPromptItem(
                    id = "env_2",
                    questionEn = "Why is planting more trees important for our city?",
                    questionVi = "Tại sao việc trồng thêm nhiều cây xanh lại quan trọng đối với thành phố của chúng ta?",
                    sampleAnswerEn = "Planting trees reduces air pollution, provides shade, and creates a healthier living space.",
                    sampleAnswerIpa = "/ˈplɑːntɪŋ triːz rɪˈdjuːsɪz eə pəˈluːʃn, prəˈvaɪdz ʃeɪd, ænd kriˈeɪts ə ˈhelθiə ˈlɪvɪŋ speɪs/",
                    sampleAnswerVi = "Trồng cây giúp giảm ô nhiễm không khí, tạo bóng mát và mang lại không gian sống lành mạnh hơn."
                ),
                SpeakingPromptItem(
                    id = "env_3",
                    questionEn = "How can your school encourage a greener lifestyle?",
                    questionVi = "Trường bạn có thể khuyến khích lối sống xanh hơn bằng cách nào?",
                    sampleAnswerEn = "We can organize a clean-up Sunday and encourage everyone to ride bicycles to school.",
                    sampleAnswerIpa = "/wiː kæn ˈɔːɡənaɪz ə ˈkliːnʌp ˈsʌndeɪ ænd ɪnˈkʌrɪdʒ ˈevriwʌn tuː raɪd ˈbaɪsɪklz tuː skuːl/",
                    sampleAnswerVi = "Chúng mình có thể tổ chức Chủ nhật xanh dọn vệ sinh và khuyến khích mọi người đạp xe đến trường."
                )
            )
        ),
        SpeakingTopic(
            id = "technology",
            titleVi = "Công nghệ & Học tập",
            titleEn = "Smart Learning",
            subtitleVi = "Thiết bị số & tự học thông minh",
            badge = "Hiện đại",
            prompts = listOf(
                SpeakingPromptItem(
                    id = "tech_1",
                    questionEn = "How do digital apps help you learn English more effectively?",
                    questionVi = "Các ứng dụng số giúp bạn học tiếng Anh hiệu quả hơn như thế nào?",
                    sampleAnswerEn = "Learning apps let me practice pronunciation anytime and remember vocabulary with spaced repetition.",
                    sampleAnswerIpa = "/ˈlɜːnɪŋ æps let miː ˈpræktɪs prəˌnʌnsiˈeɪʃn ˈenitaɪm ænd rɪˈmembə vəˈkæbjələri wɪð speɪst ˌrepəˈtɪʃn/",
                    sampleAnswerVi = "Ứng dụng học tập giúp mình luyện phát âm mọi lúc và nhớ từ vựng bằng lặp lại ngắt quãng."
                ),
                SpeakingPromptItem(
                    id = "tech_2",
                    questionEn = "What should teenagers keep in mind when using social media?",
                    questionVi = "Thanh thiếu niên cần lưu ý điều gì khi sử dụng mạng xã hội?",
                    sampleAnswerEn = "We should manage screen time wisely and verify information before sharing it online.",
                    sampleAnswerIpa = "/wiː ʃʊd ˈmænɪdʒ skriːn taɪm ˈwaɪzli ænd ˈverɪfaɪ ˌɪnfəˈmeɪʃn bɪˈfɔː ˈʃeərɪŋ ɪt ˈɒnlaɪn/",
                    sampleAnswerVi = "Chúng ta nên quản lý thời gian dùng màn hình hợp lý và kiểm chứng thông tin trước khi chia sẻ."
                )
            )
        )
    )
}
