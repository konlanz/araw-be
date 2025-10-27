package com.araw.araw.presentation;

import com.araw.araw.application.dto.admin.AdminDashboardSummaryResponse;
import com.araw.araw.application.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/summary")
    public AdminDashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }
}
