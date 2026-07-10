package com.legalknowledge.common.filter;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StringMap;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Log4j2 + TTL 桥接适配器。
 *
 * 原理：
 *   Log4j2 的 ThreadContext 默认用 ThreadLocal 存 MDC。
 *   本适配器替换成 TransmittableThreadLocal，
 *   使得 MDC 值在 new Thread()、线程池等场景下自动透传。
 *
 * 生效方式：
 *   log4j2.component.properties 中配置
 *   log4j2.threadContextMap=com.legalknowledge.config.TtlMdcAdapter
 */
@Component
public class TtlMdcAdapter implements ThreadContextMap {

    private static final Logger log = LoggerFactory.getLogger(TtlMdcAdapter.class);

    /** 用 TTL 取代普通 ThreadLocal */
    private static final TransmittableThreadLocal<Map<String, String>> context =
            new TransmittableThreadLocal<Map<String, String>>() {
                @Override
                protected Map<String, String> initialValue() {
                    return new HashMap<>();
                }

                /**
                 * 关键：子线程从父线程 Copy 时，深拷贝一份，
                 * 避免子线程的 put/remove 影响父线程。
                 */
                @Override
                public Map<String, String> copy(Map<String, String> parentValue) {
                    return new HashMap<>(parentValue);
                }
            };

    private static Map<String, String> getMap() {
        return context.get();
    }

    @Override
    public void clear() {
        getMap().clear();
    }

    @Override
    public boolean containsKey(String key) {
        return getMap().containsKey(key);
    }

    @Override
    public String get(String key) {
        return getMap().get(key);
    }

    @Override
    public boolean isEmpty() {
        return getMap().isEmpty();
    }

    @Override
    public void put(String key, String value) {
        getMap().put(key, value);
    }

    @Override
    public void remove(String key) {
        getMap().remove(key);
    }

    @Override
    public Map<String, String> getCopy() {
        return new HashMap<>(getMap());
    }

    @Override
    public Map<String, String> getImmutableMapOrNull() {
        Map<String, String> map = getMap();
        return map.isEmpty() ? null : Map.copyOf(map);
    }

//    @Override
//    public StringMap getReadOnlyContextData() {
//        // 适配新版本 Log4j2 接口
//        return null;
//    }
//
//    @Override
//    public void putAll(Map<String, String> map) {
//        getMap().putAll(map);
//    }
}
