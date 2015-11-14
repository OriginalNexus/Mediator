package com.originalnexus.mediator.activities;

import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.originalnexus.mediator.dialogs.AboutDialog;
import com.originalnexus.mediator.dialogs.DeleteDialog;
import com.originalnexus.mediator.dialogs.HelpDialog;
import com.originalnexus.mediator.dialogs.NameDialog;
import com.originalnexus.mediator.fragments.MediatorFrag;
import com.originalnexus.mediator.fragments.ReportCardFrag;
import com.originalnexus.mediator.fragments.SubjectFrag;
import com.originalnexus.mediator.R;
import com.originalnexus.mediator.Subject;

public class MainActivity extends AppCompatActivity implements NameDialog.NameDialogListener, ReportCardFrag.SubjectClickListener, DeleteDialog.DeleteDialogListener{

	// Singleton
	private static MainActivity instance = null;
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
		switch (themeValue) {
			case "dark" :
				setTheme(R.style.Mediator_Dark);
				break;
			case "light" :
				setTheme(R.style.Mediator_Light);
				break;
		}

		// Call super class
		super.onCreate(savedInstanceState);

		// Set layout
		setContentView(R.layout.main);

		// Setup Toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			toolbar.setElevation(getResources().getDimension(R.dimen.ActionBar_elevation));
		}
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
				FragmentManager fragMan = getSupportFragmentManager();
				boolean closeDrawer = true;
				switch (menuItem.getItemId()) {
					case R.id.drawer_item_mediator:
						menuItem.setChecked(true);
						if (getSupportActionBar() != null)
							getSupportActionBar().setTitle(R.string.app_name);
						openMediator(null, false);
						fragMan.executePendingTransactions();
						break;
					case R.id.drawer_item_report_card:
						if (getSupportActionBar() != null)
							getSupportActionBar().setTitle(R.string.report_card_title);
						menuItem.setChecked(true);
						openReportCard();
						fragMan.executePendingTransactions();
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
				if (closeDrawer) drawerLayout.closeDrawers();

				return false;
			}
		});

		// Add drawer toggle feature to the toolbar
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
		drawerLayout.setDrawerListener(drawerToggle);

		// Add/Restore fragment
		if (savedInstanceState == null)
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MediatorFrag()).commit();

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

	@Override
	public void onNameDialogConfirm(int requestCode, String input) {
		switch (requestCode) {
			case ReportCardFrag.DIALOG_REQ_CODE:
				Subject s = new Subject(input);
				ReportCardFrag.sAdapter.addSubject(s);
				ReportCardFrag.sAdapter.saveSubjects();
				break;
			case SubjectFrag.DIALOG_REQ_CODE:
				SubjectFrag f = SubjectFrag.instance;
				if (f != null) {
					f.s.name = input;
					f.updateViews();
				}
				break;
		}
	}

	@Override
	public void onSubjectClick(int index) {
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, SubjectFrag.newInstance(index)).addToBackStack(null).commit();
	}

	@Override
	public void onDeleteSubject(int index) {
		SubjectFrag.sAdapter.removeSubject(index);
		ReportCardFrag.sAdapter.saveSubjects();
		openReportCard();
	}

	public void openMediator(@Nullable Subject subject, boolean addToBackStack) {
		MediatorFrag f;
		if (subject != null) f = MediatorFrag.newInstance(subject);
		else f = new MediatorFrag();

		if (addToBackStack) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, f).addToBackStack(null).commit();
		}
		else {
			getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, f).commit();
		}

	}

	private void openReportCard() {
		getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ReportCardFrag()).commit();
	}

	@Override
	public void onBackPressed() {
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
		boolean suppress = false;
		if (f == null) suppress = false;
		else if (f instanceof MediatorFrag) {
			suppress = ((MediatorFrag) f).onBackPressed();
		}
		else if (f instanceof SubjectFrag) {
			suppress = ((SubjectFrag) f).onBackPressed();
		}
		if (!suppress) super.onBackPressed();
	}

}
