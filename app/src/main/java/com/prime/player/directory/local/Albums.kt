package com.prime.player.directory.local

import android.provider.MediaStore
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.prime.player.Theme
import com.prime.player.caption2
import com.prime.player.common.ContentElevation
import com.prime.player.common.ContentPadding
import com.prime.player.core.Repository
import com.prime.player.core.compose.Image
import com.prime.player.core.compose.ToastHostState
import com.prime.player.core.compose.show
import com.prime.player.core.db.Album
import com.prime.player.core.db.Audio
import com.prime.player.core.playback.Remote
import com.prime.player.core.uri
import com.prime.player.directory.*
import com.prime.player.primaryContainer
import com.prime.player.small2
import com.primex.core.Text
import com.primex.ui.Label
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import com.prime.player.R
import com.prime.player.common.LocalNavController
import com.primex.ui.Rose

private const val TAG = "AlbumsViewModel"

private val Album.firstTitleChar
    inline get() = title.uppercase(Locale.ROOT)[0].toString()

typealias Albums = AlbumsViewModel.Companion

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val repository: Repository,
    private val toaster: ToastHostState,
    private val remote: Remote,
) : DirectoryViewModel<Album>(handle) {

    companion object {
        private const val HOST = "_local_audio_albums"

        val route = compose(HOST)
        fun direction(
            query: String = NULL_STRING,
            order: GroupBy = GroupBy.Name,
            ascending: Boolean = true,
            viewType: ViewType = ViewType.List
        ) = compose(HOST, NULL_STRING, query, order, ascending, viewType)
    }

    init {
        // emit the name to meta
        //TODO: Add other fields in future versions.
        meta = MetaData(Text("Albums"))
    }

    override fun toggleViewType() {
        // we only currently support single viewType. Maybe in future might support more.
        viewModelScope.launch {
            toaster.show("Toggle not implemented yet.", "ViewType")
        }
    }

    override val mActions: List<Action?> = emptyList()
    override val actions: List<Action> = emptyList()
    override val orders: List<GroupBy> = listOf(GroupBy.None, GroupBy.Name, GroupBy.Artist)
    private val GroupBy.toMediaOrder
        get() = when (this) {
            GroupBy.None -> MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
            GroupBy.Name -> MediaStore.Audio.Albums.ALBUM
            GroupBy.Artist -> MediaStore.Audio.Albums.ARTIST
            else -> error("Invalid order: $this ")
        }

    override val data: Flow<Mapped<Album>> =
        repository.observe(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI)
            .combine(filter) { f1, f2 -> f2 }.map {
                val (order, query, ascending) = it
                val list = repository.getAlbums(query, order.toMediaOrder, ascending)
                when (order) {
                    GroupBy.None -> mapOf(Text("") to list)
                    GroupBy.Name -> list.groupBy { album -> Text(album.firstTitleChar) }
                    GroupBy.Artist -> list.groupBy { album -> Text(album.artist) }
                    else -> error("$order invalid")
                }
            }
            .catch {
                // any exception.
                toaster.show(
                    "Some unknown error occured!.",
                    "Error",
                    leading = Icons.Outlined.Error,
                    accent = Color.Rose,
                    duration = ToastHostState.Duration.Indefinite
                )
            }
}

private val TILE_WIDTH = 80.dp
private val GridItemPadding =
    PaddingValues(vertical = 6.dp, horizontal = 10.dp)

@Composable
fun Album(
    value: Album,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            // clip the ripple
            .clip(Theme.shapes.medium)
            .then(modifier)
            // add padding after size.
            .padding(GridItemPadding)
            // add preferred size with min/max width
            .then(Modifier.width(TILE_WIDTH))
            // wrap the height of the content
            .wrapContentHeight(),
    ) {

        Surface(
            shape = Theme.shapes.small2,
            elevation = ContentElevation.medium,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.65f),
            content = {
                Image(
                    data = value.uri,
                    fallback = painterResource(id = R.drawable.default_art)
                )
            },
        )

        // title
        Label(
            text = value.title,
            maxLines = 2,
            modifier = Modifier.padding(top = ContentPadding.medium),
            style = Theme.typography.caption,
        )

        // Subtitle
        Label(
            text = "Year: ${value.firstYear}",
            style = Theme.typography.caption2
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Albums(viewModel: AlbumsViewModel) {
    val navigator = LocalNavController.current
    Directory(
        viewModel = viewModel,
        cells = GridCells.Adaptive(TILE_WIDTH + (4.dp * 2)),
        onAction = { /*TODO: Currently we don't support more actions.*/},
        key = { it.id },
        contentPadding = PaddingValues(horizontal = ContentPadding.normal),
    ) {
        Album(
            value = it,
            modifier = Modifier
                .clickable {
                    val direction = Audios.direction(Audios.GET_FROM_ALBUM, it.title)
                    navigator.navigate(direction)
                }
                .animateItemPlacement()
        )
    }
}