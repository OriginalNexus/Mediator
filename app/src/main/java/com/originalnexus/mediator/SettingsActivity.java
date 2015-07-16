package com.originalnexus.mediator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


/**
 * This fragment handles settings for the app
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Restore theme
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String themeValue = settings.getString("pref_theme", "light");
		if (themeValue != null)
			switch (themeValue) {
				case "dark":
					setTheme(R.style.AppTheme_Dark);
					break;
				case "light":
					setTheme(R.style.AppTheme_Light);
					break;
			}

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		Preference themePref = findPreference("pref_theme");
		if (themePref != null && themeValue != null)
			switch (themeValue) {
				case "dark":
					themePref.setSummary(R.string.pref_theme_summary_dark);
					break;
				case "light":
					themePref.setSummary(R.string.pref_theme_summary_light);
					break;
			}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("pref_theme")) {
			// Restart app
			MainActivity ac = MainActivity.getInstance();
			if (ac != null)
				ac.finish();
			finish();
			startActivity(new Intent(this, MainActivity.class));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
}
