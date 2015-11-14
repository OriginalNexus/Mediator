package com.originalnexus.mediator;

import android.support.annotation.Nullable;

import java.util.ArrayList;

public class Subject {
	public String name;
	public final ArrayList<Integer> grades;
	public int thesis;

	/**
	 * Creates a new Subject with the given name
	 * @param name Name of the new Subject
	 */
	public Subject (String name) {
		this.name = (name == null) ? "" : name;
		this.grades = new ArrayList<>();
		this.thesis = 0;
	}

	/**
	 * Creates a new Subject from an existing one.
	 * @param s The Subject to copy the values from or null
	 */
	public Subject(@Nullable Subject s) {
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

}
