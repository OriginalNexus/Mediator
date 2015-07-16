package com.originalnexus.mediator;

import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

	// Constants

	private static final int LONG_PRESS_REMOVE_DELAY = 100;
	private static final String STATE_GRADES = "grades";
	private static final String STATE_THESIS = "thesis";
	private static final String STATE_EXTRA_GRADES = "extra_grades";
	private static final String STATE_INPUT_FIELD = "input_field";
	private static final String STATE_KEYPAD_IS_HIDDEN = "keypad_hidden";

	// Global Variables
	// Singleton
	private static MainActivity instance = null;
	// Grade cards tag prefix
	private final String gradesCardTagPrefix = "gradesCard_";
	// Array containing all the grades
	private ArrayList<Integer> grades = new ArrayList<>();
	// Thesis
	private int thesis = 0;
	// Extra grades
	private int extraGrades = 0;
	// Id of the active input field
	private int activeInputFieldId = 0;
	// Keypad fragment
	private KeypadFrag keypad;
	// Drawer toggle for the action bar
	private ActionBarDrawerToggle drawerToggle;

	@Nullable
	public static MainActivity getInstance() {
		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Singleton
		instance = this;

		// Set default settings if they are missing
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// Restore theme
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String themeValue = settings.getString("pref_theme", "light");
		if (themeValue != null) {
			switch (themeValue) {
				case "dark" :
					setTheme(R.style.AppTheme_Dark);
					break;
				case "light" :
					setTheme(R.style.AppTheme_Light);
					break;
			}
		}

		// Call super class
		super.onCreate(savedInstanceState);

		// Set layout
		setContentView(R.layout.activity_main);

		// Create grade card fragments
		createGradeCards();

		// Get keypad fragment
		keypad = (KeypadFrag) getSupportFragmentManager().findFragmentById(R.id.keypad_fragment);

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
				else if (e.btnType == KeypadFrag.KeypadBtnType.DIGIT) {
					if (activeInputFieldId == R.id.grades_input_field) {
						// Add to grades array
						grades.add(grades.size(), e.digit);
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
			public void keyLongPressed(final KeypadFrag.KeypadEvent e) {
				if (e.btnType == KeypadFrag.KeypadBtnType.REMOVE) {
					if (activeInputFieldId == R.id.grades_input_field) {
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
					else if (activeInputFieldId == R.id.thesis_input_field) {
						// Clear thesis
						thesis = 0;
						updateViews();
					}
				}
				else if (e.btnType == KeypadFrag.KeypadBtnType.DIGIT) {
					if (activeInputFieldId == R.id.grades_input_field) {
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

								// Add one grade
								if (activeInputFieldId == R.id.grades_input_field) {
									// Add to grades array
									grades.add(grades.size(), digitLongPressed);
								}

								// Update
								runOnUiThread(new Runnable() {
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

		// Setup Toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(toolbar);

		// Fix status bar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			getWindow().setStatusBarColor(Color.TRANSPARENT);
		}

		// Setup drawer
		final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		NavigationView navigationView = (NavigationView) findViewById(R.id.drawer_left);

		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {
				// When an item is selected from the navigation drawer
				boolean closeDrawer = true;
				switch (menuItem.getItemId()) {
					case R.id.drawer_item_mediator:

						break;
					case R.id.drawer_item_report_card:
						Toast.makeText(getApplicationContext(), "Coming Soon!", Toast.LENGTH_SHORT).show();
						closeDrawer = false;
						break;

					case R.id.drawer_item_settings:
						// Show app settings
						Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
						startActivity(intent);
						break;

					case R.id.drawer_item_help:
						// Show help dialog
						DialogFragment helpDialog = new HelpDialog();
						helpDialog.show(getSupportFragmentManager(), "help_dialog");
						break;

					case R.id.drawer_item_about:
						// Show about dialog
						DialogFragment aboutDialog = new AboutDialog();
						aboutDialog.show(getSupportFragmentManager(), "about_dialog");
						break;

					default:
						closeDrawer = false;
						break;
				}

				// Close the drawer
				if (closeDrawer)
					drawerLayout.closeDrawers();

				return true;
			}
		});

		// Add drawer toggle feature to the toolbar
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
		drawerLayout.setDrawerListener(drawerToggle);

		// Update text views
		updateViews();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync drawer toggle button state
		drawerToggle.syncState();
	}

	@Override
	public void finish() {
		instance = null;
		super.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_help:
				// Show help dialog
				DialogFragment helpDialog = new HelpDialog();
				helpDialog.show(getSupportFragmentManager(), "help_dialog");
				return true;
			case R.id.menu_about:
				// Show about dialog
				DialogFragment aboutDialog = new AboutDialog();
				aboutDialog.show(getSupportFragmentManager(), "about_dialog");
				return true;
			case R.id.menu_settings:
				// Show app settings
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Handles multiple views click event
 	 */
	@SuppressWarnings("unused")
	public void onClickEventListener(View view) {
		if (view.getId() == R.id.grades_input_field) {
			// Set new focus and toggle keypad
			keypad.toggleKeypad(1);
			focusField(R.id.grades_input_field);
		}
		else if (view.getId() == R.id.thesis_input_field) {
			// Set new focus and toggle keypad
			keypad.toggleKeypad(1);
			focusField(R.id.thesis_input_field);
		}
		else if (view.getId() == R.id.extra_grades_minus_btn) {
			// Decrement extra grades
			extraGrades--;
			if (extraGrades < 0) extraGrades = 0;
			updateViews();
		}
		else if (view.getId() == R.id.extra_grades_plus_btn) {
			// Increment extra grades
			extraGrades++;
			updateViews();
		}
	}

	/**
	 * Updates the text views
	 */
	@SuppressWarnings("WeakerAccess")
	void updateViews() {
		// Set grades input field
		TextView outTV = (TextView) findViewById(R.id.grades_input_field);
		String outStr = "";
		for (int i = 0; i < grades.size(); i++) {
			outStr += grades.get(i);
			if (i < grades.size() - 1) outStr += ", ";
		}
		outTV.setText(outStr);

		// Set thesis input field
		if (thesis != 0)
			((TextView) findViewById(R.id.thesis_input_field)).setText(Integer.toString(thesis));
		else
			((TextView) findViewById(R.id.thesis_input_field)).setText("");

		// Set average text and visibility
		if (!grades.isEmpty()) {
			double average;
			if (thesis != 0)
				average = GradeCalc.averageWithThesis(intArrayListToPrimitive(grades), thesis);
			else
				average = GradeCalc.average(intArrayListToPrimitive(grades));

			((TextView) findViewById(R.id.average_text_view)).setText(Double.toString(average));
			findViewById(R.id.average_container).setVisibility(View.VISIBLE);
		}
		else {
			findViewById(R.id.average_container).setVisibility(View.GONE);
		}

		// Set extra grades
		((TextView)findViewById(R.id.extra_grades_input_field)).setText(Integer.toString((extraGrades)));

		// Update grades cards
		if (extraGrades <= 0) {
			findViewById(R.id.grades_cards_container).setVisibility(View.GONE);
		}
		else {
			updateGradeCards();
			findViewById(R.id.grades_cards_container).setVisibility(View.VISIBLE);
		}

		// Focus field
		focusField(activeInputFieldId);
	}

	/**
	 * Creates all the grades cards fragments and adds them to their container
	 */
	private void createGradeCards() {
		for (int i = 1; i <= 10; i++) {
			GradesCard f = GradesCard.newInstance();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.grades_card_fragments_container, f, gradesCardTagPrefix + i)
					.commit();
		}
	}

	/**
	 * Updates all the grades cards (sets new content and/or visibility)
	 */
	private void updateGradeCards() {
		FragmentManager fragMan = getSupportFragmentManager();
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
	private void focusField(int viewId) {
		View v = findViewById(viewId);
		if (v != null) {
			v.setFocusableInTouchMode(true);
			v.requestFocus();
			v.setFocusableInTouchMode(false);
			activeInputFieldId = viewId;
		}
	}

	@Override
	public void onBackPressed() {
		if (!keypad.isHidden) {
			keypad.toggleKeypad(0);
		}
		else super.onBackPressed();
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putIntegerArrayList(STATE_GRADES, grades);
		outState.putInt(STATE_THESIS, thesis);
		outState.putInt(STATE_EXTRA_GRADES, extraGrades);
		outState.putInt(STATE_INPUT_FIELD, activeInputFieldId);
		outState.putBoolean(STATE_KEYPAD_IS_HIDDEN, keypad.isHidden);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		grades = savedInstanceState.getIntegerArrayList(STATE_GRADES);
		thesis = savedInstanceState.getInt(STATE_THESIS);
		extraGrades = savedInstanceState.getInt(STATE_EXTRA_GRADES);
		activeInputFieldId = savedInstanceState.getInt(STATE_INPUT_FIELD);
		keypad.toggleKeypad(savedInstanceState.getBoolean(STATE_KEYPAD_IS_HIDDEN) ? 0 : 1);

		updateViews();
	}

}
