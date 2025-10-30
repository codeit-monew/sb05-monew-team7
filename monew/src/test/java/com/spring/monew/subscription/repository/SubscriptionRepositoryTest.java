package com.spring.monew.subscription.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.subscription.domain.Subscription;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DataJpaTest
class SubscriptionRepositoryTest {

  @Autowired SubscriptionRepository subscriptionRepository;
  @Autowired UserRepository userRepository;
  @Autowired InterestRepository interestRepository;

  @Test
  @DisplayName("구독 존재 여부 확인")
  void findByUser_IdAndInterest_Id() {
    // given
    User user = userRepository.save(new User("test1@example.com","userA", "password"));
    Interest interest = interestRepository.save(new Interest("야구", List.of("스포츠", "경기")));
    subscriptionRepository.save(new Subscription(user, interest));

    // when
    boolean exists = subscriptionRepository.existsByUser_IdAndInterest_Id(user.getId(), interest.getId());

    // then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("특정 유저의 구독 조회")
  void existsByUser_IdAndInterest_Id() {
    User user = userRepository.save(new User("test2@example.com","userB", "password"));
    Interest interest = interestRepository.save(new Interest("축구", List.of("스포츠")));
    Subscription saved = subscriptionRepository.save(new Subscription(user, interest));

    Optional<Subscription> found = subscriptionRepository.findByUser_IdAndInterest_Id(user.getId(), interest.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
  }

  // 테스트 Configuration 주입
  @TestConfiguration
  static class QuerydslTestConfig {

    @PersistenceContext
    private EntityManager em;

    @Bean
    public JPAQueryFactory queryFactory() {
      return new JPAQueryFactory(em);
    }
  }
}
