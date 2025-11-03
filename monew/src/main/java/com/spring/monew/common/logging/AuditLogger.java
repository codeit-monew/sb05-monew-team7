package com.spring.monew.common.logging;

import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditLogger {

    public void logSoftDelete(UUID articleId, UUID userId, String requestId) {
        log.info("AUDIT [SOFT_DELETE] articleId={} userId={} requestId={} status=SUCCESS",
                articleId, userId, requestId);
    }

    public void logSoftDeleteFailure(UUID articleId, UUID userId, String requestId, String reason) {
        log.warn("AUDIT [SOFT_DELETE] articleId={} userId={} requestId={} status=FAILURE reason={}",
                articleId, userId, requestId, reason);
    }

    public void logHardDelete(UUID articleId, UUID userId, String requestId, int commentsDeleted, int viewsDeleted) {
        log.info("AUDIT [HARD_DELETE] articleId={} userId={} requestId={} commentsDeleted={} viewsDeleted={} status=SUCCESS",
                articleId, userId, requestId, commentsDeleted, viewsDeleted);
    }

    public void logHardDeleteFailure(UUID articleId, UUID userId, String requestId, String reason) {
        log.warn("AUDIT [HARD_DELETE] articleId={} userId={} requestId={} status=FAILURE reason={}",
                articleId, userId, requestId, reason);
    }

    public void logRestore(Instant fromDate, Instant toDate, int restoredCount, UUID userId, String requestId) {
        log.info("AUDIT [RESTORE] fromDate={} toDate={} restoredCount={} userId={} requestId={} status=SUCCESS",
                fromDate, toDate, restoredCount, userId, requestId);
    }

    public void logRestoreFailure(Instant fromDate, Instant toDate, UUID userId, String requestId, String reason) {
        log.warn("AUDIT [RESTORE] fromDate={} toDate={} userId={} requestId={} status=FAILURE reason={}",
                fromDate, toDate, userId, requestId, reason);
    }

    public void logBackupCreation(UUID articleId, String backupKey, String requestId) {
        log.info("AUDIT [BACKUP_CREATE] articleId={} backupKey={} requestId={} status=SUCCESS",
                articleId, backupKey, requestId);
    }

    public void logBackupCreationFailure(UUID articleId, String requestId, String reason) {
        log.warn("AUDIT [BACKUP_CREATE] articleId={} requestId={} status=FAILURE reason={}",
                articleId, requestId, reason);
    }
}
