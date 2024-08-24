package itlsy.service;

import itlsy.req.PassengersQueryReq;
import itlsy.req.PassengersRemoveReq;
import itlsy.req.PassengersSaveReq;
import itlsy.resp.PageResp;
import itlsy.resp.PassengerActualRespDTO;
import itlsy.resp.PassengersQueryResp;

import java.util.List;

public interface PassengerService {
    /**
     * 保存和更新乘车人信息
     *
     * @param passengersSaveReq 乘车人信息
     */
    public void SaveAndUpdate(PassengersSaveReq passengersSaveReq);
    public PageResp<PassengersQueryResp> queryList(PassengersQueryReq passengersQueryReq);
    /**
     * 移除乘车人
     *
     * @param req 移除乘车人信息
     */
    public void removePassenger(PassengersRemoveReq req);
    /**
     * 根据用户名查询乘车人列表
     *
     * @return 乘车人返回列表
     */
    public List<PassengersQueryResp> listPassengerQueryByUsername();

    /**
     * 根据乘车人 ID 集合查询乘车人列表
     *
     * @param username 用户名
     * @param ids      乘车人 ID 集合
     * @return 乘车人返回列表
     */
    List<PassengerActualRespDTO> listPassengerQueryByIds(String username, List<Long> ids);
}
