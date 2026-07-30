package com.example.labteamwork.service;

import com.example.labteamwork.dto.response.UserStatsDto;

public interface UserStatsService {

    UserStatsDto getUserStats(Long userId);
}