package com.originalnexus.mediator.activities;

import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.originalnexus.mediator.dialogs.AboutDialog;
import com.originalnexus.mediator.dialogs.HelpDialog;
import com.originalnexus.mediator.fragments.MediatorFrag;
import com.originalnexus.mediator.fragments.ReportCardFrag;
import com.originalnexus.mediator.fragments.SettingsFrag;
import com.originalnexus.mediator.R;
import com.originalnexus.mediator.fragments.SubjectFrag;
import com.originalnexus.mediator.models.Subject;

public class MainActivity extends AppCompatActivity {

	public interface BackPressedListener {
		/**
		 * Called when back is pressed
		 * @return Return true if the event was consumed
		 */
		boolean onBackPressed();
	}

	private Toolbar mToolbar;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private Runnable mDrawerPendingRunnable;
	private boolean mCanExitOnBack = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Set default settings if they are missing
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// Restore theme
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useDarkTheme = settings.getBoolean("pref_dark_theme", false);
		if (useDarkTheme) setTheme(R.style.Mediator_Dark);
		else setTheme(R.style.Mediator_Light);

		super.onCreate(savedInstanceState);

		// Set layout
		setContentView(R.layout.act_main);

		// Setup Toolbar
		mToolbar = findViewById(R.id.act_main_toolbar);
		ViewCompat.setElevation(mToolbar, getResources().getDimension(R.dimen.app_bar_elevation));
		setSupportActionBar(mToolbar);

		// Transparent status bar for Lollipop and newer
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setStatusBarColor(Color.TRANSPARENT);
		}

		// Setup drawer
		mDrawerLayout = findViewById(R.id.act_main_drawer_layout);
		NavigationView navigationView = findViewById(R.id.act_main_navigation);

		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
				mDrawerLayout.closeDrawer(Gravity.START);
				if (menuItem.isChecked()) return false;
				mDrawerPendingRunnable = new Runnable() {
					@Override
					public void run() {
						onMenuItemSelected(menuItem);
					}
				};
				return menuItem.isCheckable();
			}
		});
		navigationView.setCheckedItem(0);

		// Add drawer toggle feature to the mToolbar
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (mDrawerPendingRunnable != null) {
					mDrawerPendingRunnable.run();
					mDrawerPendingRunnable = null;
				}
			}
		};
		mDrawerLayout.addDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			openMediator();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync drawer toggle button state
		mDrawerToggle.syncState();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return mDrawerToggle.onOptionsItemSelected(item) || onMenuItemSelected(item) || super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
			mDrawerLayout.closeDrawer(Gravity.START);
			return;
		}

		Fragment f = getFragmentManager().findFragmentById(R.id.act_main_fragment);
		if ((f instanceof BackPressedListener) && ((BackPressedListener) f).onBackPressed()) return;

		if (getFragmentManager().getBackStackEntryCount() > 0) {
			super.onBackPressed();
		}
		else if (!mCanExitOnBack) {
			Toast.makeText(this, R.string.act_main_on_back_pressed, Toast.LENGTH_SHORT).show();
			mCanExitOnBack = true;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mCanExitOnBack = false;
				}
			}, 2000);
		}
		else {
			super.onBackPressed();
		}
	}

	private boolean onMenuItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_drawer_mediator:
				openMediator();
				break;
			case R.id.menu_drawer_report_card:
				openReportCard();
				break;
			case R.id.menu_drawer_settings:
			case R.id.menu_main_settings:
				openSettings();
				break;
			case R.id.menu_drawer_help:
			case R.id.menu_main_help:
				openHelp();
				break;
			case R.id.menu_drawer_about:
			case R.id.menu_main_about:
				openAbout();
				break;
			default:
				return false;
		}
		return true;
	}

	private void clearBackStack() {
		FragmentManager fm = getFragmentManager();
		int count = fm.getBackStackEntryCount();
		for (int i = 0; i < count; i++) {
			fm.popBackStack();
		}
	}

	private void openFragment(Fragment f, boolean addToBackStack) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			f.setEnterTransition(new Fade());
			f.setReturnTransition(null);
			f.setReenterTransition(new Fade());
		}

		FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.act_main_fragment, f);
		if (addToBackStack) ft.addToBackStack(null);
		else clearBackStack();
		ft.commit();
	}

	private void openMediator() {
		openFragment(new MediatorFrag(), false);
	}

	public void openMediator(Subject subject) {
		openFragment(MediatorFrag.newInstance(subject), true);
	}

	public void openReportCard() {
		openFragment(new ReportCardFrag(), false);
	}

	public void openSubject(int index) {
		openFragment(SubjectFrag.newInstance(index), true);
	}

	private void openSettings() {
		if (!(getFragmentManager().findFragmentById(R.id.act_main_fragment) instanceof SettingsFrag)) {
			openFragment(new SettingsFrag(), true);
		}
	}

	private void openHelp() {
		DialogFragment helpDialog = new HelpDialog();
		helpDialog.show(getFragmentManager(), null);
	}

	private void openAbout() {
		DialogFragment aboutDialog = new AboutDialog();
		aboutDialog.show(getFragmentManager(), null);
	}

	public void setCheckedNavigationItem(int id) {
		((NavigationView) findViewById(R.id.act_main_navigation)).setCheckedItem(id);
	}

	public Toolbar getToolbar() {
		return mToolbar;
	}

}
