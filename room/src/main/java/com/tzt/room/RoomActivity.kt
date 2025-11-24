package com.tzt.room

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tzt.room.model.Playlist
import com.tzt.room.model.PlaylistSongCrossRef
import com.tzt.room.model.Song
import kotlinx.coroutines.launch

class RoomActivity : AppCompatActivity(), OnClickListener {
    companion object {
        const val TAG = "RoomActivity"
    }

    lateinit var etSong: EditText
    lateinit var etPlaylist: EditText
    lateinit var tvSongLog: TextView
    lateinit var tvSongWithListsLog: TextView
    lateinit var tvDisplayListLog: TextView
    lateinit var tvDisplayListWithSongsLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_room)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
    }

    private fun initView() {
        etSong = findViewById(R.id.et_song)
        etPlaylist = findViewById(R.id.et_playlist)

        tvSongLog = findViewById(R.id.tv_song_log)
        tvSongWithListsLog = findViewById(R.id.tv_song_with_displaylists_log)
        tvDisplayListLog = findViewById(R.id.tv_displaylist_log)
        tvDisplayListWithSongsLog = findViewById(R.id.tv_displaylist_with_songs_log)

        findViewById<Button>(R.id.bt_add_song).apply {
            setOnClickListener(this@RoomActivity)
        }

        findViewById<Button>(R.id.bt_query_song).apply {
            setOnClickListener(this@RoomActivity)
        }

        findViewById<Button>(R.id.bt_delete_song).apply {
            setOnClickListener(this@RoomActivity)
        }

        findViewById<Button>(R.id.bt_add_playlist).apply {
            setOnClickListener(this@RoomActivity)
        }

        findViewById<Button>(R.id.bt_query_playlist).apply {
            setOnClickListener(this@RoomActivity)
        }

        findViewById<Button>(R.id.bt_delete_playlist).apply {
            setOnClickListener(this@RoomActivity)
        }

        findViewById<Button>(R.id.bt_add_song_and_displaylist).apply {
            setOnClickListener(this@RoomActivity)
        }

        findViewById<Button>(R.id.bt_query_song_with_displaylists).apply {
            setOnClickListener(this@RoomActivity)
        }

        findViewById<Button>(R.id.bt_query_displaylist_with_songs).apply {
            setOnClickListener(this@RoomActivity)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bt_add_song -> addSong()
            R.id.bt_query_song -> {
                v.isClickable = false
                collectSong()
            }

            R.id.bt_delete_song -> deleteSong()
            R.id.bt_add_playlist -> addPlaylist()
            R.id.bt_query_playlist -> {
                v.isClickable = false
                collectPlaylist()
            }

            R.id.bt_delete_playlist -> deletePlaylist()

            //
            R.id.bt_add_song_and_displaylist -> {
                addPairOfSong2DisplayList()
            }

            R.id.bt_query_song_with_displaylists -> {
                collectSongWithDisplaylists()
            }

            R.id.bt_query_displaylist_with_songs -> {
                collectDisplayListWithSongs()
            }
        }
    }


    private fun addSong() {
        val songName = etSong.text.toString()
        workScope.launch {
            musicDatabase.musicDao().addSong(Song(name = "song $songName"))
        }
    }

    private fun collectSong() {
        workScope.launch {
            musicDatabase.musicDao().querySong().collect {
                Log.d(TAG, "[collectSong] song data is -> $it")
                val logBuilder = StringBuilder()
                it.forEach {
                    logBuilder.append("$it\n")
                }
                mainScope.launch {
                    tvSongLog.text = "[collectSong] song data is [${it.size}] ->\n $logBuilder"
                }
            }
        }
    }

    private fun deleteSong() {
        val songId = etSong.text.toString().toIntOrNull() ?: 0
        workScope.launch {
            musicDatabase.musicDao().deleteSong(songId)
        }
    }

    private fun addPlaylist() {
        val displayName = etPlaylist.text.toString()
        workScope.launch {
            musicDatabase.musicDao().addPlaylist(Playlist(name = "playlist $displayName"))
        }
    }

    private fun collectPlaylist() {
        workScope.launch {
            musicDatabase.musicDao().queryPlaylist().collect {
                Log.d(TAG, "[collectPlaylist] playlist data is -> $it")
                val logBuilder = StringBuilder()
                it.forEach {
                    logBuilder.append("$it\n")
                }
                mainScope.launch {
                    tvDisplayListLog.text =
                        "[collectPlaylist] playlist data is [${it.size}] ->\n $logBuilder"
                }
            }
        }
    }

    private fun deletePlaylist() {
        val displayId = etPlaylist.text.toString().toIntOrNull() ?: 0
        workScope.launch {
            musicDatabase.musicDao().deletePlaylistAll(displayId)
        }
    }

    private fun addPairOfSong2DisplayList() {
        val songId = etSong.text.toString().toIntOrNull() ?: 0
        val playlistId = etPlaylist.text.toString().toIntOrNull() ?: 0
        workScope.launch {
            musicDatabase.musicDao().addSongToPlaylist(PlaylistSongCrossRef(songId, playlistId))
        }
    }

    private fun collectSongWithDisplaylists() {
        val songId = etSong.text.toString().toIntOrNull() ?: 0
        workScope.launch {
            musicDatabase.musicDao().querySongWithPlaylist(songId).collect {
                Log.d(TAG, "[collectSongWithDisplaylists] song is -> ${it?.song}")
                val logBuilder =
                    StringBuilder("[collectSongWithDisplaylists] \n当前歌曲: -> ${it?.song}\n")
                it?.lists?.forEach {
                    logBuilder.append("所在播放歌单:$it\n")
                }
                mainScope.launch {
                    tvSongWithListsLog.text =
                        "[collectSongWithDisplaylists] playlist data is ->\n $logBuilder"
                }
            }
        }
    }

    private fun collectDisplayListWithSongs() {
        val displayId = etPlaylist.text.toString().toIntOrNull() ?: 0
        workScope.launch {
            musicDatabase.musicDao().queryPlaylistWithSong(displayId).collect {
                Log.d(TAG, "[collectDisplayListWithSongs] playlist is -> ${it?.playlist}")
                val logBuilder =
                    StringBuilder("[collectDisplayListWithSongs] \n当前播放列表： -> ${it?.playlist}\n")
                it?.songs?.forEach {
                    logBuilder.append("所包含音乐:$it\n")
                }
                mainScope.launch {
                    tvDisplayListWithSongsLog.text =
                        "[collectDisplayListWithSongs] playlist data is ->\n $logBuilder"
                }
            }
        }
    }

    //Expected performance impact from inlining is insignificant. Inlining works best for functions with parameters of function types.
    private inline fun testInline(noinline block: (a: Int) -> Int) {
        Log.d(TAG, "testInline()...")
        block(1)
        testA(block)
        workScope.launch {
            block(1)
        }
    }

    private fun testA(a: (Int) -> Int) {

    }

}