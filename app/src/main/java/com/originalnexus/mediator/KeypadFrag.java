package com.originalnexus.mediator;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;

/**
 * Provides a simple keypad for the user to type grades faster.
 * Fragment MUST be placed inside a LinearLayout, or else crash
 */
public class KeypadFrag extends Fragment {
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
	public boolean isHidden = true;
	/**
	 * View that contains the keyboard
	 */
	private LinearLayout kView;
	/**
	 * Keypad measured height in px
	 */
	private int kHeight;
	/**
	 * Hide animation
	 */
	private Animation hideAnim;
	/**
	 * Show animation
	 */
	private Animation showAnim;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		kView = (LinearLayout) inflater.inflate(R.layout.keypad, container, false);
		return kView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Let view "render" it's sizes and then "lock" everything (for the slide up/down animations)
		kView.setVisibility(View.INVISIBLE);
		kView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				// After the view is ready to be shown we can get it's exact sizes and then hide it
				for (int i = 0; i < kView.getChildCount(); i++) {
					View child = kView.getChildAt(i);
					// Makes the buttons keep their size
					child.setLayoutParams(new LinearLayout.LayoutParams(child.getWidth(), child.getHeight(), 0));
				}
				// Remove this listener
				kView.getViewTreeObserver().removeOnPreDrawListener(this);
				// Exact size
				kHeight = kView.getHeight();
				// Hide the keypad
				setHeight(0);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					((LinearLayout.LayoutParams) kView.getLayoutParams()).weight = 0;
				}
				else {
					/**
					 * On android api level < 11 the support library adds an extra view to the
					 * view hierarchy: NoSaveStateFrameLayout which extends FrameLayout
					 * Therefore we need to acces the parent of our view.
					 */
					((LinearLayout.LayoutParams) ((View) kView.getParent()).getLayoutParams()).weight = 0;
				}
				return false;
			}
		});

		// Create animations
		hideAnim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				setHeight((int) (kHeight * (1 - interpolatedTime)));
				kView.requestLayout();
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		hideAnim.setDuration(ANIMATION_LENGTH);
		hideAnim.setInterpolator(new AccelerateInterpolator());
		hideAnim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				hide();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		showAnim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				setHeight((int) (kHeight * interpolatedTime));
				kView.requestLayout();
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		showAnim.setDuration(ANIMATION_LENGTH);
		showAnim.setInterpolator(new DecelerateInterpolator());
		showAnim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				kView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				show();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		// For some reason animations do not work on api level < 11 so disable them
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			showAnim = null;
			hideAnim = null;
		}

		// All the buttons in specific order
		int[] buttonIds = {
				R.id.digit_1, R.id.digit_2, R.id.digit_3,
				R.id.digit_4, R.id.digit_5, R.id.digit_6,
				R.id.digit_7, R.id.digit_8, R.id.digit_9,
				R.id.digit_10, R.id.digit_minus, R.id.digit_done};

		for (int i = 0; i < 12; i++) {
			// Find button view
			View btnView = kView.findViewById(buttonIds[i]);
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

	/**
	 * Get actual parent
 	 */
	private ViewParent getParent() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return kView.getParent();
		}
		else {
			return kView.getParent().getParent();
		}
	}

	/**
	 * Open/Close/Toggle the keypad
	 *
	 * @param sw 1 = open ; 0 = close; other = toggle
	 */
	public void toggleKeypad(int sw) {
		// If an animation is in progress do nothing
		if (kView.getAnimation() != null) return;

		// Set isHidden property based on the switch

		if (sw == 0) isHidden = true;
		else //noinspection SimplifiableIfStatement
			if (sw == 1) isHidden = false;
			else isHidden = !isHidden;

		// If the keypad needs to be hid
		if (isHidden && kView.getVisibility() == View.VISIBLE) {
			if (hideAnim != null) {
				// Start animation
				kView.startAnimation(hideAnim);
			}
			else {
				// Hide the view
				hide();
			}
		}

		// If the keypad needs to be shown
		else if (!isHidden && kView.getVisibility() != View.VISIBLE) {
			if (showAnim != null) {
				// Start animation
				((View)getParent()).invalidate();
				kView.startAnimation(showAnim);
			}
			else {
				// Show the view
				show();
				getParent().requestLayout();
			}
		}
	}

	/**
	 * Shows the keypad immediately
	 */
	private void show() {
		setHeight(kHeight);
		kView.setVisibility(View.VISIBLE);
	}

	/**
	 * Hides the keypad immediately
	 */
	private void hide() {
		setHeight(0);
		kView.setVisibility(View.GONE);
	}

	/**
	 * Sets keypad height
	 *
	 * @param height The new height in pixels or using one of the constants:
	 *               LinearLayout.LayoutParams.WRAP_CONTENT
	 *               LinearLayout.LayoutParams.MATCH_PARENT
	 */
	private void setHeight(int height) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			kView.getLayoutParams().height = height;
		}
		else {
			/**
			 * On android api level < 11 the support library adds an extra view to the
			 * view hierarchy: NoSaveStateFrameLayout which extends FrameLayout
			 * Therefore we need to acces the parent of our view.
			 */
			((View) kView.getParent()).getLayoutParams().height = height;
		}
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
}
