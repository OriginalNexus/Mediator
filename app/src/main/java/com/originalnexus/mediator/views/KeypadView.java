package com.originalnexus.mediator.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.Keep;
import androidx.gridlayout.widget.GridLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.originalnexus.mediator.R;

import java.security.InvalidParameterException;
import java.util.EventListener;
import java.util.Timer;
import java.util.TimerTask;


public class KeypadView extends FrameLayout {

	private enum KeypadBtnType {
		DIGIT,
		REMOVE,
		DONE
	}

	private class KeypadBtn {
		private final KeypadBtnType mType;
		private final int mDigit;

		KeypadBtn(KeypadBtnType type, int digit) {
			mType = type;
			mDigit = digit;
		}
	}

	public interface KeypadListener extends EventListener {
		void onInput(int digit);
		void onRemove();
	}

	private static final String STATE_INSTANCE = "instance";
	private static final String STATE_IS_OPEN = "mIsOpen";
	private static final int ANIMATION_LENGTH = 350;
	private static final int LONG_PRESS_REMOVE_DELAY = 100;

	private int mOrientation;
	private KeypadListener mListener;

	private boolean mIsOpen = false;
	private int mSize;

	private ObjectAnimator mHideAnim;
	private ObjectAnimator mShowAnim;

	public KeypadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.KeypadView, 0, 0);
		try {
			mOrientation = a.getInt(R.styleable.KeypadView_android_orientation, LinearLayout.VERTICAL);
			mSize = a.getDimensionPixelSize(R.styleable.KeypadView_size, -1);
			if (mSize < 0) throw new InvalidParameterException("Must provide valid 'size'");
		}
		finally {
			a.recycle();
		}

		// Inflate layout
		inflate(getContext(), R.layout.view_keypad, this);

		// Set inner LinearLayout mSize
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		if (mOrientation == LinearLayout.VERTICAL) lp.height = mSize;
		else lp.width = mSize;
		findViewById(R.id.view_keypad_root).setLayoutParams(lp);

		setAnimations();
		hide();

		GridLayout grid = findViewById(R.id.view_keypad_grid);
		for (int i = 0; i < grid.getChildCount(); i++) {
			View btnView = grid.getChildAt(i);
			if (btnView == null) break;

			KeypadBtnType btnType;
			int digit = 0;

			switch (i) {
				case 9:
					btnType = KeypadBtnType.REMOVE;
					break;
				case 11:
					btnType = KeypadBtnType.DONE;
					break;
				default:
					btnType = KeypadBtnType.DIGIT;
					digit = (i != 10) ? i + 1 : i;
					((TextView) btnView).setText(String.valueOf(digit));
					break;
			}

			// Create keypad events
			final KeypadBtn btn = new KeypadBtn(btnType, digit);
			btnView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onKeypadBtnClick(btn);
				}
			});

			btnView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					final Timer timer = new Timer();
					final View btnView = v;
					timer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							if (btnView == null || !btnView.isPressed()) {
								timer.cancel();
								timer.purge();
								return;
							}

							post(new TimerTask() {
								@Override
								public void run() {
									onKeypadBtnClick(btn);
								}
							});
						}
					}, 0, LONG_PRESS_REMOVE_DELAY);

					return true;
				}
			});
		}
	}

	private void onKeypadBtnClick(KeypadBtn btn) {
		if (mListener != null) {
			if (btn.mType == KeypadBtnType.DIGIT) mListener.onInput(btn.mDigit);
			else if (btn.mType == KeypadBtnType.REMOVE) mListener.onRemove();
		}
		if (btn.mType == KeypadBtnType.DONE) {
			hideKeypad();
		}
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle b = new Bundle();
		b.putParcelable(STATE_INSTANCE, super.onSaveInstanceState());
		b.putBoolean(STATE_IS_OPEN, mIsOpen);
		return b;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle b = (Bundle) state;
			mIsOpen = b.getBoolean(STATE_IS_OPEN);
			state = b.getParcelable(STATE_INSTANCE);
		}
		super.onRestoreInstanceState(state);

		if (mIsOpen) show();
		else hide();
	}


	private void setAnimations() {
		mShowAnim = ObjectAnimator.ofInt(this, "height", 0, mSize).setDuration(ANIMATION_LENGTH);
		mShowAnim.setInterpolator(new DecelerateInterpolator());
		mShowAnim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) { setVisibility(VISIBLE); }

			@Override
			public void onAnimationCancel(Animator animation) { setVisibility(GONE); }
		});
		mShowAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) { requestLayout(); }
		});

		mHideAnim = ObjectAnimator.ofInt(this, "height", mSize, 0).setDuration(ANIMATION_LENGTH);
		mHideAnim.setInterpolator(new AccelerateInterpolator());
		mHideAnim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) { setVisibility(GONE); }

			@Override
			public void onAnimationCancel(Animator animation) { setVisibility(VISIBLE); }
		});
		mHideAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) { requestLayout(); }
		});

		if (mOrientation == LinearLayout.HORIZONTAL) {
			mShowAnim.setPropertyName("width");
			mHideAnim.setPropertyName("width");
		}
	}

	private boolean isAnimating() {
		return mShowAnim.isRunning() || mHideAnim.isRunning();
	}

	private void stopAnimations() {
		if (mShowAnim.isRunning()) mShowAnim.cancel();
		if (mHideAnim.isRunning()) mHideAnim.cancel();
	}

	private void show() {
		stopAnimations();
		if (mOrientation == LinearLayout.HORIZONTAL) setWidth(mSize);
		else setHeight(mSize);
		setVisibility(VISIBLE);
	}

	private void hide() {
		stopAnimations();
		if (mOrientation == LinearLayout.HORIZONTAL) setWidth(0);
		else setHeight(0);
		setVisibility(GONE);
	}

	@Keep
	private void setHeight(int height) {
		ViewGroup.LayoutParams lp = getLayoutParams();
		if (getLayoutParams() != null) lp.height = height;
	}

	@Keep
	private void setWidth(int width) {
		ViewGroup.LayoutParams lp = getLayoutParams();
		if (getLayoutParams() != null) lp.width = width;
	}

	public void showKeypad() {
		if (isAnimating()) return;
		if (!mIsOpen) mShowAnim.start();
		mIsOpen = true;
	}

	public void hideKeypad() {
		if (isAnimating()) return;
		if (mIsOpen) mHideAnim.start();
		mIsOpen = false;
	}

	public void setKeypadListener(KeypadListener listener) {
		this.mListener = listener;
	}


	public boolean isKeypadOpen() {
		return mIsOpen;
	}

}
