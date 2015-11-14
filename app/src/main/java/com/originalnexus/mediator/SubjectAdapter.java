package com.originalnexus.mediator;

import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.originalnexus.mediator.activities.MainActivity;

import java.util.ArrayList;
import java.util.EventListener;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {
	public class SubjectViewHolder extends RecyclerView.ViewHolder {
		final View view;
		final TextView nameView;
		final TextView gradesView;
		final TextView thesisView;
		final TextView averageView;
		final TextView thesisTextView;
		public int index;

		SubjectViewHolder(View v) {
			super(v);
			view = v;
			nameView = (TextView) v.findViewById(R.id.name);
			gradesView = (TextView) v.findViewById(R.id.grades);
			thesisView = (TextView) v.findViewById(R.id.thesis);
			averageView = (TextView) v.findViewById(R.id.average);
			thesisTextView = (TextView) v.findViewById(R.id.thesis_text);
		}

	}

	private static final String SAVE_SUBJECTS = "subjects";

	public ArrayList<Subject> subjects;

	public SubjectAdapter() {
		this.subjects = new ArrayList<>();
	}

	public interface ItemClickListener extends EventListener {
		void onClick(SubjectViewHolder subjectViewHolder);
	}

	private ItemClickListener itemClickListener;

	public void setItemClickListener(ItemClickListener l) {
		itemClickListener = l;
	}

	@Override
	public int getItemCount() {
		return subjects.size();
	}

	@Override
	public SubjectViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		final SubjectViewHolder svh = new SubjectViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subject_item, viewGroup, false));
		svh.view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				itemClickListener.onClick(svh);
			}
		});
		return svh;
	}

	@Override
	public void onBindViewHolder(SubjectViewHolder subjectViewHolder, int i) {
		Subject s = subjects.get(i);
		subjectViewHolder.index = i;
		subjectViewHolder.nameView.setText(s.name);
		subjectViewHolder.gradesView.setText((s.grades.size() > 0) ? GradeCalc.arrayListToString(s.grades) : "");
		if (s.thesis != 0) {
			subjectViewHolder.thesisTextView.setVisibility(View.VISIBLE);
			subjectViewHolder.thesisView.setText(String.format("%d", s.thesis));
		}
		else {
			subjectViewHolder.thesisTextView.setVisibility(View.GONE);
			subjectViewHolder.thesisView.setText("");
		}

		// Get average
		double average = GradeCalc.average(s.grades, s.thesis);
		subjectViewHolder.averageView.setText((average != 0) ? String.format("%.2f", average) : "");
	}

	/**
	 * Add a subject to the end of the list
	 * @param sub The subject to be added
	 */
	public void addSubject(Subject sub) {
		subjects.add(sub);
		notifyItemInserted(getItemCount() - 1);
	}

	public void removeSubject(int index) {
		subjects.remove(index);
		notifyItemRemoved(index);
	}

	public void saveSubjects() {
		Gson gson = new Gson();
		MainActivity mainA = MainActivity.getInstance();
		if (mainA != null)
			PreferenceManager.getDefaultSharedPreferences(mainA).edit().putString(SAVE_SUBJECTS, gson.toJson(subjects)).commit();
	}

	public void loadSubjects() {
		Gson gson = new Gson();
		String savedString = null;
		MainActivity mainA = MainActivity.getInstance();
		if (mainA != null)
			savedString = PreferenceManager.getDefaultSharedPreferences(mainA).getString(SAVE_SUBJECTS, null);

		if (savedString != null)
			subjects = gson.fromJson(savedString, new TypeToken<ArrayList<Subject>>(){}.getType());

		notifyDataSetChanged();
	}
}
