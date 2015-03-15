package rest.handler;

import org.restexpress.Request;
import org.restexpress.Response;

public class EchoHandler {
	// HTTP method - Get
	public Object read(Request request, Response response) {
		String message = null;
		String value = request.getHeader("echo");
		System.out.println("\t >> message : " + value);

		if (value != null) {
			message = "received message is '" + value + "'.";
		} else {
			message = " try again....";
		}
		return new ResponseVO(message);
	}
}
