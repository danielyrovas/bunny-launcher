/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yrovas.bunnylauncher

import com.android.launcher3.ConstantItem
import com.android.launcher3.LauncherPrefs

/** Preferences specific to Bunny Launcher, kept out of [LauncherPrefs] to ease rebasing. */
object BunnyPrefs {
    /** Shared preference key for [DRAWER_AUTO_KEYBOARD]. */
    const val DRAWER_AUTO_KEYBOARD_KEY: String = "pref_drawer_auto_keyboard"

    /** Whether opening the app drawer focuses the search box and shows the keyboard. */
    @JvmField
    val DRAWER_AUTO_KEYBOARD: ConstantItem<Boolean> =
        LauncherPrefs.backedUpItem(DRAWER_AUTO_KEYBOARD_KEY, true)
}
