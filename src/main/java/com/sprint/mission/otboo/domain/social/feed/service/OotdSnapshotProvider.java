package com.sprint.mission.otboo.domain.social.feed.service;

import com.sprint.mission.otboo.domain.clothesrecommend.clothes.dto.ClothesDto;
import com.sprint.mission.otboo.domain.clothesrecommend.clothes.service.ClothesService;
import com.sprint.mission.otboo.domain.social.feed.dto.OotdDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OotdSnapshotProvider {

  private final ClothesService clothesService;

  public List<OotdDto> readOotds(List<UUID> clothesIds) {
    if (clothesIds == null || clothesIds.isEmpty()) {
      return List.of();
    }
    List<ClothesDto> clothesList = clothesService.getClothesByIds(clothesIds);
    log.debug("착장 조회 완료: 요청={}, 조회={}", clothesIds.size(), clothesList.size());
    return clothesList.stream()
        .map(this::toOotdDto)
        .toList();
  }

  private OotdDto toOotdDto(ClothesDto clothes) {
    return new OotdDto(
        clothes.id(),
        clothes.name(),
        clothes.imageUrl(),
        clothes.type(),
        clothes.attributes()
    );
  }
}