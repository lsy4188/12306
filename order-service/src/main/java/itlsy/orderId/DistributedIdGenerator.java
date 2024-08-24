package itlsy.orderId;

import org.springframework.stereotype.Component;

/**
 * 全局唯一订单号生成器
 */
public class DistributedIdGenerator {

    private static final long EPOCH = 1609459200000L;
    private static final int NODE_BITS = 5;
    private static final int SEQUENCE_BITS = 7;
    private final long nodeID;
    private long lastTimesTemp = -1L;
    private long sequence = 0L;

    public DistributedIdGenerator(long nodeID) {
        this.nodeID = nodeID;
    }

    //synchronized-防止高并发
    public synchronized long generateId() {
        long timesTemp = System.currentTimeMillis() - EPOCH;
        if (timesTemp < lastTimesTemp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate ID.");
        }
        if (timesTemp == lastTimesTemp) {
            sequence = (sequence + 1) & ((1 << SEQUENCE_BITS) - 1);
            if (sequence == 0) {
                timesTemp = tilNextMillis(lastTimesTemp);
            }
        } else {
            sequence = 0L;
        }
        lastTimesTemp = timesTemp;
        return (timesTemp << (NODE_BITS + SEQUENCE_BITS)) | (nodeID << SEQUENCE_BITS) | sequence;
    }

    private long tilNextMillis(long lastTimesTemp) {
        long timesTemp = System.currentTimeMillis() - EPOCH;
        while (timesTemp <= lastTimesTemp) {
            timesTemp = System.currentTimeMillis() - EPOCH;
        }
        return timesTemp;
    }
}
