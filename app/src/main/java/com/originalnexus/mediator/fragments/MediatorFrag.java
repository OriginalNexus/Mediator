package com.originalnexus.mediator.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.originalnexus.mediator.others.GradeCalc;
import com.originalnexus.mediator.activities.MainActivity;
import com.originalnexus.mediator.models.GradesHint;
import com.originalnexus.mediator.views.KeypadView;
import com.originalnexus.mediator.R;
import com.originalnexus.mediator.models.Subject;

import java.util.ArrayList;
import java.util.Locale;


public class MediatorFrag extends Fragment implements MainActivity.BackPressedListener {

	private static final String STATE_GRADES = "grades";
	private static final String STATE_THESIS = "thesis";
	private static final String STATE_EXTRA_GRADES = "extraGrades";
	private static final String STATE_INPUT_FIELD_ID = "inputFieldId";

	private ArrayList<Integer> mGrades = new ArrayList<>();
	private int mThesis = 0;
	private int mExtraGrades = 0;
	private int mActiveInputFieldId = 0;
	private KeypadView mKeypad;
	private final GradesHint[] mGradesHints = new GradesHint[9];

	public static MediatorFrag newInstance(Subject s) {
		MediatorFrag f = new MediatorFrag();
		Subject clone = new Subject(s);
		f.mGrades = clone.grades;
		f.mThesis = clone.thesis;
		f.mExtraGrades = 1;
		return f;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mGrades = savedInstanceState.getIntegerArrayList(STATE_GRADES);
			mThesis = savedInstanceState.getInt(STATE_THESIS);
			mExtraGrades = savedInstanceState.getInt(STATE_EXTRA_GRADES);
			mActiveInputFieldId = savedInstanceState.getInt(STATE_INPUT_FIELD_ID);
		}

		for (int i = 2; i <= 10; i++) mGradesHints[i - 2] = new GradesHint();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_mediator, container, false);
		LinearLayout parent = (LinearLayout) v.findViewById(R.id.frag_mediator_table_content);
		for (int i = 2; i <= 10; i++) {
			inflater.inflate(R.layout.item_grades_hint, parent, true);
		}
		return v;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Set title
		if (getFragmentManager().getBackStackEntryCount() == 0) {
			getActivity().setTitle(R.string.frag_mediator_title);
			((MainActivity) getActivity()).setCheckedNavigationItem(R.id.menu_drawer_mediator);
		}
		else {
			getActivity().setTitle(R.string.menu_drawer_report_card);
			((MainActivity) getActivity()).setCheckedNavigationItem(R.id.menu_drawer_none);
		}

		if (getView() == null) return;

		// Set keypad
		mKeypad = (KeypadView) getView().findViewById(R.id.keypad_view);
		mKeypad.setKeypadListener(new KeypadView.KeypadListener() {
			@Override
			public void onInput(int digit) {
				if (mActiveInputFieldId == R.id.frag_mediator_grades) mGrades.add(digit);
				if (mActiveInputFieldId == R.id.frag_mediator_thesis) mThesis = digit;
				updateViews();
			}

			@Override
			public void onRemove() {
				if (mActiveInputFieldId == R.id.frag_mediator_grades && !mGrades.isEmpty()) mGrades.remove(mGrades.size() - 1);
				if (mActiveInputFieldId == R.id.frag_mediator_thesis) mThesis = 0;
				updateViews();
			}

		});

		// Add click events for other components
		View.OnClickListener l = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();
				switch (id) {
					case R.id.frag_mediator_grades:
					case R.id.frag_mediator_thesis:
							mActiveInputFieldId = id;
							mKeypad.showKeypad();
						break;
					case R.id.frag_mediator_extra_minus:
						mExtraGrades--;
						if (mExtraGrades < 0) mExtraGrades = 0;
						updateViews();
						break;
					case R.id.frag_mediator_extra_plus:
						mExtraGrades++;
						updateViews();
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

		getView().findViewById(R.id.frag_mediator_grades).setOnClickListener(l);
		getView().findViewById(R.id.frag_mediator_grades).setOnFocusChangeListener(fl);
		getView().findViewById(R.id.frag_mediator_thesis).setOnClickListener(l);
		getView().findViewById(R.id.frag_mediator_thesis).setOnFocusChangeListener(fl);
		getView().findViewById(R.id.frag_mediator_extra_minus).setOnClickListener(l);
		getView().findViewById(R.id.frag_mediator_extra_plus).setOnClickListener(l);

		updateViews();
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putIntegerArrayList(STATE_GRADES, mGrades);
		outState.putInt(STATE_THESIS, mThesis);
		outState.putInt(STATE_EXTRA_GRADES, mExtraGrades);
		outState.putInt(STATE_INPUT_FIELD_ID, mActiveInputFieldId);

		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onBackPressed() {
		if (mKeypad.isKeypadOpen()) {
			mKeypad.hideKeypad();
			return true;
		}
		return false;
	}

	private void updateViews() {
		if (getView() == null) return;
		// Set grades input field
		((TextView) getView().findViewById(R.id.frag_mediator_grades)).setText(GradeCalc.arrayListToString(mGrades));

		// Set thesis input field
		if (mThesis != 0) ((TextView) getView().findViewById(R.id.frag_mediator_thesis)).setText(String.valueOf(mThesis));
		else ((TextView) getView().findViewById(R.id.frag_mediator_thesis)).setText("");

		// Set average text and visibility
		if (!mGrades.isEmpty()) {
			double average = GradeCalc.average(mGrades, mThesis);
			((TextView) getView().findViewById(R.id.frag_mediator_average)).setText(String.format(Locale.ENGLISH, "(%.2f)", GradeCalc.floorDecimals(average)));
			((TextView) getView().findViewById(R.id.frag_mediator_average_round)).setText(String.valueOf(GradeCalc.roundAverage(average)));
			getView().findViewById(R.id.frag_mediator_average_container).setVisibility(View.VISIBLE);
		}
		else {
			getView().findViewById(R.id.frag_mediator_average_container).setVisibility(View.GONE);
		}

		// Set extra grades
		((TextView) getView().findViewById(R.id.frag_mediator_extra)).setText(String.valueOf(mExtraGrades));

		// Update grades hints
		if (mExtraGrades <= 0) {
			getView().findViewById(R.id.frag_mediator_table_header).setVisibility(View.GONE);
			getView().findViewById(R.id.frag_mediator_table_content).setVisibility(View.GONE);
		}
		else {
			updateGradesHints();
			getView().findViewById(R.id.frag_mediator_table_header).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.frag_mediator_table_content).setVisibility(View.VISIBLE);
		}

		// Set input focus if needed
		if (mActiveInputFieldId != 0) getView().findViewById(mActiveInputFieldId).requestFocus();

	}

	private void updateGradesHints() {
		if (getView() == null) return;

		LinearLayout parent = (LinearLayout) getView().findViewById(R.id.frag_mediator_table_content);

		for (int i = 2; i <= 10; i++) {
			GradesHint hint = mGradesHints[i - 2];
			View hintView = parent.getChildAt(i - 2);
			TextView hintText = (TextView) hintView.findViewById(R.id.item_grades_hint_result);

			((TextView) hintView.findViewById(R.id.item_grades_hint_average)).setText(String.valueOf(i));

			hint.computeHint(mGrades, mThesis, mExtraGrades, i);
			switch (hint.getType()) {
				case INVALID:
					hintView.setVisibility(View.GONE);
					break;

				case UNDER:
					if (i > 2 && mGradesHints[i - 3].getType() == GradesHint.GradesHintType.UNDER) {
						hintView.setVisibility(View.GONE);
					}
					else {
						hintText.setText(R.string.frag_grades_under);
						setTextAppearance(hintText, R.style.Grades_Under);
						hintView.setVisibility(View.VISIBLE);
					}
					break;

				case OVER:
					hintText.setText(R.string.frag_grades_over);
					setTextAppearance(hintText, R.style.Grades_Over);
					hintView.setVisibility(View.VISIBLE);

					if (i > 2 && mGradesHints[i - 3].getType() == GradesHint.GradesHintType.OVER) {
						View prevHintView = parent.getChildAt(i - 3);
						if (prevHintView != null) prevHintView.setVisibility(View.GONE);
					}
					break;

				case VALID:
					hintText.setText(GradeCalc.arrayListToString(hint.getResult()));
					setTextAppearance(hintText, R.style.Grades_Middle);
					hintView.setVisibility(View.VISIBLE);
					break;
			}
		}
	}

	private void setTextAppearance(TextView tv, int resId) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
			//noinspection deprecation
			tv.setTextAppearance(tv.getContext(), resId);
		else
			tv.setTextAppearance(resId);
	}
}
