package com.HATW.service;

import com.HATW.dto.UserDTO;
import com.HATW.mapper.UserMapper;
import com.HATW.util.GoogleAuthUtil;
import com.HATW.util.JwtUtil;
import com.HATW.util.KakaoAuthUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import com.HATW.util.DuplicateUserIdException;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public List<UserDTO> findAll() {
        return userMapper.findAll();
    }

    /* 회원가입 */
    @Override
    public void register(UserDTO user) {
        if (userMapper.existsByUserId(user.getUserId()) > 0) {
            throw new DuplicateUserIdException("이미 사용 중인 아이디입니다.");
        }
        String hashed = BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt());
        user.setPasswordHash(hashed);
        userMapper.insertUser(user);


    }

    @Override
    public boolean existsByUserId(String userId) {
        return userMapper.existsByUserId(userId) > 0;
    }


    @Override
    public String login(String userId, String rawOrHashed) {
        UserDTO user = userMapper.findByUserId(userId);
        if (user == null) {
            return null;
        }

        Boolean active = user.getIsActive();
        if (active == null || !active) {
            throw new IllegalStateException("계정이 비활성화되어 로그인이 불가합니다.");
        }

        String storedHash = user.getPasswordHash();
        // 1) 클라이언트가 이미 해시를 보냈을 때
        boolean matchByHash = storedHash.equals(rawOrHashed);
        // 2) 평문을 보냈을 때만 BCrypt 검증
        boolean matchByPlain = false;
        if (!matchByHash && storedHash != null &&
                (storedHash.startsWith("$2a$") ||
                        storedHash.startsWith("$2b$") ||
                        storedHash.startsWith("$2y$"))) {
            matchByPlain = BCrypt.checkpw(rawOrHashed, storedHash);
        }

        if (matchByHash || matchByPlain) {
            return jwtUtil.generateToken(user.getUserId());
        }
        return null;
    }

    /* 로그아웃 (stateless JWT라 특별 처리는 없음) */
    @Override
    public void logout(String token) {
        // 클라이언트가 토큰을 버리면 됩니다.
    }

    /* 회원 정보 수정 */
    @Override
    @Transactional
    public void update(String token, UserDTO user) {
        String jwt = token.replace("Bearer ", "");
        String userId = jwtUtil.getUserIdFromToken(jwt);

        UserDTO original = userMapper.findByUserId(userId);
        if (original != null) {
            if (StringUtils.hasText(user.getPasswordHash())) {
                String hashed = BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt());
                original.setPasswordHash(hashed);
            }
            if (StringUtils.hasText(user.getName())) {
                original.setName(user.getName());
            }
            userMapper.update(original);
        }
    }

    /** 회원 탈퇴 */
    @Override
    public void delete(String token) {
        String jwt = token.replace("Bearer ", "");
        String userId = jwtUtil.getUserIdFromToken(jwt);
        userMapper.delete(userId);
    }

    /* 토큰으로 사용자 조회 */
    @Override
    public UserDTO getUserInfoFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        String userId = jwtUtil.getUserIdFromToken(jwt);

        UserDTO user = userMapper.findByUserId(userId);
        if (user != null) {
            user.setPasswordHash(null);
        }
        return user;
    }

    /* 카카오 로그인 */
    @Override
    public UserDTO kakaoLogin(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // 1) 액세스 토큰 요청
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("grant_type", "authorization_code");
        tokenParams.add("client_id", KakaoAuthUtil.CLIENT_ID);
        tokenParams.add("redirect_uri", KakaoAuthUtil.REDIRECT_URI);
        tokenParams.add("code", code);

        HttpEntity<MultiValueMap<String, String>> tokenRequest =
                new HttpEntity<>(tokenParams, tokenHeaders);
        String tokenResponse = restTemplate.postForObject(
                KakaoAuthUtil.TOKEN_URL, tokenRequest, String.class
        );
        String accessToken = parseAccessToken(tokenResponse);

        // 2) 사용자 정보 요청
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<String> userResponse = restTemplate.exchange(
                KakaoAuthUtil.USERINFO_URL,
                HttpMethod.GET,
                userRequest,
                String.class
        );

        return handleUserResponse(userResponse.getBody());
    }

    /** 카카오 응답에서 access_token 파싱 */
    private String parseAccessToken(String response) {
        try {
            JsonNode tokenJson = objectMapper.readTree(response);
            return tokenJson.path("access_token").asText();
        } catch (IOException e) {
            throw new RuntimeException("카카오 토큰 파싱 실패", e);
        }
    }

    /* 카카오 사용자 정보 핸들링 */
    private UserDTO handleUserResponse(String body) {
        try {
            JsonNode kakaoJson = objectMapper.readTree(body);
            JsonNode account = kakaoJson.path("kakao_account");

            String kakaoId   = kakaoJson.path("id").asText();
            String nickname  = account.path("profile").path("nickname").asText("카카오유저");
            String userId    = "kakao_" + kakaoId;

            UserDTO user = userMapper.findByUserId(userId);
            if (user == null) {
                UserDTO joinUser = new UserDTO();
                joinUser.setUserId(userId);
                joinUser.setPasswordHash(""); // 소셜은 빈 해시
                joinUser.setName(nickname);
                joinUser.setSsn1("SOCIAL");
                joinUser.setSsn2("SOCIAL");
                joinUser.setPhone("SOCIAL");
                joinUser.setIsActive(true);
                joinUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));

                userMapper.insertKakaoUser(joinUser);
                user = userMapper.findByUserId(userId);
            }
            return user;

        } catch (IOException e) {
            throw new RuntimeException("카카오 사용자 정보 파싱 실패", e);
        }
    }

    /* google 로그인 */

    @Override
    public UserDTO googleLogin(String code) {
        RestTemplate rt = new RestTemplate();

        // 1) 구글 Access Token 요청
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("code",         code);
        tokenParams.add("client_id",    GoogleAuthUtil.CLIENT_ID);
        tokenParams.add("client_secret",GoogleAuthUtil.CLIENT_SECRET);
        tokenParams.add("redirect_uri", GoogleAuthUtil.REDIRECT_URI);
        tokenParams.add("grant_type",   "authorization_code");
        HttpEntity<MultiValueMap<String,String>> tokenRequest =
                new HttpEntity<>(tokenParams, tokenHeaders);

        @SuppressWarnings("unchecked")
        Map<String,Object> tokenResponse = rt.postForObject(
                GoogleAuthUtil.TOKEN_URL, tokenRequest, Map.class);
        String accessToken = (String) tokenResponse.get("access_token");

        // 2) 구글 사용자 정보 조회
        HttpHeaders infoHeaders = new HttpHeaders();
        infoHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> infoRequest = new HttpEntity<>(infoHeaders);

        @SuppressWarnings("unchecked")
        Map<String,Object> profile = rt.exchange(
                GoogleAuthUtil.USERINFO_URL,
                HttpMethod.GET,
                infoRequest,
                Map.class
        ).getBody();

        String userId   = (String) profile.get("id");
        String name     = (String) profile.get("name");

        // 3) DB 처리 (기존 카카오 로그인 흐름과 동일)
        UserDTO user = userMapper.findByUserId(userId);
        if (user == null) {
            UserDTO joinUser = new UserDTO();
            joinUser.setUserId(userId);
            joinUser.setPasswordHash("");
            joinUser.setName(name);
            joinUser.setSsn1("SOCIAL");
            joinUser.setSsn2("SOCIAL");
            joinUser.setPhone("SOCIAL");
            joinUser.setIsActive(true);
            joinUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            userMapper.insertKakaoUser(joinUser);  // insertKakaoUser와 동일한 컬럼 맵핑 사용
            user = userMapper.findByUserId(userId);
        }

        return user;
    }

    /* 사용자 ID로 조회 */
    @Override
    public UserDTO getUserByUserId(String userId) {
        return userMapper.findByUserId(userId);
    }
    @Override
    public boolean checkUserIdExists(String userId) {
        return userMapper.existsByUserId(userId) > 0;
    }

    @Override
    public String findUserIdByNameAndPhone(String name, String phone) {
        // bc vf                                                                                                                                                                                        System.out.println("✅ [DEBUG] 아이디 찾기 메서드 호출됨");
        name = name.trim();
        phone = phone.trim();
        System.out.println("💡 이름: " + name);
        System.out.println("💡 전화번호: " + phone);

        UserDTO user = userMapper.findByNameAndPhone(name, phone);

        if (user == null) {
            System.out.println(" 일치하는 사용자 없음");
        } else {
            System.out.println(" 찾은 사용자: " + user.getUserId());
        }

        return user != null ? user.getUserId() : null;
    }

    @Override
    public void setActiveStatus(String userId, boolean isActive) {
        int activeFlag = isActive ? 1 : 0;
        userMapper.updateActive(userId, activeFlag);
    }
}

