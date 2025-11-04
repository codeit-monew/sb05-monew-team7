package com.spring.monew.interest.service.impl;

import com.spring.monew.article.domain.Article;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.interest.controller.dto.request.InterestRegisterRequest;
import com.spring.monew.interest.controller.dto.request.InterestUpdateRequest;
import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.interest.service.InterestService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

  private final InterestRepository interestRepository;
  private final ArticleRepository articleRepository;

  @Override
  @Transactional
  public InterestDto addInterest(InterestRegisterRequest registerRequest) {
    List<String> similarNames = interestRepository.findSimilarNames(registerRequest.name(), 0.45);

    if (interestRepository.existsByName(registerRequest.name())) {
      throw new IllegalArgumentException("같은 이름이 존재합니다.");
    }
    if(!similarNames.isEmpty()) {
      throw new IllegalArgumentException("유사도가 높은 이름이 존재합니다.");
    }
    
    Interest interest = interestRepository.save(
        new Interest(registerRequest.name(), registerRequest.keywords()));

    return new InterestDto(
        interest.getId(),
        interest.getName(),
        interest.getKeywords(),
        interest.getSubscriptionsCount(),
        false,
        interest.getCreatedAt());
  }

  @Override
  @Transactional
  public CursorPageResponseInterestDto getInterests(
      String keyword,
      String orderBy,
      String direction,
      String cursor,
      Instant after,
      int limit,
      UUID userId
  ) {
    return interestRepository.findCursorPagedInterests(
        keyword, orderBy, direction, cursor, after, limit, userId);
  }

  @Override
  @Transactional
  public InterestDto modifyInterest(UUID interestId, InterestUpdateRequest updateRequest) {
    Interest interest = interestRepository.findById(interestId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 관심사입니다."));

    interest.update(updateRequest.keywords());

    return new InterestDto(
        interest.getId(),
        interest.getName(),
        interest.getKeywords(),
        interest.getSubscriptionsCount(),
        false,
        interest.getCreatedAt());
  }

  @Override
  @Transactional
  public void removeInterest(UUID interestId) {
    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 관심사입니다."));

    List<Article> relatedArticles = articleRepository.findAllByInterestId(interestId);
    
    if (!relatedArticles.isEmpty()) {
      log.info("관심사 삭제로 인한 연관 게시글 소프트 삭제: interestId={}, articleCount={}", 
          interestId, relatedArticles.size());
      articleRepository.deleteAll(relatedArticles);
    }

    interestRepository.delete(interest);
  }
}
