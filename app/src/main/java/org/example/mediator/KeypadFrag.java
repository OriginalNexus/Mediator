package org.example.mediator;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;

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
	private boolean isHidden = true;
	/**
	 * View that contains the keyboard
	 */
	private View kView;
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
		kView = inflater.inflate(R.layout.keypad, container, false);
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
				for (int i = 0; i < ((LinearLayout) kView).getChildCount(); i++) {
					View child = ((LinearLayout) kView).getChildAt(i);
					// Makes the buttons keep their size
					child.setLayoutParams(new LinearLayout.LayoutParams(child.getWidth(), child.getHeight(), 0));
				}
				// Remove this listener
				kView.getViewTreeObserver().removeOnPreDrawListener(this);
				// Exact size
				kHeight = kView.getHeight();
				// Hide the keypad
				kView.getLayoutParams().height = 0;
				((LayoutParams)kView.getLayoutParams()).weight = 0;
				return false;
			}
		});

		// Create animations
		hideAnim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				kView.getLayoutParams().height = (int)(kHeight * (1 - interpolatedTime));
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
				kView.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		showAnim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				kView.getLayoutParams().height = (int) (kHeight * interpolatedTime);
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
				kView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		// All the buttons in specific order
		String buttonIds[] = {
				"digit_1", "digit_2", "digit_3",
				"digit_4", "digit_5", "digit_6",
				"digit_7", "digit_8", "digit_9",
				"digit_10", "digit_minus", "digit_done"};

		for (int i = 0; i < 12; i++) {
			// Find button view
			View btnView = kView.findViewById(getActivity().getResources().getIdentifier(buttonIds[i], "id", getActivity().getPackageName()));
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
				final KeypadEvent e = new KeypadEvent(this, btnType, digit);
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
	 * Bind a key press event listener
	 * @param listener The listener to be notified
	 */
	public void addKeypadListener(KeypadListener listener) {
		if (!keypadListenerList.contains(listener)) {
			keypadListenerList.add(listener);
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
		else //noinspection SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement
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
				kView.setVisibility(View.GONE);
			}
		}

		// If the keypad needs to be shown
		else if (!isHidden && kView.getVisibility() != View.VISIBLE) {
			if (showAnim != null) {
				// Start animation
				kView.startAnimation(showAnim);
			}
			else {
				// Show the view
				kView.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * The type of the pressed button
	 */
	public enum KeypadBtnType{
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
		public int digit = 0;

		/**
		 * Constructor of the event
		 * @param source Source object
		 * @param btnType The type of the pressed button
		 * @param digit The pressed digit or 0 if not available
		 */
		public KeypadEvent(Object source, KeypadBtnType btnType, int digit) {
			super(source);
			this.btnType = btnType;
			this.digit = digit;

		}
	}

}
