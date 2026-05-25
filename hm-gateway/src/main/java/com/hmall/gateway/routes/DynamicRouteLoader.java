package com.hmall.gateway.routes;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executor;

@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final NacosConfigManager nacosConfigManager;

    private final String dataId = "gateway-routes.json";

    private final String group = "DEFAULT_GROUP";

    private final RouteDefinitionWriter routeDefinitionWriter;

    private final Set<String> routeIds = new HashSet<>();

    @PostConstruct
    public void initRouteConfig() throws NacosException {
        //项目启动时，拉取配置并添加监听器
        ConfigService configService = nacosConfigManager.getConfigService();
        String configInfo = configService.getConfigAndSignListener(dataId, group, 3000L, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                //监听到路由配置变更时，更新路由表
                updateRoutes(configInfo);
            }
        });
        //根据初始配置，初始化路由表
        updateRoutes(configInfo);
    }

    public void updateRoutes(String configInfo){
        log.info("监听到路由信息改变:{}",configInfo);
        //解析配置信息
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        //删除路由表
        if(!routeIds.isEmpty())
            for (String routeId : routeIds) {
                routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
            }
        routeIds.clear();
        //更新路由表
        for (RouteDefinition routeDefinition : routeDefinitions) {
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            //记录路由Id，便于更新时删除
            routeIds.add(routeDefinition.getId());
        }
    }
}
