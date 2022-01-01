package com.alibaba.csp.sentinel.dashboard.rule.config;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

/**
 * @author taofe
 * @date 2021/11/7
 */
public interface ConfigServiceFT extends ConfigService
{
    String getConfig(String var1, String var2, String var3, long var4) throws NacosException;

    boolean publishConfig(String var1, String var2, String var3, String var4, String var5, String var6) throws NacosException;
}
