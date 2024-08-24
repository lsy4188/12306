package itlsy.core;

import cn.hutool.core.util.StrUtil;
import itlsy.UserContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * 用户信息传输过滤器
 * 过滤器：满足 @ConditionalOnWebApplication 的条件，那么该自动配置类会被自动加载和执行
 * 过滤器（filters）通常由框架或平台自动管理和调用，因此可以在不需要显式注册的情况下自动起作用。
 * 拦截器：需要手动注册才能使用
 */
public class UserTransmitFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String userId = httpServletRequest.getHeader(UserConstant.USER_ID_KEY);
        if (StrUtil.isNotBlank(userId)) {
            String userName = httpServletRequest.getHeader(UserConstant.USER_NAME_KEY);
            String realName = httpServletRequest.getHeader(UserConstant.REAL_NAME_KEY);
            if (StrUtil.isNotBlank(userName)) {
                userName = URLDecoder.decode(userName, UTF_8);
            }
            if (StrUtil.isNotBlank(realName)) {
                realName = URLDecoder.decode(realName, UTF_8);
            }
            String token = httpServletRequest.getHeader(UserConstant.USER_TOKEN_KEY);
            UserInfo userInfoDTO = UserInfo.builder()
                    .userId(userId)
                    .username(userName)
                    .realName(realName)
                    .token(token)
                    .build();
            UserContext.setUser(userInfoDTO);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}
