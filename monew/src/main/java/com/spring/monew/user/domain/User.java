package com.spring.monew.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //외부 new 막음
@AllArgsConstructor
@SQLDelete(sql = "UPDATE users SET is_deleted = true, deleted_at = NOW() WHERE id = ?") //논리삭제 SQL
@Filter(name = "deletedFilter", condition = "is_deleted = :isDeleted")
public class User {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, length = 20)
  private String nickname;

  @Column(nullable = false)
  private String password;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role; //Admin

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted = false;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  public User(String email, String nickname, String password) {
    this.email = email;
    this.nickname = nickname;
    this.password = password;
    this.createdAt = Instant.now();
    this.role = UserRole.USER;
    this.isDeleted = false;
  }

  public User(String email, String nickname, String password, Instant createdAt, UserRole role) {
    this.email = email;
    this.nickname = nickname;
    this.password = password;
    this.createdAt = createdAt;
    this.role = role;
    this.isDeleted = false;
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

}
