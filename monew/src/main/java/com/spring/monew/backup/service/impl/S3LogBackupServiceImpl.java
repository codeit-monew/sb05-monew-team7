package com.spring.monew.backup.service.impl;

import com.spring.monew.backup.exception.S3ServiceException;
import com.spring.monew.backup.service.LogBackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
public class S3LogBackupServiceImpl implements LogBackupService {

  private final S3Client s3Client;
  private final String logBucketName;
  private final String logFilePath;
  private final String logFileName;

  private static final String KEY_PREFIX = "logs/";
  private static final String FILE_EXTENSION = ".log.gz";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public S3LogBackupServiceImpl(
      S3Client s3Client,
      @Value("${aws.s3.log-bucket}") String logBucketName,
      @Value("${logging.file.path}") String logFilePath,
      @Value("${logging.file.name}") String logFileName
  ) {
    this.s3Client = s3Client;
    this.logBucketName = logBucketName;
    this.logFilePath = logFilePath;
    this.logFileName = logFileName;
  }

  @Override
  public void uploadLogFile(LocalDate logDate) {
    String logFilePathStr = generateLogFilePath(logDate);
    Path logFile = Paths.get(logFilePathStr);

    if (!Files.exists(logFile)) {
      log.warn("로그 파일이 존재하지 않습니다: {}", logFilePathStr);
      return;
    }

    String s3Key = generateS3Key(logDate);

    try {
      byte[] logBytes = Files.readAllBytes(logFile);
      byte[] compressedBytes = compressGzip(logBytes);

      PutObjectRequest putRequest = PutObjectRequest.builder()
          .bucket(logBucketName)
          .key(s3Key)
          .contentType("application/gzip")
          .build();

      s3Client.putObject(putRequest, RequestBody.fromBytes(compressedBytes));

      log.info("S3에 로그 파일 업로드 성공: bucket={}, key={}, size={} bytes", 
          logBucketName, s3Key, compressedBytes.length);

      Files.delete(logFile);
      log.info("업로드 후 로컬 로그 파일 삭제 완료: {}", logFilePathStr);

    } catch (IOException e) {
      log.error("로그 파일 읽기 또는 압축 실패: {}", logFilePathStr, e);
      throw new S3ServiceException("로그 파일 업로드 처리 실패", e);
    } catch (S3Exception e) {
      log.error("로그 업로드 중 S3 오류 발생: bucket={}, key={}, statusCode={}", 
          logBucketName, s3Key, e.statusCode(), e);
      throw new S3ServiceException("로그 업로드 중 S3 서비스 오류", e);
    }
  }

  @Override
  public void cleanupLocalLogs(int daysToKeep) {
    Path logDir = Paths.get(logFilePath);

    if (!Files.exists(logDir) || !Files.isDirectory(logDir)) {
      log.warn("로그 디렉토리가 존재하지 않습니다: {}", logFilePath);
      return;
    }

    LocalDate cutoffDate = LocalDate.now().minusDays(daysToKeep);

    try (Stream<Path> files = Files.list(logDir)) {
      files.filter(path -> path.toString().endsWith(".log"))
          .filter(path -> {
            String fileName = path.getFileName().toString();
            LocalDate fileDate = extractDateFromFileName(fileName);
            return fileDate != null && fileDate.isBefore(cutoffDate);
          })
          .forEach(path -> {
            try {
              Files.delete(path);
              log.info("오래된 로그 파일 삭제 완료: {}", path);
            } catch (IOException e) {
              log.error("오래된 로그 파일 삭제 실패: {}", path, e);
            }
          });
    } catch (IOException e) {
      log.error("로그 디렉토리 조회 실패: {}", logFilePath, e);
    }
  }

  @Override
  public boolean logFileExists(LocalDate logDate) {
    String logFilePathStr = generateLogFilePath(logDate);
    return Files.exists(Paths.get(logFilePathStr));
  }

  private String generateLogFilePath(LocalDate logDate) {
    return Paths.get(logFilePath, logFileName + "-" + logDate.format(DATE_FORMATTER) + ".log").toString();
  }

  private String generateS3Key(LocalDate logDate) {
    return KEY_PREFIX + logDate.format(DATE_FORMATTER) + FILE_EXTENSION;
  }

  private byte[] compressGzip(byte[] data) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
    try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
      gzipStream.write(data);
    }
    return byteStream.toByteArray();
  }

  private LocalDate extractDateFromFileName(String fileName) {
    try {
      int dateStart = fileName.lastIndexOf('-');
      int extensionIndex = fileName.lastIndexOf(".log");
      if (dateStart < 0 || extensionIndex < 0 || dateStart >= extensionIndex) {
        return null;
      }
      String dateStr = fileName.substring(dateStart + 1, extensionIndex);
      return LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (Exception e) {
      return null;
    }
  }
}
