package com.alibaba.csp.sentinel.dashboard.rule.nacos.authority;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.config.ConfigServiceFT;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.rule.properties.AppNacosRegisterProperties;
import com.alibaba.csp.sentinel.dashboard.rule.properties.NacosPropertiesConfiguration;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Sentinel Dashboard 'Rule Push 模式' Nacos持久化改造 - fengt add
 *
 * @author taofe
 * @date 2021/10/13
 */
@Component("authorityRuleNacosPublisher")
public class AuthorityRuleNacosPublisher implements DynamicRulePublisher<List<AuthorityRuleEntity>> {

    @Autowired
    private ConfigServiceFT configServiceFT;
    @Autowired
    private Converter<List<AuthorityRuleEntity>, String> converter;
    @Autowired
    private NacosPropertiesConfiguration nacosProperties;

    @Override
    public void publish(String app, List<AuthorityRuleEntity> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }

        Map<String, AppNacosRegisterProperties> apps = nacosProperties.getLocation();

        configServiceFT.publishConfig(apps.containsKey(app) ? apps.get(app).getNamespace() : nacosProperties.getNamespace(),
                app + NacosConfigUtil.AUTHORITY_DATA_ID_POSTFIX,
                apps.containsKey(app) ? apps.get(app).getGroupId() : nacosProperties.getGroupId(),
                converter.convert(rules));
    }
}
