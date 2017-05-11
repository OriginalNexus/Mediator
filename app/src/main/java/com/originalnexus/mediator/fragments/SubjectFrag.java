package com.originalnexus.mediator.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.originalnexus.mediator.others.DataManager;
import com.originalnexus.mediator.dialogs.DeleteDialog;
import com.originalnexus.mediator.dialogs.NameDialog;
import com.originalnexus.mediator.others.GradeCalc;
import com.originalnexus.mediator.views.KeypadView;
import com.originalnexus.mediator.activities.MainActivity;
import com.originalnexus.mediator.R;
import com.originalnexus.mediator.models.Subject;

import java.util.Locale;

public class SubjectFrag extends Fragment implements NameDialog.NameDialogListener, DeleteDialog.DeleteDialogListener, MainActivity.BackPressedListener {

	private static final String STATE_SUBJECT_INDEX = "subjectIndex";
	private static final String STATE_INPUT_FIELD_ID = "inputFieldId";

	private Subject mSubject;
	private int mSubjectIndex;
	private KeypadView mKeypad;
	private int mActiveInputFieldId = 0;

	public static SubjectFrag newInstance(int index) {
		SubjectFrag frag = new SubjectFrag();
		frag.mSubjectIndex = index;
		return frag;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mSubjectIndex = savedInstanceState.getInt(STATE_SUBJECT_INDEX);
			mActiveInputFieldId = savedInstanceState.getInt(STATE_INPUT_FIELD_ID);
		}
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_subject, container, false);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getView() == null) return;

		// Set title
		getActivity().setTitle(R.string.frag_report_card_title);
		((MainActivity) getActivity()).setCheckedNavigationItem(R.id.menu_drawer_none);

		// Set custom toolbar/header
		ViewCompat.setElevation(((MainActivity) getActivity()).getToolbar(), 0);
		ViewCompat.setElevation(getView().findViewById(R.id.frag_subject_header), getResources().getDimension(R.dimen.app_bar_elevation));

		// Get subject
		mSubject = DataManager.Subjects.get(mSubjectIndex);

		// Set keypad
		mKeypad = (KeypadView) getView().findViewById(R.id.frag_subject_keypad);
		mKeypad.setKeypadListener(new KeypadView.KeypadListener() {
			@Override
			public void onInput(int digit) {
				if (mActiveInputFieldId == R.id.frag_subject_grades) mSubject.grades.add(digit);
				if (mActiveInputFieldId == R.id.frag_subject_thesis) mSubject.thesis = digit;
				updateViews();
			}

			@Override
			public void onRemove() {
				if (mActiveInputFieldId == R.id.frag_subject_grades && !mSubject.grades.isEmpty()) mSubject.grades.remove(mSubject.grades.size() - 1);
				if (mActiveInputFieldId == R.id.frag_subject_thesis) mSubject.thesis = 0;
				updateViews();
			}
		});

		// Add click events
		View.OnClickListener l = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();
				switch (id) {
					case R.id.frag_subject_grades:
					case R.id.frag_subject_thesis:
						mActiveInputFieldId = id;
						mKeypad.showKeypad();
						break;
					case R.id.frag_subject_name:
						// Open name dialog
						NameDialog dialog = NameDialog.newInstance(mSubject.name);
						dialog.setTargetFragment(SubjectFrag.this, 0);
						dialog.show(getFragmentManager(), null);
						break;
					case R.id.frag_subject_mediator_btn:
						// Open mediator with existing data
						((MainActivity) getActivity()).openMediator(mSubject);
						break;
				}
			}
		};

		View.OnFocusChangeListener fl = new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) view.performClick();
			}
		};

		getView().findViewById(R.id.frag_subject_grades).setOnClickListener(l);
		getView().findViewById(R.id.frag_subject_grades).setOnFocusChangeListener(fl);
		getView().findViewById(R.id.frag_subject_thesis).setOnClickListener(l);
		getView().findViewById(R.id.frag_subject_thesis).setOnFocusChangeListener(fl);
		getView().findViewById(R.id.frag_subject_name).setOnClickListener(l);
		getView().findViewById(R.id.frag_subject_mediator_btn).setOnClickListener(l);

		updateViews();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putInt(STATE_SUBJECT_INDEX, mSubjectIndex);
		outState.putInt(STATE_INPUT_FIELD_ID, mActiveInputFieldId);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.subject, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_subject_delete) {
			DeleteDialog f = DeleteDialog.newInstance(mSubjectIndex);
			f.setTargetFragment(this, 0);
			f.show(getFragmentManager(), null);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// Restore Toolbar
		ViewCompat.setElevation(((MainActivity) getActivity()).getToolbar(), getResources().getDimension(R.dimen.app_bar_elevation));
	}

	private void updateViews() {
		if (getView() == null) return;

		// Set grades input field
		((TextView) getView().findViewById(R.id.frag_subject_grades)).setText(GradeCalc.arrayListToString(mSubject.grades));

		// Set thesis input field
		((TextView) getView().findViewById(R.id.frag_subject_thesis)).setText((mSubject.thesis != 0) ? String.valueOf(mSubject.thesis) : "");

		// Set average text and visibility
		if (!mSubject.grades.isEmpty()) {
			double average = GradeCalc.average(mSubject.grades, mSubject.thesis);
			((TextView) getView().findViewById(R.id.frag_subject_average)).setText(String.format(Locale.ENGLISH, "(%.2f)", GradeCalc.floorDecimals(average)));
			((TextView) getView().findViewById(R.id.frag_subject_average_round)).setText(String.valueOf(GradeCalc.roundAverage(average)));
		}
		else {
			((TextView) getView().findViewById(R.id.frag_subject_average)).setText("");
			((TextView) getView().findViewById(R.id.frag_subject_average_round)).setText("");
		}

		// Set name text
		((TextView) getView().findViewById(R.id.frag_subject_name)).setText(mSubject.name);

		// Set input focus if needed
		if (mActiveInputFieldId != 0) getView().findViewById(mActiveInputFieldId).requestFocus();

		// Also save subjects
		DataManager.saveSubjects(getActivity());
	}


	@Override
	public boolean onBackPressed() {
		if (mKeypad.isKeypadOpen()) {
			mKeypad.hideKeypad();
			return true;
		}
		return false;
	}

	@Override
	public void onNameDialogConfirm(String input) {
		mSubject.name = input;
		updateViews();
	}

	@Override
	public void onDeleteSubject(int index) {
		DataManager.Subjects.remove(index);
		DataManager.saveSubjects(getActivity());
		((MainActivity) getActivity()).openReportCard();
	}

}
