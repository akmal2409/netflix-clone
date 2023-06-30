package com.akmal2409.github.netflix.media.domain.repository;

import com.akmal2409.github.netflix.media.domain.model.Genre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GenreRepository extends JpaRepository<Genre, Integer> {

  @Query("SELECT g FROM Genre g WHERE g.id IN :ids")
  List<Genre> findAllByIds(@Param("ids") List<Integer> ids);
}
