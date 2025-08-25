package com.HATW.util;

import com.HATW.dto.UserDTO;
import com.HATW.mapper.UserMapper;
import net.nurigo.sdk.message.exception.NurigoEmptyResponseException;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import net.nurigo.sdk.NurigoApp;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SmsService {

    private final DefaultMessageService messageService;
    private final String senderPhone;
    private final UserMapper userMapper;

    public SmsService(
            @Value("${coolsms.api-key}") String apiKey,
            @Value("${coolsms.api-secret}") String apiSecret,
            @Value("${coolsms.sender}") String senderPhone,
            UserMapper userMapper) {

        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
        this.senderPhone = senderPhone;
        this.userMapper = userMapper;
    }

    public void sendTemporaryPasswordAndReset(String phone, String userId) {
        System.out.println("💡 [SmsService] 전달된 userId: " + userId);
        System.out.println("💡 [SmsService] 전달된 phone: " + phone);
        System.out.println("✅ userMapper null 여부: " + (userMapper == null)); // ✅ 추가

        // 전화번호 유효성 체크
        if (!isValidPhoneNumber(phone)) {
            System.out.println("❌ 잘못된 전화번호 형식입니다.");
            throw new IllegalArgumentException("잘못된 전화번호 형식입니다.");
        }

        // 사용자 조회
        System.out.println("🔍 사용자 조회 시도...");
        UserDTO user = userMapper.findByIdAndPhone(userId, phone);

        if (user == null) {
            System.out.println("❌ 사용자 조회 결과: null");
        } else {
            System.out.println("✅ 사용자 조회 성공: " + user.getUserId());
        }

        if (user == null) {
            System.out.println("❌ 사용자 조회 실패: 일치하는 사용자 없음");
            throw new IllegalArgumentException("입력하신 정보와 일치하는 사용자가 없습니다.");
        }
        System.out.println("✅ 사용자 조회 성공: " + user.getUserId());

        // 임시 비밀번호 생성
        String tempPassword = generateTempPassword();
        System.out.println("🔐 생성된 임시 비밀번호: " + tempPassword);

        String hashedPassword = BCrypt.hashpw(tempPassword, BCrypt.gensalt());
        System.out.println("🔐 비밀번호 해시 완료");

        // DB 비밀번호 변경
        user.setPasswordHash(hashedPassword);
        System.out.println("💾 비밀번호 업데이트 시도");
        userMapper.updatePassword(user);
        System.out.println("✅ 비밀번호 업데이트 완료");

        // 문자 메시지 전송
        Message message = new Message();
        message.setFrom(senderPhone);
        message.setTo(phone);
        message.setText("[HATW] 임시 비밀번호: " + tempPassword + "\n로그인 후 반드시 비밀번호를 변경해주세요.");
        System.out.println("📤 문자 전송 준비 완료");

        try {
            messageService.send(message);
            System.out.println("✅ 문자 전송 성공");
        } catch (NurigoMessageNotReceivedException | NurigoEmptyResponseException | NurigoUnknownException e) {
            System.out.println("❌ 문자 전송 실패");
            e.printStackTrace(); // 더 구체적인 예외 원인을 확인 가능
            throw new RuntimeException("문자 전송 실패", e); // 💥 던지기
        }
    }


    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^01[0-9]{8,9}$");
    }

    private String generateTempPassword() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000); // 6자리 숫자
        return String.valueOf(number);
    }

    public void sendSms(String to, String content) {
        Message message = new Message();
        message.setFrom(senderPhone);
        message.setTo(to);
        message.setText(content);

        try {
            messageService.send(message);
            System.out.println("✅ 문자 전송 성공: " + to);
        } catch (NurigoMessageNotReceivedException e) {
            System.out.println("❌ 문자 전송 실패 (메시지 안 도착)");
            System.out.println("에러 메시지: " + e.getMessage());
            System.out.println("응답 내용: " + e.getFailedMessageList());
            throw new RuntimeException("문자 전송 실패", e);
        } catch (NurigoEmptyResponseException e) {
            System.out.println("❌ 문자 전송 실패 (응답 없음)");
            e.printStackTrace();
            throw new RuntimeException("문자 전송 실패", e);
        } catch (NurigoUnknownException e) {
            System.out.println("❌ 문자 전송 실패 (알 수 없는 에러)");
            e.printStackTrace();
            throw new RuntimeException("문자 전송 실패", e);
        }
    }
}