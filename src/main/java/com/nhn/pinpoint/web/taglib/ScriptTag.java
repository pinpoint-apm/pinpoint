package com.nhn.pinpoint.web.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * 
 */
public class ScriptTag extends SimpleTagSupport {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String src = "";

	public void setSrc(String src) {
		this.src = src;
	}

	@Override
	public void doTag() throws JspException {
		PageContext pageContext = (PageContext) getJspContext();
		JspWriter out = pageContext.getOut();
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("<script src=\"");
			sb.append(src);
			sb.append("\"></script>");
			out.println(sb.toString());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
