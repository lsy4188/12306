package itlsy.service;

import itlsy.dto.RouteDTO;
import itlsy.dto.SeatTypeCountDTO;
import itlsy.dto.TrainPurchaseTicketRespDTO;

import java.util.List;

/**
 * 座位接口层
 */
public interface SeatService {
    /**
     * 锁定选中以及沿途车票状态
     *
     * @param trainId                     列车 ID
     * @param departure                   出发站
     * @param arrival                     到达站
     * @param trainPurchaseTicketRespList 乘车人以及座位信息
     */
    void lockSeat(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketRespList);

    /**
     * 查询列车有余票的车厢号集合
     *
     * @param trainId      列车 ID
     * @param seatType    座位类型
     * @param departure    出发站
     * @param arrival      到达站
     * @return 车厢号集合
     */
    List<String> listUsableCarriageNumber(String trainId, String departure, String arrival, Integer seatType);

    /**
     * 获取列车车厢余票集合
     *
     * @param trainId           列车 ID
     * @param departure         出发站
     * @param arrival           到达站
     * @param trainCarriageList 车厢编号集合
     * @return 车厢余票集合
     */
    List<Integer> listSeatRemainingTicket(String trainId, String departure, String arrival, List<String> trainCarriageList);

    /**
     * 获取列车车厢中可用的座位集合
     *
     * @param trainId        列车 ID
     * @param carriageNumber 车厢号
     * @param seatType       座位类型
     * @param departure      出发站
     * @param arrival        到达站
     * @return 可用座位集合
     */
    List<String> listAvailableSeat(String trainId, String carriageNumber, Integer seatType, String departure, String arrival);
    /**
     * 解锁选中以及沿途车票状态
     *
     * @param trainId                    列车 ID
     * @param departure                  出发站
     * @param arrival                    到达站
     * @param trainPurchaseTicketResults 乘车人以及座位信息
     */
    void unlock(String trainId, String departure, String arrival,List<TrainPurchaseTicketRespDTO> trainPurchaseTicketResults);

    /**
     * 获取列车 startStation 到 endStation 区间可用座位数量
     *
     * @param trainId      列车 ID
     * @param startStation 出发站
     * @param endStation   到达站
     * @param seatTypes    座位类型集合
     * @return 座位剩余可用数量
     */
    List<SeatTypeCountDTO> listSeatTypeCount(Long trainId, String startStation, String endStation, List<Integer> seatTypes);

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
