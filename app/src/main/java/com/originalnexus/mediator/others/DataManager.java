package com.originalnexus.mediator.others;


import android.content.Context;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.originalnexus.mediator.models.Subject;

import java.util.ArrayList;

public class DataManager {

	private static final String SAVE_SUBJECTS = "subjects";

	public static final ArrayList<Subject> Subjects = new ArrayList<>();

	public static void saveSubjects(Context context) {
		Gson gson = new Gson();
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(SAVE_SUBJECTS, gson.toJson(Subjects)).apply();
	}

	public static void loadSubjects(Context context) {
		Gson gson = new Gson();
		String savedString;
		savedString = PreferenceManager.getDefaultSharedPreferences(context).getString(SAVE_SUBJECTS, null);

		Subjects.clear();
		if (savedString != null) {
			ArrayList<Subject> savedSubjects = null;
			try {
				savedSubjects = gson.fromJson(savedString, new TypeToken<ArrayList<Subject>>() {}.getType());
			} catch (JsonParseException ignored) {}
			if (savedSubjects != null) {
				// Filter corrupted subjects
				for (Subject s : savedSubjects) if (s.name != null && s.grades != null) Subjects.add(s);
			}

		}
	}

}
