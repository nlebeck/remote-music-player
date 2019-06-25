package niellebeck.remotemusicplayer;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class CustomHttpHandler extends AbstractHandler {

	private Controller controller;
	
	public CustomHttpHandler(Controller controller) {
		this.controller = controller;
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
				controller.navigate(decodedTarget);
			}
			else if (action.equals("navigateUp")) {
				controller.navigateUp();
			}
			else if (action.equals("play")) {
				String qpTarget = queryParams.get("target");
				String decodedTarget = URLDecoder.decode(qpTarget, "UTF-8");
				controller.playSong(decodedTarget);
			}
			else if (action.equals("pause")) {
				controller.pauseSong();
			}
			else if (action.equals("unpause")) {
				controller.unpauseSong();
			}
		}
		
		response.getWriter().println("<html>");
		response.getWriter().println("<body>");
		response.getWriter().println("<h1>Current directory: " + controller.getCurrentRelativeDir() + "</h1>");
		
		if (controller.songIsPaused()) {
			response.getWriter().println("<a href=\"/?action=unpause\">[Unpause]");
		}
		else {
			response.getWriter().println("<a href=\"/?action=pause\">[Pause]");
		}
		response.getWriter().println("<p>");
		
		if (!controller.getCurrentRelativeDir().equals("")) {
			response.getWriter().println("<a href=\"/?action=navigateUp\">[Up]</a>");
			response.getWriter().println("<p>");
		}
		
		List<String> childDirs = controller.getChildDirsInCurrentDir();
		for (String childDir : childDirs) {
			String encodedChildDir = URLEncoder.encode(childDir, "UTF-8");
			response.getWriter().println("<a href=\"/?action=navigate&target=" + encodedChildDir + "\">" + childDir + "</a>");
			response.getWriter().println("<p>");
		}
		List<String> songs = controller.getSongsInCurrentDir();
		for (String song : songs) {
			String encodedSong = URLEncoder.encode(song, "UTF-8");
			response.getWriter().println("<a href=\"/?action=play&target=" + encodedSong + "\">" + song + "</a>");
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
}
