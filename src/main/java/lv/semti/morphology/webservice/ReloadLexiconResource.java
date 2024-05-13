package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Analyzer;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.concurrent.*;
import java.nio.file.Files;
import java.nio.file.Paths;

class Reloader {
	private static String TEZAURS_DUMP_PATH = "../tezaurs_dump/tezaurs_dump.py";
//	private static String TEZAURS_DUMP_PATH = "python3 tezaurs_dump/tezaurs_dump.py";

	private static Date reloadStart = null;
	public static synchronized Date getReloadStart() { return reloadStart; }
	public static synchronized void setReloadStart(Date value) { reloadStart = value; }
	public static synchronized boolean isReloadInProgress() {
		return reloadStart != null;
	}
	private static boolean needsOneMoreReload = false;
	private static synchronized boolean getNeedsOneMoreReload() { return needsOneMoreReload; }
	private static synchronized void setNeedsOneMoreReload(boolean value) { needsOneMoreReload = value; }

	private static ExecutorService executor = Executors.newSingleThreadExecutor();

	public static Date attempt_reload() {
		setNeedsOneMoreReload(true);
		Date rs = getReloadStart();
		if (rs != null) {      // if we're already processing..
			System.out.println("Adding reload to queue");
			return rs;         // return the start time of that processing}
		}
		Future<?> future = executor.submit(() -> {
			while (getNeedsOneMoreReload()) {
				setReloadStart(new Date());
				setNeedsOneMoreReload(false);
				reload();
				setReloadStart(null);
			}
		});
		return null;
	}

	private static void reload() {
		System.out.println("Starting reload at " + new Date());
		try {
			// Check if the file exists
			if (!Files.exists(Paths.get(TEZAURS_DUMP_PATH))) {
				System.err.println("Tezaurs dump file does not exist: " + TEZAURS_DUMP_PATH);
				return;
			}
			// Create ProcessBuilder with command and arguments
			ProcessBuilder processBuilder = new ProcessBuilder("python", TEZAURS_DUMP_PATH);
			// Start the process
			Process process = processBuilder.start();
			// Wait for the process to finish
			int exitCode = process.waitFor();

			// Check if the process exited normally
			if (exitCode != 0) {
				System.err.println("Script execution failed with exit code: " + exitCode);
				return;
			}

			// At this point we assume that the lexicon JSON files are written somewhere and we need to load them
			// FIXME / TODO
//
//			Analyzer = new Analyzer(false);
//			analyzer.setCacheSize(1000);
//
//			if (enableLatgalian) {
//				latgalian_analyzer = new Analyzer("Latgalian.xml", false);
//				latgalian_analyzer.setCacheSize(100);
//			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Ending reload at " + new Date());
	}
}

public class ReloadLexiconResource extends ServerResource{
	@Post()
	public String reload() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("lexicon");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Date status = Reloader.attempt_reload();
		// FIXME TODO - atgriezt statusu

		return "";
	}

}
