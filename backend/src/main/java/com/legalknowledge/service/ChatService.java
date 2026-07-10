package com.legalknowledge.service;

import com.alibaba.ttl.TtlRunnable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private static final String SYSTEM_PROMPT =
            "你是一个专业的法律知识咨询助手。你有丰富的中国法律知识，能够用通俗易懂的语言解答法律问题。" +
            "回答问题时请注意：\n" +
            "1. 用普通人能理解的语言解释法律概念\n" +
            "2. 涉及具体法条时，尽量引用原文并注明出处\n" +
            "3. 如果不确定，请如实告知，不要编造法律条文\n" +
            "4. 提醒用户：本回答仅供参考，重大法律问题请咨询专业律师";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public ChatService(
            @Value("${deepseek.api-key}") String apiKey,
            @Value("${deepseek.api-url:https://api.deepseek.com/v1/chat/completions}") String apiUrl,
            @Value("${deepseek.model:deepseek-chat}") String model,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5 * 60 * 1000); // 5 分钟
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 将用户对话历史发送给 DeepSeek，流式返回回答
     */
    public SseEmitter chat(List<Map<String, String>> messages) {
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        // 构建消息列表：系统提示 + 对话历史
        List<Map<String, String>> fullMessages = new ArrayList<>();
        fullMessages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        fullMessages.addAll(messages);

        // 构建请求体
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", fullMessages);
        body.put("stream", true);
        body.put("temperature", 0.7);
        body.put("max_tokens", 2048);

        log.info("DeepSeek API 请求开始, model={}, messages count={}", model, fullMessages.size());

        // TtlRunnable.get() 捕获父线程的 MDC，子线程日志自动带 traceId
        Runnable task = TtlRunnable.get(() -> {
            try {
                // 先用 try 请求一遍，如果抛异常，读取响应体拿到错误信息
                restTemplate.execute(apiUrl, HttpMethod.POST,
                        request -> {
                            request.getHeaders().setBearerAuth(apiKey);
                            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            objectMapper.writeValue(request.getBody(), body);
                        },
                        response -> {
                            // 这里只会进入 2xx 的响应
                            log.info("DeepSeek API 连接成功, status={}", response.getStatusCode());
                            try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (line.startsWith("data: ")) {
                                        String data = line.substring(6).trim();
                                        if ("[DONE]".equals(data)) {
                                            break;
                                        }
                                        emitter.send(SseEmitter.event()
                                                .name("message")
                                                .data(data));
                                    }
                                }
                            } catch (IOException e) {
                                log.error("读取 SSE 流 IO 异常", e);
                            }
                            emitter.complete();
                            log.info("DeepSeek API 流式返回结束");
                            return null;
                        });
            } catch (Exception e) {
                log.error("DeepSeek API 调用失败: {}", e.getMessage(), e);
                try {
                    String errorJson = objectMapper.writeValueAsString(
                            Map.of("error", e.getMessage()));
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(errorJson));
                    emitter.completeWithError(e);
                } catch (Exception inner) {
                    log.error("发送错误 SSE 事件也失败了", inner);
                    emitter.completeWithError(inner);
                }
            }
        });
        new Thread(task).start();

        return emitter;
    }
}
