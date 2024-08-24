package itlsy.exception;

public enum BusinessExceptionEnum {

    MEMBER_MOBILE_EXIST("手机号已注册"),
    PASS_WORD_SAME("密码前后不匹配"),
    MEMBER_MOBILE_NOT_EXIST("用户不存在，请先注册"),
    MEMBER_MOBILE_CODE_ERROR("密码错误请重试"),
    CHECK_CODE_REPLY("验证码已发送，请勿重复发送"),

    BUSINESS_STATION_NAME_UNIQUE_ERROR("车站已存在"),
    BUSINESS_STATION_NAME_ERROR("车站不存在"),

    BUSINESS_TRAIN_CODE_UNIQUE_ERROR("车次编号已存在"),
    BUSINESS_TRAIN_CODE_ERROR("车次编号不存在"),

    BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR("同车次站序已存在"),
    BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR("同车次站名已存在"),
    BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR("同车次厢号已存在"),
    BUSINESS_TRAIN_CARRIAGE_ERROR("车厢号不存在"),
    BUSINESS_TRAIN_SEAT_ERROR("座位号不存在"),


    CONFIRM_ORDER_TICKET_COUNT_ERROR("余票不足"),
    CONFIRM_ORDER_EXCEPTION("购票异常，请稍后重试"),
    CONFIRM_ORDER_LOCK_ERROR("当前服务器繁忙，请稍后重试"),
    CONFIRM_ORDER_FLOW_FAIL("当前抢票人数过多，请重试"),
    CONFIRM_ORDER_FLOW_EXCEPTION("对接口限流当前抢票人数太多了,请重试"),
    CONFIRM_ORDER_SK_TOKEN_FAIL("当前抢票人数过多，请一分钟后重试");


    private String desc;

    BusinessExceptionEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "BusinessExceptionEnum{" +
                "desc='" + desc + '\'' +
                "} " + super.toString();
    }
}
