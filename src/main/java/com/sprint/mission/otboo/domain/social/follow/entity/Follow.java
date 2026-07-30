package com.sprint.mission.otboo.domain.social.follow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "follows")
@Entity
public class Follow {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "follower_id", nullable = false, updatable = false)
  private UUID followerId;

  @Column(name = "followee_id", nullable = false, updatable = false)
  private UUID followeeId;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  private Follow(UUID followerId, UUID followeeId) {
    this.followerId = followerId;
    this.followeeId = followeeId;
  }

  public static Follow create(UUID followerId, UUID followeeId) {
    return new Follow(followerId, followeeId);
  }
}