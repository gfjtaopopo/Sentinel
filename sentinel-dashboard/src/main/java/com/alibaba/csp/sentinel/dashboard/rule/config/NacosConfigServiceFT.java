package com.alibaba.csp.sentinel.dashboard.rule.config;

import com.alibaba.nacos.api.config.filter.IConfigRequest;
import com.alibaba.nacos.api.config.filter.IConfigResponse;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.NacosConfigService;
import com.alibaba.nacos.client.config.filter.impl.ConfigFilterChainManager;
import com.alibaba.nacos.client.config.filter.impl.ConfigRequest;
import com.alibaba.nacos.client.config.filter.impl.ConfigResponse;
import com.alibaba.nacos.client.config.http.HttpAgent;
import com.alibaba.nacos.client.config.http.MetricsHttpAgent;
import com.alibaba.nacos.client.config.http.ServerHttpAgent;
import com.alibaba.nacos.client.config.impl.ClientWorker;
import com.alibaba.nacos.client.config.impl.HttpSimpleClient;
import com.alibaba.nacos.client.config.impl.LocalConfigInfoProcessor;
import com.alibaba.nacos.client.config.utils.ContentUtils;
import com.alibaba.nacos.client.config.utils.ParamUtils;
import com.alibaba.nacos.client.utils.LogUtils;
import com.alibaba.nacos.client.utils.ParamUtil;
import com.alibaba.nacos.client.utils.ValidatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Copy From com.alibaba.nacos.client.config.NacosConfigService
 *
 * 新增实现方法 com.alibaba.csp.sentinel.dashboard.rule.config.ConfigServiceFT#getConfig(java.lang.String, java.lang.String, java.lang.String, long)
 * 新增实现方法 com.alibaba.csp.sentinel.dashboard.rule.config.ConfigServiceFT#publishConfig(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
 *
 * @author taofe
 * @date 2021/10/31
 */
public class NacosConfigServiceFT implements ConfigServiceFT
{
    private static final Logger LOGGER = LogUtils.logger(NacosConfigService.class);
    private static final long POST_TIMEOUT = 3000L;
    private HttpAgent agent;
    private ClientWorker worker;
    private String namespace;
    private String encode;
    private ConfigFilterChainManager configFilterChainManager = new ConfigFilterChainManager();

    public NacosConfigServiceFT(Properties properties) throws NacosException {
        ValidatorUtils.checkInitParam(properties);
        String encodeTmp = properties.getProperty("encode");
        if (StringUtils.isBlank(encodeTmp)) {
            this.encode = "UTF-8";
        } else {
            this.encode = encodeTmp.trim();
        }

        this.initNamespace(properties);
        this.agent = new MetricsHttpAgent(new ServerHttpAgent(properties));
        this.agent.start();
        this.worker = new ClientWorker(this.agent, this.configFilterChainManager, properties);
    }

    private void initNamespace(Properties properties) {
        this.namespace = ParamUtil.parseNamespace(properties);
        properties.put("namespace", this.namespace);
    }

    public String getConfig(String dataId, String group, long timeoutMs) throws NacosException {
        return this.getConfigInner(this.namespace, dataId, group, timeoutMs);
    }

    public String getConfigAndSignListener(String dataId, String group, long timeoutMs, Listener listener) throws NacosException {
        String content = this.getConfig(dataId, group, timeoutMs);
        this.worker.addTenantListenersWithContent(dataId, group, content, Arrays.asList(listener));
        return content;
    }

    public void addListener(String dataId, String group, Listener listener) throws NacosException {
        this.worker.addTenantListeners(dataId, group, Arrays.asList(listener));
    }

    public boolean publishConfig(String dataId, String group, String content) throws NacosException {
        return this.publishConfigInner(this.namespace, dataId, group, (String)null, (String)null, (String)null, content);
    }

    public boolean removeConfig(String dataId, String group) throws NacosException {
        return this.removeConfigInner(this.namespace, dataId, group, (String)null);
    }

    public void removeListener(String dataId, String group, Listener listener) {
        this.worker.removeTenantListener(dataId, group, listener);
    }

    private String getConfigInner(String tenant, String dataId, String group, long timeoutMs) throws NacosException {
        group = this.null2defaultGroup(group);
        ParamUtils.checkKeyParam(dataId, group);
        ConfigResponse cr = new ConfigResponse();
        cr.setDataId(dataId);
        cr.setTenant(tenant);
        cr.setGroup(group);
        String content = LocalConfigInfoProcessor.getFailover(this.agent.getName(), dataId, group, tenant);
        if (content != null) {
            LOGGER.warn("[{}] [get-config] get failover ok, dataId={}, group={}, tenant={}, config={}", new Object[]{this.agent.getName(), dataId, group, tenant, ContentUtils.truncateContent(content)});
            cr.setContent(content);
            this.configFilterChainManager.doFilter((IConfigRequest)null, cr);
            content = cr.getContent();
            return content;
        } else {
            try {
                String[] ct = this.worker.getServerConfig(dataId, group, tenant, timeoutMs);
                cr.setContent(ct[0]);
                this.configFilterChainManager.doFilter((IConfigRequest)null, cr);
                content = cr.getContent();
                return content;
            } catch (NacosException var9) {
                if (403 == var9.getErrCode()) {
                    throw var9;
                } else {
                    LOGGER.warn("[{}] [get-config] get from server error, dataId={}, group={}, tenant={}, msg={}", new Object[]{this.agent.getName(), dataId, group, tenant, var9.toString()});
                    LOGGER.warn("[{}] [get-config] get snapshot ok, dataId={}, group={}, tenant={}, config={}", new Object[]{this.agent.getName(), dataId, group, tenant, ContentUtils.truncateContent(content)});
                    content = LocalConfigInfoProcessor.getSnapshot(this.agent.getName(), dataId, group, tenant);
                    cr.setContent(content);
                    this.configFilterChainManager.doFilter((IConfigRequest)null, cr);
                    content = cr.getContent();
                    return content;
                }
            }
        }
    }

    private String null2defaultGroup(String group) {
        return null == group ? "DEFAULT_GROUP" : group.trim();
    }

    private boolean removeConfigInner(String tenant, String dataId, String group, String tag) throws NacosException {
        group = this.null2defaultGroup(group);
        ParamUtils.checkKeyParam(dataId, group);
        String url = "/v1/cs/configs";
        List<String> params = new ArrayList();
        params.add("dataId");
        params.add(dataId);
        params.add("group");
        params.add(group);
        if (StringUtils.isNotEmpty(tenant)) {
            params.add("tenant");
            params.add(tenant);
        }

        if (StringUtils.isNotEmpty(tag)) {
            params.add("tag");
            params.add(tag);
        }

        HttpSimpleClient.HttpResult result = null;

        try {
            result = this.agent.httpDelete(url, (List)null, params, this.encode, 3000L);
        } catch (IOException var9) {
            LOGGER.warn("[remove] error, " + dataId + ", " + group + ", " + tenant + ", msg: " + var9.toString());
            return false;
        }

        if (200 == result.code) {
            LOGGER.info("[{}] [remove] ok, dataId={}, group={}, tenant={}", new Object[]{this.agent.getName(), dataId, group, tenant});
            return true;
        } else if (403 == result.code) {
            LOGGER.warn("[{}] [remove] error, dataId={}, group={}, tenant={}, code={}, msg={}", new Object[]{this.agent.getName(), dataId, group, tenant, result.code, result.content});
            throw new NacosException(result.code, result.content);
        } else {
            LOGGER.warn("[{}] [remove] error, dataId={}, group={}, tenant={}, code={}, msg={}", new Object[]{this.agent.getName(), dataId, group, tenant, result.code, result.content});
            return false;
        }
    }

    /**
     * 修改方法, 新增配置文件类型参数 type
     */
    private boolean publishConfigInner(String tenant, String dataId, String group, String tag, String appName, String betaIps, String content, String type) throws NacosException {
        group = this.null2defaultGroup(group);
        ParamUtils.checkParam(dataId, group, content);
        ConfigRequest cr = new ConfigRequest();
        cr.setDataId(dataId);
        cr.setTenant(tenant);
        cr.setGroup(group);
        cr.setContent(content);
        this.configFilterChainManager.doFilter(cr, (IConfigResponse)null);
        content = cr.getContent();
        String url = "/v1/cs/configs";
        List<String> params = new ArrayList();
        params.add("dataId");
        params.add(dataId);
        params.add("group");
        params.add(group);
        params.add("content");
        params.add(content);
        if (StringUtils.isNotEmpty(tenant)) {
            params.add("tenant");
            params.add(tenant);
        }

        if (StringUtils.isNotEmpty(appName)) {
            params.add("appName");
            params.add(appName);
        }

        if (StringUtils.isNotEmpty(tag)) {
            params.add("tag");
            params.add(tag);
        }

        if (StringUtils.isNotEmpty(type)) {
            params.add("type");
            params.add(type);
        }

        List<String> headers = new ArrayList();
        if (StringUtils.isNotEmpty(betaIps)) {
            headers.add("betaIps");
            headers.add(betaIps);
        }

        HttpSimpleClient.HttpResult result = null;

        try {
            result = this.agent.httpPost(url, headers, params, this.encode, 3000L);
        } catch (IOException var14) {
            LOGGER.warn("[{}] [publish-single] exception, dataId={}, group={}, msg={}", new Object[]{this.agent.getName(), dataId, group, var14.toString()});
            return false;
        }

        if (200 == result.code) {
            LOGGER.info("[{}] [publish-single] ok, dataId={}, group={}, tenant={}, config={}", new Object[]{this.agent.getName(), dataId, group, tenant, ContentUtils.truncateContent(content)});
            return true;
        } else if (403 == result.code) {
            LOGGER.warn("[{}] [publish-single] error, dataId={}, group={}, tenant={}, code={}, msg={}", new Object[]{this.agent.getName(), dataId, group, tenant, result.code, result.content});
            throw new NacosException(result.code, result.content);
        } else {
            LOGGER.warn("[{}] [publish-single] error, dataId={}, group={}, tenant={}, code={}, msg={}", new Object[]{this.agent.getName(), dataId, group, tenant, result.code, result.content});
            return false;
        }
    }

    /**
     * 新增方法
     */
    private boolean publishConfigInner(String tenant, String dataId, String group, String tag, String appName, String betaIps, String content) throws NacosException
    {
        return publishConfigInner(tenant, dataId, group, tag, appName, betaIps, content, null);
    }

    public String getServerStatus() {
        return this.worker.isHealthServer() ? "UP" : "DOWN";
    }

    /**
     * 新增方法
     */
    @Override
    public String getConfig(String namespace, String dataId, String group, long timeoutMs) throws NacosException
    {
        return this.getConfigInner(namespace == null ? this.namespace : namespace.trim(), dataId, group, timeoutMs);
    }

    /**
     * 新增方法
     */
    public boolean publishConfig(String namespace, String dataId, String group, String content, String appName, String type) throws NacosException
    {
        return this.publishConfigInner(namespace == null ? this.namespace : namespace.trim(), dataId, group, (String) null, appName, (String) null, content, type);
    }
}