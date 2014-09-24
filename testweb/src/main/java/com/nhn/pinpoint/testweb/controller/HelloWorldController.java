package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.connector.apachehttp4.ApacheHttpClient4;
import com.nhn.pinpoint.testweb.connector.apachehttp4.HttpConnectorOptions;
import com.nhn.pinpoint.testweb.domain.Member;
import com.nhn.pinpoint.testweb.service.CacheService;
import com.nhn.pinpoint.testweb.service.DummyService;
import com.nhn.pinpoint.testweb.service.MemberService;
import com.nhn.pinpoint.testweb.util.Description;
import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.npc.connector.NpcHessianConnector;
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
import org.springframework.web.bind.annotation.ResponseBody;
import perftest.LevelManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    @ResponseBody
    public String dummy() {
        dummyService.doSomething();
        return "OK";
    }

    @RequestMapping(value = "/encoding")
    @ResponseBody
    public String encoding(Model model, @RequestParam("name") String name) {
        logger.debug("name=" + name);
        return "OK";
    }

    @RequestMapping(value = "/arcus")
    @ResponseBody
    public String arcus() {
        cacheService.arcus();
        return "OK";
    }

    @RequestMapping(value = "/memcached")
    @ResponseBody
    public String memcached() {
        cacheService.memcached();
        return "OK";
    }

    @RequestMapping(value = "/mysql")
    @ResponseBody
    public String mysql() {
        int id = (new Random()).nextInt();

        Member member = new Member();
        member.setId(id);
        member.setName("pinpoint_user");
        member.setJoined(new Date());

        // add
        service.add(member);

        // list
        service.list();

        // del
        service.delete(id);

        return "OK";
    }

    @Description("바인드 변수 + 상수값 파싱 로직테스트")
    @RequestMapping(value = "/mysqlStatement")
    @ResponseBody
    public String mysqlStatement() {
        int id = (new Random()).nextInt();

        Member member = new Member();
        member.setId(id);
        member.setName("pinpoint_user");
        member.setJoined(new Date());

        // add
        service.addStatement(member);

        // list
        service.list();

        // del
        service.delete(id);

        return "OK";
    }

    @RequestMapping(value = "/nested")
    @ResponseBody
    public String nested() {
        ApacheHttpClient4 client2 = new ApacheHttpClient4(new HttpConnectorOptions());
        client2.execute("http://localhost:8080/donothing.pinpoint", new HashMap<String, Object>());

        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("http://www.naver.com/", new HashMap<String, Object>());
        mysql();
        return "OK";
    }

    @RequestMapping(value = "/remotecombination")
    @ResponseBody
    public String remotecombination() {
        String[] ports = new String[]{"9080", "10080", "11080"};
        Random random = new Random();
        String port = ports[random.nextInt(3)];

        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("http://localhost:" + port + "/combination.pinpoint", new HashMap<String, Object>());

        ApacheHttpClient4 client2 = new ApacheHttpClient4(new HttpConnectorOptions());
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
        return "OK";
    }

    @RequestMapping(value = "/remotearcus")
    @ResponseBody
    public String remotearcus() {
        arcus();

        String[] ports = new String[]{"9080", "10080", "11080"};
        Random random = new Random();
        String port = ports[random.nextInt(3)];
        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("http://localhost:" + port + "/arcus.pinpoint", new HashMap<String, Object>());
        return "OK";
    }

    @RequestMapping(value = "/combination")
    @ResponseBody
    public String combination() {

        mysql();

        arcus();

        memcached();


        randomSlowMethod();

        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("http://www.naver.com/", new HashMap<String, Object>());
        client.execute("http://www.naver.com/", new HashMap<String, Object>());

        client.execute("http://section.cafe.naver.com/", new HashMap<String, Object>());
        client.execute("http://section.cafe.naver.com/", new HashMap<String, Object>());

        npc();

        return "OK";
    }

    @RequestMapping(value = "/httperror")
    @ResponseBody
    public String httperror() {
        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("http://127.0.0.1/", new HashMap<String, Object>());
        return "OK";
    }

    @RequestMapping(value = "/error500")
    @ResponseBody
    public String error500() {
        int i = 1 / 0;
        return "OK";

    }

    @RequestMapping(value = "/slow")
    @ResponseBody
    public String slow() {
        try {
            Thread.sleep(new Random().nextInt(10) * 100);
        } catch (InterruptedException e) {
        }
        return "OK";
    }

    @RequestMapping(value = "/throwexception")
    @ResponseBody
    public String exception() {
        throw new RuntimeException("Exception test");
    }

    @RequestMapping(value = "/arcustimeout")
    @ResponseBody
    public String arcustimeout() {
        Future<Boolean> future = null;
        try {
            future = arcus.set("pinpoint:expect-timeout", 10, "Hello, Timeout.");
            future.get(10L, TimeUnit.MICROSECONDS);
        } catch (Exception e) {
            if (future != null) {
                future.cancel(true);
            }
            throw new RuntimeException(e);
        }
        return "OK";
    }

    @RequestMapping(value = "/remotesimple")
    @ResponseBody
    public String remotesimple() {
        String[] ports = new String[]{"9080", "10080", "11080"};
        Random random = new Random();
        String port = ports[random.nextInt(3)];

        arcus();

        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("http://localhost:" + port + "/arcus.pinpoint", new HashMap<String, Object>());
        return "OK";
    }

    @RequestMapping(value = "/remoteerror")
    @ResponseBody
    public String remoteError() {
        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("http://localhost:10080/rpcerror.pinpoint", new HashMap<String, Object>());
        return "OK";
    }

    @RequestMapping(value = "/rpcerror")
    @ResponseBody
    public String rpcError() {
        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("UNKNOWN_URL", new HashMap<String, Object>());
        return "OK";
    }

    @RequestMapping(value = "/npc")
    @ResponseBody
    public String npc() {
        try {
            InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);
            NpcHessianConnector connector = new NpcHessianConnector(serverAddress, true);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("message", "hello pinpoint");

            InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

            future.await();

            // Object result = future.get();
            Object result = future.getReturnValue();
            logger.debug("npc result={}", result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "OK";
    }

    @RequestMapping(value = "/perftest")
    @ResponseBody
    public String perfTest() {
        levelManager.traverse();
        return "OK";
    }

    @Override
    public void destroy() throws Exception {
        arcus.shutdown();
        memcached.shutdown();
    }
}
