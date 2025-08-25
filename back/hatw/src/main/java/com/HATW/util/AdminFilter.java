package com.HATW.util;

import com.HATW.dto.UserDTO;
import com.HATW.service.UserService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// isAdmin int [not null, default: 0] "관리자 여부(0 = 일반,1 = 관리자)"
public class AdminFilter implements Filter {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AdminFilter(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            System.out.println("AdminFilter: OPTIONS 요청 감지. 다음 필터 체인으로 통과.");
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 없습니다.");
            return;
        }

        try {
            String token = authHeader.substring(7);
            UserDTO user = userService.getUserInfoFromToken(authHeader); // 또는 userService.getUserInfoFromToken(token);

            if (user == null) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
                return;
            }
            if (Boolean.FALSE.equals(user.getIsAdmin())) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자만 접근할 수 없습니다.");
                return;
            }

            res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            res.setHeader("Pragma", "no-cache");
            res.setDateHeader("Expires", 0);

            chain.doFilter(request, response);
        } catch (Exception e) {
            System.err.println("AdminFilter: 토큰 유효성 검증 또는 사용자 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰 인증 실패: " + e.getMessage());
        }
    }
}
