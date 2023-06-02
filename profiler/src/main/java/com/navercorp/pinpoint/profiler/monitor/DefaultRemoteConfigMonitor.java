package com.navercorp.pinpoint.profiler.monitor;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.HttpUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.module.*;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.monitor.processor.ReSetConfigProcessorFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dongdd
 * @description：
 */
public class DefaultRemoteConfigMonitor implements RemoteConfigMonitor{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerConfig profilerConfig;
    private final Sampler sampler;
    private ConfigService configService;
    private final String dataId;
    private final String group;
    private final String GROUP_DEFAULT="default";
    private final Set<NacosConfig> nacosConfigSet= new HashSet<NacosConfig>();
    private ReSetConfigProcessorFactory reSetConfigProcessorFactory;
    private final int retry = 2;
    private final String REMOTETYPE_TIMEDTASK = "1";
    private static final String REMOTETYPE_LONGCONNECTION = "2";

    private Scheduler scheduler;
    private final long refreshIntervalMs;
    private final long sendIntervalMs;
    private final int maxTryPerAttempt;

    private final DataSender statDataSender;
    private final DataSender spanDataSender;
    private final DataSender tcpDataSender;

    private static final String NACOS_USERNAME="username";
    private static final String NACOS_PASSWORD="password";
    private static final String NACOS_NAMESPANCE="namespace";

    public ReSetConfigProcessorFactory getReSetConfigProcessorFactory(){
        return this.reSetConfigProcessorFactory;
    }

    @Inject
    public DefaultRemoteConfigMonitor(ProfilerConfig profilerConfig, Sampler sampler, @AgentLicence String agentLicence, @ApplicationName String applicationName
            , @StatDataSender DataSender statDataSender , @SpanDataSender DataSender spanDataSender, @AgentDataSender EnhancedDataSender<Object> tcpDataSenderProvider
            , Provider<TraceContext> traceContextProvider
            , Provider<StorageFactory> storageFactoryProvider
    ){
        this.profilerConfig = profilerConfig;
        this.sampler = sampler;
        this.dataId = agentLicence;
        this.group = applicationName;
        this.maxTryPerAttempt = 3;
        this.refreshIntervalMs = profilerConfig.getRemoteType1Gap();
        this.sendIntervalMs = profilerConfig.getRemoteType1Gap();

        this.statDataSender = statDataSender;
        this.spanDataSender = spanDataSender;
        this.tcpDataSender = tcpDataSenderProvider;
        TraceContext traceContext = traceContextProvider.get();
        StorageFactory storageFactory = storageFactoryProvider.get();
        reSetConfigProcessorFactory = new ReSetConfigProcessorFactory(this.profilerConfig, this.sampler, this.statDataSender, this.spanDataSender, this.tcpDataSender, traceContext, storageFactory);
    }
    @Override
    public void start(ConfigService configService) {
        startRemoteMonitor(configService);
    }
    private void startRemoteMonitor(ConfigService configService){
        try {
            if(null != profilerConfig && profilerConfig.getRemoteEnable() && !StringUtils.isEmpty(profilerConfig.getRemoteAddr())){
                if(REMOTETYPE_TIMEDTASK.equals(profilerConfig.getRemoteType())){
                    this.scheduler = new Scheduler();
                    scheduler.start();
                }else if(startRemoteMonitorIsBeginNacos(profilerConfig)){
                    this.configService = configService;
                    addListener(dataId, group+"-listener");
                    addListener(dataId, GROUP_DEFAULT+"-listener");
                    logger.info("AgentRemoteConfig started");
                }else{
                    this.configService = null;
                    logger.info("AgentRemoteConfig not start！");
                }
            }else{
                logger.info("Ban connection nacos！");
            }
        } catch (Throwable e) {
            logger.warn("AgentRemoteConfig start error: ",  e);
        }
    }
    public static Properties getNacosProperties(ProfilerConfig profilerConfig){
        Properties nacosProperties = new Properties();
        nacosProperties.put("serverAddr", profilerConfig.getRemoteAddr());
        nacosProperties.put("username", NACOS_USERNAME);
        nacosProperties.put("password", NACOS_PASSWORD);
        nacosProperties.put("namespace", NACOS_NAMESPANCE);
        return nacosProperties;
    }
    public static boolean startRemoteMonitorIsBeginNacos(ProfilerConfig profilerConfig){
        boolean result = false;
        if(null != profilerConfig && profilerConfig.getRemoteEnable() && !StringUtils.isEmpty(profilerConfig.getRemoteAddr()) &&
                REMOTETYPE_LONGCONNECTION.equals(profilerConfig.getRemoteType())){
            result = true;
        }
        return result;
    }

    private boolean isConnected(String ip, int port){
        boolean result = false;
        Socket connect = new Socket();
        try {
            connect.connect(new InetSocketAddress(ip, port),1500);
            result = connect.isConnected();
        } catch (IOException e) {
        }
        return result;
    }

    @Override
    public void stop() {
        if(profilerConfig.getRemoteEnable()){
            if(REMOTETYPE_TIMEDTASK.equals(profilerConfig.getRemoteType())){
                scheduler.stop();
            }else if(REMOTETYPE_LONGCONNECTION.equals(profilerConfig.getRemoteType())){
                for(NacosConfig item : nacosConfigSet){
                    configService.removeListener(item.dataId, item.group, item.listener);
                }
            }
        }
    }
    private void addListener(String dataId, String group) throws NacosException {
        Listener defaultListener = getDefaultListener();
        configService.addListener(dataId, group, defaultListener);
        nacosConfigSet.add(new NacosConfig(dataId, group, defaultListener));
    }

    private Listener getDefaultListener(){
        Listener result = new Listener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                if(logger.isDebugEnabled()){
                    logger.debug("recieve remote configInfo : [{}]",  configInfo);
                }
                if(StringUtils.isEmpty(configInfo) || null == reSetConfigProcessorFactory){
                    return;
                }
                try {
                    reSetConfigProcessorFactory.dealConfigInfo(configInfo);
                } catch (Exception e) {
                    logger.error("deal remote configInfo error: ",  e);
                }
            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        };
        return result;
    }

    class NacosConfig{
        private String dataId;
        private String group;
        private Listener listener;

        NacosConfig(String dataId, String group, Listener listener) {
            this.dataId = dataId;
            this.group = group;
            this.listener = listener;
        }

        public String getDataId() {
            return dataId;
        }

        public void setDataId(String dataId) {
            this.dataId = dataId;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public Listener getListener() {
            return listener;
        }

        public void setListener(Listener listener) {
            this.listener = listener;
        }
    }

    private interface SuccessListener {
        void onSuccess();

        SuccessListener NO_OP = new SuccessListener() {
            @Override
            public void onSuccess() {
                // noop
            }
        };
    }

    private class Scheduler {

        private static final long IMMEDIATE = 0L;
        private final Timer timer = new Timer("Pinpoint-RemoteConfig-Timer", true);
        private final Object lock = new Object();
        // protected by lock's monitor
        private boolean isRunning = true;

        private Scheduler() {
            // preload
            GetRemoteConfigTask task = new GetRemoteConfigTask(SuccessListener.NO_OP);
            task.run();
        }

        public void start() {
            final SuccessListener successListener = new SuccessListener() {
                @Override
                public void onSuccess() {
                    schedule(this, maxTryPerAttempt, refreshIntervalMs, sendIntervalMs);
                }
            };
            if (logger.isDebugEnabled()) {
                logger.debug("Start scheduler of remoteConfigGetter");
            }
            schedule(successListener, Integer.MAX_VALUE, IMMEDIATE, sendIntervalMs);
        }

        public void refresh() {
            if (logger.isDebugEnabled()) {
                logger.debug("Refresh scheduler of remoteConfigGetter");
            }
            schedule(SuccessListener.NO_OP, maxTryPerAttempt, IMMEDIATE, sendIntervalMs);
        }

        private void schedule(SuccessListener successListener, int retryCount, long delay, long period) {
            synchronized (lock) {
                if (isRunning) {
                    GetRemoteConfigTask task = new GetRemoteConfigTask(successListener, retryCount);
                    timer.scheduleAtFixedRate(task, delay, period);
                }
            }
        }

        public void stop() {
            synchronized (lock) {
                isRunning = false;
                timer.cancel();
            }
        }
    }
    private class GetRemoteConfigTask extends TimerTask {
        private final SuccessListener taskHandler;
        private final int retryCount;
        private AtomicInteger counter;
        private int connectTimeout = 3000;
        private int readTimeout = 2000;

        private GetRemoteConfigTask(SuccessListener taskHandler) {
            this(taskHandler, 0);
        }

        private GetRemoteConfigTask(SuccessListener taskHandler, int retryCount) {
            this.taskHandler = Assert.requireNonNull(taskHandler, "taskHandler");
            this.retryCount = retryCount;
            this.counter = new AtomicInteger(0);
            try {
                String connectTimeoutStr = profilerConfig.getProperties().getProperty("profiler.remote.config.connectTimeout", "3000");
                String readTimeoutStr = profilerConfig.getProperties().getProperty("profiler.remote.config.readTimeout", "2000");
                this.connectTimeout = Integer.parseInt(connectTimeoutStr);
                this.readTimeout = Integer.parseInt(readTimeoutStr);
            } catch (Exception e) {
            }
        }

        @Override
        public void run() {
            try {
                getAndSetRemoteConfig();
            } catch (Exception e) {
            } finally {
                //成功后取消当前timer
                this.cancel();
                //回调onSuccess
                taskHandler.onSuccess();
            }
        }
        private boolean getAndSetRemoteConfig(){
            try {
                loadAndSetRemoteProfile();
            } catch (IOException e) {
                logger.error("getAndSetRemoteConfig error! e=", e);
                return false;
            }
            return true;
        }
        private void loadAndSetRemoteProfile() throws IOException {
            String nacosUrl = "http://%s/nacos/v1/cs/configs?username="+NACOS_USERNAME+"&password="+NACOS_PASSWORD+"&tenant="+NACOS_NAMESPANCE+"&dataId=%s&group=%s";

            //精准匹配licence和appName dataId=licence&&group=appName-listener
            String level = dataId+":"+ group+"-listener";
            int forSize = profilerConfig.getRemoteAddr().contains(",") ? profilerConfig.getRemoteAddr().split(",").length : 1;
            for(int i=0; i<forSize; i++){
                String profileStr = HttpUtils.doGet(String.format(nacosUrl
                        , profilerConfig.getRemoteAddr().split(",")[i]
                        , dataId
                        , group+"-listener")
                        , connectTimeout, readTimeout);
                if(StringUtils.isEmpty(profileStr)){
                    //无精准匹配则二次匹配 licence dataId=licence&&group=default-listener
                    level = dataId+":"+ GROUP_DEFAULT+"-listener";
                    profileStr = HttpUtils.doGet(String.format(nacosUrl
                            , profilerConfig.getRemoteAddr().split(",")[i]
                            , dataId
                            , GROUP_DEFAULT+"-listener")
                            , connectTimeout, readTimeout);
                }
                if(!StringUtils.isEmpty(profileStr)){
                    logger.info(String.format("use remote config level: [%s]", level));
                    reSetConfigProcessorFactory.dealConfigInfo(profileStr);
                    logger.info("loadAngSetRemoteProfile successes!");
                }
                break;
            }
        }

    }

}
