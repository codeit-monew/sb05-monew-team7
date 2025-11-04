package com.spring.monew.backup.exception;

import java.time.LocalDate;

public class BackupNotFoundException extends RuntimeException {
  private final LocalDate backupDate;

  public BackupNotFoundException(LocalDate backupDate) {
    super("Backup not found for date: " + backupDate);
    this.backupDate = backupDate;
  }

  public BackupNotFoundException(LocalDate backupDate, Throwable cause) {
    super("Backup not found for date: " + backupDate, cause);
    this.backupDate = backupDate;
  }

  public LocalDate getBackupDate() {
    return backupDate;
  }
}
