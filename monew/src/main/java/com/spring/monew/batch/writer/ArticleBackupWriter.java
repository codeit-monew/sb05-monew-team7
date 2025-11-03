package com.spring.monew.batch.writer;

import com.spring.monew.backup.dto.ArticleBackupDto;
import com.spring.monew.backup.service.S3BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleBackupWriter implements ItemWriter<ArticleBackupDto> {

    private final S3BackupService s3BackupService;
    private static final String BACKUP_COUNT_KEY = "backupCount";

    @Override
    public void write(Chunk<? extends ArticleBackupDto> chunk) {
        List<ArticleBackupDto> articles = chunk.getItems().stream()
            .map(dto -> (ArticleBackupDto) dto)
            .filter(dto -> dto != null)
            .toList();

        if (articles.isEmpty()) {
            return;
        }

        LocalDate backupDate = LocalDate.now();
        s3BackupService.uploadBackup(backupDate, articles);

        var ctx = StepSynchronizationManager.getContext();
        if (ctx != null && ctx.getStepExecution() != null) {
            var ec = ctx.getStepExecution().getExecutionContext();
            Integer currentCount = (Integer) ec.get(BACKUP_COUNT_KEY);
            ec.put(BACKUP_COUNT_KEY, (currentCount != null ? currentCount : 0) + articles.size());
        }

        log.info("청크 백업 완료: {} 개의 기사", articles.size());
    }
}
