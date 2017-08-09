package com.originalnexus.mediator.models;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Subject {

	@SerializedName("name")
	public String name;
	@SerializedName("grades")
	public ArrayList<Integer> grades;
	@SerializedName("thesis")
	public int thesis;

	/**
	 * This no-args constructor is required for Gson to work properly
	 */
	@SuppressWarnings("unused")
	private Subject() {}

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
	 * Creates a copy of a subject
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
