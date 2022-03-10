package com.sun.bilibili.api.support;

import com.sun.bilibili.domain.exception.ConditionException;
import com.sun.bilibili.service.util.TokenUtil;
import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class UserSupport {

    public Long getCurrentUserId(){
        ServletRequestAttributes requestAttributes= (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String token =requestAttributes.getRequest().getHeader("token");
        Long userId= TokenUtil.verifyToken(token);
        if(userId<0){
            throw new ConditionException("非法用户");
        }
        return userId;
    }
}
