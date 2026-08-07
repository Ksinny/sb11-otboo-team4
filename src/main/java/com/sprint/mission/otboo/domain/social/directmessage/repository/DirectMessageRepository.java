package com.sprint.mission.otboo.domain.social.directmessage.repository;

import com.sprint.mission.otboo.domain.social.directmessage.entity.DirectMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

}