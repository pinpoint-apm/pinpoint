package com.nhn.pinpoint.testweb.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ArcusClient;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import perftest.LevelManager;

import com.nhn.pinpoint.testweb.domain.Member;
import com.nhn.pinpoint.testweb.service.CacheService;
import com.nhn.pinpoint.testweb.service.DummyService;
import com.nhn.pinpoint.testweb.service.MemberService;
import com.nhn.pinpoint.testweb.util.Description;
import com.nhn.pinpoint.testweb.util.HttpConnectorOptions;
import com.nhn.pinpoint.testweb.util.HttpInvoker;
import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.npc.connector.NpcHessianConnector;

@Controller
@Deprecated
public class HelloWorldController implements DisposableBean {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ArcusClient arcus;
	private final MemcachedClient memcached;
	private final LevelManager levelManager;

	public HelloWorldController() throws IOException {
		arcus = ArcusClient.createArcusClient("dev.arcuscloud.nhncorp.com:17288", "dev", new ConnectionFactoryBuilder());
		memcached = new MemcachedClient(AddrUtil.getAddresses("10.25.149.80:11244,10.25.149.80:11211,10.25.149.79:11211"));
		levelManager = new LevelManager();
	}

	@Autowired
	@Qualifier("memberService")
	private MemberService service;

	@Autowired
	private DummyService dummyService;

	@Autowired
	private CacheService cacheService;

	private void randomSlowMethod() {
		try {
			Thread.sleep((new Random().nextInt(3)) * 1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/dummy")
	public String dummy(Model model) {
		dummyService.doSomething();
		return "donothing";
	}

	@RequestMapping(value = "/encoding")
	public String encoding(Model model, @RequestParam("name") String name) {
		logger.debug("name=" + name);
		return "donothing";
	}

	@RequestMapping(value = "/arcus")
	public String arcus(Model model) {
		cacheService.arcus();
		return "arcus";
	}

	@RequestMapping(value = "/memcached")
	public String memcached(Model model) {
		cacheService.memcached();
		return "memcached";
	}

	@RequestMapping(value = "/mysql")
	public String mysql(Model model) {
		int id = (new Random()).nextInt();

		Member member = new Member();
		member.setId(id);
		member.setName("chisu");
		member.setJoined(new Date());

		// add
		service.add(member);

		// list
		service.list();

		// del
		service.delete(id);

		return "mysql";
	}

	@Description("바인드 변수 + 상수값 파싱 로직테스트")
	@RequestMapping(value = "/mysqlStatement")
	public String mysqlStatement(Model model) {
		int id = (new Random()).nextInt();

		Member member = new Member();
		member.setId(id);
		member.setName("chisu");
		member.setJoined(new Date());

		// add
		service.addStatement(member);

		// list
		service.list();

		// del
		service.delete(id);

		return "mysql";
	}

	@RequestMapping(value = "/nested")
	public String nested(Model model) {
		HttpInvoker client2 = new HttpInvoker(new HttpConnectorOptions());
		client2.execute("http://localhost:8080/donothing.pinpoint", new HashMap<String, Object>());

		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute("http://www.naver.com/", new HashMap<String, Object>());
		mysql(model);
		return "remotecombination";
	}

	@RequestMapping(value = "/remotecombination")
	public String remotecombination(Model model) {
		String[] ports = new String[] { "9080", "10080", "11080" };
		Random random = new Random();
		String port = ports[random.nextInt(3)];

		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute("http://localhost:" + port + "/combination.pinpoint", new HashMap<String, Object>());

		HttpInvoker client2 = new HttpInvoker(new HttpConnectorOptions());
		client2.execute("http://localhost:8080/arcus.pinpoint", new HashMap<String, Object>());

		client.execute("http://www.naver.com/", new HashMap<String, Object>());
		client.execute("http://www.naver.com/", new HashMap<String, Object>());
		try {
			client.execute("http://very.very.very.long.long.url/", new HashMap<String, Object>());
			client.execute("http://url1/", new HashMap<String, Object>());
			client.execute("http://url2/", new HashMap<String, Object>());
			client.execute("http://url2/", new HashMap<String, Object>());
			client.execute("http://url3/", new HashMap<String, Object>());
			client.execute("http://url3/", new HashMap<String, Object>());
			client.execute("http://url3/", new HashMap<String, Object>());
		} catch (Exception e) {
		}
		return "remotecombination";
	}

	@RequestMapping(value = "/remotearcus")
	public String remotearcus(Model model) {
		arcus(model);

		String[] ports = new String[] { "9080", "10080", "11080" };
		Random random = new Random();
		String port = ports[random.nextInt(3)];
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute("http://localhost:" + port + "/arcus.pinpoint", new HashMap<String, Object>());
		return "remotecombination";
	}

	@RequestMapping(value = "/combination")
	public String combination(Model model) {
		try {
			mysql(model);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			arcus(model);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			memcached(model);
		} catch (Exception e) {
			e.printStackTrace();
		}

		randomSlowMethod();

		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute("http://www.naver.com/", new HashMap<String, Object>());
		client.execute("http://www.naver.com/", new HashMap<String, Object>());

		client.execute("http://section.cafe.naver.com/", new HashMap<String, Object>());
		client.execute("http://section.cafe.naver.com/", new HashMap<String, Object>());

		npc(model);

		return "combination";
	}

	@RequestMapping(value = "/httperror")
	public String httperror(Model model) {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute("http://127.0.0.1/", new HashMap<String, Object>());
		return "error";
	}

	@RequestMapping(value = "/error500")
	public String error500(Model model) {
		int i = 1 / 0;
		return "error";
	}

	@RequestMapping(value = "/slow")
	public String slow(Model model) {
		try {
			Thread.sleep(new Random().nextInt(10) * 100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "error";
	}

	@RequestMapping(value = "/throwexception")
	public String exception(Model model) {
		throw new RuntimeException("Exception test");
	}

	@RequestMapping(value = "/arcustimeout")
	public String arcustimeout(Model model) {
		Future<Boolean> future = null;
		try {
			future = arcus.set("pinpoint:expect-timeout", 10, "Hello, Timeout.");
			future.get(10L, TimeUnit.MICROSECONDS);
		} catch (Exception e) {
			if (future != null)
				future.cancel(true);
			e.printStackTrace();
		}
		return "timeout";
	}

	@RequestMapping(value = "/remotesimple")
	public String remotesimple(Model model) {
		String[] ports = new String[] { "9080", "10080", "11080" };
		Random random = new Random();
		String port = ports[random.nextInt(3)];

		arcus(model);

		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute("http://localhost:" + port + "/arcus.pinpoint", new HashMap<String, Object>());
		return "remotecombination";
	}

	@RequestMapping(value = "/remoteerror")
	public String remoteError(Model model) {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute("http://localhost:10080/rpcerror.pinpoint", new HashMap<String, Object>());
		return "remotecombination";
	}

	@RequestMapping(value = "/rpcerror")
	public String rpcError(Model model) {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute("UNKNOWN_URL", new HashMap<String, Object>());
		return "remotecombination";
	}

	@RequestMapping(value = "/npc")
	public String npc(Model model) {
		try {
			InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);
			NpcHessianConnector connector = new NpcHessianConnector(serverAddress, true);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("message", "hello pinpoint");

			InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

			future.await();

			// Object result = future.get();
			Object result = future.getReturnValue();
			System.out.println("npc result=" + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "npc";
	}

	@RequestMapping(value = "/perftest")
	public String perfTest(Model model) {
		levelManager.traverse();
		return "perftest";
	}

	@Override
	public void destroy() throws Exception {
		arcus.shutdown();
		memcached.shutdown();
	}
}
