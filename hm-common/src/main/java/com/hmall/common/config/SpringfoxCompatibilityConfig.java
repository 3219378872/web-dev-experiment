package com.hmall.common.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Springfox 2.10.5 与 Spring Boot 2.7 的兼容补丁。
 *
 * <p>Spring Boot 2.6+ 默认使用 {@code PathPatternParser} 进行请求路由匹配，而 actuator 端点的
 * {@code WebMvcEndpointHandlerMapping} 始终基于 {@code PathPattern}（不受
 * {@code spring.mvc.pathmatch.matching-strategy} 影响）。Springfox 2.10.5 在扫描这些映射时调用
 * {@code WebMvcPatternsRequestConditionWrapper.getPatterns()} 会得到 null 并抛出 NPE，导致启用
 * knife4j 的服务在加入 {@code spring-boot-starter-actuator}（healthcheck 所需）后启动崩溃。
 *
 * <p>本补丁在 springfox 处理前，从其 {@code WebMvcRequestHandlerProvider} 持有的 handlerMappings 中
 * 剔除使用 {@code PatternParser} 的映射（即 actuator 等 PathPattern 端点），仅保留 ant 风格映射。
 * 配合 {@code spring.mvc.pathmatch.matching-strategy=ant_path_matcher}（使应用控制器映射为 ant 风格、
 * patternParser 为 null）后，应用接口文档仍可正常生成，actuator 端点则被安全跳过。
 *
 * <p>仅在 classpath 存在 springfox 时生效；BeanPostProcessor 只作用于实际存在的
 * {@code WebMvcRequestHandlerProvider} bean，对未启用 knife4j 的服务为无操作。
 */
@Configuration
@ConditionalOnClass(WebMvcRequestHandlerProvider.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SpringfoxCompatibilityConfig {

    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
                List<T> copy = mappings.stream()
                        .filter(mapping -> Objects.isNull(mapping.getPatternParser()))
                        .collect(Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            @SuppressWarnings("unchecked")
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                if (field == null) {
                    return List.of();
                }
                field.setAccessible(true);
                return (List<RequestMappingInfoHandlerMapping>) ReflectionUtils.getField(field, bean);
            }
        };
    }
}
