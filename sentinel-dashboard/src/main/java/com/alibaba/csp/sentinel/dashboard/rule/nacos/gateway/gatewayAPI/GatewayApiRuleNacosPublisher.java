/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.rule.nacos.gateway.gatewayAPI;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.config.ConfigServiceFT;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.rule.properties.AppNacosRegisterProperties;
import com.alibaba.csp.sentinel.dashboard.rule.properties.NacosPropertiesConfiguration;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.config.ConfigType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@Component("gatewayApiRuleNacosPublisher")
public class GatewayApiRuleNacosPublisher implements DynamicRulePublisher<List<ApiDefinitionEntity>> {

    @Autowired
    private ConfigServiceFT configServiceFT;
    @Autowired
    private Converter<List<ApiDefinitionEntity>, String> converter;
    @Autowired
    private NacosPropertiesConfiguration nacosProperties;

    @Override
    public void publish(String app, List<ApiDefinitionEntity> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }

        Map<String, AppNacosRegisterProperties> apps = nacosProperties.getLocation();

        configServiceFT.publishConfig(apps.containsKey(app) ? apps.get(app).getNamespace() : nacosProperties.getNamespace(),
                app + NacosConfigUtil.GATEWAY_API_GROUP_DATA_ID_POSTFIX,
                apps.containsKey(app) ? apps.get(app).getGroupId() : nacosProperties.getGroupId(),
                converter.convert(rules),
                app, ConfigType.JSON.getType());
    }
}