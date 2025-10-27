package com.spring.monew.article.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BatchSkipListener implements SkipListener<Object, Object> {

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("읽기 중 Skip 발생: {}", t.getMessage(), t);
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        log.warn("처리 중 Skip 발생 - Item: {}, Error: {}", item, t.getMessage(), t);
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.warn("쓰기 중 Skip 발생 - Item: {}, Error: {}", item, t.getMessage(), t);
    }
}
