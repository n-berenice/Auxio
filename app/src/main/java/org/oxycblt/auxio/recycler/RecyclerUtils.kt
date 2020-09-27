package org.oxycblt.auxio.recycler

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.oxycblt.auxio.R
import org.oxycblt.auxio.music.Album
import org.oxycblt.auxio.music.Artist
import org.oxycblt.auxio.music.BaseModel
import org.oxycblt.auxio.music.Song

// RecyclerView click listener
class ClickListener<T>(val onClick: (T) -> Unit)

// Base Diff callback
class DiffCallback<T : BaseModel> : DiffUtil.ItemCallback<T>() {
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}

// ViewHolder abstraction that automates some of the things that are common for all ViewHolders.
abstract class BaseViewHolder<T : BaseModel>(
    private val baseBinding: ViewDataBinding,
    protected val listener: ClickListener<T>
) : RecyclerView.ViewHolder(baseBinding.root) {
    init {
        baseBinding.root.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    fun bind(model: T) {
        baseBinding.root.setOnClickListener { listener.onClick(model) }

        onBind(model)

        baseBinding.executePendingBindings()
    }

    abstract fun onBind(model: T)
}

// Sorting modes
enum class SortMode(val iconRes: Int) {
    // Icons for each mode are assigned to the enums themselves
    NONE(R.drawable.ic_sort_alpha_down),
    ALPHA_UP(R.drawable.ic_sort_alpha_up),
    ALPHA_DOWN(R.drawable.ic_sort_alpha_down),
    NUMERIC_UP(R.drawable.ic_sort_numeric_up),
    NUMERIC_DOWN(R.drawable.ic_sort_numeric_down);

    companion object {
        // Sort comparators are different for each music model, so they are static maps instead.
        val songSortComparators = mapOf<SortMode, Comparator<Song>>(
            NUMERIC_DOWN to compareBy { it.track },
            NUMERIC_UP to compareByDescending { it.track }
        )

        val albumSortComparators = mapOf<SortMode, Comparator<Album>>(
            NUMERIC_DOWN to compareByDescending { it.year },
            NUMERIC_UP to compareBy { it.year },

            // Alphabetic sorting needs to be case-insensitive
            ALPHA_DOWN to compareByDescending(
                String.CASE_INSENSITIVE_ORDER
            ) { it.name },
            ALPHA_UP to compareBy(
                String.CASE_INSENSITIVE_ORDER
            ) { it.name }
        )

        val artistSortComparators = mapOf<SortMode, Comparator<Artist>>(
            // Alphabetic sorting needs to be case-insensitive
            ALPHA_DOWN to compareBy(
                String.CASE_INSENSITIVE_ORDER
            ) { it.name },
            ALPHA_UP to compareByDescending(
                String.CASE_INSENSITIVE_ORDER
            ) { it.name }
        )
    }
}