package org.example.mediator;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

	// Constants

	private static final int LONG_PRESS_REMOVE_DELAY = 100;

	// Global Variables

	// Array containing all the grades
	private final ArrayList<Integer> grades = new ArrayList<>();
	// Grade cards tag prefix
	private final String gradesCardTagPrefix = "gradesCard_";
	// Thesis
	private int thesis = 0;
	// Extra grades
	private int extraGrades = 0;
	// Id for the current input field
	private int activeInputFieldId = R.id.gradesInputField;
	// Keypad fragment
	private KeypadFrag keypad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create grade card fragments
		createGradeCards();

		// Get keypad fragment
		keypad = (KeypadFrag) getFragmentManager().findFragmentById(R.id.keypadFragment);

		// Add event listener for key presses
		keypad.addKeypadListener(new KeypadFrag.KeypadListener() {
			@Override
			public void keyPressed(KeypadFrag.KeypadEvent e) {
				// This is the "Done" button
				if (e.btnType == KeypadFrag.KeypadBtnType.DONE)
					// Close the keypad
					keypad.toggleKeypad(0);
					// This is the "Remove" button
				else if (e.btnType == KeypadFrag.KeypadBtnType.REMOVE) {
					if (activeInputFieldId == R.id.gradesInputField) {
						// Remove one
						if (!grades.isEmpty()) grades.remove(grades.size() - 1);
					}
					if (activeInputFieldId == R.id.thesisInputField) {
						// Clear thesis
						thesis = 0;
					}

					// Update
					updateViews();
				}
				// This is a digit button
				else if (e.btnType == KeypadFrag.KeypadBtnType.DIGIT) {
					if (activeInputFieldId == R.id.gradesInputField) {
						// Add to grades array
						grades.add(grades.size(), e.digit);
					}
					if (activeInputFieldId == R.id.thesisInputField) {
						// Set new thesis
						thesis = e.digit;
					}
					// Update
					updateViews();
				}
			}

			@Override
			public void keyLongPressed(KeypadFrag.KeypadEvent e) {
				if (e.btnType == KeypadFrag.KeypadBtnType.REMOVE) {
					final Timer timer = new Timer();
					timer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							if (keypad.getView() != null && keypad.getView().findViewById(R.id.digit_minus) != null && !keypad.getView().findViewById(R.id.digit_minus).isPressed()) {
								timer.cancel();
								timer.purge();
							}
							// Delete one grade
							if (!grades.isEmpty()) grades.remove(grades.size() - 1);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									updateViews();
								}
							});
						}
					}, 0, LONG_PRESS_REMOVE_DELAY);
				}
				else if (e.btnType == KeypadFrag.KeypadBtnType.DIGIT) {
					final Timer timer = new Timer();
					final int digitLongPressed = e.digit;
					timer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							if (keypad.getView() != null) {
								Button pressedBtn = (Button) keypad.getView().findViewById(getResources().getIdentifier("digit_" + digitLongPressed, "id", getPackageName()));
								if (pressedBtn != null && !pressedBtn.isPressed()) {
									timer.cancel();
									timer.purge();
								}
							}

							// Add one grade
							if (activeInputFieldId == R.id.gradesInputField) {
								// Add to grades array
								grades.add(grades.size(), digitLongPressed);
							}

							// Update
							runOnUiThread(new Runnable() {
								@Override public void run () {
									updateViews();
								}
							});
						}
					},0, LONG_PRESS_REMOVE_DELAY);
				}
			}
		});

		// Update text views
		updateViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_help:
				// Show help dialog
				DialogFragment helpDialog = new HelpDialog();
				helpDialog.show(getFragmentManager(), "help_dialog");
				return true;
			case R.id.menu_about:
				// Show help dialog
				DialogFragment aboutDialog = new AboutDialog();
				aboutDialog.show(getFragmentManager(), "about_dialog");
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Handles multiple views click event
 	 */
	public void onClickEventListener(View view) {
		if (view.getId() == R.id.gradesInputField) {
			// Set new focus and toggle keypad
			keypad.toggleKeypad(1);
			focusField(R.id.gradesInputField);
		}
		else if (view.getId() == R.id.thesisInputField) {
			// Set new focus and toggle keypad
			keypad.toggleKeypad(1);
			focusField(R.id.thesisInputField);
		}
		else if (view.getId() == R.id.extraGradesMinusBtn) {
			// Decrement extra grades
			extraGrades--;
			if (extraGrades < 0) extraGrades = 0;
			updateViews();
		}
		else if (view.getId() == R.id.extraGradesPlusBtn) {
			// Increment extra grades
			extraGrades++;
			updateViews();
		}
	}

	/**
	 * Updates the text views
	 */
	void updateViews() {
		// Set grades input field
		TextView outTV = (TextView) findViewById(R.id.gradesInputField);
		String outStr = "";
		for (int i = 0; i < grades.size(); i++) {
			outStr += grades.get(i);
			if (i < grades.size() - 1) outStr += ", ";
		}
		outTV.setText(outStr);

		// Set thesis input field
		if (thesis != 0)
			((TextView) findViewById(R.id.thesisInputField)).setText(Integer.toString(thesis));
		else
			((TextView) findViewById(R.id.thesisInputField)).setText("");

		// Set average text and visibility
		if (!grades.isEmpty()) {
			double average;
			if (thesis != 0)
				average = GradeCalc.averageWithThesis(intArrayListToPrimitive(grades), thesis);
			else
				average = GradeCalc.average(intArrayListToPrimitive(grades));

			((TextView) findViewById(R.id.averageView)).setText(Double.toString(average));
			findViewById(R.id.averageContainer).setVisibility(View.VISIBLE);
		}
		else {
			findViewById(R.id.averageContainer).setVisibility(View.GONE);
		}

		// Set extra grades
		((TextView)findViewById(R.id.extraGredesInputField)).setText(Integer.toString((extraGrades)));

		// Update grades cards
		if (extraGrades <= 0) {
			findViewById(R.id.gradesCardsContainer).setVisibility(View.GONE);
		}
		else {
			updateGradeCards();
			findViewById(R.id.gradesCardsContainer).setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Creates all the grades cards fragments and adds them to their container
	 */
	private void createGradeCards() {
		for (int i = 1; i <= 10; i++) {
			GradesCard f = GradesCard.newInstance(true, true);
			getFragmentManager().beginTransaction()
					.add(R.id.gradesCardFragmentsContainer, f, gradesCardTagPrefix + i)
					.commit();
		}
	}

	/**
	 * Updates all the grades cards (sets new content and/or visibility)
	 */
	private void updateGradeCards() {
		FragmentManager fragMan = getFragmentManager();
		boolean[] cardIsVisible = new boolean[10];

		for (int i = 1; i <= 10; i++) {
			GradesCard f = (GradesCard) fragMan.findFragmentByTag(gradesCardTagPrefix + i);
			if (f == null) continue;

			if (extraGrades <= 0) {
				// Hide all cards
				cardIsVisible[i - 1] = false;
			}
			else {
				f.setData(intArrayListToPrimitive(grades), thesis, extraGrades, i);
				cardIsVisible[i - 1] = true;
				if (i > 1) {
					// Previous card
					GradesCard prev = (GradesCard) fragMan.findFragmentByTag(gradesCardTagPrefix + (i - 1));
					if (prev == null) continue;

					// Hide useless cards
					if (prev.getState() == GradesCard.GradesCardState.OVER && f.getState() == prev.getState()) {
						cardIsVisible[i - 2] = false;
					}
					else if (prev.getState() == GradesCard.GradesCardState.UNDER) {
						cardIsVisible[i - 1] = false;
					}
				}
			}
		}

		cardIsVisible[0] = false;
		FragmentTransaction fragTrans;
		for (int i = 1; i <= 10; i++) {
			fragTrans = fragMan.beginTransaction();
			GradesCard f = (GradesCard) fragMan.findFragmentByTag(gradesCardTagPrefix + i);
			if (f == null) continue;

			if (cardIsVisible[i - 1]) {
				f.updateViews();
				if (f.isHidden()) fragTrans.show(f);
			}
			else if (!cardIsVisible[i - 1] && f.isVisible())
				fragTrans.hide(f);
			fragTrans.commit();
			fragMan.executePendingTransactions();
		}
	}

	/**
	 * Converts an ArrayList<Integer> to an int[]
	 * @param a The list
	 * @return The primitive array
	 */
	private int[] intArrayListToPrimitive(ArrayList<Integer> a) {
		int[] primitive = new int[a.size()];
		for (int i = 0; i < a.size(); i++) {
			if (a.get(i) != null)
				primitive[i] = a.get(i);
		}
		return primitive;
	}

	/**
	 * Focus the given field and update activeInputFieldId
	 * @param viewId The Id of the field/view to focus
	 */
	void focusField(int viewId) {
		View v = findViewById(viewId);
		if (v != null) {
			v.setFocusableInTouchMode(true);
			v.requestFocus();
			v.setFocusableInTouchMode(false);
			activeInputFieldId = viewId;
		}
	}

}
