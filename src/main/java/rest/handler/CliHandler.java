package rest.handler;

import java.io.InputStream;

import org.restexpress.Request;
import org.restexpress.Response;

import processor.CommandProcessor;
import utils.IOUtils;

public class CliHandler extends HandlerBase {
	public CliHandler(CommandProcessor commandProcessor) {
		super(commandProcessor);
	}

	// POST
	public Object create(Request request, Response response) {
		CliRequest cliRequest = request.getBodyAs(CliRequest.class);
		String message = commandProcessor.process(cliRequest.getRequest());
		return new CliResponse(message);
	}

	// Get show input form
	public Object read(Request request, Response response) {
		response.setContentType("text/html");
		InputStream is = CliHandler.class.getResourceAsStream("cli.html");
		return IOUtils.streamToString(is);
	}
}
