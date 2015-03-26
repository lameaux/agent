package com.euromoby.processor;

public interface Command {

	String execute(String request);

	boolean match(String request);

	String name();

	String help();
}
