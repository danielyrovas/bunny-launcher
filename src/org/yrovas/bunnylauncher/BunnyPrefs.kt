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

object BunnyPrefs {
    const val DRAWER_AUTO_KEYBOARD_KEY: String = "pref_drawer_auto_keyboard"
    const val DRAWER_FUZZY_SEARCH_KEY: String = "pref_drawer_fuzzy_search"
    const val DRAWER_SEARCH_SHOW_QUIET_MODE_APPS_KEY: String = "pref_drawer_search_show_quiet_mode_apps"

    /** Whether opening the app drawer focuses the search box and shows the keyboard. */
    @JvmField
    val DRAWER_AUTO_KEYBOARD: ConstantItem<Boolean> =
        LauncherPrefs.backedUpItem(DRAWER_AUTO_KEYBOARD_KEY, true)

    /** Whether app drawer search matches apps with a `smart` algorithm. */
    @JvmField
    val DRAWER_FUZZY_SEARCH: ConstantItem<Boolean> =
        LauncherPrefs.backedUpItem(DRAWER_FUZZY_SEARCH_KEY, true)

    /** Whether app drawer search will show private space apps when it is locked. */
    @JvmField
    val DRAWER_SEARCH_SHOW_QUIET_MODE_APPS: ConstantItem<Boolean> =
        LauncherPrefs.backedUpItem(DRAWER_SEARCH_SHOW_QUIET_MODE_APPS_KEY, true)
}
