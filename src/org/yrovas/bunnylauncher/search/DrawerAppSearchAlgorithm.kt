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
package org.yrovas.bunnylauncher.search

import android.content.Context
import android.util.Log
import com.android.launcher3.LauncherPrefs
import com.android.launcher3.allapps.BaseAllAppsAdapter.AdapterItem
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm
import com.android.launcher3.search.SearchAlgorithm
import com.android.launcher3.search.SearchCallback
import org.yrovas.bunnylauncher.BunnyPrefs

/**
 * All-apps [SearchAlgorithm] used by the drawer search box. Dispatches to
 * [DefaultAppSearchAlgorithm] or [FuzzyAppSearchAlgorithm] depending on
 * [BunnyPrefs.DRAWER_FUZZY_SEARCH] configuration.
 */
class DrawerAppSearchAlgorithm(
    private val context: Context,
    addNoResultsMessage: Boolean,
) : SearchAlgorithm<AdapterItem> {

    private val defaultAlgorithm = DefaultAppSearchAlgorithm(context, addNoResultsMessage)
    private val fuzzyAlgorithm = FuzzyAppSearchAlgorithm(context, addNoResultsMessage)

    private fun selectedAlgorithm(): SearchAlgorithm<AdapterItem> =
        if (LauncherPrefs.get(context).get(BunnyPrefs.DRAWER_FUZZY_SEARCH)) {
            fuzzyAlgorithm
        } else {
            defaultAlgorithm
        }

    override fun doSearch(query: String, callback: SearchCallback<AdapterItem>) {
        selectedAlgorithm().doSearch(query, callback)
    }

    override fun cancel(interruptActiveRequests: Boolean) {
        defaultAlgorithm.cancel(interruptActiveRequests)
        fuzzyAlgorithm.cancel(interruptActiveRequests)
    }

    override fun destroy() {
        defaultAlgorithm.destroy()
        fuzzyAlgorithm.destroy()
    }
}
