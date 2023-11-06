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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.UiThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.Themes;

import com.android.launcher3.quickspace.receivers.QuickSpaceActionReceiver;
import com.android.launcher3.quickspace.views.DateTextView;

public class QuickSpaceView extends FrameLayout {

    private static final String TAG = "Launcher3:QuickSpaceView";

    public final ColorStateList mColorStateList;
    public BubbleTextView mBubbleTextView;
    public final int mQuickspaceBackgroundRes;

    public DateTextView mClockView;
    public ViewGroup mQuickspaceContent;
    public ImageView mEventSubIcon;
    public TextView mEventTitleSub;
    public TextView mGreetingsExt;
    public View mQuickEventsView;
    public View mGreetingsExtView;
    public View mGreetingsExtClock;

    private QuickSpaceActionReceiver mActionReceiver;
    public QuickEventsController mController;
    
    private boolean mFinishedInflate;

    public QuickSpaceView(Context context, AttributeSet set) {
        super(context, set);
        mActionReceiver = new QuickSpaceActionReceiver(context);
        mController = new QuickEventsController(context);
        mColorStateList = ColorStateList.valueOf(Themes.getAttrColor(getContext(), R.attr.workspaceTextColor));
        mQuickspaceBackgroundRes = R.drawable.bg_quickspace;
        setClipChildren(false);
    }

    @UiThread
    public void updateGlance() {
    	mController.initQuickEvents();
    	prepareLayout();
    	getQuickSpaceView();
    	loadDoubleLine();
    }

    private final void loadDoubleLine() {
        setBackgroundResource(mQuickspaceBackgroundRes);
        if (Utilities.showQuickEventsMsgs(getContext())) {
        String eventTitle = mController.getActionTitle();
        mEventTitleSub.setText(eventTitle);
        mEventTitleSub.setOnClickListener(mController.getAction());
        mEventSubIcon.setImageTintList(mColorStateList);
        mEventSubIcon.setImageResource(mController.getActionIcon());
        }
        if (Utilities.isExtendedQuickSpace(getContext())) {
        String greetingsText = mController.getGreetings();
        mGreetingsExt.setText(greetingsText);
        mGreetingsExt.setEllipsize(TruncateAt.MARQUEE);
        mGreetingsExt.setMarqueeRepeatLimit(3);
        mGreetingsExtClock.setVisibility(View.VISIBLE);
        }
        bindClock(false);
    }
    
    private final void bindClock(boolean forced) {
        mClockView.setOnClickListener(mActionReceiver.getCalendarAction());
        if (forced) {
            mClockView.reloadDateFormat(true);
        }
    }

    private final void loadViews() {
        mQuickspaceContent = (ViewGroup) findViewById(R.id.quickspace_content);
        mClockView = (DateTextView) findViewById(R.id.clock_view);
    	if (Utilities.showQuickEventsMsgs(getContext())) {
        mQuickEventsView = (View) findViewById(R.id.quick_events_messages);
        mEventTitleSub = (TextView) findViewById(R.id.quick_event_title_sub);
        mEventSubIcon = (ImageView) findViewById(R.id.quick_event_icon_sub);
        }
        if (Utilities.isExtendedQuickSpace(getContext())) {
        mGreetingsExtClock = (TextView) findViewById(R.id.extended_greetings_clock);
        mGreetingsExtView = (View) findViewById(R.id.extended_greetings_view);
        mGreetingsExt = (TextView) findViewById(R.id.extended_greetings);
        }
    }

    private void prepareLayout() {
        int indexOfChild = indexOfChild(mQuickspaceContent);
        removeView(mQuickspaceContent);
        if (Utilities.useCenterQuickspaceUI(getContext())) {
        addView(LayoutInflater.from(getContext()).inflate(R.layout.quickspace_center_doubleline, this, false), indexOfChild);
        } else {
        addView(LayoutInflater.from(getContext()).inflate(R.layout.quickspace_doubleline, this, false), indexOfChild);
        }
        loadViews();
    }

    private void getQuickSpaceView() {
        if (!(mQuickspaceContent.getVisibility() == View.VISIBLE)) {
   	     mQuickspaceContent.animate()
             			.alpha(0f)
            			.setDuration(200)
            			.setListener(new AnimatorListenerAdapter() {
                		@Override
                		public void onAnimationEnd(Animator animation) {
	    			    mQuickspaceContent.setVisibility(View.GONE);
                		}
            			});
            mQuickspaceContent.setVisibility(View.VISIBLE);
            mQuickspaceContent.animate()
            		    .alpha(1.0f)
            		    .setDuration(200)
            		    .setListener(null);

    	   }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mController == null && !mFinishedInflate) {
             return;
        }
        updateGlance();
    }

    public boolean isPackageEnabled(String pkgName, Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(pkgName, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        loadViews();
        mFinishedInflate = true;
        mBubbleTextView = findViewById(R.id.dummyBubbleTextView);
        mBubbleTextView.setTag(new ItemInfo() {
            @Override
            public ComponentName getTargetComponent() {
                return new ComponentName(getContext(), "");
            }
        });
        mBubbleTextView.setContentDescription("");
        if (isAttachedToWindow()) {
            if (mController == null) {
                return;
            }
            updateGlance();
        }
    }

    @Override
    public void onLayout(boolean b, int n, int n2, int n3, int n4) {
        super.onLayout(b, n, n2, n3, n4);
    }

    public void onResume() {
    	if (mController == null) {
    	   return;
        }
        updateGlance();
    }

    public void setPadding(int n, int n2, int n3, int n4) {
        super.setPadding(0, 0, 0, 0);
    }

}
