package com.spring.monew.common.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
public class HibernateFilterAspect {

  @PersistenceContext
  private EntityManager em;

  // isDeleted가 false인 데이터만 보이게 해줌.
  @Before("execution(* com.spring.monew..repository..*(..))")
  public void enableFilter() {
    em.unwrap(Session.class)
        .enableFilter("deletedFilter")
        .setParameter("isDeleted", false);
  }
}
