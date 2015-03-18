package rest.handler;

import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;

public interface PostDataProcessor {

	void process(InterfaceHttpData data) throws IOException;
	
}
