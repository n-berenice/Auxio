/*
 * Copyright (c) 2021 Auxio Project
 * MainFragment.kt is part of Auxio.
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

package org.oxycblt.auxio

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import org.oxycblt.auxio.databinding.FragmentMainBinding
import org.oxycblt.auxio.music.MusicStore
import org.oxycblt.auxio.music.MusicViewModel
import org.oxycblt.auxio.playback.PlaybackViewModel
import org.oxycblt.auxio.util.applyEdge
import org.oxycblt.auxio.util.applyMaterialDrawable
import org.oxycblt.auxio.util.logD

/**
 * A wrapper around the home fragment that shows the playback fragment and controls
 * the more high-level navigation features.
 */
class MainFragment : Fragment() {
    private val playbackModel: PlaybackViewModel by activityViewModels()
    private val musicModel: MusicViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(inflater)

        // Build the permission launcher here as you can only do it in onCreateView/onCreate
        val permLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            musicModel.reloadMusic(requireContext())
        }

        // --- UI SETUP ---

        binding.lifecycleOwner = viewLifecycleOwner

        binding.applyEdge { bars ->
            binding.mainPlayback.updatePadding(bottom = bars.bottom)
        }

        binding.mainPlayback.applyMaterialDrawable()

        // --- VIEWMODEL SETUP ---

        // Initialize music loading. Unlike MainFragment, we can not only do this here on startup
        // but also show a SnackBar in a reasonable place in this fragment.
        musicModel.loadMusic(requireContext())

        // Change CompactPlaybackFragment's visibility here so that an animation occurs.
        binding.mainPlayback.isVisible = playbackModel.song.value != null

        playbackModel.song.observe(viewLifecycleOwner) { song ->
            binding.mainPlayback.isVisible = song != null
        }

        // Handle the music loader response.
        musicModel.loaderResponse.observe(viewLifecycleOwner) { response ->
            // Handle the loader response.
            when (response) {
                // OK, start restoring playback now
                is MusicStore.Response.Ok -> playbackModel.setupPlayback(requireContext())

                // Error, show the error to the user
                is MusicStore.Response.Err -> {
                    logD("Received Error")

                    val errorRes = when (response.kind) {
                        MusicStore.ErrorKind.NO_MUSIC -> R.string.err_no_music
                        MusicStore.ErrorKind.NO_PERMS -> R.string.err_no_perms
                        MusicStore.ErrorKind.FAILED -> R.string.err_load_failed
                    }

                    val snackbar = Snackbar.make(
                        binding.root, getString(errorRes), Snackbar.LENGTH_INDEFINITE
                    )

                    snackbar.view.apply {
                        // Change the font family to our semibold color
                        findViewById<Button>(
                            com.google.android.material.R.id.snackbar_action
                        ).typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_semibold)
                    }

                    when (response.kind) {
                        MusicStore.ErrorKind.FAILED, MusicStore.ErrorKind.NO_MUSIC -> {
                            snackbar.setAction(R.string.lbl_retry) {
                                musicModel.reloadMusic(requireContext())
                            }
                        }

                        MusicStore.ErrorKind.NO_PERMS -> {
                            snackbar.setAction(R.string.lbl_grant) {
                                permLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        }
                    }

                    snackbar.show()
                }
            }
        }

        logD("Fragment Created.")

        return binding.root
    }
}
