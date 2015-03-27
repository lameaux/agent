package com.euromoby.model;

public class Tuple<F, S> {

	private F first;
	private S seconds;

	public Tuple() {

	}

	public Tuple(F first, S seconds) {
		this.first = first;
		this.seconds = seconds;
	}

	public F getFirst() {
		return first;
	}

	public void setFirst(F first) {
		this.first = first;
	}

	public S getSeconds() {
		return seconds;
	}

	public void setSeconds(S seconds) {
		this.seconds = seconds;
	}

}
