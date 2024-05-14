package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Analyzer;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.concurrent.*;
import java.nio.file.Files;
import java.nio.file.Paths;

class Reloader {
	private static String TEZAURS_DUMP_PATH = "../tezaurs_dump/";
//	private static String TEZAURS_DUMP_PATH = "python3 tezaurs_dump/";

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

	private static void reload(){
		System.out.println("Starting reload at " + new Date());
		try {
			// Check if the file exists

			String script_path = TEZAURS_DUMP_PATH+"tezaurs_dump.py";
			if (!Files.exists(Paths.get(script_path))) {
				System.err.println("Tezaurs dump script does not exist: " + script_path);
				return;
			}
			// Create ProcessBuilder with command and arguments
			ProcessBuilder processBuilder = new ProcessBuilder("python3", script_path);
			// Start the process
			Process process = processBuilder.start();

			// Create ExecutorService for reading subprocess output
			ExecutorService executor = Executors.newFixedThreadPool(2);
			// Submit tasks to read stdout and stderr asynchronously
			Future<Void> stdoutFuture = executor.submit(() -> {
				readStream(process.getInputStream(), null);
				return null;
			});
			Future<Void> stderrFuture = executor.submit(() -> {
				readStream(process.getErrorStream(), System.err);
				return null;
			});

			// Wait for the process to finish and the stdout and stderr reading tasks to complete
			int exitCode = process.waitFor();
			stdoutFuture.get();
			stderrFuture.get();

			// Check if the process exited normally
			if (exitCode != 0) {
				System.err.println("Failed to execute script " + script_path + " with exit code: " + exitCode);
				System.out.println("Ending reload at " + new Date());
				return;
			}

			// At this point we assume that the lexicon JSON files are written somewhere and we need to load them
			System.out.println("DB extract done at " + new Date());

			// Copying all the required files from the dump and also from the .jar file to the same resources/ folder
			Path sourcePath = Paths.get( "tezaurs_lexemes.json");
			if (!Files.exists(sourcePath)) {
				System.err.println("DB extract result file not found " + sourcePath);
				return;
			}
			Path targetPath = Paths.get("resources/").resolve(sourcePath.getFileName());
			Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

			sourcePath = Paths.get( "tezaurs_latgalian.json");
			if (!Files.exists(sourcePath)) {
				System.err.println("DB extract result file not found " + sourcePath);
				return;
			}
			targetPath = Paths.get("resources/").resolve(sourcePath.getFileName());
			Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

			extractResourceFromJAR("Lexicon_v2.xml");
			extractResourceFromJAR("Lexicon_minicore.xml");
			extractResourceFromJAR("Lexicon_firstnames.xml");
			extractResourceFromJAR("Lexicon_vietas.xml");
			extractResourceFromJAR("Latgalian.xml");
			extractResourceFromJAR("Latgalian_minicore.xml");

			Analyzer analyzer = new Analyzer("resources/Lexicon_v2.xml",false);
			analyzer.setCacheSize(1000);
			MorphoServer.setAnalyzer(analyzer);
			if (MorphoServer.enableLatgalian) {
				Analyzer latgalian_analyzer = new Analyzer("Latgalian.xml", false);
				latgalian_analyzer.setCacheSize(100);
				MorphoServer.setLatgalian_analyzer(latgalian_analyzer);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Ending reload at " + new Date());
	}

	private static void readStream(InputStream inputStream, PrintStream output) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (output != null)
					output.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void extractResourceFromJAR(String filename) throws IOException {
		// Read the resource file from the JAR
		InputStream inputStream = Reloader.class.getClassLoader().getResourceAsStream(filename);
		if (inputStream == null) {
			throw new IOException("Resource file not found: " + filename);
		}
		// Create a new file in the local working directory
		OutputStream outputStream = new FileOutputStream("resources/"+filename);
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
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
