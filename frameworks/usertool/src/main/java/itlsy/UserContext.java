package itlsy;

import com.alibaba.ttl.TransmittableThreadLocal;
import itlsy.core.UserInfo;

import java.util.Optional;

/**
 * 存储用户上下文
 */
public final class UserContext {
    /**
     * TransmittableThreadLocal:
     * 在 ThreadLocal 的基础上进行了扩展,允许在线程复用或切换时传递或继承变量值
     */
    private static final ThreadLocal<UserInfo> localUser = new TransmittableThreadLocal<>();

    /**
     * 设置用户至上下文
     *
     * @param userInfo 用户详情信息
     */
    public static void setUser(UserInfo userInfo){
        localUser.set(userInfo);
    }

    /**
     * 获取上下文中用户 ID
     *
     * @return 用户 ID
     */
    public static String getUserId(){
        UserInfo userInfo = localUser.get();
        return Optional.ofNullable(userInfo).map(UserInfo::getUserId).orElse(null);
    }

    /**
     * 获取上下文中用户名称
     *
     * @return 用户名称
     */
    public static String getUserName(){
        UserInfo userInfo = localUser.get();
        return Optional.ofNullable(userInfo).map(UserInfo::getUsername).orElse(null);
    }

    /**
     * 获取上下文中用户真实姓名
     *
     * @return 用户真实姓名
     */
    public static String getRealName(){
        UserInfo userInfo = localUser.get();
        return Optional.ofNullable(userInfo).map(UserInfo::getRealName).orElse(null);
    }

    /**
     * 获取上下文中用户 Token
     *
     * @return 用户 Token
     */
    public static String getToken(){
        UserInfo userInfo = localUser.get();
        return Optional.ofNullable(userInfo).map(UserInfo::getToken).orElse(null);
    }

    public static void removeUser(){
        localUser.remove();
    }
}
