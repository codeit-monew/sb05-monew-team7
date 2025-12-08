package com.spring.monew.backup.service;

import java.time.LocalDate;

public interface LogBackupService {
  void uploadLogFile(LocalDate logDate);
  
  void cleanupLocalLogs(int daysToKeep);
  
  boolean logFileExists(LocalDate logDate);
}
