package com.classpets.backend.controller;

import com.classpets.backend.auth.dto.LoginRequestDTO;
import com.classpets.backend.auth.service.AuthService;
import com.classpets.backend.auth.vo.LoginVO;
import com.classpets.backend.common.ApiResponse;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void loginShouldUseForwardedIpAndReturnServiceData() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("teacher01");
        request.setPassword("pwd123456");

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getHeader("X-Forwarded-For")).thenReturn("203.0.113.10, 10.0.0.1");

        LoginVO vo = new LoginVO();
        vo.setToken("token-1");
        when(authService.login(request, "ua-1", "203.0.113.10")).thenReturn(vo);

        ApiResponse<LoginVO> response = controller.login(request, "ua-1", servletRequest);

        assertEquals(0, response.getCode());
        assertSame(vo, response.getData());
    }

    @Test
    void logoutShouldExtractBearerToken() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);

        ApiResponse<Void> response = controller.logout("Bearer token-abc");

        verify(authService).logoutByToken("token-abc");
        assertEquals(0, response.getCode());
    }

    @Test
    void logoutShouldPassNullForInvalidAuthorization() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);

        controller.logout("Invalid token");

        verify(authService).logoutByToken(null);
    }

    @Test
    void loginShouldUseRealIpWhenForwardedHeaderMissing() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("teacher02");
        request.setPassword("pwd123456");

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(servletRequest.getHeader("X-Real-IP")).thenReturn("198.51.100.7");

        LoginVO vo = new LoginVO();
        vo.setToken("token-2");
        when(authService.login(request, "ua-2", "198.51.100.7")).thenReturn(vo);

        ApiResponse<LoginVO> response = controller.login(request, "ua-2", servletRequest);

        assertEquals(0, response.getCode());
        assertSame(vo, response.getData());
    }

    @Test
    void loginShouldUseRemoteAddrAsLastFallback() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("teacher03");
        request.setPassword("pwd123456");

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getHeader("X-Forwarded-For")).thenReturn(" ");
        when(servletRequest.getHeader("X-Real-IP")).thenReturn("unknown");
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        LoginVO vo = new LoginVO();
        vo.setToken("token-3");
        when(authService.login(request, "ua-3", "127.0.0.1")).thenReturn(vo);

        ApiResponse<LoginVO> response = controller.login(request, "ua-3", servletRequest);

        assertEquals(0, response.getCode());
        assertSame(vo, response.getData());
    }
}
