package com.originalnexus.mediator.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.originalnexus.mediator.GradeCalc;
import com.originalnexus.mediator.KeypadView;
import com.originalnexus.mediator.R;
import com.originalnexus.mediator.Subject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Mediator Fragment
 */
public class MediatorFrag extends Fragment {

	// Constants

	private static final int LONG_PRESS_REMOVE_DELAY = 100;
	private static final String STATE_GRADES = "grades";
	private static final String STATE_THESIS = "thesis";
	private static final String STATE_EXTRA_GRADES = "extra_grades";
	private static final String STATE_INPUT_FIELD = "input_field";

	// Global variables

	// Grade cards tag prefix
	private final String gradesCardTagPrefix = "frag_gradesCard_";
	// Array containing all the grades
	private ArrayList<Integer> grades = new ArrayList<>();
	// Thesis
	private int thesis = 0;
	// Extra grades
	private int extraGrades = 0;
	// Id of the active input field
	private int activeInputFieldId = 0;
	// Keypad fragment
	private KeypadView keypad;

	public static MediatorFrag newInstance(Subject s) {
		MediatorFrag f = new MediatorFrag();
		Subject sClone = new Subject(s);
		f.grades = sClone.grades;
		f.thesis = sClone.thesis;
		f.extraGrades = 1;
		return f;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create grade card fragments
		createGradeCards();

		if (getView() != null) {
			keypad = (KeypadView) getView().findViewById(R.id.keypad_view);

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
						if (activeInputFieldId == R.id.grades_input_field) {
							// Remove one
							if (!grades.isEmpty()) grades.remove(grades.size() - 1);
						}
						if (activeInputFieldId == R.id.thesis_input_field) {
							// Clear thesis
							thesis = 0;
						}

						// Update
						updateViews();
					}
					// This is a digit button
					else if (e.btnType == KeypadView.KeypadBtnType.DIGIT) {
						if (activeInputFieldId == R.id.grades_input_field) {
							// Add to grades array
							grades.add(e.digit);
						}
						if (activeInputFieldId == R.id.thesis_input_field) {
							// Set new thesis
							thesis = e.digit;
						}
						// Update
						updateViews();
					}
				}

				@Override
				public void keyLongPressed(final KeypadView.KeypadEvent e) {
					if (e.btnType == KeypadView.KeypadBtnType.REMOVE) {
						if (activeInputFieldId == R.id.grades_input_field) {
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
									if (!grades.isEmpty()) grades.remove(grades.size() - 1);
									getActivity().runOnUiThread(new Runnable() {
										@Override
										public void run() {
											updateViews();
										}
									});
								}
							}, 0, LONG_PRESS_REMOVE_DELAY);
						}
						else if (activeInputFieldId == R.id.thesis_input_field) {
							// Clear thesis
							thesis = 0;
							updateViews();
						}
					}
					else if (e.btnType == KeypadView.KeypadBtnType.DIGIT) {
						if (activeInputFieldId == R.id.grades_input_field) {
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
									grades.add(digitLongPressed);

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
						else if (activeInputFieldId == R.id.thesis_input_field) {
							// Set new thesis
							thesis = e.digit;
							updateViews();
						}
					}
				}
			});

			// Add click events for other components
			View.OnClickListener l = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v.getId() == R.id.grades_input_field) {
						// Set new focus and toggle keypad
						keypad.toggleKeypad(1);
						focusField(R.id.grades_input_field);
					}
					else if (v.getId() == R.id.thesis_input_field) {
						// Set new focus and toggle keypad
						keypad.toggleKeypad(1);
						focusField(R.id.thesis_input_field);
					}
					else if (v.getId() == R.id.extra_grades_minus_btn) {
						// Decrement extra grades
						extraGrades--;
						if (extraGrades < 0) extraGrades = 0;
						updateViews();
					}
					else if (v.getId() == R.id.extra_grades_plus_btn) {
						// Increment extra grades
						extraGrades++;
						updateViews();
					}
				}
			};

			getView().findViewById(R.id.grades_input_field).setOnClickListener(l);
			getView().findViewById(R.id.thesis_input_field).setOnClickListener(l);
			getView().findViewById(R.id.extra_grades_plus_btn).setOnClickListener(l);
			getView().findViewById(R.id.extra_grades_minus_btn).setOnClickListener(l);
		}

		// Restore state
		if (savedInstanceState != null) {
			grades = savedInstanceState.getIntegerArrayList(STATE_GRADES);
			thesis = savedInstanceState.getInt(STATE_THESIS);
			extraGrades = savedInstanceState.getInt(STATE_EXTRA_GRADES);
			activeInputFieldId = savedInstanceState.getInt(STATE_INPUT_FIELD);
		}

		updateViews();
	}

	/**
	 * Updates all views
	 */
	private void updateViews() {
		if (getView() != null) {
			// Set grades input field
			((TextView) getView().findViewById(R.id.grades_input_field)).setText(GradeCalc.arrayListToString(grades));

			// Set thesis input field
			if (thesis != 0) ((TextView) getView().findViewById(R.id.thesis_input_field)).setText(String.format("%d", thesis));
			else ((TextView) getView().findViewById(R.id.thesis_input_field)).setText("");

			// Set average text and visibility
			if (!grades.isEmpty()) {
				double average = GradeCalc.average(grades, thesis);
				((TextView) getView().findViewById(R.id.average_text_view)).setText(String.format("(%.2f)", GradeCalc.floorDecimals(average)));
				((TextView) getView().findViewById(R.id.average_round_text_view)).setText(String.format("%d", GradeCalc.roundAverage(average)));
				getView().findViewById(R.id.average_container).setVisibility(View.VISIBLE);
			}
			else {
				getView().findViewById(R.id.average_container).setVisibility(View.GONE);
			}

			// Set extra grades
			((TextView) getView().findViewById(R.id.extra_grades_input_field)).setText(String.format("%d", extraGrades));

			// Update grades cards
			if (extraGrades <= 0) {
				getView().findViewById(R.id.grades_cards_container).setVisibility(View.GONE);
			}
			else {
				updateGradeCards();
				getView().findViewById(R.id.grades_cards_container).setVisibility(View.VISIBLE);
			}

			// Focus field
			focusField(activeInputFieldId);
		}
	}

	/**
	 * Creates all the grades cards fragments and adds them to their container
	 */
	private void createGradeCards() {
		FragmentTransaction fragTrans = getChildFragmentManager().beginTransaction();
		for (int i = 1; i <= 10; i++) {
			fragTrans.add(R.id.grades_card_fragments_container, new GradesCardFrag(), gradesCardTagPrefix + i);
		}
		fragTrans.commit();
		getChildFragmentManager().executePendingTransactions();
	}

	/**
	 * Updates all the grades cards (sets new content and/or visibility)
	 */
	private void updateGradeCards() {
		FragmentManager fragMan = getChildFragmentManager();
		boolean[] cardIsVisible = new boolean[10];

		for (int i = 1; i <= 10; i++) {
			GradesCardFrag f = (GradesCardFrag) fragMan.findFragmentByTag(gradesCardTagPrefix + i);
			if (f == null) continue;

			if (extraGrades <= 0) {
				// Hide all cards
				cardIsVisible[i - 1] = false;
			}
			else {
				f.setData(grades, thesis, extraGrades, i);
				cardIsVisible[i - 1] = true;
				if (i > 1) {
					// Previous card
					GradesCardFrag prev = (GradesCardFrag) fragMan.findFragmentByTag(gradesCardTagPrefix + (i - 1));
					if (prev == null) continue;

					// Hide useless cards
					if (prev.getState() == GradesCardFrag.GradesCardState.OVER && f.getState() == prev.getState()) {
						cardIsVisible[i - 2] = false;
					}
					else if (prev.getState() == GradesCardFrag.GradesCardState.UNDER) {
						cardIsVisible[i - 1] = false;
					}
				}
			}
		}

		cardIsVisible[0] = false;
		FragmentTransaction fragTrans = fragMan.beginTransaction();
		for (int i = 1; i <= 10; i++) {
			GradesCardFrag f = (GradesCardFrag) fragMan.findFragmentByTag(gradesCardTagPrefix + i);
			if (f == null) continue;

			if (cardIsVisible[i - 1]) {
				if (!f.created) f.startHidden = false;
				else {
					if (f.isHidden()) {
						fragTrans.show(f);
					}
					f.updateViews();
				}
			}
			else if (!cardIsVisible[i - 1])
				if (!f.created) f.startHidden = true;
				else if (f.isVisible())
					fragTrans.hide(f);
		}
		fragTrans.commit();
		fragMan.executePendingTransactions();
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

	/**
	 * Call this to handle keyboard on back press
	 * @return Whether the keyboard was hid or not
	 */
	public boolean onBackPressed() {
		if (keypad.isKeypadHidden()) return false;
		keypad.toggleKeypad(0);
		return true;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.mediator, container, false);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putIntegerArrayList(STATE_GRADES, grades);
		outState.putInt(STATE_THESIS, thesis);
		outState.putInt(STATE_EXTRA_GRADES, extraGrades);
		outState.putInt(STATE_INPUT_FIELD, activeInputFieldId);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			grades = savedInstanceState.getIntegerArrayList(STATE_GRADES);
			thesis = savedInstanceState.getInt(STATE_THESIS);
			extraGrades = savedInstanceState.getInt(STATE_EXTRA_GRADES);
			activeInputFieldId = savedInstanceState.getInt(STATE_INPUT_FIELD);
		}
	}
}
