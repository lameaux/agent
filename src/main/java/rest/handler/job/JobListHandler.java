package rest.handler.job;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.InputStream;

import rest.handler.RestHandlerBase;
import utils.IOUtils;

public class JobListHandler extends RestHandlerBase {

	public static final String URL = "/joblist";

	@Override
	public FullHttpResponse doGet() {
		InputStream is = JobListHandler.class.getResourceAsStream("joblist.html");
		String pageContent = IOUtils.streamToString(is);
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		return createHttpResponse(HttpResponseStatus.OK, content);
	}

}
