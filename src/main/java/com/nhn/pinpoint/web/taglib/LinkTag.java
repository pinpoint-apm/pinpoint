package com.nhn.pinpoint.web.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.util.MavenProjectInformation;

/**
 * 
 * @author netspider
 * 
 */
public class LinkTag extends SimpleTagSupport {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private MavenProjectInformation mavenMetaInformation = MavenProjectInformation.getInstance();

	private String rel = "";
	private String href = "";

	public void setRel(String rel) {
		this.rel = rel;
	}

	public void setHref(String href) {
		this.href = href;
	}

	@Override
	public void doTag() throws JspException {
		PageContext pageContext = (PageContext) getJspContext();
		JspWriter out = pageContext.getOut();
		try {
			StringBuffer sb = new StringBuffer();

			sb.append("<link rel=\"");
			sb.append(rel);
			sb.append("\" href=\"");
			sb.append(href);

			if (mavenMetaInformation != null) {
				sb.append("?v=");
				sb.append(mavenMetaInformation.getBuildTime());
			}

			sb.append("\">");

			out.println(sb.toString());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
