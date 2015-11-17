package org.apache.hadoop.util;

import org.springframework.beans.factory.DisposableBean;

public class ShutdownHookManagerProxy implements DisposableBean {

    @Override
    public void destroy() throws Exception {
        ShutdownHookManager.get().getShutdownHooksInOrder();
    }

}
