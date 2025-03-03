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
 
package org.oxycblt.auxio.home.list

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import org.oxycblt.auxio.databinding.FragmentHomeListBinding
import org.oxycblt.auxio.home.HomeViewModel
import org.oxycblt.auxio.home.fastscroll.FastScrollRecyclerView
import org.oxycblt.auxio.playback.PlaybackViewModel
import org.oxycblt.auxio.ui.Item
import org.oxycblt.auxio.ui.MenuItemListener
import org.oxycblt.auxio.ui.NavigationViewModel
import org.oxycblt.auxio.ui.ViewBindingFragment

/**
 * A Base [Fragment] implementing the base features shared across all list fragments in the home UI.
 * @author OxygenCobalt
 */
abstract class HomeListFragment<T : Item> :
    ViewBindingFragment<FragmentHomeListBinding>(),
    MenuItemListener,
    FastScrollRecyclerView.PopupProvider,
    FastScrollRecyclerView.OnFastScrollListener {
    abstract fun setupRecycler(recycler: RecyclerView)

    protected val playbackModel: PlaybackViewModel by activityViewModels()
    protected val navModel: NavigationViewModel by activityViewModels()
    protected val homeModel: HomeViewModel by activityViewModels()

    override fun onCreateBinding(inflater: LayoutInflater) =
        FragmentHomeListBinding.inflate(inflater)

    override fun onBindingCreated(binding: FragmentHomeListBinding, savedInstanceState: Bundle?) {
        setupRecycler(binding.homeRecycler)
        binding.homeRecycler.popupProvider = this
        binding.homeRecycler.onDragListener = this
    }

    override fun onDestroyBinding(binding: FragmentHomeListBinding) {
        homeModel.updateFastScrolling(false)
        binding.homeRecycler.apply {
            adapter = null
            popupProvider = null
            onDragListener = null
        }
    }

    override fun onFastScrollStart() {
        homeModel.updateFastScrolling(true)
    }

    override fun onFastScrollStop() {
        homeModel.updateFastScrolling(false)
    }
}
