package com.nhn.hippo.testweb.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
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

import com.nhn.hippo.testweb.domain.Member;
import com.nhn.hippo.testweb.service.DummyService;
import com.nhn.hippo.testweb.service.MemberService;
import com.nhn.hippo.testweb.util.HttpConnectorOptions;
import com.nhn.hippo.testweb.util.HttpInvoker;

@Controller
public class HelloWorldController implements DisposableBean {

	private final ArcusClient arcus;
	private final MemcachedClient memcached;

	public HelloWorldController() throws IOException {
		arcus = ArcusClient.createArcusClient("dev.arcuscloud.nhncorp.com:17288", "dev", new ConnectionFactoryBuilder());
		memcached = new MemcachedClient(AddrUtil.getAddresses("10.25.149.80:11211"));
	}

	@Autowired
	private MemberService service;

	@Autowired
	private DummyService dummyService;

	private void randomSlowMethod() {
		try {
			Thread.sleep((new Random().nextInt(5)) * 1000L);
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
		System.out.println("name=" + name);
		return "donothing";
	}

	@RequestMapping(value = "/donothing")
	public String donothing(Model model) {
		System.out.println("do nothing.");
		return "donothing";
	}

	@RequestMapping(value = "/arcus")
	public String arcus(Model model) {
		Future<Boolean> future = null;
		try {
			future = arcus.set("hippo:testkey", 10, "Hello, Hippo.");
			future.get(1000L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if (future != null)
				future.cancel(true);
		}
		return "arcus";
	}

	@RequestMapping(value = "/memcached")
	public String memcached(Model model) {
		Future<Boolean> future = null;
		try {
			future = memcached.set("hippo:testkey", 10, "Hello, Hippo.");
			future.get(1000L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if (future != null)
				future.cancel(true);
		}
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

	@RequestMapping(value = "/mysqlsimple")
	public String mysqlsimple(Model model) {
		int id = (new Random()).nextInt();

		Member member = new Member();
		member.setId(id);
		member.setName("chisu");
		member.setJoined(new Date());

		// add
		service.add(member);

		return "mysql";
	}

	@RequestMapping(value = "/remotecombination")
	public String remotecombination(Model model) {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.executeToBloc("http://localhost:8080/combination.hippo", new HashMap<String, Object>());

		client.executeToBloc("http://www.naver.com/", new HashMap<String, Object>());
		client.executeToBloc("http://www.naver.com/", new HashMap<String, Object>());
		try {
			client.executeToBloc("http://very.very.very.long.long.url/", new HashMap<String, Object>());
			client.executeToBloc("http://url1/", new HashMap<String, Object>());
			client.executeToBloc("http://url2/", new HashMap<String, Object>());
			client.executeToBloc("http://url2/", new HashMap<String, Object>());
			client.executeToBloc("http://url3/", new HashMap<String, Object>());
			client.executeToBloc("http://url3/", new HashMap<String, Object>());
			client.executeToBloc("http://url3/", new HashMap<String, Object>());
		} catch (Exception e) {
		}
		return "remotecombination";
	}

	@RequestMapping(value = "/remotecombination2")
	public String remotecombination2(Model model) throws MalformedURLException {
        URL url = new URL("http://localhost:8080/combination2.hippo");
        HttpURLConnection request = null;
        try {
			request = (HttpURLConnection) url.openConnection();
			request.setRequestMethod("GET");
			request.setRequestProperty("Content-type", "text/xml; charset=UTF-8");
			// request.connect();

			InputStream is = request.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			System.out.println(reader.readLine());

			reader.close();

		} catch (Exception e) {
            e.printStackTrace();
		} finally {
            if(request != null) {
                request.disconnect();
            }
        }

		return "remotecombination";
	}

	@RequestMapping(value = "/remotemysql")
	public String remotemysql(Model model) {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.executeToBloc("http://localhost:8080/mysql.hippo", new HashMap<String, Object>());
		return "remotecombination";
	}

	@RequestMapping(value = "/combination")
	public String combination(Model model) {
		mysql(model);
		arcus(model);
		memcached(model);

//		randomSlowMethod();

		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.executeToBloc("http://www.naver.com/", new HashMap<String, Object>());
		client.executeToBloc("http://www.naver.com/", new HashMap<String, Object>());

		client.executeToBloc("http://section.cafe.naver.com/", new HashMap<String, Object>());
		client.executeToBloc("http://section.cafe.naver.com/", new HashMap<String, Object>());

		return "combination";
	}

	@RequestMapping(value = "/combination2")
	public String combination2(Model model) {
		mysql(model);
		arcus(model);
		memcached(model);

		randomSlowMethod();

		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.executeToBloc("http://www.naver.com/", new HashMap<String, Object>());
		client.executeToBloc("http://www.naver.com/", new HashMap<String, Object>());

		return "combination";
	}

	@RequestMapping(value = "/httperror")
	public String httperror(Model model) {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.executeToBloc("http://127.0.0.1/", new HashMap<String, Object>());
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
			future = arcus.set("hippo:expect-timeout", 10, "Hello, Timeout.");
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
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.executeToBloc("http://macpro:8080/mysql.hippo", new HashMap<String, Object>());
		return "remotecombination";
	}
	
	@Override
	public void destroy() throws Exception {
		arcus.shutdown();
		memcached.shutdown();
	}
}
