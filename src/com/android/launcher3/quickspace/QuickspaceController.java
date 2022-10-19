/*
 * Copyright (C) 2018 CypherOS
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
package com.android.launcher3.quickspace;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;

import com.android.launcher3.quickspace.QuickEventsController;

import java.util.ArrayList;

public class QuickspaceController {

    public final ArrayList<OnDataListener> mListeners = new ArrayList();
    private static final boolean DEBUG = false;
    private static final String TAG = "Launcher3:QuickspaceController";

    private final Handler mHandler;
    private QuickEventsController mEventsController;

    public interface OnDataListener {
        void onDataUpdated();
    }

    public QuickspaceController(Context context) {
        mHandler = new Handler();
        mEventsController = new QuickEventsController(context);
    }

    public void addListener(OnDataListener listener) {
        mListeners.add(listener);
        listener.onDataUpdated();
    }

    public void removeListener(OnDataListener listener) {
        mListeners.remove(listener);
    }

    public QuickEventsController getEventController() {
        return mEventsController;
    }

    public void onPause() {
          mEventsController.onPause();
    }

    public void onResume() {
        if (mEventsController != null) {
            mEventsController.onResume();
            notifyListeners();
        }
    }

    public void notifyListeners() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (OnDataListener list : mListeners) {
                    list.onDataUpdated();
                }
            }
        });
    }
}
