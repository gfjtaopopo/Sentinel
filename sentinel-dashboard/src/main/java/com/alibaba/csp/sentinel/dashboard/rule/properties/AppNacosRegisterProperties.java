package com.alibaba.csp.sentinel.dashboard.rule.properties;

import com.alibaba.fastjson.JSON;

/**
 * @author taofe
 * @date 2021/10/31
 */
public class AppNacosRegisterProperties
{
//    private String name;
    private String namespace;
    private String groupId;

    public AppNacosRegisterProperties()
    {
    }

    public AppNacosRegisterProperties(String namespace, String groupId)
    {
//        this.name = name;
        this.namespace = namespace;
        this.groupId = groupId;
    }

    @Override
    public String toString()
    {
        return JSON.toJSONString(this);
    }

//    public String getName()
//    {
//        return name;
//    }

//    public void setName(String name)
//    {
//        this.name = name;
//    }

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
}
