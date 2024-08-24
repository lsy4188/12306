package itlsy.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import itlsy.core.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;

import static itlsy.core.UserConstant.*;

/**
 * JWT工具类
 */
@Slf4j
public class JWTUtil {
    private static final long EXPIRATION_TIME = 86400L;
    private static final String TOKEN_PREFIX = "User";
    public static final String ISS = "lsy";

    /**
     * 盐值很重要，不能泄漏，且每个项目都应该不一样，可以放到配置文件中
     */
    public static final String SECRET = "lsy123456789";

    /**
     * 生成用户 Token
     *
     * @param userInfo 用户信息
     * @return 用户访问 Token
     */
    public static String createToken(UserInfo userInfo) {
        //内容
        HashMap<String, Object> user = new HashMap<>();
        user.put(USER_ID_KEY, userInfo.getUserId());
        user.put(USER_NAME_KEY, userInfo.getUsername());
        user.put(REAL_NAME_KEY, userInfo.getRealName());

        HashMap<String, Object> payload = new HashMap<>();
        //签发时间
        payload.put(JWTPayload.ISSUED_AT,new Date());
        //过期时间
        payload.put(JWTPayload.EXPIRES_AT,new Date(System.currentTimeMillis() + EXPIRATION_TIME * 1000));
        //签发人
        payload.put(JWTPayload.ISSUER,ISS);
        //主题
        payload.put(JWTPayload.SUBJECT, JSONUtil.toJsonStr(user));

        String jwtToken = cn.hutool.jwt.JWTUtil.createToken(payload, SECRET.getBytes());

        return TOKEN_PREFIX+ jwtToken;
    }

    /**
     * 解析用户 Token
     *
     * @param jwtToken 用户访问 Token
     * @return 用户信息
     */
    public static UserInfo parseJwtToken(String jwtToken) {
        if (StringUtils.hasText(jwtToken)) {
            String actualJwtToken = jwtToken.replaceFirst(TOKEN_PREFIX, "");
            if(cn.hutool.jwt.JWTUtil.verify(actualJwtToken, SECRET.getBytes())){
                try {
                    JWT jwt = cn.hutool.jwt.JWTUtil.parseToken(actualJwtToken).setKey(SECRET.getBytes());
                    //获取jwt的载荷
                    JSONObject payload = jwt.getPayloads();
                    //获取jwt的签发时间,并判断是否过期
                    Date expTime = payload.getDate(JWTPayload.EXPIRES_AT);
                    if (expTime.after(new Date())){
                        Object subject = payload.get(JWTPayload.SUBJECT);
                        return JSONUtil.toBean(subject.toString(),UserInfo.class);
                    }

                } catch (Exception e) {
                    log.error("解析JWT令牌失败,请检查", e);
                }
            }
        }
        return null;
    }
}
