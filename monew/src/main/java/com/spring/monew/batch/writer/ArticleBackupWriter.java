package com.spring.monew.batch.writer;

import com.spring.monew.backup.dto.ArticleBackupDto;
import com.spring.monew.backup.service.S3BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleBackupWriter implements ItemWriter<ArticleBackupDto>, StepExecutionListener {

    private final S3BackupService s3BackupService;
    private static final String BACKUP_COUNT_KEY = "backupCount";
    private final List<ArticleBackupDto> aggregatedArticles = new ArrayList<>();

    @Override
    public void write(Chunk<? extends ArticleBackupDto> chunk) {
        List<ArticleBackupDto> articles = chunk.getItems().stream()
            .map(dto -> (ArticleBackupDto) dto)
            .filter(dto -> dto != null)
            .toList();

        if (articles.isEmpty()) {
            log.warn("청크가 비어있음 - 건너뜀");
            return;
        }

        aggregatedArticles.addAll(articles);

        var ctx = StepSynchronizationManager.getContext();
        if (ctx != null && ctx.getStepExecution() != null) {
            var ec = ctx.getStepExecution().getExecutionContext();
            Integer currentCount = (Integer) ec.get(BACKUP_COUNT_KEY);
            ec.put(BACKUP_COUNT_KEY, (currentCount != null ? currentCount : 0) + articles.size());
        }

        log.info("청크 처리 완료: {} 개의 기사 (누적: {}개)", articles.size(), aggregatedArticles.size());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("afterStep 호출됨 - aggregatedArticles 크기: {}", aggregatedArticles.size());
        
        if (!aggregatedArticles.isEmpty()) {
            LocalDate backupDate = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
            log.info("S3 업로드 시작: backupDate={}, 기사 수={}", backupDate, aggregatedArticles.size());
            s3BackupService.uploadBackup(backupDate, new ArrayList<>(aggregatedArticles));
            log.info("S3 백업 완료: {} 개의 기사", aggregatedArticles.size());
            aggregatedArticles.clear();
        } else {
            log.warn("백업할 기사가 없음 - S3 업로드 건너뜀");
        }
        
        return ExitStatus.COMPLETED;
    }
}
