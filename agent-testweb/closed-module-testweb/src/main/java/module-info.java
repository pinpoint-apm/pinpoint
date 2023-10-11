open module com.navercorp.pinpoint.closedcaller {
    requires spring.web;
    requires spring.context;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.core;

    requires pinpoint.closed.module.testlib;
}
