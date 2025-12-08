package com.spring.monew.interest.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.interest.controller.dto.request.InterestRegisterRequest;
import com.spring.monew.interest.controller.dto.request.InterestUpdateRequest;
import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.interest.service.InterestService;
import com.spring.monew.interest.service.impl.InterestServiceImpl;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

  @Mock
  private InterestRepository interestRepository;

  @Mock
  private ArticleRepository articleRepository;

  @InjectMocks
  private InterestServiceImpl interestService;

  private Interest interest;

  @BeforeEach
  void setUp() {
    interest = new Interest("테스트", List.of("야구", "축구"));
  }

  @Test
  @DisplayName("관심사 등록 성공")
  void addInterest_success() {
    InterestRegisterRequest request = new InterestRegisterRequest("테스트", List.of("야구", "축구"));

    given(interestRepository.existsByName(anyString())).willReturn(false);
    given(interestRepository.findSimilarNames(anyString(), anyDouble())).willReturn(List.of());
    given(interestRepository.save(any(Interest.class))).willReturn(interest);

    InterestDto result = interestService.addInterest(request);

    assertThat(result.name()).isEqualTo("테스트");
    assertThat(result.keywords()).containsExactly("야구", "축구");
    verify(interestRepository).save(any(Interest.class));
  }

  @Test
  @DisplayName("같은 이름 존재 시 예외 발생")
  void addInterest_duplicateName_throwsException() {
    InterestRegisterRequest request = new InterestRegisterRequest("테스트", List.of("야구", "축구"));
    given(interestRepository.existsByName("테스트")).willReturn(true);

    assertThatThrownBy(() -> interestService.addInterest(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("같은 이름이 존재합니다.");
  }

  @Test
  @DisplayName("유사 이름 존재 시 예외 발생")
  void addInterest_similarName_throwsException() {
    InterestRegisterRequest request = new InterestRegisterRequest("테스트", List.of("야구"));
    given(interestRepository.existsByName(anyString())).willReturn(false);
    given(interestRepository.findSimilarNames(anyString(), anyDouble()))
        .willReturn(List.of("테스투", "테스터"));

    assertThatThrownBy(() -> interestService.addInterest(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("유사도가 높은 이름이 존재합니다.");
  }

  @Test
  @DisplayName("커서 기반 조회 성공")
  void getInterests_success() {
    CursorPageResponseInterestDto response = new CursorPageResponseInterestDto(
        List.of(new InterestDto(UUID.randomUUID(), "테스트", List.of("야구"), 0L, false, Instant.now())),
        "cursorValue",
        Instant.now(),
        1,
        1L,
        false
    );
    given(interestRepository.findCursorPagedInterests(
        any(), any(), any(), any(), any(), anyInt(), any())).willReturn(response);

    CursorPageResponseInterestDto result = interestService.getInterests(
        "테스트", "name", "ASC", null, null, 10, UUID.randomUUID());

    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).name()).isEqualTo("테스트");
  }

  @Test
  @DisplayName("관심사 수정 성공")
  void modifyInterest_success() {
    UUID interestId = UUID.randomUUID();
    InterestUpdateRequest updateRequest = new InterestUpdateRequest(List.of("테니스", "골프"));
    given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));

    InterestDto result = interestService.modifyInterest(interestId, updateRequest);

    assertThat(result.name()).isEqualTo("테스트");
    assertThat(result.keywords()).containsExactly("테니스", "골프");
  }

  @Test
  @DisplayName("존재하지 않으면 예외 발생")
  void modifyInterest_notFound_throwsException() {
    UUID interestId = UUID.randomUUID();
    given(interestRepository.findById(interestId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> interestService.modifyInterest(interestId,
        new InterestUpdateRequest( List.of("테니스"))))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessage("존재하지 않는 관심사입니다.");
  }

  @Test
  @DisplayName("관심사 삭제 성공 - 관련 게시글 없음")
  void removeInterest_success_noArticles() {
    UUID interestId = UUID.randomUUID();
    given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
    given(articleRepository.findAllByInterestId(interestId)).willReturn(List.of());

    interestService.removeInterest(interestId);

    verify(articleRepository).findAllByInterestId(interestId);
    verify(interestRepository).delete(interest);
  }

  @Test
  @DisplayName("관심사 삭제 성공 - 관련 게시글도 소프트 삭제")
  void removeInterest_success_withArticles() {
    UUID interestId = UUID.randomUUID();
    Article article1 = Article.of(interest, ArticleSource.NAVER, 
        "https://example.com/1", "Article 1", Instant.now(), "Summary 1");
    Article article2 = Article.of(interest, ArticleSource.NAVER, 
        "https://example.com/2", "Article 2", Instant.now(), "Summary 2");
    List<Article> relatedArticles = List.of(article1, article2);
    
    given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
    given(articleRepository.findAllByInterestId(interestId)).willReturn(relatedArticles);

    interestService.removeInterest(interestId);

    verify(articleRepository).findAllByInterestId(interestId);
    verify(articleRepository).deleteAll(relatedArticles);
    verify(interestRepository).delete(interest);
  }

  @Test
  @DisplayName("존재하지 않으면 예외 발생")
  void removeInterest_notFound_throwsException() {
    UUID interestId = UUID.randomUUID();
    given(interestRepository.findById(interestId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> interestService.removeInterest(interestId))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessage("존재하지 않는 관심사입니다.");
  }
}
