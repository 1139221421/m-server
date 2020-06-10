package com.lxl.message.websocket.discovery;

import com.lxl.utils.common.SpringContextUtils;
import com.netflix.appinfo.MyDataCenterInstanceConfig;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import java.lang.management.ManagementFactory;
import java.util.Set;

public class InstanceConfig extends MyDataCenterInstanceConfig {

    @Override
    public String getHostName(boolean refresh) {
        return SpringContextUtils.getBean(InstanceProperties.class).getHost();
    }

    @Override
    public int getNonSecurePort() {
        int port;
        try {
            MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
                    Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));

            port = Integer.valueOf(objectNames.iterator().next().getKeyProperty("port"));
        } catch (Exception e) {
            return super.getNonSecurePort();
        }
        return port;
    }
}
