package com.alibaba.csp.sentinel.dashboard.rule.config;

import com.alibaba.nacos.api.exception.NacosException;

import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * Copy From com.alibaba.nacos.api.config.ConfigFactory
 *
 * @author taofe
 * @date 2021/11/7
 */
public class ConfigFactoryFT
{
    public ConfigFactoryFT() {
    }

    public static ConfigServiceFT createConfigService(Properties properties) throws NacosException {
        try {
            Class<?> driverImplClass = Class.forName("com.alibaba.csp.sentinel.dashboard.rule.config.NacosConfigServiceFT");
            Constructor constructor = driverImplClass.getConstructor(Properties.class);
            ConfigServiceFT vendorImpl = (ConfigServiceFT)constructor.newInstance(properties);
            return vendorImpl;
        } catch (Throwable var4) {
            throw new NacosException(-400, var4);
        }
    }

    public static ConfigServiceFT createConfigService(String serverAddr) throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        return createConfigService(properties);
    }
}
