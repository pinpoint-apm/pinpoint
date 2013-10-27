package com.nhn.pinpoint.testweb.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ArcusClient;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import perftest.LevelManager;

import com.nhn.pinpoint.testweb.service.MemberService;
import com.nhn.pinpoint.testweb.util.HttpConnectorOptions;
import com.nhn.pinpoint.testweb.util.HttpInvoker;
import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.npc.connector.NpcHessianConnector;

@Controller
public class AllInOneController implements DisposableBean {

	private final ArcusClient arcus;
	private final MemcachedClient memcached;
	private final LevelManager levelManager;

	public AllInOneController() throws IOException {
		arcus = ArcusClient.createArcusClient("dev.arcuscloud.nhncorp.com:17288", "dev", new ConnectionFactoryBuilder());
		memcached = new MemcachedClient(AddrUtil.getAddresses("10.25.149.80:11244,10.25.149.80:11211,10.25.149.79:11211"));
		levelManager = new LevelManager();
	}

	@Autowired
	private MemberService service;

	private void arcus() {
		int rand = new Random().nextInt(100);
		String key = "pinpoint:testkey-" + rand;
		Future<Object> getFuture = null;
		try {
			getFuture = arcus.asyncGet(key);
			getFuture.get(1000L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if (getFuture != null)
				getFuture.cancel(true);
		}
	}

	private void memcached() {
		int rand = new Random().nextInt(100);
		String key = "pinpoint:testkey-" + rand;
		Future<Object> getFuture = null;
		try {
			getFuture = memcached.asyncGet(key);
			getFuture.get(1000L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if (getFuture != null)
				getFuture.cancel(true);
		}
	}

	private void mysql() {
		service.list();
	}

	private void npc() {
		try {
			InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);
			NpcHessianConnector connector = new NpcHessianConnector(serverAddress, true);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("message", "hello pinpoint");

			InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

			future.await();

			Object result = future.get();
			System.out.println("npc result=" + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void nested() {
		HttpInvoker client2 = new HttpInvoker(new HttpConnectorOptions());
		client2.execute("http://localhost:8080/donothing.pinpoint", new HashMap<String, Object>());
	}

	private void unknown() {
		try {
			HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
			client.execute("http://www.naver.com/", new HashMap<String, Object>());
			client.execute("http://www.naver.com/", new HashMap<String, Object>());
			client.execute("http://very.very.very.long.long.url/", new HashMap<String, Object>());
			client.execute("http://url1/", new HashMap<String, Object>());
			client.execute("http://url2/", new HashMap<String, Object>());
			client.execute("http://url2/", new HashMap<String, Object>());
			client.execute("http://url3/", new HashMap<String, Object>());
			client.execute("http://url3/", new HashMap<String, Object>());
			client.execute("http://url3/", new HashMap<String, Object>());
		} catch (Exception e) {
		}

	}

	@RequestMapping(value = "/allInOne2")
	public String allInOne2(Model model,
			@RequestParam(value = "arcus2", required = false, defaultValue = "true") boolean arcus2,
			@RequestParam(value = "memcached2", required = false, defaultValue = "false") boolean memcached2,
			@RequestParam(value = "mysql2", required = false, defaultValue = "false") boolean mysql2,
			@RequestParam(value = "npc2", required = false, defaultValue = "false") boolean npc2,
			@RequestParam(value = "unknown2", required = false, defaultValue = "false") boolean unknown2) {

		if (arcus2) arcus();
		if (memcached2) memcached();
		if (mysql2) mysql();
		if (npc2) npc();
		if (unknown2) unknown();
		
		return "remotecombination";
	}

	@RequestMapping(value = "/allInOne")
	public String allInOne(Model model,
							@RequestParam(value = "arcus", required = false, defaultValue = "true") boolean arcus,
							@RequestParam(value = "memcached", required = false, defaultValue = "false") boolean memcached,
							@RequestParam(value = "mysql", required = false, defaultValue = "false") boolean mysql,
							@RequestParam(value = "npc", required = false, defaultValue = "false") boolean npc,
							@RequestParam(value = "unknown", required = false, defaultValue = "false") boolean unknown,
							@RequestParam(value = "nested", required = false, defaultValue = "true") boolean nested,
							
							@RequestParam(value = "arcus2", required = false, defaultValue = "true") boolean arcus2,
							@RequestParam(value = "memcached2", required = false, defaultValue = "true") boolean memcached2,
							@RequestParam(value = "mysql2", required = false, defaultValue = "true") boolean mysql2,
							@RequestParam(value = "npc2", required = false, defaultValue = "true") boolean npc2,
							@RequestParam(value = "unknown2", required = false, defaultValue = "true") boolean unknown2,
							
							@RequestParam(value = "remote", required = false, defaultValue = "true") boolean remote,
							@RequestParam(value = "randomRemotePort", required = false, defaultValue = "false") boolean randomRemotePort) {
		
		if (arcus) arcus();
		if (memcached) memcached();
		if (mysql) mysql();
		if (npc) npc();
		if (unknown) unknown();
		if (nested) nested();

		if (remote) {
			String[] ports = new String[] { "9080", "10080", "11080" };
			Random random = new Random();
			String port = (randomRemotePort) ? ports[random.nextInt(3)] : ports[0];
			HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
			
			StringBuilder params = new StringBuilder();
			params.append("arcus2=").append(arcus2).append("&");
			params.append("memcached2=").append(memcached2).append("&");
			params.append("mysql2=").append(mysql2).append("&");
			params.append("npc2=").append(npc2).append("&");
			params.append("unknown2=").append(unknown2).append("&");
			
			client.execute("http://localhost:" + port + "/allInOne2.pinpoint?", new HashMap<String, Object>());
		}
		
		return "remotecombination";
	}
	
	@Override
	public void destroy() throws Exception {
		arcus.shutdown();
		memcached.shutdown();
	}
}
