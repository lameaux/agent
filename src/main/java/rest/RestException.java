package rest;

import io.netty.handler.codec.http.HttpResponseStatus;

public class RestException extends Exception {

	private static final long serialVersionUID = 1L;

	private HttpResponseStatus status = HttpResponseStatus.BAD_REQUEST;

	public RestException() {
		super();
	}

	public RestException(Exception e) {
		super(e);
	}

	public RestException(HttpResponseStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpResponseStatus getStatus() {
		return status;
	}

}
