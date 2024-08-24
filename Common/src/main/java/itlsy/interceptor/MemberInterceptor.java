package itlsy.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.core.context.RootContext;
import io.seata.integration.http.XidResource;
import itlsy.constant.Userconstant;
import itlsy.context.LoginMemberContext;
import itlsy.info.UserInfo;
import itlsy.resp.UserLoginResp;
import itlsy.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 拦截器：Spring框架特有的，常用于登录校验，权限校验，请求日志打印
 */
@Component
@Slf4j
public class MemberInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(MemberInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        LOG.info("NumberInterceptor 开始");
        //解决远程传输全局事务id的问题
        String xid = RootContext.getXID();
        log.info("全局事务id为{}", xid);
        String txXid = request.getHeader("TX_XID");
        log.info("当前请求头中的TX_TID为{}", txXid);
        if (xid == null && txXid != null) {
            RootContext.bind(txXid);
        }

        //获取header的token参数
        String token = request.getHeader(Userconstant.USER_TOKEN_KEY);
        if (StrUtil.isNotBlank(token)) {
            LOG.info("获取会员登录token：{}", token);
            UserInfo loginMember = JwtUtil.parseJwtToken(token);
            System.out.println(loginMember);
            LOG.info("当前登录会员：{}", loginMember);
            //将loginMember转换成MemberLoginResp类
            UserLoginResp member = BeanUtil.copyProperties(loginMember, UserLoginResp.class);
            //将MemberLoginResp对象设置到LoginMemberContext中
            LoginMemberContext.setMember(member);

            LOG.info(LoginMemberContext.getId().toString());
        }
        LOG.info("NumberInterceptor 结束");

        return true;
    }

    //事务完成后清除xid
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (RootContext.inGlobalTransaction()) {
            XidResource.cleanXid(request.getHeader(RootContext.KEY_XID));
        }
    }

}
