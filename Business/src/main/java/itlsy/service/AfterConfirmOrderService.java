package itlsy.service;

import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import itlsy.entry.ConfirmOrder;
import itlsy.entry.DailyTrainSeat;
import itlsy.entry.DailyTrainTicket;
import itlsy.enums.ConfirmOrderStatusEnum;
import itlsy.feign.NumberFeign;
import itlsy.mapper.ConfirmOrderMapper;
import itlsy.mapper.DailyTrainSeatMapper;
import itlsy.mapper.cust.DailyTrainTicketMapperCust;
import itlsy.req.ConfirmOrderTicketReq;
import itlsy.req.NumberTicketReq;
import itlsy.resp.CommonResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AfterConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(AfterConfirmOrderService.class);

    @Autowired
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    @Autowired
    private DailyTrainTicketMapperCust dailyTrainTicketMapperCust;

    @Autowired
    private NumberFeign numberFeign;

    @Autowired
    private ConfirmOrderMapper confirmOrderMapper;
    /**
     *  选中座位后事务处理：
     *    座位表修改售卖情况sell；
     *    余票详情表修改余票；
     *    为会员增加购票记录
     *    更新确认订单为成功
     */
//    @Transactional传统事务无法回滚
    @GlobalTransactional(rollbackFor = Exception.class)
    public void afterDoConfirm(DailyTrainTicket dailyTrainTicket, List<DailyTrainSeat> finalSeatList, List<ConfirmOrderTicketReq> tickets,ConfirmOrder confirmOrder) throws Exception {
        LOG.info("seata的全局事务id:{}", RootContext.getXID());
        for (int j = 0; j < finalSeatList.size(); j++) {
            DailyTrainSeat seat = finalSeatList.get(j);
            DailyTrainSeat dailyTrainSeat = new DailyTrainSeat();
            dailyTrainSeat.setId(seat.getId());
            dailyTrainSeat.setSell(seat.getSell());
            dailyTrainSeat.setUpdateTime(new Date());
            //updateByPrimaryKeySelective->更新其中的属性
            dailyTrainSeatMapper.updateByPrimaryKeySelective(dailyTrainSeat);
            /**
             * 1.计算这个站卖出去后，影响了哪些站的余票库存
             * 2.影响的库存：本次选座之前没买过票的，和本次购买的区间有交集的区间
             * 如：10个站，本次买4~7站
             * 原售：001000001
             * 购买：000011100
             * 新售：001011101
             * 影响：xxx11111x
             * Integer startIndex=4;
             * Integer endIndex=7;
             * Integer minStartIndex=startIndex-往前碰到的最后一个0；
             * Integer maxStartIndex=endIndex-1
             * Integer minEndIndex=startIndex+1;
             * Integer maxEndIndex=endIndex+往后碰到的最后一个0；
             */
            Integer startIndex = dailyTrainTicket.getStartIndex();
            Integer endIndex = dailyTrainTicket.getEndIndex();
            char[] chars = dailyTrainSeat.getSell().toCharArray();
            Integer maxStartIndex = endIndex - 1;
            Integer minEndIndex = startIndex + 1;

            //类似于Integer minStartIndex=startIndex-往前碰到的最后一个0；
            Integer minStartIndex = 0;
            for (int i = startIndex - 1; i >= 0; i--) {
                char aChar = chars[i];
                if (aChar == '1') {
                    minStartIndex = i + 1;
                    break;
                }
            }
            LOG.info("影响的出发站期间为：{}~{}", minStartIndex, maxStartIndex);

            //类似于Integer maxEndIndex=endIndex+往后碰到的最后一个0；
            Integer maxEndIndex = dailyTrainSeat.getSell().length();
            for (int i = endIndex; i < dailyTrainSeat.getSell().length(); i++) {
                char aChar = chars[i];
                if (aChar == '1') {
                    maxEndIndex = i;
                    break;
                }
            }
            LOG.info("影响的到达站期间为：{}~{}", minEndIndex, maxEndIndex);

            dailyTrainTicketMapperCust.updateCountBySell(
                    seat.getDate(),
                    seat.getTrainCode(),
                    seat.getSeatType(),
                    minStartIndex,
                    maxStartIndex,
                    minEndIndex,
                    maxEndIndex);

            //调用会员服务接口，为会员增加一张车票
            NumberTicketReq numberTicketReq = new NumberTicketReq();
            //numberTicketReq.setNumberId(LoginMemberContext.getId());
            numberTicketReq.setNumberId(confirmOrder.getNumberId());
            numberTicketReq.setPassengerId(tickets.get(j).getPassengerId());
            numberTicketReq.setPassengerName(tickets.get(j).getPassengerName());
            numberTicketReq.setTrainDate(dailyTrainTicket.getDate());
            numberTicketReq.setTrainCode(dailyTrainTicket.getTrainCode());
            numberTicketReq.setCarriageIndex(seat.getCarriageIndex());
            numberTicketReq.setSeatRow(seat.getRow());
            numberTicketReq.setSeatCol(seat.getCol());
            numberTicketReq.setStartStation(dailyTrainTicket.getStart());
            numberTicketReq.setStartTime(dailyTrainTicket.getStartTime());
            numberTicketReq.setEndStation(dailyTrainTicket.getEnd());
            numberTicketReq.setEndTime(dailyTrainTicket.getEndTime());
            numberTicketReq.setSeatType(seat.getSeatType());
            CommonResp<Object>  commonResp = numberFeign.save(numberTicketReq);
            LOG.info("调用number接口，返回：{}",commonResp);

            //更新订单状态为成功
            ConfirmOrder updateOrder = new ConfirmOrder();
            updateOrder.setId(confirmOrder.getId());
            updateOrder.setUpdateTime(new Date());
            updateOrder.setStatus(ConfirmOrderStatusEnum.SUCCESS.getCode());
            confirmOrderMapper.updateByPrimaryKeySelective(updateOrder);

//            //模拟调用异常
//            if (1==1) {
//                throw new Exception("调用异常");
//            }
        }
    }
}
