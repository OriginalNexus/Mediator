package org.example.mediator;

public class GradeCalc {

	private static final double thesisPercent = 0.25;
	private static final int numOfDecimals = 2;

	/**
	 * Calculates the average of the grades
	 * @param grades Array containing the grades
	 * @return The average with numOfDecimals decimals after the period
	 */
	public static double average(int[] grades) {
		if (grades == null) return 0;
		if (grades.length == 0) return 0;
		double avg = sum(grades) / (double) grades.length;
		return floorDecimals(avg);
	}

	/**
	 * Calculates the sum of the given numbers
	 * @param numbers Array containing the numbers
	 * @return Sum of the numbers
	 */
	private static int sum(int[] numbers) {
		if (numbers == null) return 0;
		int s = 0;
		for (int num : numbers) {
			s += num;
		}
		return s;
	}

	/**
	 * Calculates the average with the thesis
	 * @param grades Array containing the grades
	 * @param thesis Thesis thesis
	 * @return The average with numOfDecimals decimals after the period
	 */
	public static double averageWithThesis(int[] grades, int thesis) {
		return floorDecimals(average(grades) * (1 - thesisPercent) + thesis * thesisPercent);
	}

	/**
	 * Figure out the minimum grades needed to have the wanted average
	 * @param grades Current grades, null if none
	 * @param thesis Thesis or 0 if none
	 * @param numOfGrades Num of grades that are needed. Must be greater or equal to 1
	 * @param finalAverage The wanted average
	 * @return Returns an array with numOfGrades elements representing the grades needed to achieve the finalAverage if possible.
	 * If not possible the first element will be 0
	 * If finalAverage is achieved with the lowest possible grades (1, 1, 1, ...) then the first element will be 11
	 * If anything is invalid, the first element will be -1
	 */
	public static int[] calculateRequiredGrades(int[] grades, int thesis, int numOfGrades, int finalAverage) {
		int[] error = {-1};
		if (numOfGrades <= 0) return error;
		if (finalAverage < 1 || finalAverage > 10) return error;

		double s;
		if (thesis >= 1 && thesis <= 10) {
			s = (finalAverage - 0.5 - thesis * thesisPercent) / (1 - thesisPercent);
		}
		else
			s = finalAverage - 0.5;
		s = Math.ceil(s * Math.pow(10, numOfDecimals)) / Math.pow(10, numOfDecimals);
		if (grades != null) {
			s *= grades.length + numOfGrades;
			s = Math.ceil(s);
			for (int grade : grades) {
				s -= grade;
			}
		}
		else {
			s *= numOfGrades;
			s = Math.ceil(s);
		}

		if (s <= numOfGrades) return new int[] {11};
		if (s > numOfGrades * 10) return new int[] {0};

		int[] output = new int[numOfGrades];
		for (int i = 0; i < numOfGrades; i++) {
			output[i] = (int) Math.ceil(s / (double)(numOfGrades - i));
			s -= output[i];
		}

		return output;
	}

	/**
	 * Floor the number to numOfDecimals decimals
	 * @param d The number to be floored
	 */
	private static double floorDecimals(double d) {
		return Math.floor(d * Math.pow(10, numOfDecimals)) / Math.pow(10, numOfDecimals);
	}
}
