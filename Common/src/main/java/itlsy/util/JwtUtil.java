package itlsy.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.Claims;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import itlsy.info.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import java.util.Date;
import java.util.HashMap;
import static itlsy.constant.Userconstant.*;

@Slf4j
public class JwtUtil {
    private static final long EXPIRATION_TIME = 86400L;
    private static final String TOKEN_PREFIX = "User";
    public static final String ISS = "lsy";

    /**
     * 盐值很重要，不能泄漏，且每个项目都应该不一样，可以放到配置文件中
     */
    public static final String SECRET = "lsy123456789";


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

        String jwtToken = JWTUtil.createToken(payload, SECRET.getBytes());

        return TOKEN_PREFIX+ jwtToken;
    }

    /**
     * 解析并验证一个JWT令牌
     *
     * @param jwtToken
     * @return
     */
    public static UserInfo parseJwtToken(String jwtToken) {
        if (StringUtils.hasText(jwtToken)) {
            String actualJwtToken = jwtToken.replaceFirst(TOKEN_PREFIX, "");
            if(JWTUtil.verify(actualJwtToken, SECRET.getBytes())){
                try {
                    JWT jwt = JWTUtil.parseToken(actualJwtToken).setKey(SECRET.getBytes());
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
