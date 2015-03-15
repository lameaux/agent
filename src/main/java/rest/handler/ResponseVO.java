package rest.handler;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ResponseVO {
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm ss a");
	private String message;
	private String date;

	public ResponseVO(String message) {
		this.message = message;
		this.date = formatter.format(new Date());
	}

	public String getMessage() {
		return message;
	}

	public String getDate() {
		return date;
	}

}
