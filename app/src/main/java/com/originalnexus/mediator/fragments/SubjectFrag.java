package com.originalnexus.mediator.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.originalnexus.mediator.dialogs.DeleteDialog;
import com.originalnexus.mediator.dialogs.NameDialog;
import com.originalnexus.mediator.GradeCalc;
import com.originalnexus.mediator.KeypadView;
import com.originalnexus.mediator.activities.MainActivity;
import com.originalnexus.mediator.R;
import com.originalnexus.mediator.Subject;
import com.originalnexus.mediator.SubjectAdapter;

import java.util.Timer;
import java.util.TimerTask;

public class SubjectFrag extends Fragment {

	private static final int LONG_PRESS_REMOVE_DELAY = 100;

	private static final String STATE_INDEX = "subject";
	private static final String STATE_INPUT_FIELD = "inputField";

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
	private KeypadView keypad;

	/**
	 * 	Create a new SubjectFrag
	 *  @param index index of the subject to display, edit
 	 */
	public static SubjectFrag newInstance(int index) {
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

		// Restore state
		if (savedInstanceState != null) {
			sIndex = savedInstanceState.getInt(STATE_INDEX);
			activeInputFieldId = savedInstanceState.getInt(STATE_INPUT_FIELD);
		}

		// Get subject
		s = sAdapter.subjects.get(sIndex);

		if (getView() != null) {
			keypad = (KeypadView) getView().findViewById(R.id.sub_frag_keypad_fragment);

			// Add event listener for key presses
			keypad.addKeypadListener(new KeypadView.KeypadListener() {
				@Override
				public void keyPressed(KeypadView.KeypadEvent e) {
					// This is the "Done" button
					if (e.btnType == KeypadView.KeypadBtnType.DONE) {
						// Close the keypad
						keypad.toggleKeypad(0);
					}
					// This is the "Remove" button
					else if (e.btnType == KeypadView.KeypadBtnType.REMOVE) {
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
					else if (e.btnType == KeypadView.KeypadBtnType.DIGIT) {
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
				public void keyLongPressed(final KeypadView.KeypadEvent e) {
					if (e.btnType == KeypadView.KeypadBtnType.REMOVE) {
						if (activeInputFieldId == R.id.sub_frag_grades) {
							final Timer timer = new Timer();
							timer.scheduleAtFixedRate(new TimerTask() {
								@Override
								public void run() {
									// Check to see if button is still pressed
									if (!e.sender.isPressed() || getActivity() == null) {
										timer.cancel();
										timer.purge();
										return;
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
					else if (e.btnType == KeypadView.KeypadBtnType.DIGIT) {
						if (activeInputFieldId == R.id.sub_frag_grades) {
							final Timer timer = new Timer();
							final int digitLongPressed = e.digit;
							timer.scheduleAtFixedRate(new TimerTask() {
								@Override
								public void run() {
									// Check to see if button is still pressed
									if (!e.sender.isPressed() || getActivity() == null) {
										timer.cancel();
										timer.purge();
										return;
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

			View.OnClickListener l = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v.getId() == R.id.sub_frag_grades) {
						// Set new focus and toggle keypad
						keypad.toggleKeypad(1);
						focusField(R.id.sub_frag_grades);
					}
					else if (v.getId() == R.id.sub_frag_thesis) {
						// Set new focus and toggle keypad
						keypad.toggleKeypad(1);
						focusField(R.id.sub_frag_thesis);
					}
					else if (v.getId() == R.id.sub_frag_name) {
						// Open name dialog
						NameDialog dialog = NameDialog.newInstance(DIALOG_REQ_CODE, s.name);
						dialog.show(getChildFragmentManager(), null);
					}
					else if (v.getId() == R.id.sub_frag_mediator_btn) {
						// Send info to mediator and open it
						MainActivity m = MainActivity.getInstance();
						if (m != null) m.openMediator(s, true);
					}
				}
			};

			getView().findViewById(R.id.sub_frag_grades).setOnClickListener(l);
			getView().findViewById(R.id.sub_frag_thesis).setOnClickListener(l);
			getView().findViewById(R.id.sub_frag_name).setOnClickListener(l);
			getView().findViewById(R.id.sub_frag_mediator_btn).setOnClickListener(l);
		}

		updateViews();
	}

	/**
	 * Updates the text views
	 */
	public void updateViews() {
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
			((TextView) getView().findViewById(R.id.sub_frag_thesis)).setText((s.thesis != 0) ? String.format("%d", s.thesis) : "");

			// Set average text and visibility
			if (!s.grades.isEmpty()) {
				double average = GradeCalc.average(s.grades, s.thesis);
				((TextView) getView().findViewById(R.id.sub_frag_average)).setText(String.format("(%.2f)", GradeCalc.floorDecimals(average)));
				((TextView) getView().findViewById(R.id.sub_frag_average_round)).setText(String.format("%d", GradeCalc.roundAverage(average)));
			}
			else {
				((TextView) getView().findViewById(R.id.sub_frag_average)).setText("");
				((TextView) getView().findViewById(R.id.sub_frag_average_round)).setText("");
			}

			// Set name text
			((TextView) getView().findViewById(R.id.sub_frag_name)).setText(s.name);

			// Focus field
			focusField(activeInputFieldId);
		}

		// Also save subjects
		sAdapter.saveSubjects();
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

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		if (savedInstanceState != null) {
			sIndex = savedInstanceState.getInt(STATE_INDEX);
			activeInputFieldId = savedInstanceState.getInt(STATE_INPUT_FIELD);
		}
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.subject, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_delete) {
			DeleteDialog f = DeleteDialog.newInstance(sIndex);
			f.show(getChildFragmentManager(), null);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Call this to handle keyboard on back press
	 * @return Whether the keyboard was hid or not
	 */
	public boolean onBackPressed() {
		if (keypad.isKeypadHidden()) return false;
		keypad.toggleKeypad(0);
		return true;
	}
}
