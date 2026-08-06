package com.sprint.mission.otboo.domain.social.feed.repository.querydsl.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedCommentParams;
import com.sprint.mission.otboo.domain.social.feed.entity.Comment;
import com.sprint.mission.otboo.domain.social.feed.repository.querydsl.CommentCustomRepository;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public CursorPageResponse<Comment> findComments(FeedCommentParams params) {
    return new CursorPageResponse<>(List.of(), null, null, false, 0L, "createdAt", null);
  }
}