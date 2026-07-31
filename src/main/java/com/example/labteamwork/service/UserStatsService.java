package com.example.labteamwork.service;

import com.example.labteamwork.dto.response.GlobalLeaderboardEntryDto;
import com.example.labteamwork.dto.response.UserStatsDto;

import java.util.List;

public interface UserStatsService {

    UserStatsDto getUserStats(Long userId);

    List<GlobalLeaderboardEntryDto> getGlobalLeaderboard(int limit);
}