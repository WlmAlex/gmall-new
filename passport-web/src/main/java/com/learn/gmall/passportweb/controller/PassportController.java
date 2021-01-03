package com.learn.gmall.passportweb.controller;

import com.learn.gmall.api.api.UserService;
import com.learn.gmall.api.bean.UmsMember;
import com.learn.gmall.web.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.learn.gmall.passportweb.constant.ConstantIfc.generate_token_fail;

@Controller
public class PassportController {

    @DubboReference
    private UserService userService;

    @GetMapping("/verify")
    @ResponseBody
    public Map<String, Object> verify(String token, String ip) {


        Map<String, Object> decodeMap = JwtUtil.decode(token, "wlmAlexGmallLearning", ip);
        if (!CollectionUtils.isEmpty(decodeMap)) {
            decodeMap.put("status", "success");
        } else {
            decodeMap = new HashMap<>();
            decodeMap.put("status", "fail");
        }
        return decodeMap;
    }

    @PostMapping("/login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {
        String token = generate_token_fail;
        UmsMember umsMemberFromDb = userService.login(umsMember);
        if (umsMemberFromDb != null) {
            //制作token
            token = generateToken(request, umsMemberFromDb);
            userService.addUserToken(token, umsMemberFromDb.getId());
        }
        return token;
    }

    @GetMapping("/index")
    public String index(String originUrl, ModelMap modelMap) {
        modelMap.put("originUrl", originUrl);
        return "index";
    }

    private String generateToken(HttpServletRequest request, UmsMember umsMemberFromDb) {
        String token = generate_token_fail;
        Map<String, Object> encodeMap = new HashMap<>();
        encodeMap.put("memberId", umsMemberFromDb.getId());
        encodeMap.put("nickname", umsMemberFromDb.getNickname());
        token = JwtUtil.encode("wlmAlexGmallLearning", encodeMap, getRequestAddr(request));
        return token;
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
