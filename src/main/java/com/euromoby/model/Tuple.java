package com.euromoby.model;

public class Tuple<F, S> {

	private F first;
	private S second;
	
	private Tuple() {

	}

	private Tuple(F first, S second) {
		this.first = first;
		this.second = second;
	}


	public static <F, S> Tuple<F, S> empty() {
		return new Tuple<F, S>();
	}

	public static <F, S> Tuple<F, S> of(F first, S second) {
		return new Tuple<F, S>(first, second);
	}	
	
	public F getFirst() {
		return first;
	}

	public void setFirst(F first) {
		this.first = first;
	}

	public S getSecond() {
		return second;
	}

	public void setSecond(S second) {
		this.second = second;
	}

}
