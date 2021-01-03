package com.learn.gmall.web.interceptor;

import com.google.gson.Gson;
import com.learn.gmall.util.HttpclientUtil;
import com.learn.gmall.web.annotation.LoginRequired;
import com.learn.gmall.web.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class LoginHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            LoginRequired methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequired.class);

            if (methodAnnotation == null) {
                //没有拦截注解, 直接放行
                return true;
            }

            //有拦截注解 进行token校验
            String token = CookieUtil.getCookieValue(request, "oldToken", true);
            String newToken = request.getParameter("token");
            if (StringUtils.isNotBlank(newToken)) {
                token = newToken;
            }

            Map<String, Object> verifyResult = new HashMap<>();
            if (StringUtils.isNotBlank(token)) {
                String ip = getRequestAddr(request);
                String verifyStr = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token + "&ip=" + ip);
                verifyResult = new Gson().fromJson(verifyStr, Map.class);
            }

            boolean loginSuccess = methodAnnotation.loginSuccess();

            String status = String.valueOf(verifyResult.get("status"));
            //必须验证通过才能调用逻辑
            if (loginSuccess) {
                if (!"success".equals(status)) {
                    System.err.println(request.getMethod());
                    response.sendRedirect("http://passport.gmall.com:8085/index?originUrl=" + request.getRequestURL());
                    return false;
                }

                //验证通过, 更新cookie
                request.setAttribute("memberId", verifyResult.get("memberId"));
                request.setAttribute("nickname", verifyResult.get("nickname"));
                CookieUtil.setCookie(request, response, "oldToken", token, 2 * 60 * 60, true);
            } else {
                //验证不通过也能调用逻辑
                if ("success".equals(status)) {
                    request.setAttribute("memberId", verifyResult.get("memberId"));
                    request.setAttribute("nickname", verifyResult.get("nickname"));
                    CookieUtil.setCookie(request, response, "oldToken", token, 2 * 60 * 60, true);
                }
            }
        }
        return true;
    }

    private String getRequestAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forward-for");
        if (StringUtils.isBlank(ip)) {
            ip = request.getRemoteAddr();
            if (StringUtils.isBlank(ip)) {
                ip = "192.168.1.6";
            }
        }
        return ip;
    }
}