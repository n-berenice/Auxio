/*
 * Copyright (c) 2021 Auxio Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
 
package org.oxycblt.auxio.detail.recycler

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import org.oxycblt.auxio.IntegerTable
import org.oxycblt.auxio.R
import org.oxycblt.auxio.coil.bindAlbumCover
import org.oxycblt.auxio.coil.bindArtistImage
import org.oxycblt.auxio.databinding.ItemDetailBinding
import org.oxycblt.auxio.databinding.ItemParentBinding
import org.oxycblt.auxio.databinding.ItemSongBinding
import org.oxycblt.auxio.music.Album
import org.oxycblt.auxio.music.Artist
import org.oxycblt.auxio.music.Song
import org.oxycblt.auxio.ui.ArtistViewHolder
import org.oxycblt.auxio.ui.BindingViewHolder
import org.oxycblt.auxio.ui.Item
import org.oxycblt.auxio.ui.MenuItemListener
import org.oxycblt.auxio.ui.SimpleItemCallback
import org.oxycblt.auxio.util.context
import org.oxycblt.auxio.util.getPluralSafe
import org.oxycblt.auxio.util.inflater
import org.oxycblt.auxio.util.textSafe

/**
 * An adapter for displaying [Artist] information and it's children. Unlike the other adapters, this
 * one actually contains both album information and song information.
 * @author OxygenCobalt
 */
class ArtistDetailAdapter(listener: Listener) :
    DetailAdapter<DetailAdapter.Listener>(listener, DIFFER) {
    private var currentAlbum: Album? = null
    private var currentAlbumHolder: Highlightable? = null

    private var currentSong: Song? = null
    private var currentSongHolder: Highlightable? = null

    override fun getCreatorFromItem(item: Item) =
        super.getCreatorFromItem(item)
            ?: when (item) {
                is Artist -> ArtistDetailViewHolder.CREATOR
                is Album -> ArtistAlbumViewHolder.CREATOR
                is Song -> ArtistSongViewHolder.CREATOR
                else -> null
            }

    override fun getCreatorFromViewType(viewType: Int) =
        super.getCreatorFromViewType(viewType)
            ?: when (viewType) {
                ArtistDetailViewHolder.CREATOR.viewType -> ArtistDetailViewHolder.CREATOR
                ArtistAlbumViewHolder.CREATOR.viewType -> ArtistAlbumViewHolder.CREATOR
                ArtistSongViewHolder.CREATOR.viewType -> ArtistSongViewHolder.CREATOR
                else -> null
            }

    override fun onBind(viewHolder: RecyclerView.ViewHolder, item: Item, listener: Listener) {
        super.onBind(viewHolder, item, listener)
        when (item) {
            is Artist -> (viewHolder as ArtistDetailViewHolder).bind(item, listener)
            is Album -> (viewHolder as ArtistAlbumViewHolder).bind(item, listener)
            is Song -> (viewHolder as ArtistSongViewHolder).bind(item, listener)
            else -> {}
        }
    }

    override fun onHighlightViewHolder(viewHolder: Highlightable, item: Item) {
        // If the item corresponds to a currently playing song/album then highlight it
        if (item.id == currentAlbum?.id && item is Album) {
            currentAlbumHolder?.setHighlighted(false)
            currentAlbumHolder = viewHolder
            viewHolder.setHighlighted(true)
        } else if (item.id == currentSong?.id && item is Song) {
            currentSongHolder?.setHighlighted(false)
            currentSongHolder = viewHolder
            viewHolder.setHighlighted(true)
        } else {
            viewHolder.setHighlighted(false)
        }
    }

    /**
     * Update the current [album] that this adapter should highlight
     * @param recycler The recyclerview the highlighting should act on.
     */
    fun highlightAlbum(album: Album?, recycler: RecyclerView) {
        if (album == currentAlbum) return
        currentAlbum = album
        currentAlbumHolder?.setHighlighted(false)
        currentAlbumHolder = highlightItem(album, recycler)
    }

    /**
     * Update the [song] that this adapter should highlight
     * @param recycler The recyclerview the highlighting should act on.
     */
    fun highlightSong(song: Song?, recycler: RecyclerView) {
        if (song == currentSong) return
        currentSong = song
        currentSongHolder?.setHighlighted(false)
        currentSongHolder = highlightItem(song, recycler)
    }

    companion object {
        private val DIFFER =
            object : SimpleItemCallback<Item>() {
                override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                    return when {
                        oldItem is Artist && newItem is Artist ->
                            ArtistDetailViewHolder.DIFFER.areItemsTheSame(oldItem, newItem)
                        oldItem is Album && newItem is Album ->
                            ArtistAlbumViewHolder.DIFFER.areItemsTheSame(oldItem, newItem)
                        oldItem is Song && newItem is Song ->
                            ArtistSongViewHolder.DIFFER.areItemsTheSame(oldItem, newItem)
                        else -> DetailAdapter.DIFFER.areItemsTheSame(oldItem, newItem)
                    }
                }
            }
    }
}

private class ArtistDetailViewHolder private constructor(private val binding: ItemDetailBinding) :
    BindingViewHolder<Artist, DetailAdapter.Listener>(binding.root) {

    override fun bind(item: Artist, listener: DetailAdapter.Listener) {
        binding.detailCover.bindArtistImage(item)
        binding.detailName.textSafe = item.resolveName(binding.context)

        // Get the genre that corresponds to the most songs in this artist, which would be
        // the most "Prominent" genre.
        binding.detailSubhead.textSafe =
            item.songs
                .groupBy { it.genre.resolveName(binding.context) }
                .entries
                .maxByOrNull { it.value.size }
                ?.key
                ?: binding.context.getString(R.string.def_genre)

        binding.detailInfo.textSafe =
            binding.context.getString(
                R.string.fmt_two,
                binding.context.getPluralSafe(R.plurals.fmt_album_count, item.albums.size),
                binding.context.getPluralSafe(R.plurals.fmt_song_count, item.songs.size))

        binding.detailPlayButton.setOnClickListener { listener.onPlayParent() }
        binding.detailShuffleButton.setOnClickListener { listener.onShuffleParent() }
    }

    companion object {
        val CREATOR =
            object : Creator<ArtistDetailViewHolder> {
                override val viewType: Int
                    get() = IntegerTable.ITEM_TYPE_ARTIST_DETAIL

                override fun create(context: Context) =
                    ArtistDetailViewHolder(ItemDetailBinding.inflate(context.inflater))
            }

        val DIFFER = ArtistViewHolder.DIFFER
    }
}

private class ArtistAlbumViewHolder
private constructor(
    private val binding: ItemParentBinding,
) : BindingViewHolder<Album, MenuItemListener>(binding.root), Highlightable {
    override fun bind(item: Album, listener: MenuItemListener) {
        binding.parentImage.bindAlbumCover(item)
        binding.parentName.textSafe = item.resolveName(binding.context)
        binding.parentInfo.textSafe =
            if (item.year != null) {
                binding.context.getString(R.string.fmt_number, item.year)
            } else {
                binding.context.getString(R.string.def_date)
            }

        binding.root.apply {
            setOnClickListener { listener.onItemClick(item) }
            setOnLongClickListener { view ->
                listener.onOpenMenu(item, view)
                true
            }
        }
    }

    override fun setHighlighted(isHighlighted: Boolean) {
        binding.parentName.isActivated = isHighlighted
    }

    companion object {
        val CREATOR =
            object : Creator<ArtistAlbumViewHolder> {
                override val viewType: Int
                    get() = IntegerTable.ITEM_TYPE_ARTIST_ALBUM

                override fun create(context: Context) =
                    ArtistAlbumViewHolder(ItemParentBinding.inflate(context.inflater))
            }

        val DIFFER =
            object : SimpleItemCallback<Album>() {
                override fun areItemsTheSame(oldItem: Album, newItem: Album) =
                    oldItem.rawName == newItem.rawName && oldItem.year == newItem.year
            }
    }
}

private class ArtistSongViewHolder
private constructor(
    private val binding: ItemSongBinding,
) : BindingViewHolder<Song, MenuItemListener>(binding.root), Highlightable {
    override fun bind(item: Song, listener: MenuItemListener) {
        binding.songAlbumCover.bindAlbumCover(item)
        binding.songName.textSafe = item.resolveName(binding.context)
        binding.songInfo.textSafe = item.album.resolveName(binding.context)

        binding.root.apply {
            setOnClickListener { listener.onItemClick(item) }
            setOnLongClickListener { view ->
                listener.onOpenMenu(item, view)
                true
            }
        }
    }

    override fun setHighlighted(isHighlighted: Boolean) {
        binding.songName.isActivated = isHighlighted
    }

    companion object {
        val CREATOR =
            object : Creator<ArtistSongViewHolder> {
                override val viewType: Int
                    get() = IntegerTable.ITEM_TYPE_ARTIST_SONG

                override fun create(context: Context) =
                    ArtistSongViewHolder(ItemSongBinding.inflate(context.inflater))
            }

        val DIFFER =
            object : SimpleItemCallback<Song>() {
                override fun areItemsTheSame(oldItem: Song, newItem: Song) =
                    oldItem.rawName == newItem.rawName &&
                        oldItem.album.rawName == newItem.album.rawName
            }
    }
}
