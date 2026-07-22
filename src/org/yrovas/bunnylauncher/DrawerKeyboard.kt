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

import android.util.Log
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherPrefs
import com.android.launcher3.LauncherState.ALL_APPS

object DrawerKeyboard {
    var lastInvokeAttemptSuccess = true

    @JvmStatic
    fun attemptAutoInvoke(launcher: Launcher) {
        if (!LauncherPrefs.get(launcher).get(BunnyPrefs.DRAWER_AUTO_KEYBOARD)) return
        val e = launcher.appsView
            ?.searchUiManager
            ?.editText ?: return

        lastInvokeAttemptSuccess = e.showKeyboard() == true
        if (!lastInvokeAttemptSuccess) {
            e.post {
                if (!lastInvokeAttemptSuccess && e.hasWindowFocus()) {
                    lastInvokeAttemptSuccess = e.showKeyboard() == true
                }
            }
        }
    }
    @JvmStatic
    fun retryFailedInvoke(launcher: Launcher) {
        if (!LauncherPrefs.get(launcher).get(BunnyPrefs.DRAWER_AUTO_KEYBOARD)) return

        if (launcher.stateManager.targetState == ALL_APPS && !lastInvokeAttemptSuccess) {
            attemptAutoInvoke(launcher)
        }

        if (!lastInvokeAttemptSuccess) {
            launcher.appsView.postDelayed({
                if (launcher.stateManager.targetState == ALL_APPS && !lastInvokeAttemptSuccess) {
                    attemptAutoInvoke(launcher)
                }
            }, 40)
        }
    }
}
