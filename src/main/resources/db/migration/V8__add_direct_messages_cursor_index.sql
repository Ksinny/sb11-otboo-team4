-- DM 대화 내역 조회는 양방향 조건 (sender=A AND receiver=B) OR (sender=B AND receiver=A) 이며
-- createdAt DESC + id DESC 커서 정렬을 사용한다.
-- V1의 IDX_direct_messages_sender_id_receiver_id는 (sender, receiver) 순이라
-- 역방향 절과 정렬을 커버하지 못해, 양방향 각각에 커서 인덱스를 추가한다. (follows V5 선례)

CREATE INDEX IDX_direct_messages_sender_receiver_created_id
    ON direct_messages (sender_id, receiver_id, created_at DESC, id DESC);

CREATE INDEX IDX_direct_messages_receiver_sender_created_id
    ON direct_messages (receiver_id, sender_id, created_at DESC, id DESC);