package com.fellio.app.data

data class GenreName(val fa: String, val en: String, val tr: String, val ar: String) {
    fun of(l: Lang): String = when (l) {
        Lang.FA -> fa
        Lang.EN -> en
        Lang.TR -> tr
        Lang.AR -> ar
    }
}

object Genres {
    private fun g(fa: String, en: String, tr: String, ar: String) = GenreName(fa, en, tr, ar)

    /** Every TMDB genre (movie + TV ids) with names in the four app languages. */
    val names: Map<Int, GenreName> = mapOf(
        28 to g("اکشن", "Action", "Aksiyon", "أكشن"),
        12 to g("ماجراجویی", "Adventure", "Macera", "مغامرة"),
        16 to g("انیمیشن", "Animation", "Animasyon", "رسوم متحركة"),
        35 to g("کمدی", "Comedy", "Komedi", "كوميديا"),
        80 to g("جنایی", "Crime", "Suç", "جريمة"),
        99 to g("مستند", "Documentary", "Belgesel", "وثائقي"),
        18 to g("درام", "Drama", "Dram", "دراما"),
        10751 to g("خانوادگی", "Family", "Aile", "عائلي"),
        14 to g("فانتزی", "Fantasy", "Fantastik", "فانتازيا"),
        36 to g("تاریخی", "History", "Tarih", "تاريخي"),
        27 to g("ترسناک", "Horror", "Korku", "رعب"),
        10402 to g("موزیکال", "Music", "Müzik", "موسيقى"),
        9648 to g("معمایی", "Mystery", "Gizem", "غموض"),
        10749 to g("عاشقانه", "Romance", "Romantik", "رومانسي"),
        878 to g("علمی‌تخیلی", "Science Fiction", "Bilim Kurgu", "خيال علمي"),
        10770 to g("فیلم تلویزیونی", "TV Movie", "TV Filmi", "فيلم تلفزيوني"),
        53 to g("هیجان‌انگیز", "Thriller", "Gerilim", "إثارة"),
        10752 to g("جنگی", "War", "Savaş", "حرب"),
        37 to g("وسترن", "Western", "Vahşi Batı", "ويسترن"),
        10759 to g("اکشن و ماجراجویی", "Action & Adventure", "Aksiyon ve Macera", "أكشن ومغامرة"),
        10762 to g("کودکان", "Kids", "Çocuk", "أطفال"),
        10763 to g("اخبار", "News", "Haberler", "أخبار"),
        10764 to g("واقعیت", "Reality", "Reality Şov", "واقعي"),
        10765 to g("علمی‌تخیلی و فانتزی", "Sci-Fi & Fantasy", "Bilim Kurgu ve Fantezi", "خيال علمي وفانتازيا"),
        10766 to g("سریال روزانه", "Soap", "Pembe Dizi", "مسلسل درامي"),
        10767 to g("گفت‌وگو", "Talk", "Talk Show", "حواري"),
        10768 to g("جنگ و سیاست", "War & Politics", "Savaş ve Politika", "حرب وسياسة")
    )

    fun label(id: Int, lang: Lang): String? = names[id]?.of(lang)

    /** One filter option; movie and TV use different TMDB ids for some genres. */
    data class Option(val key: String, val nameId: Int, val movie: List<Int>, val tv: List<Int>) {
        val name: GenreName get() = Genres.names.getValue(nameId)
        fun availableFor(category: String): Boolean = when (category) {
            "movie" -> movie.isNotEmpty()
            "tv" -> tv.isNotEmpty()
            else -> movie.isNotEmpty() || tv.isNotEmpty()
        }
    }

    val options: List<Option> = listOf(
        Option("action", 28, listOf(28), listOf(10759)),
        Option("adventure", 12, listOf(12), listOf(10759)),
        Option("animation", 16, listOf(16), listOf(16)),
        Option("comedy", 35, listOf(35), listOf(35)),
        Option("crime", 80, listOf(80), listOf(80)),
        Option("documentary", 99, listOf(99), listOf(99)),
        Option("drama", 18, listOf(18), listOf(18)),
        Option("family", 10751, listOf(10751), listOf(10751)),
        Option("fantasy", 14, listOf(14), listOf(10765)),
        Option("history", 36, listOf(36), emptyList()),
        Option("horror", 27, listOf(27), emptyList()),
        Option("kids", 10762, emptyList(), listOf(10762)),
        Option("music", 10402, listOf(10402), emptyList()),
        Option("mystery", 9648, listOf(9648), listOf(9648)),
        Option("news", 10763, emptyList(), listOf(10763)),
        Option("reality", 10764, emptyList(), listOf(10764)),
        Option("romance", 10749, listOf(10749), emptyList()),
        Option("scifi", 878, listOf(878), listOf(10765)),
        Option("soap", 10766, emptyList(), listOf(10766)),
        Option("talk", 10767, emptyList(), listOf(10767)),
        Option("thriller", 53, listOf(53), emptyList()),
        Option("tvmovie", 10770, listOf(10770), emptyList()),
        Option("war", 10752, listOf(10752), listOf(10768)),
        Option("western", 37, listOf(37), listOf(37))
    )

    /** Original-language filter (TMDB ISO 639-1 codes) with names in the four app languages. */
    val languages: List<Pair<String, GenreName>> = listOf(
        "en" to g("انگلیسی", "English", "İngilizce", "الإنجليزية"),
        "ja" to g("ژاپنی", "Japanese", "Japonca", "اليابانية"),
        "ko" to g("کره‌ای", "Korean", "Korece", "الكورية"),
        "tr" to g("ترکی", "Turkish", "Türkçe", "التركية"),
        "fa" to g("فارسی", "Persian", "Farsça", "الفارسية"),
        "ar" to g("عربی", "Arabic", "Arapça", "العربية"),
        "hi" to g("هندی", "Hindi", "Hintçe", "الهندية"),
        "zh" to g("چینی", "Chinese", "Çince", "الصينية"),
        "fr" to g("فرانسوی", "French", "Fransızca", "الفرنسية"),
        "es" to g("اسپانیایی", "Spanish", "İspanyolca", "الإسبانية"),
        "de" to g("آلمانی", "German", "Almanca", "الألمانية"),
        "it" to g("ایتالیایی", "Italian", "İtalyanca", "الإيطالية"),
        "ru" to g("روسی", "Russian", "Rusça", "الروسية"),
        "th" to g("تایلندی", "Thai", "Tayca", "التايلاندية")
    )
}
