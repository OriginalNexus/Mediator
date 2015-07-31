package com.originalnexus.mediator;

import android.support.annotation.Nullable;

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

	Subject(@Nullable Subject s) {
		if (s == null) {
			this.name = "";
			this.grades = new ArrayList<>();
			this.thesis = 0;
			return;
		}
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
