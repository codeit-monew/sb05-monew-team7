package com.spring.monew.backup.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spring.monew.backup.dto.ArticleBackupDto;
import com.spring.monew.backup.exception.BackupNotFoundException;
import com.spring.monew.backup.exception.S3ServiceException;
import com.spring.monew.backup.service.S3BackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
public class S3BackupServiceImpl implements S3BackupService {

  private final S3Client s3Client;
  private final String bucketName;
  private final ObjectMapper objectMapper;

  private static final String KEY_PREFIX = "articles/";
  private static final String FILE_EXTENSION = ".json.gz";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public S3BackupServiceImpl(
      S3Client s3Client,
      @Value("${aws.s3.bucket}") String bucketName
  ) {
    this.s3Client = s3Client;
    this.bucketName = bucketName;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  @Override
  public void uploadBackup(LocalDate backupDate, List<ArticleBackupDto> articles) {
    String key = generateS3Key(backupDate);

    try {
      byte[] jsonBytes = objectMapper.writeValueAsBytes(articles);
      byte[] compressedBytes = compressGzip(jsonBytes);

      PutObjectRequest putRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .contentType("application/gzip")
          .build();

      s3Client.putObject(putRequest, RequestBody.fromBytes(compressedBytes));

      log.info("S3에 백업 업로드 성공: bucket={}, key={}, articles={}", 
          bucketName, key, articles.size());
    } catch (IOException e) {
      log.error("S3 백업 업로드 실패: bucket={}, key={}", bucketName, key, e);
      throw new S3ServiceException("S3 백업 업로드 실패", e);
    } catch (S3Exception e) {
      log.error("백업 업로드 중 S3 오류 발생: bucket={}, key={}, statusCode={}", 
          bucketName, key, e.statusCode(), e);
      throw new S3ServiceException("백업 업로드 중 S3 서비스 오류", e);
    }
  }

  @Override
  public List<ArticleBackupDto> downloadBackup(LocalDate backupDate) throws IOException {
    String key = generateS3Key(backupDate);

    try {
      GetObjectRequest getRequest = GetObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      byte[] compressedBytes = s3Client.getObjectAsBytes(getRequest).asByteArray();
      byte[] jsonBytes = decompressGzip(compressedBytes);

      List<ArticleBackupDto> articles = objectMapper.readValue(
          jsonBytes, 
          new TypeReference<List<ArticleBackupDto>>() {}
      );

      log.info("S3에서 백업 다운로드 성공: bucket={}, key={}, articles={}", 
          bucketName, key, articles.size());

      return articles;
    } catch (NoSuchKeyException e) {
      log.error("S3에서 백업을 찾을 수 없음: bucket={}, key={}", bucketName, key);
      throw new BackupNotFoundException(backupDate, e);
    } catch (S3Exception e) {
      log.error("백업 다운로드 중 S3 오류 발생: bucket={}, key={}, statusCode={}", 
          bucketName, key, e.statusCode(), e);
      throw new S3ServiceException("백업 다운로드 중 S3 서비스 오류", e);
    }
  }

  @Override
  public boolean backupExists(LocalDate backupDate) {
    String key = generateS3Key(backupDate);

    try {
      HeadObjectRequest headRequest = HeadObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      s3Client.headObject(headRequest);
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    } catch (S3Exception e) {
      log.error("백업 존재 여부 확인 중 S3 오류 발생: bucket={}, key={}, statusCode={}", 
          bucketName, key, e.statusCode(), e);
      return false;
    }
  }

  private String generateS3Key(LocalDate backupDate) {
    return KEY_PREFIX + backupDate.format(DATE_FORMATTER) + FILE_EXTENSION;
  }

  private byte[] compressGzip(byte[] data) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
    try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
      gzipStream.write(data);
    }
    return byteStream.toByteArray();
  }

  private byte[] decompressGzip(byte[] compressedData) throws IOException {
    try (ByteArrayInputStream byteStream = new ByteArrayInputStream(compressedData);
         GZIPInputStream gzipStream = new GZIPInputStream(byteStream);
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      
      byte[] buffer = new byte[1024];
      int len;
      while ((len = gzipStream.read(buffer)) > 0) {
        outputStream.write(buffer, 0, len);
      }
      return outputStream.toByteArray();
    }
  }
}
