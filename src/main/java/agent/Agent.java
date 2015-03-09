package agent;
import telnet.TelnetServer;

public class Agent {

	public static final String VERSION = "0.1";

	public static void main(String[] args) {

		// start telnet server
		try {
			TelnetServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
