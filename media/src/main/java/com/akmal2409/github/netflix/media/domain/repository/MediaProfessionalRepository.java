package com.akmal2409.github.netflix.media.domain.repository;

import com.akmal2409.github.netflix.media.domain.model.MediaProfessional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MediaProfessionalRepository extends JpaRepository<MediaProfessional, UUID> {

  @Query("SELECT p FROM MediaProfessional p WHERE p.id IN :ids")
  List<MediaProfessional> findAllByIds(@Param("ids") List<UUID> ids);
}
