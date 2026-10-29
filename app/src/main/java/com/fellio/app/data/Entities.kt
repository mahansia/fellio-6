package com.fellio.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** id = "movie_123" or "tv_456" */
@Entity(tableName = "titles")
data class TitleEntity(
    @PrimaryKey val id: String,
    val tmdbId: Int,
    val mediaType: String,
    val name: String,
    val year: String,
    val posterPath: String?,
    val overview: String,
    val movieWatched: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0") val inWatchlist: Boolean = false,
    @ColumnInfo(defaultValue = "0") val rating: Double = 0.0,
    @ColumnInfo(defaultValue = "''") val genreIds: String = "",
    @ColumnInfo(defaultValue = "0") val runtime: Int = 0,
    @ColumnInfo(defaultValue = "''") val castNames: String = "",
    @ColumnInfo(defaultValue = "''") val backdropPath: String = "",
    /** "id|name|profilePath" triples separated by ";;" */
    @ColumnInfo(defaultValue = "''") val castJson: String = "",
    @ColumnInfo(defaultValue = "''") val originalLang: String = ""
)

data class CastMember(val personId: Int, val name: String, val profilePath: String?)

fun TitleEntity.castMembers(): List<CastMember> {
    if (castJson.isBlank()) return emptyList()
    return castJson.split(";;").mapNotNull { row ->
        val parts = row.split("|")
        if (parts.size < 2) return@mapNotNull null
        val id = parts[0].toIntOrNull() ?: return@mapNotNull null
        CastMember(id, parts[1], parts.getOrNull(2)?.ifBlank { null })
    }
}

fun List<CastMember>.toJson(): String = joinToString(";;") { "${it.personId}|${it.name}|${it.profilePath ?: ""}" }

fun TitleEntity.isTv() = mediaType == "tv"

/** Anime = Japanese-language animation. */
fun TitleEntity.isAnime() = originalLang == "ja" && genreIds.split(",").contains("16")

@Entity(
    tableName = "episodes",
    foreignKeys = [ForeignKey(
        entity = TitleEntity::class,
        parentColumns = ["id"],
        childColumns = ["titleId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("titleId")]
)
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleId: String,
    val season: Int,
    val episode: Int,
    val name: String,
    val airDate: String,
    val watched: Boolean = false,
    @ColumnInfo(defaultValue = "0") val runtime: Int = 0,
    @ColumnInfo(defaultValue = "1") val notified: Boolean = true
)


@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val icon: String,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_items",
    primaryKeys = ["playlistId", "titleId"],
    foreignKeys = [
        ForeignKey(entity = PlaylistEntity::class, parentColumns = ["id"], childColumns = ["playlistId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TitleEntity::class, parentColumns = ["id"], childColumns = ["titleId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("titleId"), Index("playlistId")]
)
data class PlaylistItemEntity(
    val playlistId: Long,
    val titleId: String,
    val position: Long = System.currentTimeMillis()
)
