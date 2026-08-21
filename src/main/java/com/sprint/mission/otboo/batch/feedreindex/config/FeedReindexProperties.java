package com.sprint.mission.otboo.batch.feedreindex.config;

import com.sprint.mission.otboo.global.batch.BatchConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * 피드 검색 인덱스 재색인 배치 설정.
 *
 * <p>설정 파일이 .gitignore 대상이라 키가 없어도 기동되도록 기본값을 둔다.
 */
@Validated
@ConfigurationProperties(prefix = "batch.feed-reindex")
public record FeedReindexProperties(

    @DefaultValue("500") @Positive @Max(BatchConstants.MAX_CHUNK_SIZE) int chunkSize,

    // 증분 재색인이 훑을 과거 구간
    // 행 주기(1시간)보다 넉넉히 잡아 한 번 걸러져도 다음 실행이 덮는다.
    @DefaultValue("PT2H") Duration incrementalLookback
) {

}
