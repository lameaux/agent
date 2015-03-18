package rest.handler.cli;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import processor.CommandProcessor;
import rest.handler.PostDataProcessor;
import rest.handler.RestHandlerBase;
import utils.IOUtils;

import com.google.gson.Gson;

public class CliHandler extends RestHandlerBase {

	private static final String REQUEST_INPUT_NAME = "request";	
	
	protected final CommandProcessor commandProcessor;

	public CliHandler(CommandProcessor commandProcessor) {
		this.commandProcessor = commandProcessor;
	}

	// Get show input form
	@Override
	public FullHttpResponse doGet() {
		InputStream is = CliHandler.class.getResourceAsStream("cli.html");
		return getFullHttpResponse(Unpooled.copiedBuffer(IOUtils.streamToString(is), CharsetUtil.UTF_8));
	}

	// POST
	@Override
	public FullHttpResponse doPost() throws IOException {

		final Map<String, String> requestParameters = new HashMap<String, String>();
		
		processPostData(new PostDataProcessor() {
			public void process(InterfaceHttpData data) throws IOException {
				if (data.getHttpDataType() == HttpDataType.Attribute) {
					Attribute attribute = (Attribute) data;
					requestParameters.put(attribute.getName(), attribute.getValue());
				}
			}
		});
		
		String message = commandProcessor.process(requestParameters.get(REQUEST_INPUT_NAME));

		Gson gson = new Gson();
		String jsonResponse = gson.toJson(new CliResponse(message));
		FullHttpResponse response = getFullHttpResponse(Unpooled.copiedBuffer(jsonResponse, CharsetUtil.UTF_8));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
		return response;
	}

}
