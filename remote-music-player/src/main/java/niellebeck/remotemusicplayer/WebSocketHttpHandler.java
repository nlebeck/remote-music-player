package niellebeck.remotemusicplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * A Jetty handler implementing an HTTP server that hosts the
 * JavaScript/WebSocket client. This server exists only to serve the client
 * files and doesn't interact with the rest of the music player at all.
 */
public class WebSocketHttpHandler extends AbstractHandler {

	private static final String CLIENT_BASE_DIR = ".." + File.separator + "javascript-client";
	private static final String HTML_FILE = CLIENT_BASE_DIR + File.separator + "index.html";
	private static final String CSS_FILE = CLIENT_BASE_DIR + File.separator + "style.css";
	private static final String JS_FILE = CLIENT_BASE_DIR + File.separator + "script.js";
	
	private String ipAddress;
	private int webSocketPort;
	
	public WebSocketHttpHandler(String ipAddress, int webSocketPort) {
		this.ipAddress = ipAddress;
		this.webSocketPort = webSocketPort;
	}
	
	@Override
	public void handle(String target,
			Request baseRequest,
			HttpServletRequest request,
			HttpServletResponse response)
					throws IOException, ServletException {
		if (target.equals("/index.html") || target.equals("/")) {
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			writeFile(HTML_FILE, response.getWriter());
		}
		else if (target.equals("/style.css")) {
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			writeFile(CSS_FILE, response.getWriter());
		}
		else if (target.equals("/script.js")) {
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			response.getWriter().write("var ipAddress = \"" + ipAddress + "\";\n");
			response.getWriter().write("var port = \"" + webSocketPort + "\";\n");
			writeFile(JS_FILE, response.getWriter());
		}
	}
	
	private void writeFile(String filePath, Writer writer)
					throws IOException {
		File file = new File(filePath);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		while (reader.ready()) {
			writer.write(reader.readLine() + "\n");
		}
		reader.close();
	}

}
