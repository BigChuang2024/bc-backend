package com.haderacher.bcbackend.repository;

import com.haderacher.bcbackend.model.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {
}
