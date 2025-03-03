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
 
package org.oxycblt.auxio.playback.state

import org.oxycblt.auxio.IntegerTable
import org.oxycblt.auxio.R

/**
 * Enum that determines the playback repeat mode.
 * @author OxygenCobalt
 */
enum class LoopMode {
    NONE,
    ALL,
    TRACK;

    /** Increment the LoopMode, e.g from [NONE] to [ALL] */
    fun increment(): LoopMode {
        return when (this) {
            NONE -> ALL
            ALL -> TRACK
            TRACK -> NONE
        }
    }

    val icon: Int
        get() =
            when (this) {
                NONE -> R.drawable.ic_loop
                ALL -> R.drawable.ic_loop_on
                TRACK -> R.drawable.ic_loop_one
            }

    /**
     * Convert the LoopMode to an int constant that is saved in PlaybackStateDatabase
     * @return The int constant for this mode
     */
    val intCode: Int
        get() =
            when (this) {
                NONE -> IntegerTable.LOOP_MODE_NONE
                ALL -> IntegerTable.LOOP_MODE_ALL
                TRACK -> IntegerTable.LOOP_MODE_TRACK
            }

    companion object {
        /** Convert an int [constant] into a LoopMode, or null if it isn't valid. */
        fun fromIntCode(constant: Int): LoopMode? {
            return when (constant) {
                IntegerTable.LOOP_MODE_NONE -> NONE
                IntegerTable.LOOP_MODE_ALL -> ALL
                IntegerTable.LOOP_MODE_TRACK -> TRACK
                else -> null
            }
        }
    }
}
