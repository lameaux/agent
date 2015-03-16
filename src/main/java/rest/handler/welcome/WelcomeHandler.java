package rest.handler.welcome;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import rest.handler.RestHandlerBase;
import agent.Agent;

public class WelcomeHandler extends RestHandlerBase {

	@Override
	public FullHttpResponse doGet() {

		// response.setContentType("text/html");
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html><html><head><title>");
		sb.append(getWelcomeString());
		sb.append("</title></head><body>");
		sb.append(getWelcomeString());
		sb.append("</body></html>");

		return getFullHttpResponse(Unpooled.copiedBuffer(sb.toString(), CharsetUtil.UTF_8));
	}

	private String getWelcomeString() {
		return Agent.TITLE + " " + Agent.VERSION;
	}
}
