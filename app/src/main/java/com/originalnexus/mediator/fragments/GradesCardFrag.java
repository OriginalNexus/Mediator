package com.originalnexus.mediator.fragments;


import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.originalnexus.mediator.GradeCalc;
import com.originalnexus.mediator.R;

import java.util.ArrayList;


public class GradesCardFrag extends Fragment {

	private ArrayList<Integer> grades = null;
	private int thesis = 0;
	private int extra = 0;
	private int finalAvg = 5;

	public boolean created = false;
	public boolean startHidden = true;

	private GradesCardState state = GradesCardState.INVALID;


	/**
	 * Updates the card data and state. DOES NOT UPDATE VIEWS. Use updateViews() for that
	 * @param grades Grades till now, can be null
	 * @param thesis Thesis or 0
	 * @param extraNum How many extra grades to get
	 * @param finalAverage Final wanted average
	 */
	public void setData(ArrayList<Integer> grades, int thesis, int extraNum, int finalAverage) {
		this.grades = grades;
		this.thesis = thesis;
		this.extra = extraNum;
		this.finalAvg = finalAverage;

		// Set new state
		ArrayList<Integer> result = GradeCalc.calculateRequiredGrades(this.grades, this.thesis, this.extra, this.finalAvg);
		if (result.get(0) == -1)
			state = GradesCardState.INVALID;
		if (result.get(0) == 0)
			state = GradesCardState.UNDER;
		else if (result.get(0) == 11)
			state = GradesCardState.OVER;
		else
			state = GradesCardState.VALID;
	}

	/**
	 * Update the grades card: set content, result, background, visibility
	 */
	public void updateViews() {
		if (getView() != null) {
			// Set average view
			((TextView) getView().findViewById(R.id.gradesCardFragAverage)).setText(String.format("%d", finalAvg));

			TextView outText = (TextView) getView().findViewById(R.id.gradesCardFragOutput);

			switch (state) {
				case INVALID:
					// Something is incorrect
					getFragmentManager().beginTransaction().hide(this).commit();
					return;
				case UNDER:
					// Gets under given average
					outText.setText(getResources().getString(R.string.grades_card_under));
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
						//noinspection deprecation
						outText.setTextAppearance(getActivity(), R.style.GradesCard_Under);
					else
						outText.setTextAppearance(R.style.GradesCard_Under);
					break;
				case OVER:
					// Gets over given average
					outText.setText(getResources().getString(R.string.grades_card_over));
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
						//noinspection deprecation
						outText.setTextAppearance(getActivity(), R.style.GradesCard_Over);
					else
						outText.setTextAppearance(R.style.GradesCard_Over);
					break;
				case VALID:
					// Can get given average
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
						//noinspection deprecation
						outText.setTextAppearance(getActivity(), R.style.GradesCard_Middle);
					else
						outText.setTextAppearance(R.style.GradesCard_Middle);
					ArrayList<Integer> result = GradeCalc.calculateRequiredGrades(grades, thesis, extra, finalAvg);
					outText.setText(GradeCalc.arrayListToString(result));
					break;
			}
		}
	}

	/**
	 * Get the card's state
	 * @return <p><b>OVER</b> if the minimum final average is OVER the given average</p>
	 * <p><b>UNDER</b> if the maximum final average is UNDER the given average</p>
	 * <p><b>INVALID</b> if anything is incorrect</p>
	 */
	public GradesCardState getState() {
		return state;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.grades_card, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (startHidden) getFragmentManager().beginTransaction().hide(this).commit();
		updateViews();
		created = true;
	}

	/**
	 * State of the grades
	 */
	public enum GradesCardState {
		/**
		 * Something is incorrect, the card is probable empty
		 */
		INVALID,
		/**
		 * Everything is fine and given average can be achieved
		 */
		VALID,
		/**
		 * The giver average is surely achievable
		 */
		OVER,
		/**
		 * The given average is not achievable
		 */
		UNDER
	}

}
