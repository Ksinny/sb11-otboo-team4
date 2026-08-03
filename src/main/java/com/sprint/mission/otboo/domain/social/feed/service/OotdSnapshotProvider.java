package com.sprint.mission.otboo.domain.social.feed.service;

import com.sprint.mission.otboo.domain.clothesrecommend.clothes.service.ClothesService;
import com.sprint.mission.otboo.domain.social.feed.dto.OotdDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OotdSnapshotProvider {

  private final ClothesService clothesService;

  public List<OotdDto> readOotds(List<UUID> clothesIds) {
    return List.of();
  }
}