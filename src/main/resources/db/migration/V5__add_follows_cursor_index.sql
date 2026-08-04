-- 팔로잉 목록 조회(findFollowings): follower_id 필터 + (created_at, id) DESC 커서 페이지네이션 커버링
-- 팔로워 목록 조회(findFollowers): followee_id 필터 + (created_at, id) DESC 커서 페이지네이션 커버링

CREATE INDEX IDX_follows_follower_created_id
    ON follows (follower_id, created_at DESC, id DESC);

CREATE INDEX IDX_follows_followee_created_id
    ON follows (followee_id, created_at DESC, id DESC);