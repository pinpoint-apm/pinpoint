package com.navercorp.pinpoint.plugin.elasticsearchbboss;


import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author yinbp[yin-bp@163.com]
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.bbossgroups.plugins:bboss-elasticsearch-rest-jdbc:5.1.5"})
public class ElasticsearchExecutorIT {
	@Test
	public void test(){
		try {
			//build a elasticsearch client instance.
			ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
			//use the elasticsearch client instance to validate the indice exist or not
			boolean existIndice = clientUtil.existIndice("twitter");

		}
		catch (Exception e){

		}
		PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
		verifier.printCache();
		verifier.verifyTraceCount(1);
	}
}
