package com.originalnexus.mediator;

import java.util.ArrayList;

class Subject {
	String name;
	final ArrayList<Integer> grades;
	int thesis;

	Subject (String name) {
		this.name = name;
		this.grades = new ArrayList<>();
		this.thesis = 0;
	}

	Subject(Subject s) {
		this.name = s.name;
		this.grades = new ArrayList<>(s.grades);
		this.thesis = s.thesis;
	}

	@SuppressWarnings("unused")
	Subject (String name, ArrayList<Integer> grades, int thesis) {
		this.name = name;
		this.grades = grades;
		this.thesis = thesis;
	}
}
