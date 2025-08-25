package com.HATW.mapper;

import com.HATW.dto.ReportDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {
    List<ReportDTO> findAllForAdmin();

    void insertReport(ReportDTO report);


//     제보 처리 상태 업데이트 (관리자용)
    void updateStatus(@Param("idx") int idx,
                      @Param("status") String status,
                      @Param("updatedAt") LocalDateTime updatedAt);
    List<Map<String, Object>> findByTypes(@Param("types") List<Integer> types);

    // 로그인유저가 작성한 제보 가져오기
    List<ReportDTO> findByUserId(@Param("userId") String userId);


    // 신고된 위험 지점 전체 조회
    List<ReportDTO> findApprovedReports();

    //제보수정
    ReportDTO findById(@Param("reportId") Long reportId);
    void updateImageUrl(@Param("reportId") Long reportId, @Param("imageUrl") String imageUrl);
    void updateComment(@Param("reportId") Long reportId, @Param("comment") String comment);
}
