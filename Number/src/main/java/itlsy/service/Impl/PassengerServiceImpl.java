package itlsy.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import itlsy.DistributedCache;
import itlsy.UserContext;
import itlsy.constant.RedisKeyConstant;
import itlsy.context.LoginMemberContext;
import itlsy.entry.Passengers;
import itlsy.entry.PassengersExample;
import itlsy.exception.ServiceException;
import itlsy.mapper.PassengersMapper;
import itlsy.req.PassengersQueryReq;
import itlsy.req.PassengersRemoveReq;
import itlsy.req.PassengersSaveReq;
import itlsy.resp.PageResp;
import itlsy.resp.PassengerActualRespDTO;
import itlsy.resp.PassengersQueryResp;
import itlsy.service.PassengerService;
import itlsy.util.SnowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class PassengerServiceImpl implements PassengerService {
    @Autowired
    private PassengersMapper passengersMapper;
    @Autowired
    private DistributedCache distributedCache;

    /**
     * 保存和更新乘车人信息
     *
     * @param passengersSaveReq 乘车人信息
     */
    @Override
    public void SaveAndUpdate(PassengersSaveReq passengersSaveReq) {
        //校验乘客信息，暂时不添加会影响性能
        //verifyPassenger(passengersSaveReq);
        Passengers passengers = BeanUtil.copyProperties(passengersSaveReq, Passengers.class);
        if (ObjectUtil.isNull(passengers.getId())) {
            passengers.setId(SnowUtil.getSnowflakeNextId());
            passengers.setUsername(UserContext.getUserName());
            passengers.setDelFlag(false);
            passengers.setCreateTime(DateTime.now());
            passengers.setUpdateTime(DateTime.now());
            int inserted = passengersMapper.insert(passengers);
            if (inserted<=0){
                throw new ServiceException(String.format("[%s] 新增乘车人失败",UserContext.getUserName()));
            }
        } else {
            passengers.setUpdateTime(DateTime.now());
            int updated = passengersMapper.updateByPrimaryKeySelective(passengers);
            if (updated<=0){
                throw new ServiceException(String.format("[%s] 修改乘车人失败",UserContext.getUserName()));
            }
        }
        delUserPassengerCache(UserContext.getUserName());
    }


    /**
     * 查询我的所有乘客
     */
    @Override
    public List<PassengersQueryResp> listPassengerQueryByUsername(String username) {
        String actualUserPassengerListSt = getActualUserPassengerListStr(username);
        List<PassengersQueryResp> passengersQueryResps = Optional.ofNullable(actualUserPassengerListSt)
                .map(each -> JSONUtil.parseArray(each).toList(Passengers.class))
                .map(item -> BeanUtil.copyToList(item, PassengersQueryResp.class))
                .orElse(null);
        return passengersQueryResps;
    }

    @Override
    public List<PassengerActualRespDTO> listPassengerQueryByIds(String username, List<Long> ids) {
        List<PassengerActualRespDTO> actualResp = new ArrayList<>();
        PassengersExample passengersExample = new PassengersExample();
        passengersExample.createCriteria().andUsernameEqualTo(username);
        List<Passengers> passengers = passengersMapper.selectByExample(passengersExample);
        for (Passengers passenger : passengers) {
            if (ids.contains(passenger.getId())) {
                PassengerActualRespDTO actualRespDTO = BeanUtil.copyProperties(passenger, PassengerActualRespDTO.class);
                actualResp.add(actualRespDTO);
            }
        }
        return actualResp;
    }

    @Override
    public PageResp<PassengersQueryResp> queryList(PassengersQueryReq passengersQueryReq) {

        PassengersExample passengerExample = new PassengersExample();

        passengerExample.setOrderByClause("id desc");

        PassengersExample.Criteria criteria = passengerExample.createCriteria();

        if (ObjectUtil.isNotNull(passengersQueryReq.getNumberId())) {
            // criteria.andMemberIdEqualTo(passengersQueryReq.getNumberId());

        }

        List<Passengers> passengersList = passengersMapper.selectByExample(passengerExample);

        //利用PageHelper插件实现分页
        PageHelper.startPage(passengersQueryReq.getPage(), passengersQueryReq.getSize());

        List<PassengersQueryResp> list = BeanUtil.copyToList(passengersList, PassengersQueryResp.class);

        PageResp<PassengersQueryResp> resp = new PageResp<>();
        resp.setList(list);
        resp.setTotal(new PageInfo<>(passengersList).getTotal());

        return resp;
    }

    @Override
    public void removePassenger(PassengersRemoveReq req) {
        String userName = UserContext.getUserName();
        Passengers passenger=selectPassage(userName,req.getId());
        if (ObjectUtil.isNull(passenger)) {
            throw new RuntimeException("乘客数据不存在");
        }
        PassengersExample passengersExample = new PassengersExample();
        passengersExample.createCriteria()
                .andUsernameEqualTo(userName)
                .andIdEqualTo(Long.valueOf(req.getId()));
        //逻辑删除，修改数据库表记录 del_flag
        int deleted = passengersMapper.deleteByExample(passengersExample);
        if (deleted<1){
            throw new ServiceException(String.format("[%s] 删除乘车人失败", userName));
        }
        delUserPassengerCache(userName);
    }

    private Passengers selectPassage(String userName, String passengerId) {
        PassengersExample passengersExample = new PassengersExample();
        passengersExample.createCriteria()
                .andUsernameEqualTo(userName)
                .andIdEqualTo(Long.valueOf(passengerId));
        return passengersMapper.selectByExample(passengersExample).get(0);
    }

    private void delUserPassengerCache(String userName) {
        distributedCache.delete(RedisKeyConstant.USER_PASSENGER_LIST+userName);
    }

    private void verifyPassenger(PassengersSaveReq passengersSaveReq) {
        int length = passengersSaveReq.getRealName().length();
        if (!(length >= 2 && length <= 16)) {
            throw new RuntimeException("乘车人名称请设置2-16位的长度");
        }
        if (!PhoneUtil.isMobile(passengersSaveReq.getPhone())) {
            throw new RuntimeException("乘车人手机号错误");
        }
        if (!IdcardUtil.isValidCard(passengersSaveReq.getIdCard())) {
            throw new RuntimeException("乘车人身份证号错误");
        }
    }
    private String getActualUserPassengerListStr(String username) {
        return distributedCache.safeGet(RedisKeyConstant.USER_PASSENGER_LIST + username,
                String.class,
                () -> {
                    PassengersExample passengersExample = new PassengersExample();
                    passengersExample.createCriteria().andUsernameEqualTo(username);
                    List<Passengers> passengersList = passengersMapper.selectByExample(passengersExample);
                    return CollUtil.isNotEmpty(passengersList) ? JSONUtil.toJsonStr(passengersList) : null;
                },
                1,
                TimeUnit.DAYS
        );
    }
}
