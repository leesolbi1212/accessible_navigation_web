package com.HATW.service;

import com.HATW.dto.InquiryDTO;
import java.util.List;

public interface InquiryService {
    List<InquiryDTO> findAllForAdmin();

    void insertInquiry(InquiryDTO inquiry);

    void updateInquiry(InquiryDTO inquiry);

    // 로그인 유저가 쓴 문의 가져오기
    List<InquiryDTO> findByUserId(String userId);
}
