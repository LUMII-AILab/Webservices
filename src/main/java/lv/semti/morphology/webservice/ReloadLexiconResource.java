package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Analyzer;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Resource for exporting Tēzaurs DB data as a morphological analyzer lexicon.
 */
public class ReloadLexiconResource extends ServerResource{
	@Post()
	public String reload() {
		if (CentralServer.debug)
			System.out.println(getRequest().getMethod().getName() + " call handled by service" + this.getClass().getName());
		getResponse().setAccessControlAllowOrigin("*");
		Boolean latgalian = CentralServer.isTypeLatgalian((String) getRequest().getAttributes().get("lexicon"));
		if (latgalian == null)
		{
			doError(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			return null;
		}
		//String query = (String) getRequest().getAttributes().get("lexicon");
		String wait = (String) getRequest().getAttributes().get("wait");
		if (CentralServer.debug) {
			System.out.println("Request keys: " +
					getRequest().getAttributes().keySet().stream().reduce((a, b) -> a + ", " + b).orElse("null"));
		}
		System.out.println("Requested lexicon: " + (latgalian ? "Latgalian" : "Latvian") + ", already loading: " + wait);

		if (latgalian && !CentralServer.enableLatgalian) {
			this.getResponse().setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
			return "Latgalian corpus not enabled on this server";
		}

		if (wait == null) {
			System.out.println("Not waiting");
			Date status = Reloader.getReloader(latgalian).attempt_reload();
			// FIXME TODO - atgriezt statusu
		} else {
			System.out.println("Waiting until completion");
			Reloader.getReloader(latgalian).reload();
		}

		return "";
	}

	@Options()
	public String cors() {
		getResponse().setAccessControlAllowOrigin("*");
		HashSet<String> headersHashSet = new HashSet<>();
		headersHashSet.add("*");
		getResponse().setAccessControlAllowHeaders(headersHashSet);
		HashSet<Method> methodHashSet = new HashSet<>();
		methodHashSet.add(Method.POST);
		getResponse().setAccessControlAllowMethods(methodHashSet);
		return "";
	}
}


class Reloader {
	static Path TEZAURS_DUMP_PATH = Paths.get("../TezaursMorphoDump/");

	private static Reloader latvian_reloader = null;
	private static Reloader latgalian_reloader = null;

	public static Reloader getReloader(boolean latgalian) {
		//if (lexicon.equalsIgnoreCase("latgalian")) {
		if (latgalian) {
			if (latgalian_reloader == null) {
				latgalian_reloader = new Reloader();
				latgalian_reloader.latgalian = true;
			}
			return latgalian_reloader;
		}
		if (latvian_reloader == null) {
			latvian_reloader = new Reloader();
		}
		return latvian_reloader;
	}

	private boolean latgalian = false;
	private Date reloadStart = null;
	public synchronized Date getReloadStart() { return reloadStart; }
	public synchronized void setReloadStart(Date value) { reloadStart = value; }
	public synchronized boolean isReloadInProgress() {
		return reloadStart != null;
	}
	private boolean needsOneMoreReload = false;
	private synchronized boolean getNeedsOneMoreReload() { return needsOneMoreReload; }
	private synchronized void setNeedsOneMoreReload(boolean value) { needsOneMoreReload = value; }

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	public Date attempt_reload() {
		setNeedsOneMoreReload(true);
		Date rs = getReloadStart();
		if (rs != null) {      // if we're already processing...
			System.out.println("Adding reload to queue");
			return rs;         // return the start time of that processing
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

	protected void reload(){
		System.out.println("Starting reload at " + new Date());
		try {
			Path script_path = TEZAURS_DUMP_PATH.resolve("tezaurs_dump.py");
			// Check if the file exists
			if (!Files.exists(script_path)) {
				System.err.println("Tezaurs dump script does not exist: " + script_path);
				return;
			}
			// Create ProcessBuilder with command and arguments
			ProcessBuilder processBuilder;
			if (this.latgalian) {
				processBuilder = new ProcessBuilder("python3", script_path.toString(), "latgalian");
			} else {
				processBuilder = new ProcessBuilder("python3", script_path.toString());
			}

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

			// At this point we assume that the lexicon JSONL files are written somewhere and we need to load them
			System.out.println("DB extract done at " + new Date());

			// Copying all the required files from the dump and also from the .jar file to the same resources/ folder
			Path sourcePath;
			String lexiconFileName;
			if (this.latgalian) {
				sourcePath = Paths.get( "tezaurs_latgalian.jsonl");
				lexiconFileName = "resources/Latgalian.xml";
			} else {
				sourcePath = Paths.get( "tezaurs_lexemes.jsonl");
				lexiconFileName = "resources/Lexicon_v2.xml";
			}
			if (!Files.exists(sourcePath)) {
				System.err.println("DB extract result file not found " + sourcePath);
				return;
			}
			Files.createDirectories(Paths.get("resources/"));
			Path targetPath = Paths.get("resources/").resolve(sourcePath.getFileName());
			Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

			extractResourceFromJAR("Lexicon_v2.xml");
			extractResourceFromJAR("Lexicon_minicore.xml");
			extractResourceFromJAR("Lexicon_firstnames.xml");
			extractResourceFromJAR("Lexicon_vietas.xml");
			extractResourceFromJAR("Latgalian.xml");
			extractResourceFromJAR("Latgalian_minicore.xml");

			Analyzer analyzer = new Analyzer(lexiconFileName,false);
			if (this.latgalian) {
				analyzer.setCacheSize(100);
				CentralServer.setLatgalian_analyzer(analyzer);
			} else {
				analyzer.setCacheSize(1000);
				CentralServer.setAnalyzer(analyzer);
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
