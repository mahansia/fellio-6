package com.fellio.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DidamDao {
    @Query("SELECT * FROM titles ORDER BY addedAt DESC")
    abstract fun titles(): Flow<List<TitleEntity>>

    @Query("SELECT * FROM titles WHERE id = :id")
    abstract fun title(id: String): Flow<TitleEntity?>

    @Query("SELECT * FROM titles WHERE id = :id")
    abstract suspend fun titleOnce(id: String): TitleEntity?

    @Query("SELECT * FROM titles")
    abstract suspend fun titlesOnce(): List<TitleEntity>

    @Query("SELECT * FROM titles WHERE mediaType = 'tv'")
    abstract suspend fun tvTitlesOnce(): List<TitleEntity>

    @Query("SELECT * FROM episodes ORDER BY season, episode")
    abstract fun allEpisodes(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes")
    abstract suspend fun allEpisodesOnce(): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE titleId = :id ORDER BY season, episode")
    abstract fun episodes(id: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE titleId = :id ORDER BY season, episode")
    abstract suspend fun episodesOnce(id: String): List<EpisodeEntity>

    @Query("SELECT COUNT(*) FROM titles WHERE id = :id")
    abstract suspend fun count(id: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertTitle(t: TitleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTitles(list: List<TitleEntity>)

    @Insert
    abstract suspend fun insertEpisodes(list: List<EpisodeEntity>)

    @Update
    abstract suspend fun updateEpisodes(list: List<EpisodeEntity>)

    @Update
    abstract suspend fun updateTitle(t: TitleEntity)

    @Transaction
    open suspend fun addTitleWithEpisodes(t: TitleEntity, eps: List<EpisodeEntity>) {
        if (insertTitle(t) != -1L) insertEpisodes(eps)
    }

    @Transaction
    open suspend fun replaceAll(titles: List<TitleEntity>, eps: List<EpisodeEntity>) {
        deleteAll()
        insertTitles(titles)
        insertEpisodes(eps)
    }

    @Query("DELETE FROM titles")
    abstract suspend fun deleteAll()

    @Query("DELETE FROM titles WHERE id = :id")
    abstract suspend fun deleteTitle(id: String)

    @Query("UPDATE titles SET movieWatched = :w WHERE id = :id")
    abstract suspend fun setMovieWatched(id: String, w: Boolean)

    @Query("UPDATE titles SET inWatchlist = :w WHERE id = :id")
    abstract suspend fun setWatchlist(id: String, w: Boolean)

    @Query("UPDATE episodes SET watched = :w WHERE id = :epId")
    abstract suspend fun setEpisodeWatched(epId: Long, w: Boolean)

    @Query("UPDATE episodes SET watched = :w WHERE titleId = :id AND season = :season")
    abstract suspend fun setSeasonWatched(id: String, season: Int, w: Boolean)

    @Query(
        "UPDATE episodes SET watched = 1 WHERE titleId = :id AND " +
            "(season < :season OR (season = :season AND episode <= :ep))"
    )
    abstract suspend fun markUpTo(id: String, season: Int, ep: Int)

    @Query(
        "SELECT * FROM episodes WHERE notified = 0 AND airDate != '' AND airDate <= :today " +
            "ORDER BY titleId, season, episode"
    )
    abstract suspend fun pendingNotifications(today: String): List<EpisodeEntity>

    @Query("UPDATE episodes SET notified = 1 WHERE id IN (:ids)")
    abstract suspend fun markNotified(ids: List<Long>)

    // ---- playlists
    @Query("SELECT * FROM playlists ORDER BY sortOrder")
    abstract fun playlists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists ORDER BY sortOrder")
    abstract suspend fun playlistsOnce(): List<PlaylistEntity>

    @Query("SELECT * FROM playlists WHERE id = :id")
    abstract fun playlist(id: Long): Flow<PlaylistEntity?>

    @Insert
    abstract suspend fun insertPlaylist(p: PlaylistEntity): Long

    @Update
    abstract suspend fun updatePlaylist(p: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    abstract suspend fun deletePlaylist(id: Long)

    @Query("SELECT * FROM playlist_items WHERE playlistId = :id ORDER BY position")
    abstract fun playlistItems(id: Long): Flow<List<PlaylistItemEntity>>

    @Query("SELECT * FROM playlist_items WHERE titleId = :titleId")
    abstract fun playlistsForTitle(titleId: String): Flow<List<PlaylistItemEntity>>

    @Query("SELECT playlistId FROM playlist_items WHERE titleId = :titleId")
    abstract suspend fun playlistIdsForTitleOnce(titleId: String): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addToPlaylist(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND titleId = :titleId")
    abstract suspend fun removeFromPlaylist(playlistId: Long, titleId: String)

    @Update
    abstract suspend fun updatePlaylistItems(items: List<PlaylistItemEntity>)

    @Query("SELECT * FROM playlist_items")
    abstract suspend fun allPlaylistItemsOnce(): List<PlaylistItemEntity>

    @Query("SELECT * FROM playlist_items")
    abstract fun allPlaylistItems(): Flow<List<PlaylistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPlaylists(list: List<PlaylistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPlaylistItems(list: List<PlaylistItemEntity>)

    @Query("DELETE FROM playlists")
    abstract suspend fun deleteAllPlaylists()

    @Query("DELETE FROM playlist_items")
    abstract suspend fun deleteAllPlaylistItems()

    @Transaction
    open suspend fun replaceAllWithPlaylists(
        titles: List<TitleEntity>, eps: List<EpisodeEntity>,
        playlists: List<PlaylistEntity>, items: List<PlaylistItemEntity>
    ) {
        deleteAll(); deleteAllPlaylists(); deleteAllPlaylistItems()
        insertTitles(titles); insertEpisodes(eps)
        insertPlaylists(playlists); insertPlaylistItems(items)
    }
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE titles ADD COLUMN inWatchlist INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE titles ADD COLUMN rating REAL NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE titles ADD COLUMN genreIds TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE titles ADD COLUMN runtime INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE titles ADD COLUMN castNames TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE titles ADD COLUMN backdropPath TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE episodes ADD COLUMN runtime INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE episodes ADD COLUMN notified INTEGER NOT NULL DEFAULT 1")
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE titles ADD COLUMN castJson TEXT NOT NULL DEFAULT ''")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS playlists (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT NOT NULL, color INTEGER NOT NULL, icon TEXT NOT NULL, " +
                "createdAt INTEGER NOT NULL, sortOrder INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS playlist_items (playlistId INTEGER NOT NULL, titleId TEXT NOT NULL, " +
                "position INTEGER NOT NULL, PRIMARY KEY(playlistId, titleId), " +
                "FOREIGN KEY(playlistId) REFERENCES playlists(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(titleId) REFERENCES titles(id) ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_playlist_items_titleId ON playlist_items(titleId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_playlist_items_playlistId ON playlist_items(playlistId)")
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE titles ADD COLUMN originalLang TEXT NOT NULL DEFAULT ''")
    }
}

@Database(
    entities = [TitleEntity::class, EpisodeEntity::class, PlaylistEntity::class, PlaylistItemEntity::class],
    version = 4, exportSchema = false
)
abstract class DidamDb : RoomDatabase() {
    abstract fun dao(): DidamDao

    companion object {
        @Volatile private var inst: DidamDb? = null
        fun get(ctx: Context): DidamDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(ctx.applicationContext, DidamDb::class.java, "didam.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build().also { inst = it }
        }
    }
}
