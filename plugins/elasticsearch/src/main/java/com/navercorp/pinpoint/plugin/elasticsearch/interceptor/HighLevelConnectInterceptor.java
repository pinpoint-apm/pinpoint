/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.elasticsearch.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.elasticsearch.accessor.EndPointAccessor;
import com.navercorp.pinpoint.plugin.elasticsearch.accessor.HttpHostInfoAccessor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Roy Kim
 */
public class HighLevelConnectInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public HighLevelConnectInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logBeforeInterceptor(target, args);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logAfterInterceptor(target, args, result, throwable);
        }

        if (args == null) {
            return;
        }

        getEndPoint(args, target);
//        getClusterInfoPoint(target);
    }

    private void getEndPoint(Object[] args, Object target) {
        final List<String> hostList = getHostList(args[0]);

        if (target instanceof EndPointAccessor) {
            if (((EndPointAccessor) target)._$PINPOINT$_getEndPoint() == null) {
                ((EndPointAccessor) target)._$PINPOINT$_setEndPoint(merge(hostList));
            }
        }
    }

    private String merge(List<String> host) {
        if (host.isEmpty()) {
            return "";
        }
        String single = host.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append(single);
        for (int i = 1; i < host.size(); i++) {
            sb.append(',');
            sb.append(host.get(i));
        }
        return sb.toString();
    }

    private void logBeforeInterceptor(Object target, Object[] args) {
        logger.beforeInterceptor(target, args);
    }

    private void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        logger.afterInterceptor(target, args, result, throwable);
    }


    private List<String> getHostList(Object arg) {
        if (!(arg instanceof RestClient)) {
            return Collections.emptyList();
        }

        final List<String> hostList = new ArrayList<String>();

        HttpHost[] httpHosts = null;
        if (arg instanceof HttpHostInfoAccessor) {
            httpHosts = ((HttpHostInfoAccessor) arg)._$PINPOINT$_getHttpHostInfo();
        }

        //v6.4 ~
        if (httpHosts == null) {
            for (Node node : ((RestClient) arg).getNodes()) {
                final String hostAddress = HostAndPort.toHostAndPortString(node.getHost().getHostName(), node.getHost().getPort());
                hostList.add(hostAddress);
            }
        } else {
            //v6.0 ~ 6.3
            for (HttpHost httpHost : httpHosts) {
                final String hostAddress = HostAndPort.toHostAndPortString(httpHost.getHostName(), httpHost.getPort());
                hostList.add(hostAddress);
            }
        }

        return hostList;
    }

    //TODO leave code as comment for future needs for Cluster Information
//    private void getClusterInfoPoint(Object target) {
//
//        StringBuilder clusterInfo = new StringBuilder();
//
//        if (target instanceof RestHighLevelClient && target instanceof ClusterInfoAccessor) {
//            if (((ClusterInfoAccessor) target)._$PINPOINT$_getClusterInfo() == null) {
//
//                /**
//                 v6.0 ~ v6.3
//                 org.elasticsearch.action.main.MainResponse response = ((RestHighLevelClient) target).info();
//
//                 v6.4 ~ v7.1
//                 org.elasticsearch.action.main.MainResponse response = ((RestHighLevelClient) target).info(RequestOptions.DEFAULT);
//
//                 7.2 ~
//                 org.elasticsearch.client.core.MainResponse response = ((RestHighLevelClient) target).info(RequestOptions.DEFAULT);
//                 **/
//
//                //finding method
//                Method method = null;
//                //invoked method
//                Object mainResponse = null;
//
//                try {
//                    //v7.2 ~
//                    method = RestHighLevelClient.class.getMethod("info", Class.forName("org.elasticsearch.client.RequestOptions.class"));
//                    mainResponse = method.invoke(target, RequestOptions.DEFAULT);
//                } catch (NoSuchMethodException e) {
//                    e.printStackTrace();
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//                if (method == null) {
//                    //v6.0 ~ v6.3
//                    try {
//                        method = RestHighLevelClient.class.getMethod("info", Class.forName("[Lorg.apache.http.Header;"));
//                        mainResponse = method.invoke(target, (Object) new Header[]{});
//                    } catch (NoSuchMethodException e) {
//                        e.printStackTrace();
//                    } catch (ClassNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    } catch (InvocationTargetException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                //Casting Class find
//                Class mainResponseClass = null;
//                try {
//                    //7.2~
//                    mainResponseClass = Class.forName("org.elasticsearch.client.core.MainResponse");
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                }
//                if (mainResponseClass == null) {
//                    try {
//                        //6.0~7.2
//                        mainResponseClass = Class.forName("org.elasticsearch.action.main.MainResponse");
//                    } catch (ClassNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                System.out.println("mainResponse" + mainResponse);
//                System.out.println("mainResponseClass" + mainResponseClass);
//
//                mainResponseClass.cast(mainResponse);
//
//                try {
//                    clusterInfo.append("ClusterName: ");
//                    clusterInfo.append(mainResponse.getClass().getMethod("getClusterName").invoke(mainResponse));
//                    clusterInfo.append(", Version: ");
//                    if (mainResponseClass.getName().equals("org.elasticsearch.client.core.MainResponse")) {
//                        Object object = mainResponse.getClass().getMethod("getVersion").invoke(mainResponse);
//
//                        Class version = null;
//                        try {
//                            version = Class.forName("org.elasticsearch.action.main.MainResponse$Version");
//                        } catch (ClassNotFoundException e) {
//                            e.printStackTrace();
//                        }
//                        version.cast(object);
//                        clusterInfo.append(object.getClass().getMethod("getNumber").invoke(object));
//
//                    } else {
//                        clusterInfo.append(mainResponse.getClass().getMethod("getVersion").invoke(mainResponse));
//                    }
//
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                } catch (NoSuchMethodException e) {
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            clusterInfo.append("ClusterName: ");
//            clusterInfo.append("not found");
//            clusterInfo.append(", Version: ");
//            clusterInfo.append("not found");
//        }
//
//        ((ClusterInfoAccessor) target)._$PINPOINT$_setClusterInfo(clusterInfo.toString());
//    }
}
