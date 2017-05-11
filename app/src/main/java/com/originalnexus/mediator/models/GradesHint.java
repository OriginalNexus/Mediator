package com.originalnexus.mediator.models;


import com.originalnexus.mediator.others.GradeCalc;
import java.util.ArrayList;

public class GradesHint {

	public enum GradesHintType {
		INVALID,
		UNDER,
		VALID,
		OVER
	}

	private GradesHintType mType = GradesHintType.INVALID;
	private final ArrayList<Integer> mResult = new ArrayList<>();


	public GradesHint() {}

	public ArrayList<Integer> getResult() {
		return mResult;
	}

	public GradesHintType getType() {
		return mType;
	}

	public void computeHint(ArrayList<Integer> grades, int thesis, int extraNum, int finalAvg) {
		mType = GradesHintType.INVALID;
		mResult.clear();

		if (extraNum <= 0) return;
		if (finalAvg < 1 || finalAvg > 10) return;

		double s;
		if (thesis >= 1 && thesis <= 10) {
			s = (finalAvg - 0.5 - thesis * GradeCalc.THESIS_PERCENT) / (1 - GradeCalc.THESIS_PERCENT);
		}
		else {
			s = finalAvg - 0.5;
		}
		s = Math.ceil(s * Math.pow(10, GradeCalc.DECIMALS)) / Math.pow(10, GradeCalc.DECIMALS);
		if (grades != null) {
			s *= grades.size() + extraNum;
			s = Math.ceil(s);
			for (int grade : grades) {
				s -= grade;
			}
		}
		else {
			s *= extraNum;
			s = Math.ceil(s);
		}

		if (s <= extraNum) mType = GradesHintType.OVER;
		else if (s > extraNum * 10) mType = GradesHintType.UNDER;
		else {
			mType = GradesHintType.VALID;

			for (int i = 0; i < extraNum; i++) {
				mResult.add((int) Math.ceil(s / (double) (extraNum - i)));
				s -= mResult.get(i);
			}
		}
	}

}
