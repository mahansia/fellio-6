package com.fellio.app.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.fellio.app.data.Lang

/** All UI text in four languages: Persian, English, Turkish, Arabic. */
class Strings(val lang: Lang) {
    val fa: Boolean get() = lang == Lang.FA

    fun t(fa: String, en: String, tr: String, ar: String): String = when (lang) {
        Lang.FA -> fa
        Lang.EN -> en
        Lang.TR -> tr
        Lang.AR -> ar
    }

    // ---- common
    val appName get() = t("Fellio", "Fellio", "Fellio", "Fellio")
    val movie get() = t("فیلم", "Movie", "Film", "فيلم")
    val series get() = t("سریال", "Series", "Dizi", "مسلسل")
    val movies get() = t("فیلم‌ها", "Movies", "Filmler", "الأفلام")
    val seriesPlural get() = t("سریال‌ها", "Series", "Diziler", "المسلسلات")
    val anime get() = t("انیمه", "Anime", "Anime", "أنمي")
    val all get() = t("همه", "All", "Tümü", "الكل")
    val back get() = t("بازگشت", "Back", "Geri", "رجوع")
    val loadFailed get() = t("بارگذاری انجام نشد", "Couldn't load this title", "Bu başlık yüklenemedi", "تعذر تحميل العنوان")
    val retry get() = t("تلاش مجدد", "Retry", "Yeniden dene", "إعادة المحاولة")
    val cancel get() = t("انصراف", "Cancel", "İptal", "إلغاء")
    val delete get() = t("حذف", "Delete", "Sil", "حذف")
    val confirm get() = t("تأیید", "OK", "Tamam", "موافق")
    val save get() = t("ذخیره", "Save", "Kaydet", "حفظ")
    val create get() = t("ساخت", "Create", "Oluştur", "إنشاء")
    val close get() = t("بستن", "Close", "Kapat", "إغلاق")
    val done get() = t("تمام", "Done", "Bitti", "تم")
    val clear get() = t("پاک کردن", "Clear", "Temizle", "مسح")
    val more get() = t("بیشتر", "More", "Daha fazla", "المزيد")
    val openLabel get() = t("باز کردن", "Open", "Aç", "فتح")
    val watched get() = t("دیدم", "Watched", "İzledim", "شاهدت")
    val notWatched get() = t("ندیده", "Not watched", "İzlenmedi", "لم أشاهد")

    fun typeYear(tv: Boolean, year: String) = (if (tv) series else movie) + if (year.isNotBlank()) " • $year" else ""
    fun episodesCount(w: Int, total: Int) = t("$w / $total قسمت", "$w / $total episodes", "$w / $total bölüm", "$w / $total حلقة")
    fun nextEpisode(s: Int, e: Int) = t("قسمت بعدی: فصل $s قسمت $e", "Next: S$s E$e", "Sıradaki: S$s B$e", "التالي: م$s ح$e")
    fun continueLabel(s: Int, e: Int) = t("▶ ادامه: فصل $s قسمت $e", "▶ Continue: S$s E$e", "▶ Devam: S$s B$e", "▶ متابعة: م$s ح$e")
    val seriesComplete get() = t("✓ سریال کامل شد", "✓ Series completed", "✓ Dizi tamamlandı", "✓ اكتمل المسلسل")
    fun seasonComplete(n: Int) = t("✓ فصل $n کامل شد", "✓ Season $n completed", "✓ Sezon $n tamamlandı", "✓ اكتمل الموسم $n")
    fun season(n: Int) = t("فصل $n", "Season $n", "Sezon $n", "الموسم $n")
    fun episode(n: Int) = t("قسمت $n", "Episode $n", "Bölüm $n", "الحلقة $n")
    fun minutes(n: Int) = t("$n دقیقه", "$n min", "$n dk", "$n دقيقة")

    // ---- home
    val tabWatching get() = t("در حال دیدن", "Watching", "İzleniyor", "قيد المشاهدة")
    val tabWatchlist get() = t("می‌خواهم ببینم", "Watch later", "Sonra izle", "شاهد لاحقاً")
    val tabCompleted get() = t("دیده‌شده", "Completed", "Tamamlandı", "مكتمل")
    val searchMyList get() = t("جستجو در لیست من", "Search my list", "Listemde ara", "ابحث في قائمتي")
    val search get() = t("جستجو", "Search", "Ara", "بحث")
    val add get() = t("افزودن", "Add", "Ekle", "إضافة")
    val stats get() = t("آمار", "Stats", "İstatistik", "إحصائيات")
    val settings get() = t("تنظیمات", "Settings", "Ayarlar", "الإعدادات")
    val discover get() = t("کشف", "Discover", "Keşfet", "اكتشف")
    val gridView get() = t("نمایش شبکه‌ای", "Grid view", "Izgara görünümü", "عرض شبكي")
    val listView get() = t("نمایش لیستی", "List view", "Liste görünümü", "عرض قائمة")
    val noResults get() = t("موردی پیدا نشد.", "Nothing found.", "Sonuç bulunamadı.", "لا توجد نتائج.")
    val emptyWatching get() = t(
        "هنوز چیزی در حال دیدن ندارید.\nبا دکمه + فیلم یا سریال اضافه کنید.",
        "Nothing here yet.\nTap + to add a movie or series.",
        "Henüz burada bir şey yok.\nFilm veya dizi eklemek için + simgesine dokun.",
        "لا يوجد شيء هنا بعد.\nاضغط + لإضافة فيلم أو مسلسل."
    )
    val emptyWatchlist get() = t(
        "لیست «می‌خواهم ببینم» خالی است.\nبا آیکون ساعت، چیزهایی که می‌خواهید بعداً ببینید را اضافه کنید.",
        "Your watch-later list is empty.\nAdd titles with the clock icon.",
        "Sonra izle listen boş.\nSaat simgesiyle sonra izleyeceklerini ekle.",
        "قائمة «شاهد لاحقاً» فارغة.\nأضف عناوين بأيقونة الساعة."
    )
    val emptyCompleted get() = t("هنوز چیزی را کامل ندیده‌اید.", "Nothing completed yet.", "Henüz tamamlanan bir şey yok.", "لم تُكمل أي شيء بعد.")
    val markNext get() = t("قسمت بعدی را دیدم", "Mark next as watched", "Sıradaki bölümü izledim olarak işaretle", "تحديد الحلقة التالية كمشاهدة")
    val markWatched get() = t("دیدم", "Mark as watched", "İzledim olarak işaretle", "تحديد كمشاهد")
    val customizeChips get() = t("ویرایش چیپ‌ها", "Edit chips", "Çipleri düzenle", "تعديل الشرائح")
    val chipsHint get() = t(
        "نمایش/مخفی کردن و ترتیب. چیپ‌های پیش‌فرض حذف نمی‌شوند، فقط می‌توانند مخفی شوند.",
        "Show/hide and reorder. Built-in chips can't be deleted, only hidden.",
        "Göster/gizle ve sırala. Hazır çipler silinemez, yalnızca gizlenebilir.",
        "إظهار/إخفاء وترتيب. الشرائح الافتراضية لا تُحذف، يمكن إخفاؤها فقط."
    )
    val managePlaylist get() = t("مدیریت پلی‌لیست", "Manage playlist", "Çalma listesini yönet", "إدارة القائمة")

    // ---- playlists
    val tonight get() = t("امشب چی ببینم؟", "What to watch tonight?", "Bu akşam ne izlesem?", "ماذا أشاهد الليلة؟")
    val tonightEmpty get() = t("واچ‌لیستت خالیه", "Your watchlist is empty", "İzleme listen boş", "قائمتك فارغة")
    val seasonSubs get() = t("زیرنویس این فصل", "Subtitles for this season", "Bu sezonun altyazıları", "ترجمات هذا الموسم")
    val status get() = t("در حال تماشا", "Watching", "İzleniyor", "المشاهدة")
    val titleParenLabel get() = t("عنوان دوم داخل پرانتز", "Second title in brackets", "İkinci başlık parantez içinde", "العنوان الثاني بين قوسين")
    val titleParenNone get() = t("بدون پرانتز", "None", "Yok", "بدون")
    val titleParenLocal get() = t("اصلی بیرون، ترجمه داخل ()", "Original outside, translated in ()", "Orijinal dışarıda, çeviri ()", "الأصلي خارجاً والمترجم بين ()")
    val titleParenOriginal get() = t("ترجمه بیرون، اصلی داخل ()", "Translated outside, original in ()", "Çeviri dışarıda, orijinal ()", "المترجم خارجاً والأصلي بين ()")
    val titleModeLabel get() = t("نمایش عنوان", "Title display", "Başlık gösterimi", "عرض العنوان")
    val titleLocal get() = t("زبان برنامه", "App language", "Uygulama dili", "لغة التطبيق")
    val titleOriginal get() = t("اصلی", "Original", "Orijinal", "الأصلي")
    val titleBoth get() = t("هر دو", "Both", "İkisi", "كلاهما")
    val posterLangLabel get() = t("زبان پوستر (صفحه جزئیات)", "Poster language (detail page)", "Afiş dili", "لغة الملصق")
    val posterAuto get() = t("خودکار", "Auto", "Otomatik", "تلقائي")
    val posterEn get() = t("انگلیسی", "English", "İngilizce", "الإنجليزية")
    val lists get() = t("لیست‌ها", "Lists", "Listeler", "القوائم")
    val recent get() = t("اخیراً دیده‌شده", "Recently viewed", "Son bakılanlar", "شوهد مؤخراً")
    val trendingNow get() = t("پرطرفدار این هفته", "Trending this week", "Bu haftanın popülerleri", "الأكثر رواجاً")
    val details get() = t("جزئیات", "Details", "Ayrıntılar", "التفاصيل")
    val noPlaylists get() = t("هنوز لیستی نساخته‌اید", "No lists yet", "Henüz liste yok", "لا توجد قوائم بعد")
    val playlists get() = t("پلی‌لیست‌ها", "Playlists", "Çalma listeleri", "قوائم التشغيل")
    val newPlaylist get() = t("پلی‌لیست جدید", "New playlist", "Yeni çalma listesi", "قائمة جديدة")
    val playlistName get() = t("نام پلی‌لیست", "Playlist name", "Liste adı", "اسم القائمة")
    val editPlaylist get() = t("ویرایش پلی‌لیست", "Edit playlist", "Listeyi düzenle", "تعديل القائمة")
    val deletePlaylist get() = t("حذف پلی‌لیست", "Delete playlist", "Listeyi sil", "حذف القائمة")
    fun deletePlaylistMsg(n: String) = t(
        "پلی‌لیست «$n» حذف شود؟ (خود فیلم‌ها از برنامه پاک نمی‌شوند.)",
        "Delete playlist “$n”? (The titles themselves stay in the app.)",
        "“$n” listesi silinsin mi? (Yapımlar uygulamada kalır.)",
        "حذف القائمة «$n»؟ (تبقى العناوين في التطبيق.)"
    )
    val playlistEmpty get() = t("این پلی‌لیست خالی است. با دکمه + فیلم‌ها را اضافه کنید.", "This playlist is empty. Tap + to add titles.", "Bu liste boş. Yapım eklemek için + simgesine dokun.", "هذه القائمة فارغة. اضغط + لإضافة عناوين.")
    val addToPlaylist get() = t("افزودن به پلی‌لیست", "Add to playlist", "Çalma listesine ekle", "أضف إلى قائمة")
    val noPlaylistsYet get() = t("هنوز پلی‌لیستی نساخته‌اید.", "You have no playlists yet.", "Henüz çalma listen yok.", "ليس لديك قوائم بعد.")
    val chooseColor get() = t("رنگ", "Color", "Renk", "اللون")
    val chooseIcon get() = t("آیکون", "Icon", "Simge", "الأيقونة")
    val moveUp get() = t("بالا", "Move up", "Yukarı", "أعلى")
    val moveDown get() = t("پایین", "Move down", "Aşağı", "أسفل")
    val removeFromPlaylist get() = t("حذف از پلی‌لیست", "Remove from playlist", "Listeden çıkar", "إزالة من القائمة")
    fun itemsCount(n: Int) = t("$n مورد", "$n titles", "$n yapım", "$n عنصر")
    val addTitlesToPlaylist get() = t("افزودن از لیست من", "Add from my library", "Kitaplığımdan ekle", "أضف من مكتبتي")

    // ---- suggestions / discover
    val suggestedForYou get() = t("پیشنهاد برای شما", "Suggested for you", "Senin için öneriler", "مقترح لك")
    val similarTitles get() = t("فیلم‌ها و سریال‌های مشابه", "Similar titles", "Benzer yapımlar", "عناوين مشابهة")
    fun becauseWatched(n: String) = t("چون «$n» را دیدی", "Because you watched “$n”", "“$n” izlediğin için", "لأنك شاهدت «$n»")
    fun becauseGenre(g: String) = t("چون ژانر $g را دوست داری", "Because you like $g", "$g türünü sevdiğin için", "لأنك تحب $g")
    val trendingWeek get() = t("پرطرفدار این هفته", "Trending this week", "Bu haftanın trendleri", "الرائج هذا الأسبوع")
    val trailer get() = t("تریلر", "Trailer", "Fragman", "الإعلان")
    val noTrailer get() = t("تریلری پیدا نشد", "No trailer found", "Fragman bulunamadı", "لا يوجد إعلان")
    val interests get() = t("علاقه‌مندی‌ها", "Interests", "İlgi alanları", "الاهتمامات")
    val interestsHint get() = t("هر نوع چقدر برایت مهم است؟", "How much do you care about each kind?", "Her tür senin için ne kadar önemli?", "ما مدى أهمية كل نوع؟")
    val genresLabel get() = t("ژانرهای مورد علاقه", "Favorite genres", "Favori türler", "الأنواع المفضلة")
    val impOff get() = t("نمی‌خواهم", "Off", "Kapalı", "إيقاف")
    val impNormal get() = t("معمولی", "Normal", "Normal", "عادي")
    val impHigh get() = t("خیلی مهم", "Very", "Çok", "مهم جداً")
    val kindAnime get() = t("انیمه", "Anime", "Anime", "أنمي")
    val kindTv get() = t("سریال", "Series", "Dizi", "مسلسلات")
    val kindMovie get() = t("فیلم", "Movies", "Film", "أفلام")
    val rowTitles: Map<String, String> get() = mapOf(
        "newsForYou" to t("تازه‌ها برای تو", "New for you", "Senin için yeni", "جديد لك"),
        "nowPlaying" to t("در حال اکران", "Now playing", "Vizyonda", "يعرض الآن"),
        "upcoming" to t("به‌زودی", "Coming soon", "Yakında", "قريباً"),
        "popularMovies" to t("فیلم‌های محبوب", "Popular movies", "Popüler filmler", "أفلام شائعة"),
        "popularTv" to t("سریال‌های محبوب", "Popular series", "Popüler diziler", "مسلسلات شائعة"),
        "topTv" to t("بهترین سریال‌ها", "Top rated series", "En iyi diziler", "أفضل المسلسلات"),
        "onAir" to t("در حال پخش", "On the air", "Yayında", "يُبث الآن")
    )
    val topRated get() = t("بالاترین امتیازها", "Top rated", "En yüksek puanlılar", "الأعلى تقييماً")
    val animePicks get() = t("انتخاب‌های انیمه", "Anime picks", "Anime seçkileri", "اختيارات الأنمي")
    val notInterested get() = t("علاقه ندارم", "Not interested", "İlgilenmiyorum", "غير مهتم")
    val discoverTitle get() = t("کشف فیلم و سریال", "Discover", "Keşfet", "اكتشف")
    val discoverEmpty get() = t(
        "فعلاً پیشنهادی نیست. با دیدن چند مورد، پیشنهادها دقیق‌تر می‌شوند.",
        "No suggestions right now. Watch a few titles and they get more accurate.",
        "Şu an öneri yok. Birkaç yapım izledikçe öneriler daha isabetli olur.",
        "لا توجد اقتراحات الآن. كلما شاهدت المزيد أصبحت أدق."
    )
    val seeAll get() = t("همه", "See all", "Tümünü gör", "عرض الكل")
    val resetHidden get() = t("نمایش دوباره‌ی پیشنهادهای ردشده", "Show dismissed suggestions again", "Reddedilen önerileri tekrar göster", "إظهار الاقتراحات المرفوضة مجدداً")
    val reload get() = t("بارگذاری دوباره", "Reload", "Yenile", "إعادة التحميل")

    // ---- person
    val biography get() = t("درباره", "Biography", "Biyografi", "السيرة")
    fun born(d: String) = t("تولد: $d", "Born: $d", "Doğum: $d", "تاريخ الميلاد: $d")
    val birthPlace get() = t("محل تولد", "Place of birth", "Doğum yeri", "مكان الميلاد")
    val filmography get() = t("آثار بازیگر", "Filmography", "Filmografi", "الأعمال")
    val unreleased get() = t("منتشر نشده", "Not released yet", "Henüz yayınlanmadı", "لم يُصدر بعد")
    val noBio get() = t("زندگی‌نامه‌ای موجود نیست.", "No biography available.", "Biyografi mevcut değil.", "لا توجد سيرة متاحة.")

    // ---- add / search
    val addTitle get() = t("افزودن فیلم / سریال", "Add movie / series", "Film / dizi ekle", "إضافة فيلم / مسلسل")
    val searchHint get() = t("نام فارسی یا انگلیسی…", "Search by name…", "İsme göre ara…", "ابحث بالاسم…")
    val filters get() = t("فیلترها", "Filters", "Filtreler", "عوامل التصفية")
    val clearFilters get() = t("حذف فیلترها", "Clear filters", "Filtreleri temizle", "مسح التصفية")
    val typeLabel get() = t("نوع", "Type", "Tip", "النوع")
    val category get() = t("دسته", "Category", "Kategori", "الفئة")
    val yearLabel get() = t("سال انتشار", "Release year", "Yayın yılı", "سنة الإصدار")
    val yearHint get() = t("مثلاً 2019", "e.g. 2019", "örn. 2019", "مثال: 2019")
    val genreLabel get() = t("ژانر", "Genre", "Tür", "النوع الفني")
    val allGenres get() = t("همه‌ی ژانرها", "All genres", "Tüm türler", "كل الأنواع")
    val selectGenres get() = t("انتخاب ژانر", "Select genres", "Tür seç", "اختر النوع")
    val searchGenres get() = t("جستجوی ژانر…", "Search genres…", "Tür ara…", "ابحث عن نوع…")
    val searchLanguages get() = t("جستجوی زبان…", "Search languages…", "Dil ara…", "ابحث عن لغة…")
    fun selectedCount(n: Int) = t("$n انتخاب‌شده", "$n selected", "$n seçili", "$n محدد")
    val minRating get() = t("حداقل امتیاز", "Minimum rating", "En düşük puan", "أدنى تقييم")
    val ratingAny get() = t("مهم نیست", "Any rating", "Fark etmez", "أي تقييم")
    fun ratingAtLeast(n: Int) = "★ $n+"
    val origLanguage get() = t("زبان اصلی", "Original language", "Orijinal dil", "اللغة الأصلية")
    val sortLabel get() = t("مرتب‌سازی", "Sort by", "Sırala", "ترتيب حسب")
    val sortRelevance get() = t("مرتبط‌ترین", "Relevance", "İlgili", "الأكثر صلة")
    val sortPopular get() = t("محبوب‌ترین", "Popularity", "Popüler", "الأكثر شعبية")
    val sortRating get() = t("بالاترین امتیاز", "Top rated", "En yüksek puan", "الأعلى تقييماً")
    val sortNewest get() = t("جدیدترین", "Newest", "En yeni", "الأحدث")
    val trending get() = t("پیشنهاد و ترند این هفته", "Trending this week", "Bu haftanın trendleri", "الرائج هذا الأسبوع")
    val results get() = t("نتایج", "Results", "Sonuçlar", "النتائج")
    val loadMore get() = t("نمایش بیشتر", "Load more", "Daha fazla göster", "عرض المزيد")
    val inList get() = t("✓ در لیست", "✓ In list", "✓ Listede", "✓ في القائمة")
    val inWatchlist get() = t("✓ می‌خواهم ببینم", "✓ Watch later", "✓ Sonra izle", "✓ شاهد لاحقاً")
    val addToList get() = t("افزودن به لیست", "Add to list", "Listeye ekle", "أضف إلى القائمة")
    val addToWatchlist get() = t("افزودن به «می‌خواهم ببینم»", "Add to Watch later", "Sonra izle'ye ekle", "أضف إلى «شاهد لاحقاً»")
    val addToWatching get() = t("افزودن به «در حال دیدن»", "Add to Watching", "İzleniyor'a ekle", "أضف إلى «قيد المشاهدة»")
    val watchLater get() = t("می‌خواهم ببینم", "Watch later", "Sonra izle", "شاهد لاحقاً")
    fun addedSnack(n: String) = t("«$n» به «در حال دیدن» اضافه شد", "“$n” added to Watching", "“$n” İzleniyor'a eklendi", "تمت إضافة «$n» إلى قيد المشاهدة")
    fun addedWatchSnack(n: String) = t("«$n» به «می‌خواهم ببینم» اضافه شد", "“$n” added to Watch later", "“$n” Sonra izle'ye eklendi", "تمت إضافة «$n» إلى شاهد لاحقاً")
    val goSettings get() = t("رفتن به تنظیمات", "Go to settings", "Ayarlara git", "اذهب إلى الإعدادات")

    // ---- detail
    val rating get() = t("امتیاز", "Rating", "Puan", "التقييم")
    val cast get() = t("بازیگران", "Cast", "Oyuncular", "الممثلون")
    val overview get() = t("خلاصه", "Overview", "Özet", "ملخص")
    val toggleWatchlist get() = t("می‌خواهم ببینم", "Watch later", "Sonra izle", "شاهد لاحقاً")
    val refreshEpisodes get() = t("بروزرسانی قسمت‌ها", "Refresh episodes", "Bölümleri yenile", "تحديث الحلقات")
    fun refreshed(n: Int) = if (n > 0)
        t("$n قسمت جدید پیدا شد", "$n new episodes found", "$n yeni bölüm bulundu", "تم العثور على $n حلقة جديدة")
    else
        t("قسمت جدیدی پیدا نشد", "No new episodes", "Yeni bölüm yok", "لا توجد حلقات جديدة")
    fun deleteMsg(n: String) = t("«$n» و وضعیت تماشای آن حذف شود؟", "Delete “$n” and its watch progress?", "“$n” ve izleme durumu silinsin mi?", "حذف «$n» وتقدم مشاهدته؟")
    val markUpTo get() = t("علامت‌گذاری تا قسمت...", "Mark up to episode...", "Şu bölüme kadar işaretle…", "تحديد حتى حلقة...")
    val upToTitle get() = t("تا قسمت ... دیدم", "Watched up to...", "Şuraya kadar izledim…", "شاهدت حتى...")
    val upToHint get() = t("همه‌ی قسمت‌های قبل از قسمت انتخابی هم دیده‌شده علامت می‌خورند.", "All earlier episodes will be marked as watched too.", "Önceki tüm bölümler de izlendi olarak işaretlenir.", "ستُحدَّد جميع الحلقات السابقة كمشاهدة أيضاً.")
    val markAllSeason get() = t("همه قسمت‌ها را دیده‌ام", "Mark all as watched", "Tümünü izlendi işaretle", "تحديد الكل كمشاهد")
    val unmarkAllSeason get() = t("همه را ندیده علامت بزن", "Mark all as unwatched", "Tümünü izlenmedi işaretle", "تحديد الكل كغير مشاهد")
    val seasonOptions get() = t("گزینه‌های فصل", "Season options", "Sezon seçenekleri", "خيارات الموسم")

    // ---- subtitles
    val downloadSubtitle get() = t("دانلود زیرنویس", "Download subtitles", "Altyazı indir", "تنزيل الترجمة")
    val noSubtitles get() = t("زیرنویسی پیدا نشد. زبان‌های دیگر را در تنظیمات فعال کنید.", "No subtitles found. Enable more languages in Settings.", "Altyazı bulunamadı. Ayarlardan başka diller ekle.", "لم يتم العثور على ترجمة. فعّل لغات أخرى من الإعدادات.")
    val hearingImpaired get() = t("ویژه ناشنوایان", "Hearing impaired", "İşitme engelliler için", "لضعاف السمع")
    val subsLangs get() = t("زبان‌های زیرنویس", "Subtitle languages", "Altyazı dilleri", "لغات الترجمة")
    val subsLangsHint get() = t("در جستجوی زیرنویس فقط همین زبان‌ها نشان داده می‌شود.", "Only these languages are shown when searching subtitles.", "Altyazı aramasında yalnızca bu diller gösterilir.", "تُعرض هذه اللغات فقط عند البحث عن الترجمة.")
    fun subsFound(n: Int) = t("$n زیرنویس پیدا شد", "$n subtitles found", "$n altyazı bulundu", "تم العثور على $n ترجمة")
    val subsSearching get() = t("در حال جستجوی زیرنویس…", "Searching subtitles…", "Altyazılar aranıyor…", "جارٍ البحث عن الترجمة…")
    val subsTapToDownload get() = t("برای دانلود روی یکی بزنید", "Tap one to download", "İndirmek için birine dokun", "اضغط على واحدة للتنزيل")

    // ---- stats
    val statsTitle get() = t("آمار تماشا", "Watch stats", "İzleme istatistikleri", "إحصائيات المشاهدة")
    val timeWatched get() = t("زمان تماشا", "Time watched", "İzleme süresi", "وقت المشاهدة")
    val moviesWatched get() = t("فیلم دیده‌شده", "Movies watched", "İzlenen filmler", "أفلام شوهدت")
    val episodesWatched get() = t("قسمت دیده‌شده", "Episodes watched", "İzlenen bölümler", "حلقات شوهدت")
    val seriesCompleted get() = t("سریال کامل‌شده", "Series completed", "Tamamlanan diziler", "مسلسلات مكتملة")
    val watchingNow get() = t("در حال دیدن", "Watching now", "Şu an izlenen", "قيد المشاهدة الآن")
    val inWatchlistCount get() = t("می‌خواهم ببینم", "Watch later", "Sonra izle", "شاهد لاحقاً")
    val topGenres get() = t("ژانرهای برتر شما", "Your top genres", "En sevdiğin türler", "أفضل أنواعك")
    val noStats get() = t("با تماشای چند مورد، آمار شما اینجا ساخته می‌شود.", "Watch a few titles and your stats will appear here.", "Birkaç yapım izlediğinde istatistiklerin burada görünür.", "شاهد بعض العناوين وستظهر إحصائياتك هنا.")
    fun duration(min: Long): String {
        val h = min / 60
        val m = min % 60
        return t("$h ساعت و $m دقیقه", "${h}h ${m}m", "${h}sa ${m}dk", "$h ساعة و $m دقيقة")
    }

    // ---- settings
    val sectionAppearance get() = t("ظاهر", "Appearance", "Görünüm", "المظهر")
    val sectionSubtitles get() = t("زیرنویس", "Subtitles", "Altyazılar", "الترجمة")
    val sectionData get() = t("اطلاعات و بکاپ", "Data & backup", "Veri ve yedek", "البيانات والنسخ الاحتياطي")
    val keyBuiltIn get() = t("کلید پیش‌فرض", "Built-in key", "Yerleşik anahtar", "المفتاح المدمج")
    val keyCustom get() = t("کلید شخصی", "Your own key", "Kendi anahtarın", "مفتاحك الخاص")
    val language get() = t("زبان", "Language", "Dil", "اللغة")
    val theme get() = t("پوسته", "Theme", "Tema", "المظهر")
    val themeSystem get() = t("خودکار", "System", "Sistem", "تلقائي")
    val themeLight get() = t("روشن", "Light", "Açık", "فاتح")
    val themeDark get() = t("تیره", "Dark", "Koyu", "داكن")
    val accent get() = t("رنگ اصلی برنامه", "Accent color", "Vurgu rengi", "اللون الأساسي")
    val jalaliDates get() = t("نمایش تاریخ شمسی", "Show Jalali (Persian) dates", "Celali (Fars) tarihlerini göster", "عرض التاريخ الفارسي (الشمسي)")
    val notifications get() = t("اعلان قسمت جدید", "New episode notifications", "Yeni bölüm bildirimleri", "إشعارات الحلقات الجديدة")
    val notificationsHint get() = t("هر ۱۲ ساعت یک‌بار سریال‌ها بررسی می‌شوند.", "Series are checked every 12 hours.", "Diziler her 12 saatte bir kontrol edilir.", "يتم فحص المسلسلات كل 12 ساعة.")
    val backup get() = t("بکاپ و بازیابی", "Backup & restore", "Yedekle ve geri yükle", "النسخ الاحتياطي والاستعادة")
    val exportBtn get() = t("ذخیره‌ی بکاپ در فایل", "Save backup to file", "Yedeği dosyaya kaydet", "حفظ نسخة احتياطية في ملف")
    val importBtn get() = t("بازیابی از فایل", "Restore from file", "Dosyadan geri yükle", "استعادة من ملف")
    val importConfirmTitle get() = t("بازیابی اطلاعات", "Restore data", "Verileri geri yükle", "استعادة البيانات")
    val importConfirmMsg get() = t("اطلاعات فعلی با محتوای فایل جایگزین می‌شود. ادامه می‌دهید؟", "Your current data will be replaced by the file's content. Continue?", "Mevcut verilerin yerine dosyadaki içerik geçecek. Devam edilsin mi?", "سيتم استبدال بياناتك الحالية بمحتوى الملف. هل تريد المتابعة؟")
    val refreshAll get() = t("بروزرسانی همه‌ی سریال‌ها", "Refresh all series", "Tüm dizileri yenile", "تحديث جميع المسلسلات")
    val refreshingAll get() = t("در حال بروزرسانی…", "Refreshing…", "Yenileniyor…", "جارٍ التحديث…")
    val about get() = t("نسخه‌ی ۳٫۰ — ذخیره‌ی اطلاعات فقط روی همین دستگاه است.", "Version 3.0 — your data is stored only on this device.", "Sürüm 3.0 — verilerin yalnızca bu cihazda saklanır.", "الإصدار 3.0 — بياناتك محفوظة على هذا الجهاز فقط.")

    // ---- API keys (TMDB + SubDL)
    val apiKeyTitle get() = t("کلید TMDB (اطلاعات فیلم‌ها)", "TMDB API key (movie data)", "TMDB anahtarı (film verileri)", "مفتاح TMDB (بيانات الأفلام)")
    val apiKeyDefault get() = t("در حال استفاده از کلید پیش‌فرض برنامه هستید. اگر جستجو کار نکرد یا خواستید کلید خودتان را بگذارید، کلید جدید را اینجا وارد کنید.", "You are using the app's built-in key. If search stops working, or you prefer your own key, enter it here.", "Uygulamanın yerleşik anahtarını kullanıyorsun. Arama çalışmazsa veya kendi anahtarını kullanmak istersen buraya gir.", "تستخدم المفتاح المدمج في التطبيق. إذا توقف البحث أو أردت مفتاحك الخاص، أدخله هنا.")
    val apiKeyCustom get() = t("در حال استفاده از کلید شخصی شما هستید.", "You are using your own key.", "Kendi anahtarını kullanıyorsun.", "تستخدم مفتاحك الخاص.")
    val apiKeyPlaceholder get() = t("کلید جدید را اینجا بچسبانید", "Paste your new key here", "Yeni anahtarı buraya yapıştır", "الصق المفتاح الجديد هنا")
    val testKey get() = t("تست اتصال", "Test connection", "Bağlantıyı test et", "اختبار الاتصال")
    val resetKey get() = t("بازگشت به کلید پیش‌فرض", "Use built-in key", "Yerleşik anahtara dön", "العودة إلى المفتاح الافتراضي")
    val howToKey get() = t("راهنمای گرفتن API Key جدید", "How to get a new API key", "Yeni API anahtarı nasıl alınır", "كيفية الحصول على مفتاح جديد")
    val openTmdb get() = t("باز کردن صفحه‌ی API در TMDB", "Open the TMDB API page", "TMDB API sayfasını aç", "افتح صفحة API في TMDB")
    val openSignup get() = t("ثبت‌نام در TMDB", "Sign up on TMDB", "TMDB'ye kaydol", "التسجيل في TMDB")
    val subsKeyTitle get() = t("کلید SubDL (زیرنویس)", "SubDL API key (subtitles)", "SubDL anahtarı (altyazı)", "مفتاح SubDL (الترجمة)")
    val subsKeyDefault get() = t("از کلید پیش‌فرض برنامه برای زیرنویس استفاده می‌شود. اگر دانلود زیرنویس کار نکرد یا سهمیه تمام شد، کلید خودتان را وارد کنید.", "The app's built-in key is used for subtitles. If downloads stop working or the quota runs out, enter your own key.", "Altyazılar için uygulamanın yerleşik anahtarı kullanılır. İndirme çalışmazsa veya kota dolarsa kendi anahtarını gir.", "يُستخدم المفتاح المدمج للترجمة. إذا توقف التنزيل أو انتهت الحصة، أدخل مفتاحك الخاص.")
    val subsGuideBtn get() = t("راهنمای گرفتن کلید SubDL", "How to get a SubDL key", "SubDL anahtarı nasıl alınır", "كيفية الحصول على مفتاح SubDL")
    val openSubdlSite get() = t("باز کردن سایت SubDL", "Open the SubDL website", "SubDL sitesini aç", "افتح موقع SubDL")
    val openSubdlApi get() = t("باز کردن صفحه‌ی API در SubDL", "Open the SubDL API page", "SubDL API sayfasını aç", "افتح صفحة API في SubDL")

    val guideSteps: List<String> get() = when (lang) {
        Lang.FA -> listOf(
            "۱. مرورگر گوشی را باز کنید و به سایت themoviedb.org بروید. اگر باز نشد، از VPN استفاده کنید.",
            "۲. از دکمه‌ی Sign Up یک حساب رایگان بسازید و ایمیل خود را از پیام تأیید فعال کنید.",
            "۳. وارد حسابتان شوید. روی عکس پروفایل (بالا سمت راست) بزنید و Settings را انتخاب کنید.",
            "۴. در منوی کناری روی API بزنید. (صفحه‌ی مستقیم: themoviedb.org/settings/api)",
            "۵. روی Create بزنید و نوع Developer (شخصی) را انتخاب کنید. شرایط استفاده را بپذیرید.",
            "۶. فرم را پر کنید: نام برنامه (مثلاً Didam)، آدرس سایت (هر آدرسی مثل http://localhost)، توضیح کوتاه و مشخصات تماس. سپس Submit بزنید.",
            "۷. در همان صفحه، کلید API Key (v3 auth) را می‌بینید که ۳۲ حرف و عدد است. آن را کپی کنید. توکن طولانی «API Read Access Token» هم قابل استفاده است.",
            "۸. برگردید به همین صفحه، کلید را در کادر بچسبانید، «ذخیره» و بعد «تست اتصال» را بزنید."
        )
        Lang.EN -> listOf(
            "1. Open themoviedb.org in your browser (use a VPN if it doesn't load).",
            "2. Tap Sign Up, create a free account and confirm your email.",
            "3. Log in, tap your profile picture (top right) and choose Settings.",
            "4. In the side menu tap API. (Direct page: themoviedb.org/settings/api)",
            "5. Tap Create, choose the Developer (personal) type and accept the terms.",
            "6. Fill in the form: app name (e.g. Didam), any website URL (e.g. http://localhost), a short description and your contact details, then Submit.",
            "7. The page now shows your API Key (v3 auth), 32 letters and digits. Copy it. The long “API Read Access Token” works too.",
            "8. Come back here, paste the key in the box, tap Save and then Test connection."
        )
        Lang.TR -> listOf(
            "1. Tarayıcıda themoviedb.org adresini aç (açılmazsa VPN kullan).",
            "2. Sign Up'a dokun, ücretsiz hesap oluştur ve e-postanı onayla.",
            "3. Giriş yap, sağ üstteki profil resmine dokun ve Settings'i seç.",
            "4. Yan menüden API'ye dokun. (Doğrudan sayfa: themoviedb.org/settings/api)",
            "5. Create'e dokun, Developer (kişisel) türünü seç ve koşulları kabul et.",
            "6. Formu doldur: uygulama adı (örn. Didam), herhangi bir site adresi (örn. http://localhost), kısa açıklama ve iletişim bilgileri; sonra gönder.",
            "7. Sayfada görünen API Key (v3 auth) değerini kopyala (32 harf/rakam). Uzun “API Read Access Token” da kullanılabilir.",
            "8. Buraya dön, anahtarı kutuya yapıştır, Kaydet'e ve ardından Bağlantıyı test et'e dokun."
        )
        Lang.AR -> listOf(
            "1. افتح موقع themoviedb.org في المتصفح (استخدم VPN إذا لم يفتح).",
            "2. اضغط Sign Up وأنشئ حساباً مجانياً ثم أكّد بريدك الإلكتروني.",
            "3. سجّل الدخول واضغط على صورة ملفك الشخصي ثم Settings.",
            "4. من القائمة الجانبية اختر API. (الرابط المباشر: themoviedb.org/settings/api)",
            "5. اضغط Create واختر نوع Developer (شخصي) ووافق على الشروط.",
            "6. املأ النموذج: اسم التطبيق (مثلاً Didam) وأي رابط موقع (مثل http://localhost) ووصفاً قصيراً وبيانات الاتصال ثم أرسل.",
            "7. انسخ API Key (v3 auth) المكوّن من 32 حرفاً ورقماً. كما يمكن استخدام API Read Access Token الطويل.",
            "8. ارجع إلى هنا والصق المفتاح ثم اضغط حفظ ثم اختبار الاتصال."
        )
    }

    val subsGuideSteps: List<String> get() = when (lang) {
        Lang.FA -> listOf(
            "۱. مرورگر را باز کنید و به سایت subdl.com بروید. اگر باز نشد، از VPN استفاده کنید.",
            "۲. روی Sign Up (ثبت‌نام) بزنید و یک حساب رایگان بسازید، سپس وارد حسابتان شوید.",
            "۳. در پنل کاربری به بخش API (یا Developers) بروید. آدرس مستقیم: subdl.com/panel/api",
            "۴. روی دکمه‌ی ساخت کلید (Create / Generate) بزنید.",
            "۵. کلید ساخته‌شده را کپی کنید (یک رشته‌ی طولانی از حروف و عدد).",
            "۶. به همین صفحه برگردید، کلید را در کادر بالا بچسبانید، «ذخیره» و بعد «تست اتصال» را بزنید."
        )
        Lang.EN -> listOf(
            "1. Open subdl.com in your browser (use a VPN if it doesn't load).",
            "2. Tap Sign Up, create a free account and log in.",
            "3. In your account panel open the API (or Developers) section. Direct page: subdl.com/panel/api",
            "4. Tap the button that creates an API key (Create / Generate).",
            "5. Copy the generated key (a long string of letters and digits).",
            "6. Come back here, paste the key in the box above, tap Save and then Test connection."
        )
        Lang.TR -> listOf(
            "1. Tarayıcıda subdl.com adresini aç (açılmazsa VPN kullan).",
            "2. Sign Up'a dokun, ücretsiz hesap oluştur ve giriş yap.",
            "3. Hesap panelinde API (veya Developers) bölümünü aç. Doğrudan sayfa: subdl.com/panel/api",
            "4. API anahtarı oluşturma düğmesine (Create / Generate) dokun.",
            "5. Oluşturulan anahtarı kopyala (uzun harf ve rakam dizisi).",
            "6. Buraya dön, anahtarı yukarıdaki kutuya yapıştır, Kaydet'e ve ardından Bağlantıyı test et'e dokun."
        )
        Lang.AR -> listOf(
            "1. افتح subdl.com في المتصفح (استخدم VPN إذا لم يفتح).",
            "2. اضغط Sign Up وأنشئ حساباً مجانياً ثم سجّل الدخول.",
            "3. من لوحة حسابك افتح قسم API (أو Developers). الرابط المباشر: subdl.com/panel/api",
            "4. اضغط زر إنشاء مفتاح (Create / Generate).",
            "5. انسخ المفتاح الناتج (سلسلة طويلة من الحروف والأرقام).",
            "6. ارجع إلى هنا والصق المفتاح ثم اضغط حفظ ثم اختبار الاتصال."
        )
    }

    // ---- messages / errors
    fun message(key: String): String = when {
        key == "export_ok" -> t("بکاپ ذخیره شد.", "Backup saved.", "Yedek kaydedildi.", "تم حفظ النسخة الاحتياطية.")
        key == "import_ok" -> t("اطلاعات بازیابی شد.", "Data restored.", "Veriler geri yüklendi.", "تمت استعادة البيانات.")
        key == "backup_failed" -> t("عملیات ناموفق بود. فایل را بررسی کنید.", "Operation failed. Check the file.", "İşlem başarısız. Dosyayı kontrol et.", "فشلت العملية. تحقق من الملف.")
        key == "key_ok" -> t("✓ اتصال موفق بود. کلید معتبر است.", "✓ Connected. The key works.", "✓ Bağlandı. Anahtar geçerli.", "✓ تم الاتصال. المفتاح صالح.")
        key == "key_saved" -> t("کلید ذخیره شد.", "Key saved.", "Anahtar kaydedildi.", "تم حفظ المفتاح.")
        key == "sub_saved" -> t("زیرنویس ذخیره شد.", "Subtitle saved.", "Altyazı kaydedildi.", "تم حفظ الترجمة.")
        key == "dismissed_cleared" -> t("پیشنهادهای ردشده دوباره نمایش داده می‌شوند.", "Dismissed suggestions will show again.", "Reddedilen öneriler tekrar gösterilecek.", "ستظهر الاقتراحات المرفوضة مجدداً.")
        key.startsWith("refresh_done:") -> refreshed(key.removePrefix("refresh_done:").toIntOrNull() ?: 0)
        else -> error(key)
    }

    fun error(kind: String): String = when (kind) {
        "no_key" -> t("API Key وارد نشده است.", "API key is missing.", "API anahtarı eksik.", "مفتاح API غير موجود.")
        "bad_key" -> t("API Key نامعتبر است. از تنظیمات کلید را بررسی کنید.", "The API key is invalid. Check it in Settings.", "API anahtarı geçersiz. Ayarlardan kontrol et.", "مفتاح API غير صالح. تحقق منه في الإعدادات.")
        "server" -> t("خطای سرور TMDB. کمی بعد دوباره تلاش کنید.", "TMDB server error. Try again later.", "TMDB sunucu hatası. Daha sonra tekrar dene.", "خطأ في خادم TMDB. حاول لاحقاً.")
        "network" -> t("اتصال به TMDB برقرار نشد. اینترنت (یا VPN) را بررسی کنید.", "Couldn't reach TMDB. Check your internet (or VPN).", "TMDB'ye ulaşılamadı. İnterneti (veya VPN'i) kontrol et.", "تعذر الاتصال بـ TMDB. تحقق من الإنترنت (أو VPN).")
        "subs_bad_key" -> t("کلید SubDL نامعتبر است. در تنظیمات کلید را بررسی کنید.", "The SubDL key is invalid. Check it in Settings.", "SubDL anahtarı geçersiz. Ayarlardan kontrol et.", "مفتاح SubDL غير صالح. تحقق منه في الإعدادات.")
        "subs_limit" -> t("سهمیه‌ی SubDL تمام شد. کمی بعد یا با کلید شخصی امتحان کنید.", "SubDL quota reached. Try later or use your own key.", "SubDL kotası doldu. Daha sonra dene veya kendi anahtarını kullan.", "انتهت حصة SubDL. حاول لاحقاً أو استخدم مفتاحك الخاص.")
        "subs_server" -> t("خطای سرور SubDL. بعداً دوباره تلاش کنید.", "SubDL server error. Try again later.", "SubDL sunucu hatası. Daha sonra tekrar dene.", "خطأ في خادم SubDL. حاول لاحقاً.")
        "subs_network" -> t("اتصال به SubDL برقرار نشد. اینترنت (یا VPN) را بررسی کنید.", "Couldn't reach SubDL. Check your internet (or VPN).", "SubDL'ye ulaşılamadı. İnterneti (veya VPN'i) kontrol et.", "تعذر الاتصال بـ SubDL. تحقق من الإنترنت (أو VPN).")
        "subs_notfound" -> t("فایل زیرنویس در دسترس نیست.", "The subtitle file isn't available.", "Altyazı dosyasına ulaşılamıyor.", "ملف الترجمة غير متاح.")
        else -> t("خطای غیرمنتظره رخ داد.", "Something went wrong.", "Beklenmeyen bir hata oluştu.", "حدث خطأ غير متوقع.")
    }
}

val LocalStrings = staticCompositionLocalOf { Strings(Lang.EN) }
val LocalJalali = staticCompositionLocalOf { false }
