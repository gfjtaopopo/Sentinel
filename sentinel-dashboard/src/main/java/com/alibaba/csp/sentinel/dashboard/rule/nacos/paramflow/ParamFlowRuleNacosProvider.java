package com.alibaba.csp.sentinel.dashboard.rule.nacos.paramflow;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.config.ConfigServiceFT;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.rule.properties.AppNacosRegisterProperties;
import com.alibaba.csp.sentinel.dashboard.rule.properties.NacosPropertiesConfiguration;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sentinel Dashboard 'Rule Push 模式' Nacos持久化改造 - fengt add
 *
 * @author taofe
 * @date 2021/10/13
 */
@Component("paramFlowRuleNacosProvider")
public class ParamFlowRuleNacosProvider implements DynamicRuleProvider<List<ParamFlowRuleEntity>> {

    @Autowired
    private ConfigServiceFT configServiceFT;
    @Autowired
    private Converter<String, List<ParamFlowRuleEntity>> converter;
    @Autowired
    private NacosPropertiesConfiguration nacosProperties;

    @Override
    public List<ParamFlowRuleEntity> getRules(String appName) throws Exception {

        Map<String, AppNacosRegisterProperties> apps = nacosProperties.getLocation();

        String rules = configServiceFT.getConfig(apps.containsKey(appName) ? apps.get(appName).getNamespace() : nacosProperties.getNamespace(),
                appName + NacosConfigUtil.PARAM_FLOW_DATA_ID_POSTFIX,
                apps.containsKey(appName) ? apps.get(appName).getGroupId() : nacosProperties.getGroupId(), 3000);

        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return converter.convert(rules);
    }
}
