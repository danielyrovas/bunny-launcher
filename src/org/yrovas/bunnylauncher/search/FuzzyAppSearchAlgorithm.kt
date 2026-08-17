/*
 * Copyright (C) 2015 The Android Open Source Project
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
import android.os.Handler
import android.os.UserManager
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherAppState.Companion.getInstance
import com.android.launcher3.LauncherPrefs
import com.android.launcher3.allapps.BaseAllAppsAdapter
import com.android.launcher3.allapps.BaseAllAppsAdapter.AdapterItem
import com.android.launcher3.model.AllAppsList
import com.android.launcher3.model.BgDataModel
import com.android.launcher3.model.ModelTaskController
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.pm.UserCache
import com.android.launcher3.search.SearchAlgorithm
import com.android.launcher3.search.SearchCallback
import com.android.launcher3.util.Executors
import org.yrovas.bunnylauncher.BunnyPrefs
import java.text.Normalizer

/**
 * A semi-`smart` app search implementation.
 */
class FuzzyAppSearchAlgorithm
@JvmOverloads constructor(
    context: Context,
    private val mAddNoResultsMessage: Boolean = false,
) : SearchAlgorithm<AdapterItem> {
    private val mAppState: LauncherAppState = getInstance(context)
    private val mResultHandler: Handler = Handler(Executors.MAIN_EXECUTOR.looper)

    override fun cancel(interruptActiveRequests: Boolean) {
        if (interruptActiveRequests) {
            mResultHandler.removeCallbacksAndMessages(null)
        }
    }

    override fun doSearch(
        query: String,
        callback: SearchCallback<AdapterItem>,
    ) {
        mAppState.model.enqueueModelUpdateTask { _: ModelTaskController?, _: BgDataModel?, apps: AllAppsList ->
            val result: ArrayList<AdapterItem> =
                getFuzzyMatchResult(mAppState.context, apps.data, query)
            if (mAddNoResultsMessage && result.isEmpty()) {
                result.add(getEmptyMessageAdapterItem(query))
            }
            mResultHandler.post { callback.onSearchResult(query, result) }
        }
    }

    companion object {
        private fun getEmptyMessageAdapterItem(query: String): AdapterItem {
            val item = AdapterItem(BaseAllAppsAdapter.VIEW_TYPE_EMPTY_SEARCH)
            // Add a place holder info to propagate the query
            val placeHolder = AppInfo()
            placeHolder.title = query
            item.itemInfo = placeHolder
            return item
        }

        const val SCORE_NO_MATCH = -1
        const val SCORE_EXACT = 0 // title is exactly query
        const val SCORE_PREFIX = 10 // title starts with query
        const val SCORE_WORD_START_SUBSTRING = 20 // query is a contiguous run matching the start of a word
        const val SCORE_SUBSTRING = 30 // query is a contiguous run
        const val SCORE_PREFIX_START_SUBSEQUENCE = 60 // spread-out match with first char correct
        const val SCORE_WORD_START_SUBSEQUENCE = 65 // spread-out match beginning a word
        const val SCORE_SUBSEQUENCE = 70 // spread-out match

        fun getFuzzyMatchResult(
            context: Context,
            apps: List<AppInfo>,
            query: String,
        ): ArrayList<AdapterItem> {
            val q = normalize(query)

            val results = ArrayList<Pair<AppInfo, Int>>()
            val userManager = context.getSystemService<UserManager>(UserManager::class.java)
            val userCache = UserCache.getInstance(context)
            val showQuietModeApps =
                LauncherPrefs.get(context).get(BunnyPrefs.DRAWER_SEARCH_SHOW_QUIET_MODE_APPS)

            for (info in apps) {
                if (
                    !showQuietModeApps
                    && userCache.getUserInfo(info.user).isPrivate
                    && userManager.isQuietModeEnabled(info.user)
                ) {
                    continue
                }

                scoreQuery(q, info.title.toString()).let {
                    if (it >= 0) results.add(info to it)
                }
            }
            return results.sortedBy { it.second }.map {
                    AdapterItem.asApp(it.first)
                }.toCollection(ArrayList())
        }

        private fun normalize(str: String): String =
            NON_SPACING_MARKS.replace(Normalizer.normalize(str, Normalizer.Form.NFD), "")
                .lowercase()

        /**
         * Score the query match for the search target.
         */
        fun scoreQuery(
            needle: String,
            haystack: String,
        ): Int {
            if (needle.isEmpty()) return SCORE_NO_MATCH

            val haystack = normalize(haystack)

            if (needle.length > haystack.length) return SCORE_NO_MATCH
            if (needle == haystack) return SCORE_EXACT
            if (haystack.startsWith(needle)) return SCORE_PREFIX

            val substringIndex = haystack.indexOf(needle)
            if (substringIndex >= 0) {
                return if (haystack.isWordStart(substringIndex)) {
                    SCORE_WORD_START_SUBSTRING
                } else {
                    SCORE_SUBSTRING + substringIndex
                }
            }

            val subsequenceIndex = findSubsequenceMatch(needle, haystack)
            if (subsequenceIndex >= 0) {
                return if (subsequenceIndex == 0) {
                    SCORE_PREFIX_START_SUBSEQUENCE
                } else if (haystack.isWordStart(subsequenceIndex)) {
                    SCORE_WORD_START_SUBSEQUENCE
                } else {
                    SCORE_SUBSEQUENCE + subsequenceIndex
                }
            }

            return SCORE_NO_MATCH
        }

        private fun findSubsequenceMatch(
            needle: String,
            haystack: String,
        ): Int {
            var index = 0
            var firstIndexMatch = -1

            for (i in haystack.indices) {
                if (haystack[i] == needle[index]) {
                    if (firstIndexMatch == -1) {
                        firstIndexMatch = i
                    }
                    index++
                    if (index == needle.length) return firstIndexMatch
                }
            }
            return -1
        }

        private val NON_SPACING_MARKS = Regex("\\p{Mn}+")

        private fun String.isWordStart(index: Int): Boolean =
            index == 0 || !this[index - 1].isLetterOrDigit()
    }
}
