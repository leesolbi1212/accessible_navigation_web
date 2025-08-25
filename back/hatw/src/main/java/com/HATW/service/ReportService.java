package com.HATW.service;

import com.HATW.dto.ReportDTO;
import com.HATW.dto.ReportUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReportService {
    void createReport(ReportDTO reportDTO, MultipartFile file);
    List<ReportDTO> getAllReports();
    void updateReportStatus(int idx, String status);
    List<ReportDTO> findByUserId(String userId);


    String updateReportImage(Long reportId, String userId, MultipartFile image);
    void updateReport(Long reportId, ReportUpdateRequest request);

}
