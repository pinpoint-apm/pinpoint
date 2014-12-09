package com.navercorp.pinpoint.web.filter;

/**
 * @author emeroad
 * @author netspider
 */
public interface FilterBuilder {
	Filter build(String filterText);

	Filter build(String filterText, String filterHint);
}
