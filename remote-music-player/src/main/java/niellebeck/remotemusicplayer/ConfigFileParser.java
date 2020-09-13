package niellebeck.remotemusicplayer;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Reads configuration information from the XML config file.
 * <p>
 * I referenced the following StackOverflow post to learn how to parse XML
 * files: https://stackoverflow.com/a/14968272.
 */
public class ConfigFileParser {
	private Document configFileDoc;
	
	public ConfigFileParser(String configFileName) {
		try {
			configFileDoc = loadXMLFile(configFileName);
		} catch (IOException | ParserConfigurationException | SAXException e) {
			System.err.println("Error loading config file:");
			e.printStackTrace();
		}
	}
	
	private Document loadXMLFile(String fileName)
			throws IOException, ParserConfigurationException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		Document document = documentBuilder.parse(new File(fileName));
		return document;
	}
	
	public String getBaseDir() {
		return configFileDoc.getElementsByTagName("baseDir")
				.item(0)
				.getTextContent();
	}
	
	public String[] getMusicFileTypes() {
		String typesString = configFileDoc.getElementsByTagName("musicFileTypes")
				.item(0)
				.getTextContent();
		return typesString.split(",");
	}
	
	public int getHttpPort() {
		String portString = configFileDoc.getElementsByTagName("httpPort")
				.item(0)
				.getTextContent();
		return Integer.parseInt(portString);
	}
	
	public int getWebSocketPort() {
		String portString = configFileDoc.getElementsByTagName("webSocketPort")
				.item(0)
				.getTextContent();
		return Integer.parseInt(portString);
	}

	public boolean getEnableLocalBrowser() {
		String valueString = configFileDoc.getElementsByTagName("enableLocalBrowser")
				.item(0)
				.getTextContent();
		return Boolean.parseBoolean(valueString);
	}
}
