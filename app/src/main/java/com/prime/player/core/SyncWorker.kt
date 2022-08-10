package com.prime.player.core

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.prime.player.common.FileUtils
import com.prime.player.common.MediaUtil
import com.prime.player.common.getAlbumArt
import com.primex.core.runCatching
import com.primex.preferences.Preferences
import com.primex.preferences.longPreferenceKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext as using

private const val TAG = "SyncWorker"


private val AUDIO_PROJECTION
    get() = arrayOf(
        MediaStore.MediaColumns._ID, //0
        MediaStore.MediaColumns.TITLE, // 1
        MediaStore.Audio.AudioColumns.ARTIST, // 2
        MediaStore.Audio.AudioColumns.ALBUM, // 3
        MediaStore.Audio.AudioColumns.ALBUM_ID, // 4
        MediaStore.Audio.AudioColumns.DATE_ADDED,  //5
        MediaStore.Audio.AudioColumns.COMPOSER, // , // 6
        MediaStore.Audio.AudioColumns.YEAR, // 7
        MediaStore.Audio.AudioColumns.DATA, // 8
        MediaStore.Audio.AudioColumns.DURATION, // 9
        MediaStore.Audio.AudioColumns.MIME_TYPE, // 10
        MediaStore.Audio.AudioColumns.TRACK, // 11
        MediaStore.MediaColumns.SIZE, //12
        MediaStore.Audio.AudioColumns.DATE_MODIFIED, // 14
    )

private fun Audio(
    cursor: Cursor,
    retriever: MediaMetadataRetriever
): Audio {
    val path = cursor.getString(8)
    val genre = runCatching(TAG) {
        retriever.setDataSource(path)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
    }
    return with(cursor) {
        Audio(
            id = getLong(0),
            title = getString(1) ?: MediaStore.UNKNOWN_STRING,
            albumId = getLong(4),
            path = path,
            parent = FileUtils.parent(path),
            album = getString(3) ?: MediaStore.UNKNOWN_STRING,
            artist = getString(2) ?: MediaStore.UNKNOWN_STRING,
            composer = getString(6) ?: MediaStore.UNKNOWN_STRING,
            mimeType = getString(10),
            track = getInt(11),
            dateAdded = getLong(5),
            dateModified = getLong(13),
            duration = getInt(9),
            size = getLong(12),
            year = getInt(7),
            genre = genre ?: MediaStore.UNKNOWN_STRING
        )
    }
}

private const val IMMEDIATE_UPDATE_WORKER = "immediate_update_worker"
private const val TRIGGER_UPDATE_WORKER = "trigger_update_worker"

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params = params) {

    companion object {
        /**
         * Runs Immediate Works.
         */
        fun run(context: Context) {
            // run on app start up for first time
            val workManager = WorkManager.getInstance(context.applicationContext)
            val work = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(Constraints.Builder().build())
                .build()
            workManager.enqueueUniqueWork(
                IMMEDIATE_UPDATE_WORKER,
                ExistingWorkPolicy.REPLACE,
                work
            )
        }

        /**
         * Schedule works on content Uri change.
         */
        @RequiresApi(Build.VERSION_CODES.N)
        fun schedule(context: Context) {
            // run on app start up for first time
            val workManager = WorkManager.getInstance(context.applicationContext)
            val work = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .addContentUriTrigger(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true)
                        // .addContentUriTrigger(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true)
                        .build()
                )
                .build()
            workManager.enqueueUniqueWork(
                TRIGGER_UPDATE_WORKER,
                ExistingWorkPolicy.REPLACE,
                work
            )
        }

        /**
         * The name of the poster file. Must be unique
         */
        private const val AUDIO_POSTER_UNIQUE_TAG = TAG + "_audio_poster"

        /**
         * The time duration of the audio poster.
         */
        val KEY_AUDIO_POSTER_MILLS = longPreferenceKey(AUDIO_POSTER_UNIQUE_TAG)

        /**
         * The Max number of artworks used in the poster.
         */
        const val MAX_POSTER_ALBUMS = 30

        /**
         * The size of the individual artwork image used in the poster.
         */
        const val POSTER_ARTWORK_SIZE = 256

        /**
         * The duration in mills the individual artwork is played by the lib.
         */
        const val BITMAP_DURATION = 1_000L //ms
    }

    @Inject
    lateinit var audioDb: Audios

    @Inject
    lateinit var preferences: Preferences

    /**
     * Compute and save the art work.
     */
    private suspend fun poster() {
        val albums = audioDb.getRecentAlbums(MAX_POSTER_ALBUMS)
        // don't proceed if the list is empty.
        if (albums.isEmpty())
            return

        // collect bitmaps of albums.
        val bitmaps = ArrayList<Bitmap>()
        albums.forEach { album ->
            // compute uri using the album id
            val uri = MediaUtil.composeAlbumArtUri(album.id)
            //fetch the artwork
            val artwork = context.getAlbumArt(uri = uri, POSTER_ARTWORK_SIZE)?.toBitmap()
            //add to list
            if (artwork != null) bitmaps.add(artwork)
        }

        // only proceed if the bitmaps is not empty.
        if (bitmaps.isEmpty())
            return
        // The width and height of the resultant image.
        val width = bitmaps.size * POSTER_ARTWORK_SIZE
        val height = POSTER_ARTWORK_SIZE

        // paste bitmaps on the Canvas
        val background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(background)

        var x = 0f
        bitmaps.forEach { bitmap ->
            canvas.drawBitmap(bitmap, x, 0f, null)
            x += POSTER_ARTWORK_SIZE
        }

        val key = KEY_AUDIO_POSTER_MILLS

        //save using key name and increent the version number.
        context.openFileOutput(key.name, Context.MODE_PRIVATE).use { fos ->
            background.compress(Bitmap.CompressFormat.JPEG, 60, fos)
            //increment by 1/ms if the new is equal to old.
            // so the observer will be notified.
            val oldMills = with(preferences) { preferences[key].obtain() }
            var new = bitmaps.size * BITMAP_DURATION
            if (new == oldMills)
                new += 1
            preferences[key] = new
        }
    }

    override suspend fun doWork(): Result {
        val exception =
            kotlin.runCatching {
                val resolver = context.contentResolver
                val db = audioDb
                val retriever = MediaMetadataRetriever()

                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val proj = arrayOf(MediaStore.MediaColumns._ID)
                val keys = resolver.query(uri, proj, null, null, null)?.use { cursor ->
                    List(cursor.count) {
                        cursor.moveToPosition(it)
                        cursor.getLong(0)
                    }
                } ?: return Result.failure(Data.Builder().put("error", "keys == null").build())
                // delete all those which are not in keys
                db._delete(
                    keys.joinToString(separator = ",") {
                        "'$it'"
                    }
                )

                // get all available in content resolver
                val projection = AUDIO_PROJECTION
                var changed = false
                resolver.query(uri, projection, null, null, null)?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(0)
                        // insert not exist ones only
                        if (!db.exists(id)) {
                            changed = true
                            db.insert(
                                Audio(cursor, retriever)
                            )
                        }

                    }
                }
                if (changed)
                    poster()
            }
        return if (exception.isFailure){
            Result.failure(Data.Builder().put("error", exception.exceptionOrNull() ?: "").build())
        }else {
            Result.success()
        }
    }
}
