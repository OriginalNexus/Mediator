package com.originalnexus.mediator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;


public class KeypadView extends LinearLayout {

	public enum SlideDirection {
		VERTICAL, ORIZONTAL
	}
	private SlideDirection slideDirection;

	private static final String STATE_INSTANCE_STATE = "instance_state";
	private static final String STATE_IS_HIDDEN = "is_hidden";

	/**
	 * Animation length in ms
	 */
	private static final int ANIMATION_LENGTH = 350;
	/**
	 * List of all the listeners
	 */
	private final ArrayList<KeypadListener> keypadListenerList = new ArrayList<>();
	/**
	 * State of the keyboard (hidden hidden by default)
	 */
	private boolean isHidden = true;
	/**
	 * Keypad measured height in px
	 */
	private int maxHeight;
	/**
	 * Keypad measured width in px
	 */
	private int maxWidth;
	/**
	 * Hide animation
	 */
	private ObjectAnimator hideAnim;
	/**
	 * Show animation
	 */
	private ObjectAnimator showAnim;

	public KeypadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.KeypadView, 0, 0);
		try {
			int temp = a.getInt(R.styleable.KeypadView_slide_direction, 0);
			switch (temp) {
				case 0:
					slideDirection = SlideDirection.ORIZONTAL;
					break;
				case 1:
					slideDirection = SlideDirection.VERTICAL;
					break;
			}

		}
		finally {
			a.recycle();
		}

		// Inflate layout
		inflate(getContext(), R.layout.keypad, this);

		// Add top attributes
		int padding = (int) getResources().getDimension(R.dimen.KeypadView_padding);
		setPadding(padding, padding, padding, padding);
		setOrientation(VERTICAL);

		// Let view "render" it's sizes and then "lock" everything (for the slide up/down animations)
		setVisibility(INVISIBLE);
		getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				// After the view is ready to be shown we can get it's exact sizes and then hide it
				for (int i = 0; i < getChildCount(); i++) {
					View child = getChildAt(i);
					// Makes the buttons keep their size
					child.setLayoutParams(new LayoutParams(child.getWidth(), child.getHeight(), 0));
				}
				// Remove this listener
				getViewTreeObserver().removeOnPreDrawListener(this);
				// Exact size
				maxHeight = getHeight();
				maxWidth = getWidth();
				// Remove weight
				((LayoutParams) getLayoutParams()).weight = 0;
				// Restore visibility
				toggleKeypad(isHidden ? 0 : 1);

				// Set Animations
				updateAnimations();

				return false;
			}
		});

		// All the buttons in specific order
		int[] buttonIds = {
				R.id.digit_1, R.id.digit_2, R.id.digit_3,
				R.id.digit_4, R.id.digit_5, R.id.digit_6,
				R.id.digit_7, R.id.digit_8, R.id.digit_9,
				R.id.digit_10, R.id.digit_minus, R.id.digit_done};

		for (int i = 0; i < 12; i++) {
			// Find button view
			View btnView = findViewById(buttonIds[i]);
			if (btnView != null) {
				// Type of the button
				KeypadBtnType btnType;
				// Digit if available
				int digit = 0;
				if (i == 10) btnType = KeypadBtnType.REMOVE;
				else if (i == 11) btnType = KeypadBtnType.DONE;
				else {
					btnType = KeypadBtnType.DIGIT;
					digit = i + 1;
				}

				// Create keypad events
				final KeypadEvent e = new KeypadEvent(this, btnView, btnType, digit);
				btnView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// When the button is clicked announce the event
						for (KeypadListener keyL : keypadListenerList) {
							keyL.keyPressed(e);
						}
					}
				});

				btnView.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						// When the button is long clicked announce the event
						for (KeypadListener keyL : keypadListenerList) {
							keyL.keyLongPressed(e);
						}
						return true;
					}
				});
			}
		}
	}

	private void updateAnimations() {
		if (slideDirection == SlideDirection.ORIZONTAL) {
			hideAnim = ObjectAnimator.ofInt(this, "width", maxWidth, 0).setDuration(ANIMATION_LENGTH);
			hideAnim.setInterpolator(new AccelerateInterpolator());
			hideAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					setVisibility(GONE);
				}
			});
			hideAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					requestLayout();
				}
			});

			showAnim = ObjectAnimator.ofInt(this, "width", 0, maxWidth).setDuration(ANIMATION_LENGTH);
			showAnim.setInterpolator(new DecelerateInterpolator());
			showAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					setVisibility(VISIBLE);
				}
			});
			showAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					requestLayout();
				}
			});
		}
		else {
			hideAnim = ObjectAnimator.ofInt(this, "height", maxHeight, 0).setDuration(ANIMATION_LENGTH);
			hideAnim.setInterpolator(new AccelerateInterpolator());
			hideAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					setVisibility(GONE);
				}
			});
			hideAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					requestLayout();
				}
			});

			showAnim = ObjectAnimator.ofInt(this, "height", 0, maxHeight).setDuration(ANIMATION_LENGTH);
			showAnim.setInterpolator(new DecelerateInterpolator());
			showAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					setVisibility(VISIBLE);
				}
			});
			showAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					requestLayout();
				}
			});
		}
	}

	/**
	 * Open/Close/Toggle the keypad
	 *
	 * @param sw 1 = open ; 0 = close; other = toggle
	 */
	public void toggleKeypad(int sw) {
		// If an animation is in progress do nothing
		if (getAnimation() != null) return;

		// Set isHidden property based on the switch
		if (sw == 0) isHidden = true;
		else //noinspection SimplifiableIfStatement
			if (sw == 1) isHidden = false;
			else isHidden = !isHidden;

		// If the keypad needs to be hid
		if (isHidden && getVisibility() == VISIBLE) {
			if (hideAnim != null) {
				// Start animation
				hideAnim.start();
			}
			else {
				// Hide the view
				hide();
			}
		}

		// If the keypad needs to be shown
		else if (!isHidden && getVisibility() != VISIBLE) {
			if (showAnim != null) {
				// Start animation
				showAnim.start();
			}
			else {
				// Show the view
				show();
			}
		}
	}

	/**
	 * Shows the keypad immediately
	 */
	private void show() {
		if (slideDirection == SlideDirection.ORIZONTAL)
			setWidth(maxWidth);
		else
			setHeight(maxHeight);
		setVisibility(VISIBLE);
	}

	/**
	 * Hides the keypad immediately
	 */
	private void hide() {
		if (slideDirection == SlideDirection.ORIZONTAL)
			setWidth(0);
		else
			setHeight(0);
		setVisibility(GONE);
	}

	/**
	 * Sets keypad height
	 *
	 * @param height The new height in pixels or using one of the constants:
	 *               LinearLayout.LayoutParams.WRAP_CONTENT
	 *               LinearLayout.LayoutParams.MATCH_PARENT
	 */
	private void setHeight(int height) {
		getLayoutParams().height = height;
	}

	/**
	 * Sets keypad width
	 * @param width The new width in pixels or using one of the constants:
	 *               LinearLayout.LayoutParams.WRAP_CONTENT
	 *               LinearLayout.LayoutParams.MATCH_PARENT
	 */
	private void setWidth(int width) {
		getLayoutParams().width = width;
	}

	/**
	 * Bind a key press event listener
	 *
	 * @param listener The listener to be notified
	 */
	public void addKeypadListener(KeypadListener listener) {
		if (!keypadListenerList.contains(listener)) {
			keypadListenerList.add(listener);
		}
	}

	/**
	 * The type of the pressed button
	 */
	public enum KeypadBtnType {
		DIGIT,
		REMOVE,
		DONE
	}

	/**
	 * A simple key press event listener
	 */
	public interface KeypadListener extends EventListener {
		void keyPressed(KeypadEvent e);
		void keyLongPressed(KeypadEvent e);
	}

	/**
	 * A simple event for key presses
	 */
	public class KeypadEvent extends EventObject {
		public final KeypadBtnType btnType;
		public final View sender;
		public int digit = 0;

		/**
		 * Constructor of the event
		 *
		 * @param source  Source object
		 * @param sender  The view that was clicked
		 * @param btnType The type of the pressed button
		 * @param digit   The pressed digit or 0 if not available
		 */
		public KeypadEvent(Object source, View sender, KeypadBtnType btnType, int digit) {
			super(source);
			this.btnType = btnType;
			this.digit = digit;
			this.sender = sender;

		}

	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle b = new Bundle();
		b.putParcelable(STATE_INSTANCE_STATE, super.onSaveInstanceState());
		b.putBoolean(STATE_IS_HIDDEN, isHidden);
		return b;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle b = (Bundle) state;
			isHidden = b.getBoolean(STATE_IS_HIDDEN);
			state = b.getParcelable(STATE_INSTANCE_STATE);
		}
		super.onRestoreInstanceState(state);
	}
}
