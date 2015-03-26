package utils;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread {

	private InputStream is;
	private CharArrayWriter caw;
	private volatile boolean finished = false;

	public StreamGobbler(InputStream is) {
		this.is = is;
		caw = new CharArrayWriter();
	}

	@Override
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			String line = null;
			while (!finished && (line = br.readLine()) != null) {
				if (!finished) {
					caw.append(line).append(StringUtils.CRLF);
				}
			}
		} catch (IOException ioe) {
			caw.append(ioe.getMessage());
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public CharArrayWriter getCharArrayWriter() {
		return caw;
	}

	public void finish() {
		finished = true;
	}

}
