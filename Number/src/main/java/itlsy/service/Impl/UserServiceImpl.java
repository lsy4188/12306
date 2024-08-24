package itlsy.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import itlsy.DistributedCache;
import itlsy.chain.AbstractChainContext;
import itlsy.constant.RedisKeyConstant;
import itlsy.entry.*;
import itlsy.enums.UserChainMarkEnum;
import itlsy.enums.UserRegisterErrorCodeEnum;
import itlsy.exception.ClientException;
import itlsy.exception.ServiceException;
import itlsy.info.UserInfo;
import itlsy.mapper.*;
import itlsy.req.UserLoginReq;
import itlsy.req.UserRegisterReq;
import itlsy.req.UserUpdateReq;
import itlsy.resp.*;
import itlsy.service.UserService;
import itlsy.util.JwtUtil;
import itlsy.util.SnowUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static itlsy.util.UserReuseUtil.hashShardingIdx;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private UserPhoneMapper userPhoneMapper;
    @Autowired
    private UserMailMapper userMailMapper;
    @Autowired
    private UserReuseMapper userReuseMapper;
    @Autowired
    private UserDeletionMapper userDeletionMapper;
    @Autowired
    private DistributedCache distributedCache;
    @Autowired
    private AbstractChainContext<UserRegisterReq> abstractChainContext;
    @Autowired
    private RBloomFilter<String> userRegisterCachePenetrationBloomFilter;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRegisterResp register(UserRegisterReq requestParam) {
        //首先进行参数校验
        abstractChainContext.handler(UserChainMarkEnum.USER_REGISTER_FILTER.name(), requestParam);
        //加锁
        RLock lock = redissonClient.getLock(RedisKeyConstant.LOCK_USER_REGISTER + requestParam.getUsername());
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            throw new ServiceException(UserRegisterErrorCodeEnum.HAS_USERNAME_NOTNULL.message());
        }
        User numberInfo = BeanUtil.copyProperties(requestParam, User.class);
        numberInfo.setId(SnowUtil.getSnowflakeNextId());
        numberInfo.setUserType(0);
        numberInfo.setDeletionTime(0L);
        numberInfo.setCreateTime(new Date());
        numberInfo.setUpdateTime(new Date());
        try {
            try {
                int result = userMapper.insert(numberInfo);
                if (result <= 0) {
                    throw new ServiceException(UserRegisterErrorCodeEnum.USER_REGISTER_FAIL.message());
                }
            } catch (DuplicateKeyException e) {
                log.error("用户名 [{}] 重复注册", requestParam.getUsername());
                throw new ServiceException(UserRegisterErrorCodeEnum.HAS_USERNAME_NOTNULL.message());
            }
            UserPhone userPhoneInfo = UserPhone.builder()
                    .id(SnowUtil.getSnowflakeNextId())
                    .phone(requestParam.getPhone())
                    .username(requestParam.getUsername())
                    .deletionTime(0L)
                    .createTime(new Date())
                    .updateTime(new Date())
                    .delFlag(false)
                    .build();
            try {
                userPhoneMapper.insert(userPhoneInfo);
            } catch (DuplicateKeyException e) {
                log.error("用户 [{}] 注册手机号 [{}] 重复", requestParam.getUsername(), requestParam.getPhone());
                throw new ServiceException(UserRegisterErrorCodeEnum.PHONE_REGISTERED.message());
            }
            if (StrUtil.isNotBlank(requestParam.getMail())) {
                UserMail userMailInfo = UserMail.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .mail(requestParam.getMail())
                        .username(requestParam.getUsername())
                        .deletionTime(0L)
                        .createTime(new Date())
                        .updateTime(new Date())
                        .delFlag(false)
                        .build();
                try {
                    userMailMapper.insert(userMailInfo);
                } catch (DuplicateKeyException e) {
                    log.error("用户 [{}] 注册邮箱 [{}] 重复", requestParam.getUsername(), requestParam.getMail());
                    throw new ServiceException(UserRegisterErrorCodeEnum.MAIL_REGISTERED.message());
                }
            }
            UserReuseExample userReuseExample = new UserReuseExample();
            userReuseExample.createCriteria().andUsernameEqualTo(requestParam.getUsername());
            userReuseMapper.deleteByExample(userReuseExample);
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            instance.opsForSet().remove(RedisKeyConstant.USER_REGISTER_REUSE_SHARDING + hashShardingIdx(requestParam.getUsername()), requestParam.getUsername());
            //TODO 疑问：布隆过滤器设计问题：设置多大、碰撞率以及初始容量不够了怎么办？
            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
        } finally {
            lock.unlock();
        }
        return BeanUtil.copyProperties(requestParam, UserRegisterResp.class);
    }


    public UserLoginResp login(UserLoginReq requestParam) {
        String usernameOrMailOrPhone = requestParam.getUsernameOrMailOrPhone();
        boolean mailFlag = false;
        for (char c : usernameOrMailOrPhone.toCharArray()) {
            if (c == '@') {
                mailFlag = true;
                break;
            }
        }
        String username;
        if (mailFlag) {
            UserMailExample userMailExample = new UserMailExample();
            userMailExample.createCriteria()
                    .andMailEqualTo(usernameOrMailOrPhone);
            List<UserMail> userMail = userMailMapper.selectByExample(userMailExample);
            if (ObjectUtil.isNull(userMail)) {
                throw new ClientException("用户名/手机号/邮箱不存在");
            }
        } else {
            UserPhoneExample userPhoneExample = new UserPhoneExample();
            userPhoneExample.createCriteria()
                    .andPhoneEqualTo(usernameOrMailOrPhone);
            List<UserPhone> userPhone = userPhoneMapper.selectByExample(userPhoneExample);
            if (ObjectUtil.isNull(userPhone)) {
                username = null;
            }
        }
        username = requestParam.getUsernameOrMailOrPhone();
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andUsernameEqualTo(username)
                .andPasswordEqualTo(requestParam.getPassword());
        User user = userMapper.selectByExample(userExample).get(0);
        if (ObjectUtil.isNull(user)) {
            throw new ServiceException("账号不存在或密码错误");
        }
        UserInfo userInfo = UserInfo.builder()
                .userId(String.valueOf(user.getId()))
                .username(user.getUsername())
                .realName(user.getRealName())
                .build();
        String accessToken = JwtUtil.createToken(userInfo);
        UserLoginResp actual = UserLoginResp.builder()
                .userId(Long.valueOf(userInfo.getUserId()))
                .username(requestParam.getUsernameOrMailOrPhone())
                .realName(user.getRealName())
                .accessToken(accessToken)
                .build();
        distributedCache.put(accessToken, JSONUtil.toJsonStr(actual), 30, TimeUnit.MINUTES);
        return actual;
    }

    @Override
    public void logout(String accessToken) {
        if (StrUtil.isNotBlank(accessToken)) {
            distributedCache.delete(accessToken);
        }
    }

    @Override
    public UserQueryResp queryUserByUsername(String username) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andUsernameEqualTo(username);
        User number = userMapper.selectByExample(userExample).get(0);
        if (ObjectUtil.isNull(number)) {
            throw new ClientException("用户不存在，请检查用户名是否正确");
        }
        return BeanUtil.copyProperties(number, UserQueryResp.class);
    }

    @Override
    public UserQueryActualRespDTO queryActualUserByUsername(String username) {
        return BeanUtil.copyProperties(queryUserByUsername(username), UserQueryActualRespDTO.class);
    }

    @Override
    public Boolean hasUsername(String username) {
        boolean hasUserName = userRegisterCachePenetrationBloomFilter.contains(username);
        if (!hasUserName) {
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            return instance.opsForSet().isMember(RedisKeyConstant.USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
        }
        return true;
    }

    @Override
    public Integer queryUserDeletionNum(Integer idType, String idCard) {
        UserDeletionExample userDeletionExample = new UserDeletionExample();
        userDeletionExample.createCriteria()
                .andIdTypeEqualTo(idType)
                .andIdCardEqualTo(idCard);
        // TODO 此处应该先查缓存
        List<UserDeletion> userDeletions = userDeletionMapper.selectByExample(userDeletionExample);
        return userDeletions.isEmpty() ? 0 : userDeletions.size();
    }

    @Override
    public void update(UserUpdateReq requestParam) {
        UserQueryResp userQueryResp = queryUserByUsername(requestParam.getUsername());
        User user = BeanUtil.copyProperties(requestParam, User.class);
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andUsernameEqualTo(requestParam.getUsername());
        int updated = userMapper.updateByExampleSelective(user, userExample);
        if (updated < 1) {
            throw new ServiceException(String.format("用户{%s}更新失败", requestParam.getUsername()));
        }
        if (StrUtil.isNotBlank(requestParam.getMail()) && !ObjectUtil.equals(requestParam.getMail(), userQueryResp.getMail())) {
            UserMailExample UpdateUserMail = new UserMailExample();
            UpdateUserMail.createCriteria()
                    .andMailEqualTo(userQueryResp.getMail());
            userMailMapper.deleteByExample(UpdateUserMail);
            UserMail userMailDO = UserMail.builder()
                    .id(SnowUtil.getSnowflakeNextId())
                    .mail(requestParam.getMail())
                    .username(requestParam.getUsername())
                    .createTime(new Date())
                    .updateTime(new Date())
                    .delFlag(false)
                    .build();
            userMailMapper.insert(userMailDO);
        }

    }
}
