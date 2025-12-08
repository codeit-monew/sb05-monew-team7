package com.spring.monew.backup.service;

import com.spring.monew.backup.dto.ArticleBackupDto;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface S3BackupService {
  void uploadBackup(LocalDate backupDate, List<ArticleBackupDto> articles);

  List<ArticleBackupDto> downloadBackup(LocalDate backupDate) throws IOException;

  boolean backupExists(LocalDate backupDate);
}
