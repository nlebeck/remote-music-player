package niellebeck.remotemusicplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class WebSocketHttpHandler extends AbstractHandler {

	private static final String CLIENT_BASE_DIR = ".." + File.separator + "javascript-client";
	private static final String HTML_FILE = CLIENT_BASE_DIR + File.separator + "index.html";
	private static final String JS_FILE = CLIENT_BASE_DIR + File.separator + "script.js";
	
	@Override
	public void handle(String target,
			Request baseRequest,
			HttpServletRequest request,
			HttpServletResponse response)
					throws IOException, ServletException {
		if (target.equals("/index.html") || target.equals("/")) {
			respondWithFile(baseRequest, response, HTML_FILE);
		}
		else if (target.equals("/script.js")) {
			respondWithFile(baseRequest, response, JS_FILE);
		}
	}
	
	private void respondWithFile(Request baseRequest,
			HttpServletResponse response,
			String filePath)
					throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		File file = new File(filePath);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		while (reader.ready()) {
			response.getWriter().write(reader.readLine());
		}
		reader.close();
	}

}
