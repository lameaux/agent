package com.euromoby.utils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ShellExecutor {

	public static String executeCommandLine(final String[] commandLine, final long timeout) throws IOException, InterruptedException, TimeoutException {
		ProcessBuilder pb = new ProcessBuilder().command(commandLine).redirectErrorStream(true);
		Process process = pb.start();

		// grabs output
		StreamGobbler gobbler = new StreamGobbler(process.getInputStream());
		gobbler.start();

		Worker worker = new Worker(process);
		worker.start();

		try {
			worker.join(timeout);
			if (worker.exit != null) {
				gobbler.finish();
				return gobbler.getCharArrayWriter().toString();
			}

			throw new TimeoutException();

		} catch (InterruptedException ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
			throw ex;
		} finally {
			process.destroy();
		}

	}

	private static class Worker extends Thread {
		private final Process process;
		private Integer exit;

		private Worker(Process process) {
			this.process = process;
		}

		public void run() {
			try {
				exit = process.waitFor();
			} catch (InterruptedException ignore) {
				return;
			}
		}
	}
}