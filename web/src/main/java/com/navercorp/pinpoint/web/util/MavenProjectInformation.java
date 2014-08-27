package com.nhn.pinpoint.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * 
 * @author netspider
 * 
 */
public class MavenProjectInformation {
	private static final Logger logger = LoggerFactory.getLogger(MavenProjectInformation.class);

	private static class SingletonHolder {
		private static final MavenProjectInformation INSTANCE = new MavenProjectInformation();
	}

	public static MavenProjectInformation getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private MavenProjectInformation() {
		InputStream inputStream = null;
		try {
			ClassPathResource resource = new ClassPathResource("project.properties");
			Properties p = new Properties();

			inputStream = resource.getInputStream();
			p.load(inputStream);

			this.version = p.getProperty("application.version");
			this.artifactId = p.getProperty("application.artifactId");
			this.groupId = p.getProperty("application.groupId");
			this.buildTime = p.getProperty("application.buildTime");

			logger.info("MavenProjectInformation={}", this);
		} catch (IOException e) {
			logger.error("fail load properties : e={}", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
	}

	private String version = "";
	private String artifactId = "";
	private String groupId = "";
	private String buildTime = "";

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getBuildTime() {
		return buildTime;
	}

	public void setBuildTime(String buildTime) {
		this.buildTime = buildTime;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
