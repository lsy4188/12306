package itlsy.enums;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum SeatColEnum {

    YDZ_A("A", "A", "1"),
    YDZ_C("C", "C", "1"),
    YDZ_D("D", "D", "1"),
    YDZ_F("F", "F", "1"),
    EDZ_A("A", "A", "2"),
    EDZ_B("B", "B", "2"),
    EDZ_C("C", "C", "2"),
    EDZ_D("D", "D", "2"),
    EDZ_F("F", "F", "2");

    private String code;

    private String desc;

    /**
     * 对应SeatTypeEnum.code
     */
    private String type;

    SeatColEnum(String code, String desc, String type) {
        this.code = code;
        this.desc = desc;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * 根据车箱的座位类型，筛选出所有的列，比如车箱类型是一等座，则筛选出columnList={ACDF}
     */
  public static List<SeatColEnum> getColsByType(String seatType) {
        //创建一个列的列表
        List<SeatColEnum> colList = new ArrayList<>();
        //获取所有列的枚举类型
        EnumSet<SeatColEnum> seatColEnums = EnumSet.allOf(SeatColEnum.class);
        //遍历所有列的枚举类型
        for (SeatColEnum anEnum : seatColEnums) {
            //如果座位类型和枚举类型一致，则添加到列表中
            if (seatType.equals(anEnum.getType())) {
                colList.add(anEnum);
            }
        }
        //返回列表
        return colList;
    }

}
