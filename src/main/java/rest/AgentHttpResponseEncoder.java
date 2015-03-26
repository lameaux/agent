package rest;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class AgentHttpResponseEncoder extends HttpResponseEncoder {

    @Override
	protected void encodeHeaders(HttpHeaders headers, ByteBuf buf) {
		headers.set("X-Frame-Options", "SAMEORIGIN");
        super.encodeHeaders(headers, buf);
    }	
	
}
