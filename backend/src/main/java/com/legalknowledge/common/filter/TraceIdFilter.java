package com.legalknowledge.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 请求入口：生成 traceId 放入 MDC，响应头返回给前端。
 * Order=1 确保在所有 Filter 之前执行，其他 Filter 的日志能带上 traceId。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String HEADER_NAME = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 优先从请求头取（网关或上游传过来的），没有则新生成
        String traceId = request.getHeader(HEADER_NAME);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        // ❶ 放入 MDC：当前线程 + TTL-aware 子线程自动透传
        MDC.put(TRACE_ID, traceId);

        // ❷ 返回给前端，方便用户反馈时提供
        response.setHeader(HEADER_NAME, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // ❸ 请求结束必须清理，避免内存泄露（线程池复用时污染下次请求）
            MDC.remove(TRACE_ID);
        }
    }
}
