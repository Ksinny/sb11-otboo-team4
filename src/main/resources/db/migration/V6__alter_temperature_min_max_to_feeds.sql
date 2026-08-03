-- Spring Batch 6.x 공식 PostgreSQL 스키마(schema-postgresql.sql, spring-batch-core) 그대로 사용.
-- Flyway로 버전 관리하기 위해 Boot 자동 초기화(spring.batch.jdbc.initialize-schema) 대신 마이그레이션으로 관리한다.

ALTER TABLE feeds
    ADD COLUMN temperature_min DOUBLE PRECISION;

ALTER TABLE feeds
    ADD COLUMN temperature_max DOUBLE PRECISION;