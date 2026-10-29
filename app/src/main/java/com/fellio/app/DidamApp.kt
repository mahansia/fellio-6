package com.fellio.app

import android.app.Application
import com.fellio.app.data.AppSettings
import com.fellio.app.data.DidamDb
import com.fellio.app.data.Notifier
import com.fellio.app.data.Platform
import com.fellio.app.data.Repository
import com.fellio.app.data.SubtitlesApi
import com.fellio.app.data.TmdbApi
import java.util.Locale

class DidamApp : Application() {
    val settings by lazy { AppSettings(this, Locale.getDefault().language) }
    val history by lazy { com.fellio.app.data.HistoryStore(this) }
    val repo by lazy {
        Repository(
            DidamDb.get(this),
            TmdbApi({ settings.effectiveKey }, { settings.lang.value.tmdb }, { settings.titleMode.value }, { settings.posterLang.value }, { settings.titleParen.value }),
            SubtitlesApi({ settings.effectiveSubsKey })
        )
    }

    override fun onCreate() {
        super.onCreate()
        Platform.appContext = this
        Notifier.ensureChannel(this)
        if (settings.notifications.value) Notifier.schedule(this)
    }
}
