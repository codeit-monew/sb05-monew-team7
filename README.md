# 📰 모뉴: 흩어진 뉴스를 한 곳에서
[![dev Server CI/CD](https://github.com/codeit-monew/sb05-monew-team7/actions/workflows/backend-dev-server.yml/badge.svg)](https://github.com/codeit-monew/sb05-monew-team7/actions/workflows/backend-dev-server.yml)

## 프로젝트 개요
다수의 뉴스 API(네이버, 연합뉴스, 조선일보, 한국경제)를 통합하여 사용자의 관심사에 맞춘 뉴스를 제공하고, 댓글 및 소셜 기능을 통해 사용자 간 상호작용이 가능한 통합형 뉴스 플랫폼입니다.

## 주요 특징
  -	뉴스 통합 제공: 네이버 OpenAPI와 주요 언론사 RSS를 연동하여 최신 뉴스 콘텐츠를 수집 및 제공
  -	소셜 기능: 댓글 및 의견 공유 기능을 통해 사용자 간 소통
  -	뉴스 기사 백업 및 복구 시스템

프로젝트 기간: 2025.10.20 ~ 2025.11.07

| 항목 | 내용 |
|------|------|
| **📄 협업 문서** | [Notion 페이지](https://sugared-macaroon-51d.notion.site/MONEW-7-29e9482652098081a86feca5c16624c6?source=copy_link) |
| **🔗 배포 링크** | [배포 링크](http://3.35.36.222:8080/) |
| **🎬 시연 영상** | [YouTube 시연 영상](https://youtu.be/IzG9F0YrPRg) |



### 팀원 구성
<table>
  <thead>
    <tr>
      <th style="width: 25%;">팀장/ 박유한</th>
      <th style="width: 25%;">김유민</th>
      <th style="width: 25%;">남현수</th>
      <th style="width: 25%;">조하람</th>
    </tr>
  </thead>
  <tbody>
<tr>
  <td>
    • 뉴스 기사 API 스프링 배치 처리<br>
    • AWS S3 연동<br>
    • 뉴스 기사 관리 모듈 개발<br>
    • CI/CD 파이프라인 구축
  </td>
  <td>
    • 활동 내역 관리 기능 개발<br>
    • 알림 관리 기능 개발<br>
    • MongoDB 설계
  </td>
  <td>
    • 관심사 관리 기능 개발<br>
    • 댓글 관리 기능 개발<br>
    • 시스템 모니터링 및 부하 테스트 수행
  </td>
  <td>
    • 사용자 관리 기능 개발<br>
    • 서버 인프라 구성 및 운영
  </td>
</tr>    <tr>
      <td><a href="https://github.com/yuhandemian">yuhandemianj</a></td>
      <td><a href="https://github.com/kimyumin03">kimyumin03</a></td>
      <td><a href="https://github.com/Namsoo315">Namsoo315</a></td>
      <td><a href="https://github.com/haram-jo">haram-jo</a></td>
    </tr>
  </tbody>
</table>

## 시스템 구성도

## 기술 스택

### 💻 Language

![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white)

### 🧩 Framework

![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Batch](https://img.shields.io/badge/Spring%20Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-37A64C?style=for-the-badge&logo=querydsl&logoColor=white)

### 🗄 Database

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)

### ⚡ Caching

![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

### ⚙ Build & CI/CD

![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/github%20actions-2671E5?style=for-the-badge&logo=githubactions&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker%20Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

### ☁ Cloud & Infra

![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Amazon S3](https://img.shields.io/badge/Amazon%20S3-FF9900?style=for-the-badge&logo=amazons3&logoColor=white)
![Amazon ECR](https://img.shields.io/badge/Amazon%20ECR-FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Amazon ECS](https://img.shields.io/badge/Amazon%20ECS-FF9900?style=for-the-badge&logo=amazonecs&logoColor=white)

### 📈 Monitoring & Docs

![Spring Actuator](https://img.shields.io/badge/Spring%20Actuator-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

### 🧪 테스트 & 품질 관리

![Junit](https://img.shields.io/badge/JUnit-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-EA2C6E?style=for-the-badge&logo=mockito&logoColor=white)
![JaCoCo](https://img.shields.io/badge/JaCoCo-D32F2F?style=for-the-badge&logo=jacoco&logoColor=white)
![CodeRabbit](https://img.shields.io/badge/CodeRabbit-FF070A?style=for-the-badge&logoColor=white)

### 🛠 기타 도구

![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)

## 팀원별 구현 기능 상세

### 박유한
뉴스 기사 API 스프링 배치 처리, AWS S3 연동, 뉴스 기사 관리 모듈 개발 및 CI/CD 파이프라인 구축

### 김유민
활동 내역 관리 기능, 알림 관리 기능 개발, MongoDB 설계

### 남현수
관심사 관리 기능, 댓글 관리 기능 개발, 시스템 모니터링 및 부하 테스트 수행

### 조하람
사용자 관리 기능 개발, 서버 인프라 구성 및 운영

## 파일 구조
com.spring.monew
 ┣ activity
 ┃ ┣ controller
 ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┗ Test.java
 ┃ ┃ ┃ ┣ request
 ┃ ┃ ┃ ┃ ┗ Test.java
 ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃    ┣ CommentActivityDto.java
 ┃ ┃ ┃    ┣ CommentLikeActivityDto.java
 ┃ ┃ ┃    ┗ UserActivityDto.java
 ┃ ┃ ┗ UserActivityController.java
 ┃ ┣ domain
 ┃ ┃ ┣ ActivityArticleViewDoc.java
 ┃ ┃ ┣ ActivityCommentDoc.java
 ┃ ┃ ┣ ActivityCommentLikeDoc.java
 ┃ ┃ ┗ UserInterestSubscriptionDoc.java
 ┃ ┣ repository
 ┃ ┃ ┣ impl
 ┃ ┃ ┃ ┣ ActivitySyncRepositoryImpl.java
 ┃ ┃ ┃ ┗ UserActivityQueryRepositoryImpl.java
 ┃ ┃ ┣ ActivityArticleViewRepository.java
 ┃ ┃ ┣ ActivitySyncRepository.java
 ┃ ┃ ┗ UserActivityQueryRepository.java
 ┃ ┣ service
 ┃ ┃ ┣ impl
 ┃ ┃ ┃ ┗ UserActivityServiceImpl.java
 ┃ ┃ ┗ UserActivityService.java
 ┃ ┗ util
 ┃   ┗ ActivityMapper.java
 ┣ article
 ┃ ┣ client
 ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┗ ArticleCandidate.java
 ┃ ┃ ┣ NaverNewsApiClient.java
 ┃ ┃ ┗ RssFeedClient.java
 ┃ ┣ controller
 ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┗ Test.java
 ┃ ┃ ┃ ┣ request
 ┃ ┃ ┃ ┃ ┗ Test.java
 ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃    ┣ ArticleDto.java
 ┃ ┃ ┃    ┣ ArticleRestoreResultDto.java
 ┃ ┃ ┃    ┗ CursorPageResponseArticleDto.java
 ┃ ┃ ┗ ArticleController.java
 ┃ ┣ domain
 ┃ ┃ ┣ Article.java
 ┃ ┃ ┗ ArticleSource.java
 ┃ ┣ exception
 ┃ ┃ ┗ ArticleNotFoundException.java
 ┃ ┣ repository
 ┃ ┃ ┣ impl
 ┃ ┃ ┃ ┗ ArticleRepositoryCustomImpl.java
 ┃ ┃ ┣ ArticleRepository.java
 ┃ ┃ ┗ ArticleRepositoryCustom.java
 ┃ ┗ service
 ┃   ┣ impl
 ┃   ┃ ┗ ArticleServiceImpl.java
 ┃   ┗ ArticleService.java
 ┣ articleview
 ┃ ┣ controller
 ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┣ data/
 ┃ ┃ ┃ ┣ request/
 ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃    ┗ ArticleViewDto.java
 ┃ ┃ ┗ ArticleViewController.java
 ┃ ┣ domain
 ┃ ┃ ┗ ArticleView.java
 ┃ ┣ repository
 ┃ ┃ ┣ impl
 ┃ ┃ ┃ ┗ Test.java
 ┃ ┃ ┗ ArticleViewRepository.java
 ┃ ┗ service
 ┃   ┣ impl
 ┃   ┃ ┗ ArticleViewServiceImpl.java
 ┃   ┗ ArticleViewService.java
 ┣ auth
 ┃ ┣ config
 ┃ ┃ ┣ HeaderAuthFilter.java
 ┃ ┃ ┣ HeaderUserAuthentication.java
 ┃ ┃ ┗ SecurityConfig.java
 ┃ ┣ controller
 ┃ ┃ ┗ AuthController.java
 ┃ ┗ service
 ┃   ┗ AuthService.java
 ┣ backup
 ┃ ┣ dto
 ┃ ┃ ┗ ArticleBackupDto.java
 ┃ ┣ exception
 ┃ ┃ ┣ BackupNotFoundException.java
 ┃ ┃ ┗ S3ServiceException.java
 ┃ ┗ service
 ┃   ┣ impl
 ┃   ┃ ┣ S3BackupServiceImpl.java
 ┃   ┃ ┗ S3LogBackupServiceImpl.java
 ┃   ┣ LogBackupService.java
 ┃   ┗ S3BackupService.java
 ┣ batch
 ┃ ┣ config
 ┃ ┃ ┣ ArticleBackupBatchConfig.java
 ┃ ┃ ┣ BatchSkipListener.java
 ┃ ┃ ┣ LogBackupBatchConfig.java
 ┃ ┃ ┣ NewsCollectionJobConfig.java
 ┃ ┃ ┗ RestTemplateConfig.java
 ┃ ┣ controller
 ┃ ┃ ┗ BatchJobController.java
 ┃ ┣ dto
 ┃ ┃ ┗ response
 ┃ ┃    ┣ BatchJobExecutionResponse.java
 ┃ ┃    ┣ BatchJobTriggerResponse.java
 ┃ ┃    ┣ CleanupTriggerResponse.java
 ┃ ┃    ┗ StepStatistics.java
 ┃ ┣ exception
 ┃ ┃ ┣ BatchJobExceptionHandler.java
 ┃ ┃ ┗ BatchJobExecutionNotFoundException.java
 ┃ ┣ listener
 ┃ ┃ ┗ ArticleNotificationListener.java
 ┃ ┣ processor
 ┃ ┃ ┣ ArticleBackupProcessor.java
 ┃ ┃ ┗ ArticleCandidateProcessor.java
 ┃ ┣ reader
 ┃ ┃ ┗ ArticleCandidateReader.java
 ┃ ┣ scheduler
 ┃ ┃ ┣ ArticleBackupScheduler.java
 ┃ ┃ ┣ ArticleCleanupScheduler.java
 ┃ ┃ ┣ CommentCleanupScheduler.java
 ┃ ┃ ┣ LogBackupScheduler.java
 ┃ ┃ ┣ NewsCollectionScheduler.java
 ┃ ┃ ┗ UserCleanupScheduler.java
 ┃ ┣ service
 ┃ ┃ ┗ BatchJobService.java
 ┃ ┗ writer
 ┃   ┣ ArticleBackupWriter.java
 ┃   ┗ ArticleWriter.java
 ┣ comment
 ┃ ┣ controller
 ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┣ data/
 ┃ ┃ ┃ ┣ request
 ┃ ┃ ┃ ┃ ┣ CommentRegisterRequest.java
 ┃ ┃ ┃ ┃ ┗ CommentUpdateRequest.java
 ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃    ┣ CommentDto.java
 ┃ ┃ ┃    ┗ CursorPageResponseCommentDto.java
 ┃ ┃ ┗ CommentController.java
 ┃ ┣ domain
 ┃ ┃ ┗ Comment.java
 ┃ ┣ repository
 ┃ ┃ ┣ impl
 ┃ ┃ ┃ ┗ CommentRepositoryCustomImpl.java
 ┃ ┃ ┣ CommentRepository.java
 ┃ ┃ ┗ CommentRepositoryCustom.java
 ┃ ┗ service
 ┃   ┣ impl
 ┃   ┃ ┗ CommentServiceImpl.java
 ┃   ┗ CommentService.java
 ┣ commentlike
 ┃ ┣ controller
 ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┣ data/
 ┃ ┃ ┃ ┣ request/
 ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃    ┗ CommentLikeDto.java
 ┃ ┃ ┗ CommentLikeController.java
 ┃ ┣ domain
 ┃ ┃ ┗ CommentLike.java
 ┃ ┣ repository
 ┃ ┃ ┣ impl
 ┃ ┃ ┃ ┗ Test.java
 ┃ ┃ ┗ CommentLikeRepository.java
 ┃ ┗ service
 ┃   ┣ impl
 ┃   ┃ ┗ CommentLikeServiceImpl.java
 ┃   ┗ CommentLikeService.java
 ┣ common
 ┃ ┣ config
 ┃ ┃ ┣ converter
 ┃ ┃ ┃ ┗ StringToInstantConverter.java
 ┃ ┃ ┣ AwsS3Config.java
 ┃ ┃ ┣ HibernateFilterAspect.java
 ┃ ┃ ┣ MongoConfig.java
 ┃ ┃ ┣ QuerydslConfig.java
 ┃ ┃ ┗ RedisConfig.java
 ┃ ┣ converter
 ┃ ┃ ┗ KeywordsConverter.java
 ┃ ┣ exception
 ┃ ┃ ┗ GlobalExceptionHandler.java
 ┃ ┣ filter
 ┃ ┃ ┗ RequestIdFilter.java
 ┃ ┣ logging
 ┃ ┃ ┗ AuditLogger.java
 ┃ ┗ util
 ┃   ┗ RequestUserExtractor.java
 ┣ data
 ┃ ┣ config
 ┃ ┃ ┗ Test.java
 ┃ ┗ storage
 ┃   ┗ Test.java
 ┣ interest
 ┃ ┣ controller
 ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┣ data/
 ┃ ┃ ┃ ┣ request
 ┃ ┃ ┃ ┃ ┣ InterestRegisterRequest.java
 ┃ ┃ ┃ ┃ ┗ InterestUpdateRequest.java
 ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃    ┣ CursorPageResponseInterestDto.java
 ┃ ┃ ┃    ┗ InterestDto.java
 ┃ ┃ ┗ InterestController.java
 ┃ ┣ domain
 ┃ ┃ ┗ Interest.java
 ┃ ┣ repository
 ┃ ┃ ┣ impl
 ┃ ┃ ┃ ┗ InterestRepositoryCustom.java
 ┃ ┃ ┗ InterestRepository.java
 ┃ ┗ service
 ┃   ┣ impl
 ┃   ┃ ┗ InterestServiceImpl.java
 ┃   ┗ InterestService.java
 ┣ notification
 ┃ ┣ controller
 ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┣ data/
 ┃ ┃ ┃ ┣ request/
 ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃    ┣ BulkConfirmResultDto.java
 ┃ ┃ ┃    ┣ CursorPageResponseNotificationDto.java
 ┃ ┃ ┃    ┣ NotificationConfirmResponseDto.java
 ┃ ┃ ┃    ┗ NotificationDto.java
 ┃ ┃ ┗ NotificationController.java
 ┃ ┣ domain
 ┃ ┃ ┣ Notification.java
 ┃ ┃ ┗ NotificationResourceType.java
 ┃ ┣ repository
 ┃ ┃ ┣ impl
 ┃ ┃ ┃ ┗ NotificationRepositoryImpl.java
 ┃ ┃ ┣ NotificationRepository.java
 ┃ ┃ ┗ NotificationRepositoryCustom.java
 ┃ ┣ scheduler
 ┃ ┃ ┗ NotificationCleanupScheduler.java
 ┃ ┗ service
 ┃   ┗ NotificationService.java

 ┣ subscription
 ┃ ┣ controller
 ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┣ data/
 ┃ ┃ ┃ ┣ request/
 ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃    ┗ SubscriptionDto.java
 ┃ ┃ ┗ SubscriptionController.java
 ┃ ┣ domain
 ┃ ┃ ┗ Subscription.java
 ┃ ┣ repository
 ┃ ┃ ┣ impl
 ┃ ┃ ┃ ┗ Test.java
 ┃ ┃ ┗ SubscriptionRepository.java
 ┃ ┗ service
 ┃   ┣ impl
 ┃   ┃ ┗ SubscriptionServiceImpl.java
 ┃   ┗ SubscriptionService.java
 ┣ user
 ┃ ┣ controller
 ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┗ UserDto.java
 ┃ ┃ ┃ ┗ request
 ┃ ┃ ┃    ┣ UserLoginRequest.java
 ┃ ┃ ┃    ┣ UserRegisterRequest.java
 ┃ ┃ ┃    ┗ UserUpdateRequest.java
 ┃ ┃ ┗ UserController.java
 ┃ ┣ domain
 ┃ ┃ ┣ User.java
 ┃ ┃ ┗ UserRole.java
 ┃ ┣ repository
 ┃ ┃ ┗ UserRepository.java
 ┃ ┗ service
 ┃   ┣ UserService.java
 ┃   ┗ UserServiceImpl.java
 ┗ MonewApplication.java



