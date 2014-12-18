package com.navercorp.pinpoint.bootstrap.config;

import java.util.HashSet;
import java.util.Set;

/**
 * @author emeroad
 */
public class ProfilableClassFilter implements Filter<String> {

	private final Set<String> profileInclude = new HashSet<String>();
	private final Set<String> profileIncludeSub = new HashSet<String>();

	public ProfilableClassFilter(String profilableClass) {
		if (profilableClass == null || profilableClass.isEmpty()) {
			return;
		}
		String[] className = profilableClass.split(",");
		for (String str : className) {
			if (str.endsWith(".*")) {
				this.profileIncludeSub.add(str.substring(0, str.length() - 2).replace('.', '/') + "/");
			} else {
				String replace = str.trim().replace('.', '/');
				this.profileInclude.add(replace);
			}
		}
	}

	/**
	 * TODO remove this. Added this method to test the "call stack view" on a test server
	 *
	 * @param className
	 * @return
	 */
	@Override
	public boolean filter(String className) {
		if (profileInclude.contains(className)) {
			return true;
		} else {
			final String packageName = className.substring(0, className.lastIndexOf("/") + 1);
			for (String pkg : profileIncludeSub) {
				if (packageName.startsWith(pkg)) {
					return true;
				}
			}
		}
		return false;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ProfilableClassFilter{");
		sb.append("profileInclude=").append(profileInclude);
		sb.append(", profileIncludeSub=").append(profileIncludeSub);
		sb.append('}');
		return sb.toString();
	}
}
