package com.lxl.auth;

import com.lxl.utils.config.ConfUtil;
import org.junit.BeforeClass;

public class BaseTest {
    @BeforeClass
    public static void initProperties() throws Exception {
        System.setProperty("project.name", "auth");
        System.setProperty("project.profile", "rd");
        System.setProperty("project.version", "0.0.1");
        System.setProperty("project.zkstr", "dev:2181");

        System.setProperty("spring.elasticsearch.rest.uris[0]", "http://dev:9201");
        System.setProperty("spring.elasticsearch.rest.uris[1]", "http://dev:9202");
        System.setProperty("spring.elasticsearch.rest.uris[2]", "http://dev:9203");
        System.setProperty("spring.elasticsearch.rest.username", "elastic");
        System.setProperty("spring.elasticsearch.rest.password", "123456");
        ConfUtil.initAndSetProperties();
    }
}
