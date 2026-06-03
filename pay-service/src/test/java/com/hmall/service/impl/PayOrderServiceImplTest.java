package com.hmall.service.impl;

import com.hmall.PayServiceTestBase;
import com.hmall.api.dto.PayApplyDTO;
import com.hmall.api.dto.PayOrderFormDTO;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.mq.MqConstants;
import com.hmall.common.mq.event.PaySuccessEvent;
import com.hmall.domain.po.PayOrder;
import com.hmall.enums.PayStatus;
import com.hmall.mapper.PayOrderMapper;
import com.hmall.service.IPayOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class PayOrderServiceImplTest extends PayServiceTestBase {

    @Autowired
    private IPayOrderService payOrderService;

    @Autowired
    private PayOrderServiceImpl payOrderServiceImpl;

    @Autowired
    private PayOrderMapper payOrderMapper;

    private PayOrder insertPayOrder(Long id, Long bizOrderNo, String channel, int status) {
        PayOrder po = new PayOrder();
        po.setId(id);
        po.setBizOrderNo(bizOrderNo);
        po.setPayOrderNo(id + 10000L);
        po.setBizUserId(TEST_USER_ID);
        po.setPayChannelCode(channel);
        po.setAmount(10000);
        po.setPayType(5);
        po.setStatus(status);
        po.setPayOverTime(LocalDateTime.now().plusMinutes(120));
        po.setCreateTime(LocalDateTime.now());
        payOrderMapper.insert(po);
        return po;
    }

    private PayApplyDTO applyDTO(Long bizOrderNo, String channel) {
        return PayApplyDTO.builder()
                .bizOrderNo(bizOrderNo)
                .amount(10000)
                .payChannelCode(channel)
                .payType(5)
                .orderInfo("test order")
                .build();
    }

    // ───────────────────── applyPayOrder ─────────────────────

    @Nested
    @DisplayName("applyPayOrder")
    class ApplyPayOrderTests {

        @Test
        @DisplayName("首次申请-创建新支付单")
        void newOrder_createsPayOrder() {
            String id = payOrderService.applyPayOrder(applyDTO(99999L, "balance"));

            assertThat(id).isNotBlank();
            PayOrder po = payOrderService.getById(Long.valueOf(id));
            assertThat(po.getBizOrderNo()).isEqualTo(99999L);
            assertThat(po.getStatus()).isEqualTo(PayStatus.WAIT_BUYER_PAY.getValue());
            assertThat(po.getPayOrderNo()).isNotNull();
            assertThat(po.getPayOverTime()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("已支付 → BizIllegalException")
        void alreadyPaid_throws() {
            insertPayOrder(4000L, 500001L, "balance", PayStatus.TRADE_SUCCESS.getValue());

            assertThatThrownBy(() -> payOrderService.applyPayOrder(applyDTO(500001L, "balance")))
                    .isInstanceOf(BizIllegalException.class)
                    .hasMessageContaining("已经支付");
        }

        @Test
        @DisplayName("已关闭 → BizIllegalException")
        void alreadyClosed_throws() {
            insertPayOrder(4001L, 500002L, "balance", PayStatus.TRADE_CLOSED.getValue());

            assertThatThrownBy(() -> payOrderService.applyPayOrder(applyDTO(500002L, "balance")))
                    .isInstanceOf(BizIllegalException.class)
                    .hasMessageContaining("已关闭");
        }

        @Test
        @DisplayName("渠道不一致-重置数据并更新")
        void channelMismatch_resetsData() {
            PayOrder old = insertPayOrder(4002L, 500003L, "wxPay", PayStatus.WAIT_BUYER_PAY.getValue());
            old.setQrCodeUrl("https://old-qr.example.com");
            payOrderMapper.updateById(old);

            String id = payOrderService.applyPayOrder(applyDTO(500003L, "balance"));

            assertThat(id).isEqualTo("4002");
            PayOrder updated = payOrderService.getById(4002L);
            assertThat(updated.getPayChannelCode()).isEqualTo("balance");
            assertThat(updated.getQrCodeUrl()).isEmpty();
        }

        @Test
        @DisplayName("渠道一致-直接返回旧数据")
        void channelMatch_returnsOldOrder() {
            PayOrder old = insertPayOrder(4003L, 500004L, "balance", PayStatus.WAIT_BUYER_PAY.getValue());

            String id = payOrderService.applyPayOrder(applyDTO(500004L, "balance"));

            assertThat(id).isEqualTo("4003");
        }

        @Test
        @DisplayName("渠道一致-未提交状态-直接返回旧数据")
        void notCommitStatus_returnsOldOrder() {
            insertPayOrder(4004L, 500005L, "balance", PayStatus.NOT_COMMIT.getValue());

            String id = payOrderService.applyPayOrder(applyDTO(500005L, "balance"));

            assertThat(id).isEqualTo("4004");
        }
    }

    // ───────────────────── tryPayOrderByBalance ─────────────────────

    @Nested
    @DisplayName("tryPayOrderByBalance")
    class TryPayByBalanceTests {

        @Test
        @DisplayName("余额支付成功-状态更新并发布MQ消息")
        void paySuccess() {
            insertPayOrder(5000L, 600001L, "balance", PayStatus.WAIT_BUYER_PAY.getValue());

            PayOrderFormDTO form = PayOrderFormDTO.builder()
                    .id(5000L)
                    .pw("admin123")
                    .build();
            payOrderService.tryPayOrderByBalance(form);

            PayOrder po = payOrderService.getById(5000L);
            assertThat(po.getStatus()).isEqualTo(PayStatus.TRADE_SUCCESS.getValue());
            assertThat(po.getPaySuccessTime()).isNotNull();
            verify(userClient).deductMoney("admin123", 10000);
            verify(mqMessagePublisher).publish(
                    eq(MqConstants.PAY_EXCHANGE),
                    eq(MqConstants.PAY_SUCCESS_KEY),
                    any(PaySuccessEvent.class));
        }

        @Test
        @DisplayName("状态不是待支付 → BizIllegalException")
        void wrongStatus_throws() {
            insertPayOrder(5001L, 600002L, "balance", PayStatus.TRADE_SUCCESS.getValue());

            PayOrderFormDTO form = PayOrderFormDTO.builder()
                    .id(5001L)
                    .pw("admin123")
                    .build();

            assertThatThrownBy(() -> payOrderService.tryPayOrderByBalance(form))
                    .isInstanceOf(BizIllegalException.class)
                    .hasMessageContaining("已支付或关闭");
        }

        @Test
        @DisplayName("已关闭状态 → BizIllegalException")
        void closedStatus_throws() {
            insertPayOrder(5002L, 600003L, "balance", PayStatus.TRADE_CLOSED.getValue());

            PayOrderFormDTO form = PayOrderFormDTO.builder()
                    .id(5002L)
                    .pw("admin123")
                    .build();

            assertThatThrownBy(() -> payOrderService.tryPayOrderByBalance(form))
                    .isInstanceOf(BizIllegalException.class)
                    .hasMessageContaining("已支付或关闭");
        }
    }

    // ───────────────────── markPayOrderSuccess ─────────────────────

    @Nested
    @DisplayName("markPayOrderSuccess")
    class MarkPayOrderSuccessTests {

        @Test
        @DisplayName("待支付状态-标记成功")
        void waitBuyerPay_marksSuccess() {
            insertPayOrder(6000L, 700001L, "balance", PayStatus.WAIT_BUYER_PAY.getValue());

            boolean result = payOrderServiceImpl.markPayOrderSuccess(6000L, LocalDateTime.now());

            assertThat(result).isTrue();
            PayOrder po = payOrderService.getById(6000L);
            assertThat(po.getStatus()).isEqualTo(PayStatus.TRADE_SUCCESS.getValue());
            assertThat(po.getPaySuccessTime()).isNotNull();
        }

        @Test
        @DisplayName("未提交状态-标记成功")
        void notCommit_marksSuccess() {
            insertPayOrder(6001L, 700002L, "balance", PayStatus.NOT_COMMIT.getValue());

            boolean result = payOrderServiceImpl.markPayOrderSuccess(6001L, LocalDateTime.now());

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("已支付状态-乐观锁失败返回false")
        void alreadyPaid_returnsFalse() {
            insertPayOrder(6002L, 700003L, "balance", PayStatus.TRADE_SUCCESS.getValue());

            boolean result = payOrderServiceImpl.markPayOrderSuccess(6002L, LocalDateTime.now());

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("已关闭状态-乐观锁失败返回false")
        void closed_returnsFalse() {
            insertPayOrder(6003L, 700004L, "balance", PayStatus.TRADE_CLOSED.getValue());

            boolean result = payOrderServiceImpl.markPayOrderSuccess(6003L, LocalDateTime.now());

            assertThat(result).isFalse();
        }
    }

    // ───────────────────── queryByBizOrderNo ─────────────────────

    @Nested
    @DisplayName("queryByBizOrderNo")
    class QueryByBizOrderNoTests {

        @Test
        @DisplayName("存在-返回支付单")
        void exists_returnsPayOrder() {
            insertPayOrder(7000L, 800001L, "balance", PayStatus.WAIT_BUYER_PAY.getValue());

            PayOrder result = payOrderServiceImpl.queryByBizOrderNo(800001L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(7000L);
        }

        @Test
        @DisplayName("不存在-返回null")
        void notExists_returnsNull() {
            PayOrder result = payOrderServiceImpl.queryByBizOrderNo(999999L);

            assertThat(result).isNull();
        }
    }
}
