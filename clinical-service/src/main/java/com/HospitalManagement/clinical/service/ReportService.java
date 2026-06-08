package com.HospitalManagement.clinical.service;

import com.HospitalManagement.clinical.client.AuthClient;
import com.HospitalManagement.clinical.entity.Report;
import com.HospitalManagement.shared.exception.ResourceNotFoundException;
import com.HospitalManagement.clinical.repository.ReportRepository;
import com.HospitalManagement.clinical.dto.ReportRequestDto;
import com.HospitalManagement.clinical.dto.ReportResponseDto;
import com.HospitalManagement.shared.dto.UserSummaryDto;
import com.HospitalManagement.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private final ReportRepository reportRepository;
    private final AuthClient authClient;

    @Transactional(readOnly = true)
    public List<ReportResponseDto> getAllReports() {
        logger.debug("Fetching all reports");
        List<Report> reportEntities = reportRepository.findAll();
        List<ReportResponseDto> reports = new ArrayList<>();

        for (Report report : reportEntities) {
            reports.add(toResponseDto(report));
        }

        logger.info("Retrieved {} reports", reports.size());
        return reports;
    }

    @Transactional(readOnly = true)
    public ReportResponseDto getReportById(Long reportId) {
        logger.debug("Fetching report with ID: {}", reportId);
        ReportResponseDto report = toResponseDto(findReport(reportId));
        logger.info("Retrieved report - ID: {}, Scope: {}", reportId, report.scope());
        return report;
    }

    public ReportResponseDto createReport(ReportRequestDto requestDto) {
        logger.info("Creating new report - Scope: {}", requestDto.scope());

        AuthenticatedUser currentUser = getAuthenticatedUser();

        Report report = new Report();
        mapRequestToEntity(requestDto, report);

        // Override generatedBy with logged-in user
        report.setGeneratedById(currentUser.getUserId());

        return toResponseDto(reportRepository.save(report));
    }

    public ReportResponseDto updateReport(Long reportId, ReportRequestDto requestDto) {
        logger.info("Updating report - ReportID: {}, NewScope: {}", reportId, requestDto.scope());
        Report report = findReport(reportId);
        mapRequestToEntity(requestDto, report);

        AuthenticatedUser currentUser = getAuthenticatedUser();
        report.setGeneratedById(currentUser.getUserId());

        return toResponseDto(reportRepository.save(report));
    }

    public void deleteReport(Long reportId) {
        reportRepository.delete(findReport(reportId));
    }

    private void mapRequestToEntity(ReportRequestDto requestDto, Report report) {
        report.setScope(requestDto.scope());
        report.setParametersJson(requestDto.parametersJson());
        report.setMetricsJson(requestDto.metricsJson());
        report.setGeneratedAt(requestDto.generatedAt() != null ? requestDto.generatedAt() : LocalDateTime.now());
        report.setReportUri(requestDto.reportUri());
    }

    private Report findReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));
    }

    private AuthenticatedUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser authUser) {
            return authUser;
        }
        throw new ResourceNotFoundException("Authenticated user not found");
    }

    private UserSummaryDto findUser(Long userId) {
        try {
            UserSummaryDto user = authClient.getUserById(userId);
            if (user == null) {
                return null;
            }
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    private ReportResponseDto toResponseDto(Report report) {
        UserSummaryDto generatedBy = report.getGeneratedById() != null ? findUser(report.getGeneratedById()) : null;
        return new ReportResponseDto(
                report.getReportId(),
                report.getScope(),
                report.getParametersJson(),
                report.getMetricsJson(),
                report.getGeneratedById(),
                generatedBy != null ? generatedBy.getName() : null,
                report.getGeneratedAt(),
                report.getReportUri()
        );
    }
}
