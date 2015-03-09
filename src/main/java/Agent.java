import telnet.TelnetServer;

public class Agent {

	public static void main(String[] args) {

		// start telnet server
		try {
			TelnetServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
