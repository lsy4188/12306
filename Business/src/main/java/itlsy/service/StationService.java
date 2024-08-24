package itlsy.service;

import itlsy.dto.RouteDTO;
import itlsy.req.StationQueryReq;
import itlsy.req.StationSaveReq;
import itlsy.resp.PageResp;
import itlsy.resp.StationQueryResp;
import itlsy.resp.StationQueryRespDTO;
import itlsy.resp.TrainStationQueryRespDTO;

import java.util.List;

public interface StationService {
    public PageResp<StationQueryResp> queryList(StationQueryReq req);
    public  void delete(Long id);

    /**
     * 查询所有站点
     * @return
     */
    public List<StationQueryRespDTO> queryAll();

    /**
     * 根据车次查询站点
     * @param trainId
     * @return
     */
    public List<TrainStationQueryRespDTO> listTrainStationQuery(String trainId);

    /**
     * 计算列车站点路线关系
     * 获取开始站点和目的站点及中间站点信息
     *
     * @param trainId   列车 ID
     * @param startStation 出发站
     * @param endStation   到达站
     * @return 列车站点路线关系信息
     */
    List<RouteDTO> listTrainStationRoute(String trainId, String startStation, String endStation);


    /**
     * 获取需列车站点扣减路线关系
     * 获取开始站点和目的站点、中间站点以及关联站点信息
     *
     * @param trainId   列车 ID
     * @param departure 出发站
     * @param arrival   到达站
     * @return 需扣减列车站点路线关系信息
     */
    List<RouteDTO> listTakeoutTrainStationRoute(String trainId, String departure, String arrival);
}
