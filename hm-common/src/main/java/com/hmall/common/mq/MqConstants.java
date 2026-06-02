package com.hmall.common.mq;

public final class MqConstants {

    public static final String TRADE_EXCHANGE = "trade.topic";
    public static final String PAY_EXCHANGE = "pay.topic";
    public static final String DELAY_EXCHANGE = "delay.exchange";
    public static final String DEAD_EXCHANGE = "hmall.dead";

    public static final String ORDER_CREATE_KEY = "order.create";
    public static final String ORDER_DELAY_KEY = "order.delay";
    public static final String ORDER_CLOSE_KEY = "order.close";
    public static final String DEAD_KEY = "mq.dead";
    public static final String PAY_SUCCESS_KEY = "pay.success";

    public static final String CART_ORDER_CREATE_QUEUE = "cart.order.create.queue";
    public static final String NOTIFY_ORDER_CREATE_QUEUE = "notify.order.create.queue";
    public static final String NOTIFY_ORDER_STATUS_QUEUE = "notify.order.status.queue";
    public static final String TRADE_PAY_SUCCESS_QUEUE = "trade.pay.success.queue";
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    public static final String ORDER_CLOSE_QUEUE = "order.close.queue";
    public static final String MQ_DEAD_QUEUE = "hmall.mq.dead.queue";

    public static final long ORDER_CLOSE_DELAY_MS = 30L * 60L * 1000L;

    private MqConstants() {
    }

    public static String orderStatusKey(String status) {
        return "order.status." + status;
    }
}
