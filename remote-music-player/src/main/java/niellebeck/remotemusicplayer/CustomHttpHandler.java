package niellebeck.remotemusicplayer;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class CustomHttpHandler extends AbstractHandler {

	private Controller controller;
	private String currentRelativeDir = "";
	
	/*
	 * Assigned once at startup and never modified again
	 */
	private String baseDir;
	private String[] musicFileTypes;
	
	public CustomHttpHandler(Controller controller, String baseDir, String[] musicFileTypes) {
		this.controller = controller;
		this.baseDir = baseDir;
		this.musicFileTypes = musicFileTypes;
	}
	
	@Override
	public void handle(String target,
			Request baseRequest,
			HttpServletRequest request,
			HttpServletResponse response)
					throws IOException, ServletException {
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		
		System.out.println("Received request; target = " + target + ", query string = " + baseRequest.getQueryString());

		Map<String, String> queryParams = parseQueryString(baseRequest.getQueryString());
		String action = queryParams.get("action");
		if (action != null) {
			if (action.equals("navigate")) {
				String qpTarget = queryParams.get("target");
				String decodedTarget = URLDecoder.decode(qpTarget, "UTF-8");
				String targetPath = baseDir + File.separator + currentRelativeDir + File.separator + decodedTarget;
				File targetFile = new File(targetPath);
				if (targetFile.isDirectory()) {
					currentRelativeDir = currentRelativeDir + File.separator + decodedTarget;
				}
			}
			else if (action.equals("navigateUp")) {
				if (!currentRelativeDir.equals("")) {
					File currentDirFile = new File(baseDir + File.separator + currentRelativeDir);
					String parentDir = currentDirFile.getParent();
					String parentRelativeDir = parentDir.substring(baseDir.length());
					currentRelativeDir = parentRelativeDir;
				}
			}
			else if (action.equals("play")) {
				String qpTarget = queryParams.get("target");
				String decodedTarget = URLDecoder.decode(qpTarget, "UTF-8");
				String targetPath = baseDir + File.separator + currentRelativeDir + File.separator + decodedTarget;
				if (isMusicFile(targetPath)) {
					controller.changeSong(targetPath);
				}
			}
		}
		
		response.getWriter().println("<html>");
		response.getWriter().println("<body>");
		response.getWriter().println("<h1>Current directory: " + currentRelativeDir + "</h1>");
		
		if (!currentRelativeDir.equals("")) {
			response.getWriter().println("<a href=\"/?action=navigateUp\">Up</a>");
			response.getWriter().println("<p>");
		}
		
		File currentDirFile = new File(baseDir + File.separator + currentRelativeDir);
		String[] children = currentDirFile.list();
		for (String child : children) {
			String childPath = baseDir + File.separator + currentRelativeDir + File.separator + child;
			File childFile = new File(childPath);
			String linkedAction = null;
			if (childFile.isDirectory()) {
				linkedAction = "navigate";
			}
			else if (isMusicFile(childPath) && childFile.exists()) {
				linkedAction = "play";
			}
			if (linkedAction != null) {
				String encodedChild = URLEncoder.encode(child, "UTF-8");
				response.getWriter().println("<a href=\"/?action=" + linkedAction + "&target=" + encodedChild + "\">" + child + "</a>");
			}
			response.getWriter().println("<p>");
		}

		response.getWriter().println("</body>");
		response.getWriter().println("</html>");
	}
	
	private Map<String, String> parseQueryString(String queryString) {
		if (queryString == null) {
			return new HashMap<String, String>();
		}
		
		Map<String, String> result = new HashMap<String, String>();
		String[] split = queryString.split("&");
		for(String kvPair : split) {
			String[] kvPairSplit = kvPair.split("=");
			if (kvPairSplit.length == 2) {
				String key = kvPairSplit[0];
				String value = kvPairSplit[1];
				result.put(key, value);
			}
		}
		return result;
	}

	private boolean isMusicFile(String fileName) {
		String[] split = fileName.split("\\.");
		String fileType = split[split.length - 1];
		for (String type : musicFileTypes) {
			if (fileType.equalsIgnoreCase(type)) {
				return true;
			}
		}
		return false;
	}
}
