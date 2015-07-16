package com.originalnexus.mediator;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class GradesCard extends Fragment {
	// Argument tags
	private static final String ARG_START_HIDDEN = "startHidden";
	private static final String ARG_HIDE_IF_INVALID = "hideIfInvalid";

	private int[] grades = null;
	private int thesis = 0;
	private int extra = 0;
	private int finalAvg = 5;

	private GradesCardState state = GradesCardState.INVALID;
	private boolean startHidden = false;
	private boolean hideIfInvalid = true;

	/**
	 * Creates a new instance of the fragment and sets some options
	 * @return The new fragment
	 */
	public static GradesCard newInstance() {
		GradesCard fragment = new GradesCard();
		Bundle args = new Bundle();
		args.putBoolean(ARG_START_HIDDEN, true);
		args.putBoolean(ARG_HIDE_IF_INVALID, true);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Updates the card data and state. DOES NOT UPDATE VIEWS. Use updateViews() for that
	 * @param grades Grades till now, can be null
	 * @param thesis Thesis or 0
	 * @param extraNum How many extra grades to get
	 * @param finalAverage Final wanted average
	 */
	public void setData(int[] grades, int thesis, int extraNum, int finalAverage) {
		this.grades = grades;
		this.thesis = thesis;
		this.extra = extraNum;
		this.finalAvg = finalAverage;

		// Set new state
		int[] result = GradeCalc.calculateRequiredGrades(this.grades, this.thesis, this.extra, this.finalAvg);
		if (result[0] == -1)
			state = GradesCardState.INVALID;
		if (result[0] == 0)
			state = GradesCardState.UNDER;
		else if (result[0] == 11)
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
			((TextView) getView().findViewById(R.id.gradesCardFAverage)).setText(Integer.toString(finalAvg));

			TextView outText = (TextView) getView().findViewById(R.id.gradesCardFOutput);

			switch (state) {
				case INVALID:
					// Something is incorrect
					if (hideIfInvalid)
						getFragmentManager().beginTransaction().hide(this).commit();
					return;
				case UNDER:
					// Gets under given average
					outText.setText(getResources().getString(R.string.grades_card_under));
					outText.setTextAppearance(getActivity(), R.style.GradesCard_Under);
					break;
				case OVER:
					// Gets over given average
					outText.setText(getResources().getString(R.string.grades_card_over));
					outText.setTextAppearance(getActivity(), R.style.GradesCard_Over);
					break;
				case VALID:
					// Can get given average
					outText.setTextAppearance(getActivity(), R.style.GradesCard_Middle);
					String outString = "";
					int[] result = GradeCalc.calculateRequiredGrades(grades, thesis, extra, finalAvg);
					for(int i = 0; i < result.length; i++) {
						outString += result[i];
						if (i < result.length - 1) outString += ", ";
					}
					outText.setText(outString);
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Retrieve arguments if any
		if (getArguments() != null) {
			startHidden = getArguments().getBoolean(ARG_START_HIDDEN, startHidden);
			hideIfInvalid = getArguments().getBoolean(ARG_HIDE_IF_INVALID, hideIfInvalid);
		}
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
