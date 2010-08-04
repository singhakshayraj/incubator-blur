package com.nearinfinity.blur;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BlurConfiguration implements BlurConstants {
	private static final Log LOG = LogFactory.getLog(BlurConfiguration.class);
	
	private static final String PROPERTY = "property";
	private static final String BLUR_DEFAULT_XML = "/blur-default.xml";
	private static final String VALUE = "value";
	private static final String NAME = "name";

	static {
		InputStream inputStream = BlurConfiguration.class.getResourceAsStream(BLUR_DEFAULT_XML);
		if (inputStream == null) {
			throw new RuntimeException();
		}
		properties = new HashMap<String, String>();
		populate(inputStream);
	}
	
	private static Map<String, String> properties;

	public String get(String name) {
		return get(name, null);
	}

	public String get(String name, String defaultValue) {
		String value = properties.get(name);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public int getInt(String name, int defaultValue) {
		String value = properties.get(name);
		if (value == null) {
			return defaultValue;
		}
		return Integer.parseInt(value);
	}
	
	
	private static void populate(InputStream inputStream) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputStream);
			NodeList nodeList = doc.getElementsByTagName(PROPERTY);
			populate(nodeList);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void populate(NodeList nodeList) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			populate(node);
		}
		
	}

	private static void populate(Node node) {
		String name = null;
		String value = null;
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			if (NAME.equals(n.getNodeName())) {
				name = getText(n);
			} else if (VALUE.equals(n.getNodeName())) {
				value = getText(n);
			}
		}
		if (name == null || value == null) {
			return;
		}
		properties.put(name, value);
	}

	private static String getText(Node n) {
		return n.getFirstChild().getNodeValue();
	}

	@SuppressWarnings("unchecked")
	public <T> T getNewInstance(String name, Class<? extends T> clazz) {
		String className = get(name);
		LOG.info("Using property [" + name + "] trying to create class [" + className + "] for class type of [" + clazz + "]");
		try {
			return (T) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
