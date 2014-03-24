package com.nhn.pinpoint.testweb.controller;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MainController {

	@RequestMapping(value = "/docs", method = RequestMethod.GET)
	public String getEndPointsInView(Model model) {
		Map<String, List<String>> info = new HashMap<String, List<String>>();
		try {
			String packaze = "com.nhn.pinpoint.testweb.controller";
			ArrayList<String> classNamesFromPackage = MainController.getClassNamesFromPackage(packaze);

			for (String className : classNamesFromPackage) {
				Class<?> clazz = Class.forName(packaze + "." + className);
				List<String> requestInfo = new ArrayList<String>();

				Method[] methods = clazz.getDeclaredMethods();

				for (Method m : methods) {
					Annotation[] annotations = m.getDeclaredAnnotations();

					org.springframework.web.bind.annotation.RequestMapping mappingInfo = null;
					for (Annotation a : annotations) {
						if (a instanceof org.springframework.web.bind.annotation.RequestMapping) {
							mappingInfo = (org.springframework.web.bind.annotation.RequestMapping) a;
							break;
						}
					}

					if (mappingInfo != null) {
						requestInfo.add(mappingInfo.value()[0]);
					}
				}

				if (requestInfo.size() > 0) {
					info.put(clazz.getSimpleName(), requestInfo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		model.addAttribute("mapping", info);

		return "docs";
	}

	// spring에서 제공하는 기능이 있는것 같긴 하지만 그냥 구식으로.
	public static ArrayList<String> getClassNamesFromPackage(String packageName) throws IOException, URISyntaxException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL packageURL;
		ArrayList<String> names = new ArrayList<String>();

		packageName = packageName.replace(".", "/");
		packageURL = classLoader.getResource(packageName);

		if (packageURL.getProtocol().equals("jar")) {
			String jarFileName;
			JarFile jf;
			Enumeration<JarEntry> jarEntries;
			String entryName;

			// build jar file name, then loop through zipped entries
			jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
			jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
			System.out.println(">" + jarFileName);
			jf = new JarFile(jarFileName);
			jarEntries = jf.entries();
			while (jarEntries.hasMoreElements()) {
				entryName = jarEntries.nextElement().getName();
				if (entryName.startsWith(packageName) && entryName.length() > packageName.length() + 5) {
					entryName = entryName.substring(packageName.length(), entryName.lastIndexOf('.'));
					names.add(entryName);
				}
			}

			// loop through files in classpath
		} else {
			URI uri = new URI(packageURL.toString());
			File folder = new File(uri.getPath());
			// won't work with path which contains blank (%20)
			// File folder = new File(packageURL.getFile());
			File[] contenuti = folder.listFiles();
			String entryName;
			for (File actual : contenuti) {
				entryName = actual.getName();
				entryName = entryName.substring(0, entryName.lastIndexOf('.'));
				names.add(entryName);
			}
		}
		return names;
	}
}
