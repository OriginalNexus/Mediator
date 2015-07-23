package com.originalnexus.mediator;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class SubjectFrag extends Fragment {


	private static final int LONG_PRESS_REMOVE_DELAY = 100;

	private static final String STATE_INDEX = "subject";
	private static final String STATE_INPUT_FIELD = "inputField";
	private static final String STATE_KEYPAD_IS_HIDDEN = "keypad_hidden";

	public static final int DIALOG_REQ_CODE = 1;

	@Nullable
	public static SubjectFrag instance;

	// Adapter
	public static SubjectAdapter sAdapter;

	// Global variables

	// Subject
	public Subject s;
	// Index
	private int sIndex;
	// Id of the active input field
	private int activeInputFieldId = 0;
	// Keypad fragment
	private KeypadFrag keypad;
	private boolean keypadIsHidden = true;

	/**
	 * 	Create a new SubjectFrag
	 *  @param index index of the subject to display, edit
 	 */
	static SubjectFrag newInstance(int index) {
		SubjectFrag frag = new SubjectFrag();
		frag.sIndex = index;
		return frag;
	}

	@Override
	public void onDestroy() {
		instance = null;
		super.onDestroy();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Keypad fragment
		if (savedInstanceState == null) {
			getChildFragmentManager().beginTransaction().replace(R.id.sub_frag_keypad_fragment, new KeypadFrag()).commit();
			getChildFragmentManager().executePendingTransactions();
		}

		// Restore state
		if (savedInstanceState != null) {
			sIndex = savedInstanceState.getInt(STATE_INDEX);
			activeInputFieldId = savedInstanceState.getInt(STATE_INPUT_FIELD);
			keypadIsHidden = savedInstanceState.getBoolean(STATE_KEYPAD_IS_HIDDEN);
		}

		// Get subject
		s = sAdapter.subjects.get(sIndex);

		keypad = (KeypadFrag) getChildFragmentManager().findFragmentById(R.id.sub_frag_keypad_fragment);

		// Add event listener for key presses
		keypad.addKeypadListener(new KeypadFrag.KeypadListener() {
			@Override
			public void keyPressed(KeypadFrag.KeypadEvent e) {
				// This is the "Done" button
				if (e.btnType == KeypadFrag.KeypadBtnType.DONE) {
					// Close the keypad
					keypad.toggleKeypad(0);
					keypadIsHidden = false;
				}
				// This is the "Remove" button
				else if (e.btnType == KeypadFrag.KeypadBtnType.REMOVE) {
					if (activeInputFieldId == R.id.sub_frag_grades) {
						// Remove one
						if (!s.grades.isEmpty()) s.grades.remove(s.grades.size() - 1);
					}
					if (activeInputFieldId == R.id.sub_frag_thesis) {
						// Clear thesis
						s.thesis = 0;
					}

					// Update
					updateViews();
				}
				// This is a digit button
				else if (e.btnType == KeypadFrag.KeypadBtnType.DIGIT) {
					if (activeInputFieldId == R.id.sub_frag_grades) {
						// Add to grades array
						s.grades.add(s.grades.size(), e.digit);
					}
					if (activeInputFieldId == R.id.sub_frag_thesis) {
						// Set new thesis
						s.thesis = e.digit;
					}
					// Update
					updateViews();
				}
			}

			@Override
			public void keyLongPressed(final KeypadFrag.KeypadEvent e) {
				if (e.btnType == KeypadFrag.KeypadBtnType.REMOVE) {
					if (activeInputFieldId == R.id.sub_frag_grades) {
						final Timer timer = new Timer();
						timer.scheduleAtFixedRate(new TimerTask() {
							@Override
							public void run() {
								// Check to see if button is still pressed
								if (!e.sender.isPressed()) {
									timer.cancel();
									timer.purge();
								}

								// Delete one grade
								if (!s.grades.isEmpty()) s.grades.remove(s.grades.size() - 1);
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										updateViews();
									}
								});
							}
						}, 0, LONG_PRESS_REMOVE_DELAY);
					}
					else if (activeInputFieldId == R.id.sub_frag_thesis) {
						// Clear thesis
						s.thesis = 0;
						updateViews();
					}
				}
				else if (e.btnType == KeypadFrag.KeypadBtnType.DIGIT) {
					if (activeInputFieldId == R.id.sub_frag_grades) {
						final Timer timer = new Timer();
						final int digitLongPressed = e.digit;
						timer.scheduleAtFixedRate(new TimerTask() {
							@Override
							public void run() {
								// Check to see if button is still pressed
								if (!e.sender.isPressed()) {
									timer.cancel();
									timer.purge();
								}

								// Add to grades array
								s.grades.add(s.grades.size(), digitLongPressed);

								// Update
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										updateViews();
									}
								});
							}
						}, 0, LONG_PRESS_REMOVE_DELAY);
					}
					else if (activeInputFieldId == R.id.sub_frag_thesis) {
						// Set new thesis
						s.thesis = e.digit;
						updateViews();
					}
				}
			}
		});

		// Add click events
		if (getView() != null) {
			View.OnClickListener l = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v.getId() == R.id.sub_frag_grades) {
						// Set new focus and toggle keypad
						keypad.toggleKeypad(1);
						keypadIsHidden = true;
						focusField(R.id.sub_frag_grades);
					}
					else if (v.getId() == R.id.sub_frag_thesis) {
						// Set new focus and toggle keypad
						keypad.toggleKeypad(1);
						keypadIsHidden = true;
						focusField(R.id.sub_frag_thesis);
					}
					else if (v.getId() == R.id.sub_frag_name) {
						// Open name dialog
						NameDialog dialog = NameDialog.newInstance(DIALOG_REQ_CODE, s.name);
						dialog.show(getChildFragmentManager(), null);
					}
				}
			};

			getView().findViewById(R.id.sub_frag_grades).setOnClickListener(l);
			getView().findViewById(R.id.sub_frag_thesis).setOnClickListener(l);
			getView().findViewById(R.id.sub_frag_name).setOnClickListener(l);
		}

		// Restore keypad
		// TODO: Fix Bug on api 10 when changing orientation
		// TODO: Fix not restoring
		keypad.toggleKeypad(keypadIsHidden ? 0 : 1);

		updateViews();
	}

	/**
	 * Updates the text views
	 */
	@SuppressWarnings("WeakerAccess")
	void updateViews() {
		if (getView() != null) {
			// Set grades input field
			TextView outTV = (TextView) getView().findViewById(R.id.sub_frag_grades);
			String outStr = "";
			for (int i = 0; i < s.grades.size(); i++) {
				outStr += s.grades.get(i);
				if (i < s.grades.size() - 1) outStr += ", ";
			}
			outTV.setText(outStr);

			// Set thesis input field
			if (s.thesis != 0) ((TextView) getView().findViewById(R.id.sub_frag_thesis)).setText(Integer.toString(s.thesis));
			else ((TextView) getView().findViewById(R.id.sub_frag_thesis)).setText("");

			// Set average text and visibility
			if (!s.grades.isEmpty()) {
				double average = GradeCalc.average(s.grades, s.thesis);
				((TextView) getView().findViewById(R.id.sub_frag_average)).setText(Double.toString(average));
			}
			else {
				((TextView) getView().findViewById(R.id.sub_frag_average)).setText("");
			}

			// Set name text
			((TextView) getView().findViewById(R.id.sub_frag_name)).setText(s.name);

			// Focus field
			focusField(activeInputFieldId);
		}
	}

	/**
	 * Focus the given field and update activeInputFieldId
	 * @param viewId The Id of the field/view to focus
	 */
	private void focusField(int viewId) {
		View v = (getView() != null) ? getView().findViewById(viewId) : null;
		if (v != null) {
			v.setFocusableInTouchMode(true);
			v.requestFocus();
			v.setFocusableInTouchMode(false);
			activeInputFieldId = viewId;
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.subject, container, false);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putInt(STATE_INDEX, sIndex);
		outState.putInt(STATE_INPUT_FIELD, activeInputFieldId);
		outState.putBoolean(STATE_KEYPAD_IS_HIDDEN, keypadIsHidden);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		if (savedInstanceState != null) {
			sIndex = savedInstanceState.getInt(STATE_INDEX);
			activeInputFieldId = savedInstanceState.getInt(STATE_INPUT_FIELD);
			keypadIsHidden = savedInstanceState.getBoolean(STATE_KEYPAD_IS_HIDDEN);
		}
	}

}
