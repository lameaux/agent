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
	
	public static Tuple<String, String> splitString(String string, String separator) {
		Tuple<String, String> t = Tuple.empty();
		String array[] = string.split(separator, 2);
		if (array.length == 2) {
			t.setFirst(array[0]);
			t.setSecond(array[1]);
		} else {
			t.setFirst(array[0]);
		}
		return t;
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
