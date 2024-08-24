package itlsy.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Pair;
import com.google.common.collect.Lists;
import itlsy.dto.PurchaseTicketPassengerDetailDTO;
import itlsy.dto.SelectSeatDTO;
import itlsy.dto.TrainPurchaseTicketRespDTO;
import itlsy.dto.TrainSeatBaseDTO;
import itlsy.enums.VehicleSeatTypeEnum;
import itlsy.enums.VehicleTypeEnum;
import itlsy.handler.base.AbstractTrainPurchaseTicketTemplate;
import itlsy.handler.select.SeatSelection;
import itlsy.service.SeatService;
import itlsy.util.CarriageVacantSeatCalculateUtil;
import itlsy.util.SeatNumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 高铁商务座购票组件
 */
@Component
public class TrainBusinessClassPurchaseTicketHandler extends AbstractTrainPurchaseTicketTemplate {

    @Autowired
    private SeatService seatService;
    private static final Map<Character, Integer> SEAT_Y_INT = Map.of('A', 0, 'C', 1, 'F', 2);

    @Override
    public String mark() {
        return VehicleTypeEnum.HIGH_SPEED_RAIN.getName() + VehicleSeatTypeEnum.BUSINESS_CLASS.getName();
    }

    @Override
    protected List<TrainPurchaseTicketRespDTO> selectSeats(SelectSeatDTO requestParam) {
        String trainId = requestParam.getRequestParam().getTrainId();
        String departure = requestParam.getRequestParam().getDeparture();
        String arrival = requestParam.getRequestParam().getArrival();
        // 获取请求参数中的乘客座位信息
        List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails = requestParam.getPassengerSeatDetails();
        // 调用座位服务，获取可用车厢号
        List<String> trainCarriageList = seatService.listUsableCarriageNumber(trainId, departure, arrival, requestParam.getSeatType());
        // 调用座位服务，获取各车厢剩余票数
        List<Integer> trainStationCarriageRemainingTicket = seatService.listSeatRemainingTicket(trainId, departure, arrival, trainCarriageList);
        // 计算各车厢剩余票总数
        int remainingTicketSum = trainStationCarriageRemainingTicket.stream().mapToInt(Integer::intValue).sum();
        if (remainingTicketSum < passengerSeatDetails.size()) {
            throw new RuntimeException("站点余票不足，请尝试更换座位类型或选择其它站点");
        }
        if (passengerSeatDetails.size() < 3) {
            if (CollUtil.isNotEmpty(requestParam.getRequestParam().getChooseSeats())) {
                Pair<List<TrainPurchaseTicketRespDTO>, Boolean> actualSeatPair = findMatchSeats(requestParam, trainCarriageList, trainStationCarriageRemainingTicket);
                return actualSeatPair.getKey();
            }
            return selectSeats(requestParam,trainCarriageList,trainStationCarriageRemainingTicket);
        }else {
            if (CollUtil.isNotEmpty(requestParam.getRequestParam().getChooseSeats())){
                Pair<List<TrainPurchaseTicketRespDTO>, Boolean> actualSeatPair = findMatchSeats(requestParam, trainCarriageList, trainStationCarriageRemainingTicket);
                return actualSeatPair.getKey();
            }
            return selComplexSeats(requestParam, trainCarriageList, trainStationCarriageRemainingTicket);
        }
    }

    private List<TrainPurchaseTicketRespDTO> selComplexSeats(SelectSeatDTO requestParam, List<String> trainCarriageList, List<Integer> trainStationCarriageRemainingTicket) {
        String trainId = requestParam.getRequestParam().getTrainId();
        String departure = requestParam.getRequestParam().getDeparture();
        String arrival = requestParam.getRequestParam().getArrival();
        List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails = requestParam.getPassengerSeatDetails();
        List<TrainPurchaseTicketRespDTO> actualResult = new ArrayList<>();
        Map<String, Integer> demotionStockNumMap = new LinkedHashMap<>();
        Map<String, int[][]> actualSeatsMap = new HashMap<>();
        Map<String, int[][]> carriagesNumberSeatsMap = new HashMap<>();
        String carriagesNumber;
        for (int i = 0; i < trainStationCarriageRemainingTicket.size(); i++) {
            carriagesNumber = trainCarriageList.get(i);
            List<String> listAvailableSeat = seatService.listAvailableSeat(trainId, carriagesNumber, requestParam.getSeatType(), departure, arrival);
            int[][] actualSeats = new int[2][3];
            for (int j = 1; j < 3; j++) {
                for (int k = 1; k < 4; k++) {
                    // 当前默认按照复兴号商务座排序，后续这里需要按照简单工厂对车类型进行获取 y 轴
                    actualSeats[j - 1][k - 1] = listAvailableSeat.contains("0" + j + SeatNumberUtil.convert(0, k)) ? 0 : 1;
                }
            }
            int[][] actualSeatsTranscript = deepCopy(actualSeats);
            List<int[][]> actualSelects = new ArrayList<>();
            List<List<PurchaseTicketPassengerDetailDTO>> splitPassengerSeatDetails = ListUtil.split(passengerSeatDetails, 2);
            for (List<PurchaseTicketPassengerDetailDTO> each : splitPassengerSeatDetails) {
                int[][] select = SeatSelection.adjacent(each.size(), actualSeatsTranscript);
                if (select != null) {
                    for (int[] ints : select) {
                        actualSeatsTranscript[ints[0] - 1][ints[1] - 1] = 1;
                    }
                    actualSelects.add(select);
                }
            }
            if (actualSelects.size() == splitPassengerSeatDetails.size()) {
                int[][] actualSelect = null;
                for (int j = 0; j < actualSelects.size(); j++) {
                    if (j == 0) {
                        actualSelect = mergeArrays(actualSelects.get(j), actualSelects.get(j + 1));
                    }
                    if (j != 0 && actualSelects.size() > 2) {
                        actualSelect = mergeArrays(actualSelect, actualSelects.get(j + 1));
                    }
                }
                carriagesNumberSeatsMap.put(carriagesNumber, actualSelect);
                break;
            }
            int demotionStockNum = 0;
            for (int[] actualSeat : actualSeats) {
                for (int i1 : actualSeat) {
                    if (i1 == 0) {
                        demotionStockNum++;
                    }
                }
            }
            demotionStockNumMap.putIfAbsent(carriagesNumber, demotionStockNum);
            actualSeatsMap.putIfAbsent(carriagesNumber, actualSeats);
        }
        // 如果邻座算法无法匹配，尝试对用户进行降级分配：同车厢不邻座
        if (CollUtil.isEmpty(carriagesNumberSeatsMap)) {
            for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                String carriagesNumberBack = entry.getKey();
                int demotionStockNumBack = entry.getValue();
                if (demotionStockNumBack > passengerSeatDetails.size()) {
                    int[][] seats = actualSeatsMap.get(carriagesNumberBack);
                    int[][] nonAdjacentSeats = SeatSelection.nonAdjacent(passengerSeatDetails.size(), seats);
                    if (Objects.equals(nonAdjacentSeats.length, passengerSeatDetails.size())) {
                        carriagesNumberSeatsMap.put(carriagesNumberBack, nonAdjacentSeats);
                        break;
                    }
                }
            }
        }
        // 如果同车厢也已无法匹配，则对用户座位再次降级：不同车厢不邻座
        if (CollUtil.isEmpty(carriagesNumberSeatsMap)) {
            int undistributedPassengerSize = passengerSeatDetails.size();
            for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                String carriagesNumberBack = entry.getKey();
                int demotionStockNumBack = entry.getValue();
                int[][] seats = actualSeatsMap.get(carriagesNumberBack);
                int[][] nonAdjacentSeats = SeatSelection.nonAdjacent(Math.min(undistributedPassengerSize, demotionStockNumBack), seats);
                undistributedPassengerSize = undistributedPassengerSize - demotionStockNumBack;
                carriagesNumberSeatsMap.put(entry.getKey(), nonAdjacentSeats);
            }
        }
        // 乘车人员在单一车厢座位不满足，触发乘车人元分布在不同车厢
        int count = (int) carriagesNumberSeatsMap.values().stream()
                .flatMap(Arrays::stream)
                .count();
        if (CollUtil.isNotEmpty(carriagesNumberSeatsMap) && passengerSeatDetails.size() == count) {
            int countNum = 0;
            for (Map.Entry<String, int[][]> entry : carriagesNumberSeatsMap.entrySet()) {
                List<String> selectSeats = new ArrayList<>();
                for (int[] ints : entry.getValue()) {
                    selectSeats.add("0" + ints[0] + SeatNumberUtil.convert(0, ints[1]));
                }
                for (String selectSeat : selectSeats) {
                    TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                    PurchaseTicketPassengerDetailDTO currentTicketPassenger = passengerSeatDetails.get(countNum++);
                    result.setSeatNumber(selectSeat);
                    result.setSeatType(currentTicketPassenger.getSeatType());
                    result.setCarriageNumber(entry.getKey());
                    result.setPassengerId(currentTicketPassenger.getPassengerId());
                    actualResult.add(result);
                }
            }
        }
        return actualResult;
    }

    private int[][] deepCopy(int[][] originalArray) {
        int[][] copy = new int[originalArray.length][originalArray[0].length];
        for (int i = 0; i < originalArray.length; i++) {
            System.arraycopy(originalArray[i], 0, copy[i], 0, originalArray[i].length);
        }
        return copy;
    }

    private int[][] mergeArrays(int[][] arr1, int[][] arr2) {
        ArrayList<int[]> list = new ArrayList<>(Arrays.asList(arr1));
        list.addAll(Arrays.asList(arr2));
        return list.toArray(new int[0][]);
    }

    private List<TrainPurchaseTicketRespDTO> selectSeats(SelectSeatDTO requestParam, List<String> trainCarriageList, List<Integer> trainStationCarriageRemainingTicket) {
        String trainId = requestParam.getRequestParam().getTrainId();
        String departure = requestParam.getRequestParam().getDeparture();
        String arrival = requestParam.getRequestParam().getArrival();
        List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails = requestParam.getPassengerSeatDetails();
        List<TrainPurchaseTicketRespDTO> actualResult = new ArrayList<>();
        Map<String, Integer> demotionStockNumMap = new LinkedHashMap<>();
        Map<String, int[][]> actualSeatsMap = new HashMap<>();
        Map<String, int[][]> carriagesNumberSeatsMap = new HashMap<>();
        String carriagesNumber;
        for (int i = 0; i < trainStationCarriageRemainingTicket.size(); i++) {
            carriagesNumber = trainCarriageList.get(i);
            List<String> listAvailableSeat = seatService.listAvailableSeat(trainId, carriagesNumber, requestParam.getSeatType(), departure, arrival);
            int[][] actualSeats = new int[2][3];
            for (int j = 1; j < 3; j++) {
                for (int k = 1; k < 4; k++) {
                    // 当前默认按照复兴号商务座排序，后续这里需要按照简单工厂对车类型进行获取 y 轴
                    actualSeats[j - 1][k - 1] = listAvailableSeat.contains("0" + j + SeatNumberUtil.convert(0, k)) ? 0 : 1;
                }
            }
            int[][] select = SeatSelection.adjacent(passengerSeatDetails.size(), actualSeats);
            if (select != null) {
                carriagesNumberSeatsMap.put(carriagesNumber, select);
                break;
            }
            int demotionStockNum = 0;
            for (int[] actualSeat : actualSeats) {
                for (int i1 : actualSeat) {
                    if (i1 == 0) {
                        demotionStockNum++;
                    }
                }
            }
            demotionStockNumMap.putIfAbsent(carriagesNumber, demotionStockNum);
            actualSeatsMap.putIfAbsent(carriagesNumber, actualSeats);
            if (i < trainStationCarriageRemainingTicket.size() - 1) {
                continue;
            }
            // 如果邻座算法无法匹配，尝试对用户进行降级分配：同车厢不邻座
            for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                String carriagesNumberBack = entry.getKey();
                int demotionStockNumBack = entry.getValue();
                if (demotionStockNumBack > passengerSeatDetails.size()) {
                    int[][] seats = actualSeatsMap.get(carriagesNumberBack);
                    int[][] nonAdjacentSeats = SeatSelection.nonAdjacent(passengerSeatDetails.size(), seats);
                    if (Objects.equals(nonAdjacentSeats.length, passengerSeatDetails.size())) {
                        select = nonAdjacentSeats;
                        carriagesNumberSeatsMap.put(carriagesNumberBack, select);
                        break;
                    }
                }
            }
            // 如果同车厢也已无法匹配，则对用户座位再次降级：不同车厢不邻座
            if (Objects.isNull(select)) {
                for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                    String carriagesNumberBack = entry.getKey();
                    int demotionStockNumBack = entry.getValue();
                    int[][] seats = actualSeatsMap.get(carriagesNumberBack);
                    int[][] nonAdjacentSeats = SeatSelection.nonAdjacent(demotionStockNumBack, seats);
                    carriagesNumberSeatsMap.put(entry.getKey(), nonAdjacentSeats);
                }
            }
        }
        int count = (int) carriagesNumberSeatsMap.values().stream()
                .flatMap(Arrays::stream)
                .count();
        if (CollUtil.isNotEmpty(carriagesNumberSeatsMap) && passengerSeatDetails.size() == count) {
            int countNum = 0;
            for (Map.Entry<String, int[][]> entry : carriagesNumberSeatsMap.entrySet()) {
                List<String> selectSeats = new ArrayList<>();
                for (int[] ints : entry.getValue()) {
                    selectSeats.add("0" + ints[0] + SeatNumberUtil.convert(0, ints[1]));
                }
                for (String selectSeat : selectSeats) {
                    TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                    PurchaseTicketPassengerDetailDTO currentTicketPassenger = passengerSeatDetails.get(countNum++);
                    result.setSeatNumber(selectSeat);
                    result.setSeatType(currentTicketPassenger.getSeatType());
                    result.setCarriageNumber(entry.getKey());
                    result.setPassengerId(currentTicketPassenger.getPassengerId());
                    actualResult.add(result);
                }
            }
        }
        return actualResult;
    }


    private Pair<List<TrainPurchaseTicketRespDTO>, Boolean> findMatchSeats(SelectSeatDTO requestParam, List<String> trainCarriageList, List<Integer> trainStationCarriageRemainingTicket) {
        TrainSeatBaseDTO trainSeatBaseDTO = buildTrainSeatBaseDTO(requestParam);
        // 缓存中的变量 int chooseSeatSize = trainSeatBaseDTO.getChooseSeatList().size();
        List<TrainPurchaseTicketRespDTO> actualResult = Lists.newArrayListWithCapacity(trainSeatBaseDTO.getPassengerSeatDetails().size());
        HashMap<String, List<Pair<Integer, Integer>>> carriagesSeatMap = new HashMap<>(4);
        int passengersNumber = trainSeatBaseDTO.getPassengerSeatDetails().size();
        for (int i = 0; i < trainStationCarriageRemainingTicket.size(); i++) {
            //获取每一个车厢的编号
            String carriagesNumber = trainCarriageList.get(i);
            //调用seatService的listAvailableSeat方法，获取指定车厢的可用座位
            List<String> listAvailableSeat = seatService.listAvailableSeat(trainSeatBaseDTO.getTrainId(), carriagesNumber, requestParam.getSeatType(), trainSeatBaseDTO.getDeparture(), trainSeatBaseDTO.getArrival());
            //定义一个二维数组，用来存放实际座位情况
            int[][] actualSeats = new int[2][3];
            //遍历每一个车厢
            for (int j = 1; j < 3; j++) {
                //遍历每一个座位
                for (int k = 1; k < 4; k++) {
                    //判断该座位是否可用
                    actualSeats[j - 1][k - 1] = listAvailableSeat.contains("0" + j + SeatNumberUtil.convert(0, k)) ? 0 : 1;
                }
            }
            // 构建一个空的座位列表，长度为2，每排3个座位
            List<Pair<Integer, Integer>> vacantSeatList = CarriageVacantSeatCalculateUtil.buildCarriageVacantSeatList2(actualSeats, 2, 3);
            // 获取空座位数量
            int vacantSeatCount = vacantSeatList.size();
            // 构建一个确定的座位列表
            List<Pair<Integer, Integer>> sureSeatList = new ArrayList<>();
            // 构建一个选择的座位列表，长度为乘客数量
            List<String> selectSeats = Lists.newArrayListWithCapacity(passengersNumber);
            //TODO 等待添加缓存
            if (i < trainStationCarriageRemainingTicket.size()) {
                if (vacantSeatCount > 0) {
                    carriagesSeatMap.put(carriagesNumber, vacantSeatList);
                }
                if (i == trainStationCarriageRemainingTicket.size() - 1) {
                    Pair<String, List<Pair<Integer, Integer>>> findSureCarriage = null;
                    for (Map.Entry<String, List<Pair<Integer, Integer>>> entry : carriagesSeatMap.entrySet()) {
                        if (entry.getValue().size() >= passengersNumber) {
                            //TODO
                            findSureCarriage = new Pair<>(entry.getKey(), entry.getValue().subList(0, passengersNumber));//subList创建一个新列表从0-passengersNumber的范围
                            break;
                        }
                    }

                    if (findSureCarriage != null) {
                        sureSeatList = findSureCarriage.getValue().subList(0, passengersNumber);
                        for (Pair<Integer, Integer> item : sureSeatList) {
                            selectSeats.add("0" + (item.getKey() + 1) + SeatNumberUtil.convert(0, (item.getValue()+ 1)));
                        }
                        AtomicInteger countNum = new AtomicInteger(0);
                        for (String selectSeat : selectSeats) {
                            TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                            PurchaseTicketPassengerDetailDTO currentTicketPassenger = trainSeatBaseDTO.getPassengerSeatDetails().get(countNum.getAndIncrement());
                            result.setSeatNumber(selectSeat);
                            result.setSeatType(currentTicketPassenger.getSeatType());
                            result.setCarriageNumber(findSureCarriage.getKey());
                            result.setPassengerId(currentTicketPassenger.getPassengerId());
                            actualResult.add(result);
                        }
                    } else {
                        int sureSeatListSize = 0;
                        AtomicInteger countNum = new AtomicInteger(0);
                        for (Map.Entry<String, List<Pair<Integer, Integer>>> entry : carriagesSeatMap.entrySet()) {
                            if (sureSeatListSize<passengersNumber){
                                if (sureSeatListSize+entry.getValue().size()<passengersNumber){
                                    sureSeatListSize=sureSeatListSize+entry.getValue().size();
                                    List<String> actualSelectSeats = new ArrayList<>();
                                    for (Pair<Integer, Integer> each : entry.getValue()) {
                                        actualSelectSeats.add("0" + (each.getKey() + 1) + SeatNumberUtil.convert(0, each.getValue() + 1));
                                    }
                                    for (String selectSeat : actualSelectSeats) {
                                        TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                                        PurchaseTicketPassengerDetailDTO currentTicketPassenger = trainSeatBaseDTO.getPassengerSeatDetails().get(countNum.getAndIncrement());
                                        result.setSeatNumber(selectSeat);
                                        result.setSeatType(currentTicketPassenger.getSeatType());
                                        result.setCarriageNumber(entry.getKey());
                                        result.setPassengerId(currentTicketPassenger.getPassengerId());
                                        actualResult.add(result);
                                    }
                                }else {
                                    int needSeatSize = entry.getValue().size() - (sureSeatListSize + entry.getValue().size() - passengersNumber);
                                    sureSeatListSize = sureSeatListSize + needSeatSize;
                                    if (sureSeatListSize >= passengersNumber) {
                                        List<String> actualSelectSeats = new ArrayList<>();
                                        for (Pair<Integer, Integer> each : entry.getValue().subList(0, needSeatSize)) {
                                            actualSelectSeats.add("0" + (each.getKey() + 1) + SeatNumberUtil.convert(0, each.getValue() + 1));
                                        }
                                        for (String selectSeat : actualSelectSeats) {
                                            TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                                            PurchaseTicketPassengerDetailDTO currentTicketPassenger = trainSeatBaseDTO.getPassengerSeatDetails().get(countNum.getAndIncrement());
                                            result.setSeatNumber(selectSeat);
                                            result.setSeatType(currentTicketPassenger.getSeatType());
                                            result.setCarriageNumber(entry.getKey());
                                            result.setPassengerId(currentTicketPassenger.getPassengerId());
                                            actualResult.add(result);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    return new Pair<>(actualResult,Boolean.TRUE);
                }
            }
        }
        return new Pair<>(null, Boolean.FALSE);
    }
}
