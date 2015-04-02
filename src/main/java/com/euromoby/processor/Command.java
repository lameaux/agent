package com.euromoby.processor;

public interface Command {
	
	public static final String SEPARATOR = " ";
	
	String execute(String request);

	boolean match(String request);

	String name();

	String help();
}
