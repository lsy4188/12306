package itlsy.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import itlsy.context.LoginMemberContext;
import itlsy.entry.Passengers;
import itlsy.entry.PassengersExample;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PassengerServiceImpl implements PassengerService {
    @Autowired
    private PassengersMapper passengersMapper;

    /**
     * 保存和更新乘车人信息
     * @param passengersSaveReq 乘车人信息
     */
    @Override
    public void SaveAndUpdate(PassengersSaveReq passengersSaveReq) {
        //校验乘客信息，暂时不添加会影响性能
        //verifyPassenger(passengersSaveReq);

        Passengers passengers = BeanUtil.copyProperties(passengersSaveReq, Passengers.class);
        if (ObjectUtil.isNull(passengers.getId())) {
            passengers.setId(SnowUtil.getSnowflakeNextId());
            passengers.setUsername(LoginMemberContext.getUserName());
            passengers.setDelFlag(false);
            passengers.setCreateTime(DateTime.now());
            passengers.setUpdateTime(DateTime.now());
            passengersMapper.insert(passengers);
        } else {
            passengers.setUpdateTime(DateTime.now());
            passengersMapper.updateByPrimaryKeySelective(passengers);
        }
    }

    private void verifyPassenger(PassengersSaveReq passengersSaveReq) {
        int length = passengersSaveReq.getRealName().length();
        if (!(length>=2 && length<=16)) {
            throw new RuntimeException("乘车人名称请设置2-16位的长度");
        }
        if (!PhoneUtil.isMobile(passengersSaveReq.getPhone())){
            throw new RuntimeException("乘车人手机号错误");
        }
        if (!IdcardUtil.isValidCard(passengersSaveReq.getIdCard())){
            throw new RuntimeException("乘车人身份证号错误");
        }
    }

    /**
     * 查询我的所有乘客
     */
    @Override
    public List<PassengersQueryResp> listPassengerQueryByUsername() {
        PassengersExample passengersExample = new PassengersExample();
        passengersExample.setOrderByClause("real_name asc");
        passengersExample.createCriteria().andUsernameEqualTo(LoginMemberContext.getUserName());
        List<Passengers> passengersList = passengersMapper.selectByExample(passengersExample);
        return BeanUtil.copyToList(passengersList,PassengersQueryResp.class);
    }

/*    @Override 方法一
    public List<PassengerActualRespDTO> listPassengerQueryByIds(String username, List<Long> ids) {
        PassengersExample passengersExample = new PassengersExample();
        passengersExample.createCriteria().andUsernameEqualTo(username);
        List<Passengers> passengerList = passengersMapper.selectByExample(passengersExample);
        String actualUserPassengerListStr= JSONUtil.toJsonStr(passengerList);
        if (StrUtil.isEmpty(actualUserPassengerListStr)){
            return null;
        }
        return  JSON.parseArray(actualUserPassengerListStr, Passengers.class)
                .stream().filter(passengerDO -> ids.contains(passengerDO.getId()))
                .map(each -> BeanUtil.copyProperties(each, PassengerActualRespDTO.class))
                .collect(Collectors.toList());
    }*/

    @Override
    public List<PassengerActualRespDTO> listPassengerQueryByIds(String username, List<Long> ids) {
        List<PassengerActualRespDTO> actualResp = new ArrayList<>();
        PassengersExample passengersExample = new PassengersExample();
        passengersExample.createCriteria().andUsernameEqualTo(username);
        List<Passengers> passengers = passengersMapper.selectByExample(passengersExample);
        for (Passengers passenger : passengers) {
            if (ids.contains(passenger.getId())){
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
        Passengers passengers = passengersMapper.selectByPrimaryKey(Long.valueOf(req.getId()));
        if (ObjectUtil.isNull(passengers)){
            throw new RuntimeException("乘客不存在");
        }
        passengersMapper.deleteByPrimaryKey(Long.valueOf(req.getId()));
    }
}
