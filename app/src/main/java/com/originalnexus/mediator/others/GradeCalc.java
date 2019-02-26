package com.originalnexus.mediator.others;

import java.util.ArrayList;

public class GradeCalc {

	public static final double THESIS_PERCENT = 0.25;
	public static final int DECIMALS = 2;

	private static int sum(ArrayList<Integer> numbers) {
		if (numbers == null) return 0;
		int s = 0;
		for (int num : numbers) {
			s += num;
		}
		return s;
	}

	private static double average(ArrayList<Integer> grades) {
		if (grades == null) return 0;
		if (grades.size() == 0) return 0;
		double avg = sum(grades) / (double) grades.size();
		return floorDecimals(avg);
	}

	public static double average(ArrayList<Integer> grades, int thesis) {
		if (thesis == 0) return average(grades);
		return floorDecimals(average(grades) * (1 - THESIS_PERCENT) + thesis * THESIS_PERCENT);
	}

	public static String arrayListToString(ArrayList<Integer> a) {
		StringBuilder out = new StringBuilder();
		for(int i = 0; i < a.size(); i++) {
			out.append(a.get(i));
			if (i < a.size() - 1) out.append(", ");
		}
		return out.toString();
	}

	public static int roundAverage(double avg) {
		return (int) Math.floor(avg + 0.5);
	}

	public static double floorDecimals(double d) {
		return Math.floor(d * Math.pow(10, DECIMALS)) / Math.pow(10, DECIMALS);
	}

}
