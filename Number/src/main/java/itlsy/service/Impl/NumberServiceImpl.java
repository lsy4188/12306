package itlsy.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import itlsy.entry.Member;
import itlsy.entry.MemberExample;
import itlsy.exception.BusinessException;
import itlsy.exception.BusinessExceptionEnum;
import itlsy.info.UserInfo;
import itlsy.mapper.MemberMapper;
import itlsy.req.MemberLoginReq;
import itlsy.req.MemberRegisterReq;
import itlsy.req.MemberSendCodeReq;
import itlsy.resp.*;
import itlsy.service.Numberservice;
import itlsy.util.JwtUtil;
import itlsy.util.SnowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NumberServiceImpl implements Numberservice {
    @Autowired
    private MemberMapper menberMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public CommonResp count() {
        return new CommonResp((int) menberMapper.countByExample(null));
    }

    @Override
    public NumberRegisterResp register(MemberRegisterReq req){
        //构造条件
        MemberExample menberExample = new MemberExample();
        menberExample.createCriteria().andPhoneEqualTo(req.getUsername());

        List<Member> list = menberMapper.selectByExample(menberExample);
        if (CollUtil.isNotEmpty(list)){
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);
        }

        Member number = BeanUtil.copyProperties(req, Member.class,"password");
        //密码加密
        number.setPassword(BCrypt.hashpw(req.getPassword()));
        number.setId(SnowUtil.getSnowflakeNextId());
        number.setUserType(0);
        number.setDeletionTime(0L);
        number.setCreateTime(new Date());
        number.setUpdateTime(new Date());

        menberMapper.insert(number);

        return BeanUtil.copyProperties(req,NumberRegisterResp.class);
    }

    @Override
    public CommonResp forgetPassword(MemberRegisterReq req) {
        // 检测手机号是否存在
        Member memberDB = selectByMobile(req.getPhone());
        if (ObjectUtil.isNull(memberDB)) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_NOT_EXIST);
        }
        //修改密码
        memberDB.setPassword(BCrypt.hashpw(req.getPassword()));
        menberMapper.updateByPrimaryKey(memberDB);
        return new CommonResp<>(memberDB.getId());
    }

    @Override
    public CommonResp<String> sendCode(MemberSendCodeReq req) {
        // 生成验证码
         String code = RandomUtil.randomNumbers(4);
        if (!ObjectUtil.isNull(redisTemplate.opsForValue().get(req.getMobile()))){
             throw new BusinessException(BusinessExceptionEnum.CHECK_CODE_REPLY);
         }
         redisTemplate.opsForValue().set(req.getMobile(),code,5, TimeUnit.SECONDS);

        // 保存短信记录表：手机号，短信验证码，有效期，是否已使用，业务类型，发送时间，使用时间
        log.info("保存短信记录表");

        // 对接短信通道，发送短信
        log.info("对接短信通道");

        return new CommonResp<>(code);
    }

    public MemberLoginResp login(MemberLoginReq req) {
        String username = req.getUsername();
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andUsernameEqualTo(username);
        Member memberDB = selectByuserName(req.getUsername());
        if (ObjectUtil.isNull(memberDB)) {
           throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_NOT_EXIST);
        }

        if (!BCrypt.checkpw(req.getPassword(),memberDB.getPassword())){
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_CODE_ERROR);
        }
        UserInfo userInfo = UserInfo.builder()
                .userId(StrUtil.toString(memberDB.getId()))
                .username(memberDB.getUsername())
                .realName(memberDB.getRealName())
                .build();
        String accessToken = JwtUtil.createToken(userInfo);
        MemberLoginResp resp =MemberLoginResp.builder()
                .userId(Long.valueOf(userInfo.getUserId()))
                .username(userInfo.getUsername())
                .realName(userInfo.getRealName())
                .accessToken(accessToken)
                .build();
        redisTemplate.opsForValue().set("accessToken",accessToken,30,TimeUnit.SECONDS);
        return resp;
    }

    @Override
    public void logout(String accessToken) {
        if (StrUtil.isNotBlank(accessToken)){
            redisTemplate.delete("accessToken");
        }
    }

    @Override
    public UserQueryResp queryUserByUsername(String username) {
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andUsernameEqualTo(username);
        Member number = menberMapper.selectByExample(memberExample).get(0);
        if (ObjectUtil.isNull(number)){
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_NOT_EXIST);
        }
        return BeanUtil.copyProperties(number, UserQueryResp.class);
    }

    @Override
    public UserQueryActualRespDTO queryActualUserByUsername(String username) {
        return BeanUtil.copyProperties(queryUserByUsername(username),UserQueryActualRespDTO.class);
    }

    private Member selectByuserName(String username) {
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andUsernameEqualTo(username);
        List<Member> list = menberMapper.selectByExample(memberExample);
        if (CollUtil.isEmpty(list)){
            return null;
        }else {
            return list.get(0);
        }
    }

    private Member selectByMobile(String mobile) {
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andPhoneEqualTo(mobile);
        List<Member> list = menberMapper.selectByExample(memberExample);
        if (CollUtil.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }
}
