package com.legalknowledge.common.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller 层 AOP 切面，统一记录入参/出参 + 耗时。
 * 后续审计功能直接在此扩展。
 */
@Aspect
@Component
public class ControllerLogAspect {

    private static final Logger log = LoggerFactory.getLogger(ControllerLogAspect.class);
    private static final ObjectMapper om = new ObjectMapper();
    private static final int MAX_LEN = 1500;

    private static final String[] SENSITIVE_FIELDS = {
            "password", "refreshToken", "accessToken", "api-key"
    };

    /** 拦截所有 Controller public 方法 */
    @Pointcut("execution(public * com.legalknowledge.controller..*.*(..))")
    public void controllerMethod() {}

    @Around("controllerMethod()")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();

        String httpMethod = getHttpMethod();
        String uri = getRequestUri();
        String signature = jp.getSignature().toShortString();

        // ── 入参 ──
        Map<String, Object> params = extractParams(jp);
        log.info(">> {} {} | args={}",
                httpMethod != null ? httpMethod : "?",
                uri != null ? uri : signature,
                sanitize(toJson(params)));

        // ── 执行 ──
        Object result;
        try {
            result = jp.proceed();
        } catch (Throwable e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("<< {} {} | ERROR({}ms) | exception={}",
                    httpMethod, uri != null ? uri : signature,
                    elapsed, e.getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }

        // ── 出参 ──
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > 3000) {
            log.warn("<< {} {} | SLOW({}ms) | result={}",
                    httpMethod, uri != null ? uri : signature,
                    elapsed, sanitize(truncate(toJson(result))));
        } else {
            log.info("<< {} {} | OK({}ms)",
                    httpMethod, uri != null ? uri : signature, elapsed);
        }

        return result;
    }

    /** 从 JoinPoint 提取参数名和值 */
    private Map<String, Object> extractParams(ProceedingJoinPoint jp) {
        Map<String, Object> map = new LinkedHashMap<>();
        MethodSignature ms = (MethodSignature) jp.getSignature();
        String[] names = ms.getParameterNames();
        Object[] values = jp.getArgs();

        for (int i = 0; i < names.length; i++) {
            Object v = values[i];
            // 跳过 HttpServletRequest / HttpServletResponse 等
            if (v instanceof HttpServletRequest) continue;
            if (v instanceof jakarta.servlet.http.HttpServletResponse) continue;
            map.put(names[i], v);
        }
        return map;
    }

    private String getHttpMethod() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest().getMethod() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getRequestUri() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest req = attrs.getRequest();
            String qs = req.getQueryString();
            return req.getRequestURI() + (qs != null ? "?" + qs : "");
        } catch (Exception e) {
            return null;
        }
    }

    /** 脱敏 */
    private String sanitize(String json) {
        for (String field : SENSITIVE_FIELDS) {
            json = json.replaceAll("\"" + field + "\"\\s*:\\s*\"[^\"]*\"", "\"" + field + "\":\"***\"");
        }
        return json;
    }

    private String truncate(String s) {
        if (s == null) return "null";
        if (s.length() <= MAX_LEN) return s;
        return s.substring(0, MAX_LEN) + "...(truncated " + (s.length() - MAX_LEN) + " chars)";
    }

    private String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return (String) obj;
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        try {
            return om.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}
