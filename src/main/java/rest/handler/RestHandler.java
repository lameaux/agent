package rest.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

public interface RestHandler {

	void process(ChannelHandlerContext ctx);

	void setHttpRequest(HttpRequest request);

	void setHttpPostRequestDecoder(HttpPostRequestDecoder decoder);

}
