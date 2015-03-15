package rest.handler;

import processor.CommandProcessor;

public class HandlerBase {

	protected final CommandProcessor commandProcessor;

	public HandlerBase(CommandProcessor commandProcessor) {
		this.commandProcessor = commandProcessor;
	}
}
