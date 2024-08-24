package com.itlsy.filter;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.itlsy.config.Config;
import com.itlsy.constant.Userconstant;
import com.itlsy.util.JwtUtil;
import com.itlsy.util.UserInfo;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SpringCloud Gateway Token 拦截器(自定义拦截器)
 */
@Component
public class TokenValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    public TokenValidateGatewayFilterFactory() {
        super(Config.class);
    }

    /**
     * 注销用户时需要传递 Token
     */
    public static final String DELETION_PATH = "/api/user-service/deletion";

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 获取请求对象
            ServerHttpRequest request = exchange.getRequest();
            // 获取请求路径
            String requestPath = request.getPath().toString();
            // 判断请求路径是否在黑名单路径前缀列表中
            if (isPathInBlackPreList(requestPath, config.getBlackPathPre())) {
                // 获取token
                String token;
                HttpHeaders headers = request.getHeaders();
                System.out.println(headers.getFirst("Authorization"));
                String falseToken = headers.getFirst("cookie");
                Pattern pattern = Pattern.compile("token=([^;]+)");
                Matcher matcher = pattern.matcher(falseToken);
                if (matcher.find()) {
                   token = matcher.group(1);
                } else {
                    token = null;
                }

                // 解析token
                UserInfo userInfo = JwtUtil.parseJwtToken(token);
                // 验证token是否有效
                if (!validateToken(userInfo)) {
                    // 如果无效，返回未授权状态码
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }

                // 如果有效，将用户信息添加到请求头中
                ServerHttpRequest.Builder builder = exchange.getRequest().mutate().headers(httpHeaders -> {
                    httpHeaders.set(Userconstant.USER_ID_KEY, userInfo.getUserId());
                    httpHeaders.set(Userconstant.USER_NAME_KEY, userInfo.getUsername());
                    httpHeaders.set(Userconstant.REAL_NAME_KEY, URLEncoder.encode(userInfo.getRealName(), StandardCharsets.UTF_8));
                    httpHeaders.set(Userconstant.USER_TOKEN_KEY, token);

                });
                // 继续执行过滤器链
                return chain.filter(exchange.mutate().request(builder.build()).build());
            }
            // 如果请求路径不在黑名单路径前缀列表中，则直接执行过滤器链
            return chain.filter(exchange);
        };
    }

    private boolean isPathInBlackPreList(String requestPath, List<String> blackPathPre) {
        if (CollectionUtils.isEmpty(blackPathPre)) {
            return false;
        }
        return blackPathPre.stream().anyMatch(requestPath::startsWith);
    }

    private boolean validateToken(UserInfo userInfo) {
        return userInfo != null;
    }
}
