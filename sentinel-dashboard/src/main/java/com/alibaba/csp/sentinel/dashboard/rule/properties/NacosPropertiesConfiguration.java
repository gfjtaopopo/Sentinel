package com.alibaba.csp.sentinel.dashboard.rule.properties;

import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;

/**
 * @author taofe
 * @date 2021/10/31
 */
@Configuration
@ConfigurationProperties(prefix = NacosConfigUtil.PROPERTY_PREFIX)
public class NacosPropertiesConfiguration
{
    @NotEmpty
    private String serverAddr = NacosConfigUtil.SERVER_ADDR;
    private String namespace;
    @NotEmpty
    private String groupId = NacosConfigUtil.GROUP_ID;

    private Map<String, AppNacosRegisterProperties> location = new HashMap<>();

    public NacosPropertiesConfiguration()
    {
    }

    public NacosPropertiesConfiguration(@NotEmpty String serverAddr, String namespace, @NotEmpty String groupId)
    {
        this.serverAddr = serverAddr;
        this.namespace = namespace;
        this.groupId = groupId;
    }

    @Override
    public String toString()
    {
        return JSON.toJSONString(this);
    }

    public String getServerAddr()
    {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr)
    {
        this.serverAddr = serverAddr;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public Map<String, AppNacosRegisterProperties> getLocation()
    {
        return location;
    }

    public void setLocation(Map<String, AppNacosRegisterProperties> location)
    {
        this.location = location;
    }
}
