package com.numix.web.controller;

import java.util.List;

public record DashboardApiSummaryResponse(
    String welcomeMessage,
    String generatedAt,
    List<DashboardCard> cards,
    List<DashboardActivity> activity
) {
}
