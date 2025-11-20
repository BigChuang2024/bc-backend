package com.haderacher.bcbackend.controller;

import com.haderacher.bcbackend.common.ApiResponse;
import com.haderacher.bcbackend.dto.EpisodeDto;
import com.haderacher.bcbackend.model.Episode;
import com.haderacher.bcbackend.service.EpisodeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class EpisodeController {

    private EpisodeService episodeService;


    @PostMapping("/episode")
    public ApiResponse<Episode> save(@RequestBody EpisodeDto episodeDto) {
        log.debug("get Episode {}", episodeDto);
        Episode episode = episodeService.sendToBuildGraph(episodeDto);
        return ApiResponse.created(episode);
    }
}