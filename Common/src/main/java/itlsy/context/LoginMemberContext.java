package itlsy.context;


import itlsy.resp.UserLoginResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginMemberContext {
    private static final Logger LOG = LoggerFactory.getLogger(LoginMemberContext.class);

    //TODO MemberLoginResp需要换成UserInfo
    private static ThreadLocal<UserLoginResp> member = new ThreadLocal<>();

    public static UserLoginResp getMember() {
        return member.get();
    }

    public static void setMember(UserLoginResp member) {
        LoginMemberContext.member.set(member);
    }

    public static Long getId() {
        try {
            return member.get().getUserId();
        } catch (Exception e) {
            LOG.error("获取登录会员信息异常", e);
            throw e;
        }
    }
    public static String getUserName(){
        try {
            return member.get().getUsername();
        } catch (Exception e) {
            LOG.error("获取登录会员信息异常", e);
            throw e;
        }
    }

}
