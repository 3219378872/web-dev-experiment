package com.hmall.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.client.OrderClient;
import com.hmall.api.client.UserClient;
import com.hmall.api.dto.OrderDTO;
import com.hmall.api.dto.PayApplyDTO;
import com.hmall.api.dto.PayOrderFormDTO;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.mq.MqConstants;
import com.hmall.common.mq.event.PaySuccessEvent;
import com.hmall.common.mq.outbox.MqMessagePublisher;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.PayOrder;
import com.hmall.enums.PayStatus;
import com.hmall.mapper.PayOrderMapper;
import com.hmall.service.IPayOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 支付订单 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements IPayOrderService {
    private final UserClient userClient;
    private final OrderClient orderClient;
    private final MqMessagePublisher mqMessagePublisher;

    @Override
    public String applyPayOrder(PayApplyDTO applyDTO) {
        // 校验订单归属、状态与金额，防止前端篡改
        OrderDTO order = orderClient.queryOrderById(applyDTO.getBizOrderNo());
        if (order == null) {
            throw new BizIllegalException("订单不存在");
        }
        Long userId = UserContext.getUser();
        if (!order.getUserId().equals(userId)) {
            throw new BizIllegalException("订单归属错误");
        }
        if (order.getStatus() != 1) {
            throw new BizIllegalException("订单状态异常，无法支付");
        }
        // 使用后端订单真实金额，覆盖前端传入值
        applyDTO.setAmount(order.getTotalFee());

        // 1.幂等性校验
        PayOrder payOrder = checkIdempotent(applyDTO);
        // 2.返回结果
        return payOrder.getId().toString();
    }

    @Override
    @GlobalTransactional(name = "tryPayByBalance", rollbackFor = Exception.class)
    @Transactional
    public void tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO) {
        // 1.查询支付单
        PayOrder po = getById(payOrderFormDTO.getId());
        // 1.1.校验支付单是否存在
        if (po == null) {
            throw new BizIllegalException("支付单不存在");
        }
        // 2.判断状态
        if(!PayStatus.WAIT_BUYER_PAY.equalsValue(po.getStatus())){
            // 订单不是未支付，状态异常
            throw new BizIllegalException("交易已支付或关闭！");
        }
        // 3.支付前重新校验订单归属、状态与金额，防止过期/伪造支付单
        OrderDTO order = orderClient.queryOrderById(po.getBizOrderNo());
        if (order == null) {
            throw new BizIllegalException("订单不存在");
        }
        Long userId = UserContext.getUser();
        if (!order.getUserId().equals(userId)) {
            throw new BizIllegalException("订单归属错误");
        }
        if (order.getStatus() != 1) {
            throw new BizIllegalException("订单状态异常，无法支付");
        }
        if (!order.getTotalFee().equals(po.getAmount())) {
            // 金额不一致，更新为订单真实金额后重新校验
            po.setAmount(order.getTotalFee());
            updateById(po);
        }
        // 4.尝试扣减余额
        userClient.deductMoney(payOrderFormDTO.getPw(), po.getAmount());
        // 5.修改支付单状态
        boolean success = markPayOrderSuccess(payOrderFormDTO.getId(), LocalDateTime.now());
        if (!success) {
            throw new BizIllegalException("交易已支付或关闭！");
        }
        // 6.发布支付成功事件，由 trade-service 异步修改订单状态
        try {
            mqMessagePublisher.publish(
                    MqConstants.PAY_EXCHANGE,
                    MqConstants.PAY_SUCCESS_KEY,
                    new PaySuccessEvent(po.getId(), po.getBizOrderNo(), po.getBizUserId(), LocalDateTime.now()));
        } catch (Exception e) {
            log.error("发布支付成功事件失败，支付单[id={}]状态已更新，订单状态将由补偿任务处理", po.getId(), e);
        }
    }

    public boolean markPayOrderSuccess(Long id, LocalDateTime successTime) {
        return lambdaUpdate()
                .set(PayOrder::getStatus, PayStatus.TRADE_SUCCESS.getValue())
                .set(PayOrder::getPaySuccessTime, successTime)
                .eq(PayOrder::getId, id)
                // 支付状态的乐观锁判断
                .in(PayOrder::getStatus, PayStatus.NOT_COMMIT.getValue(), PayStatus.WAIT_BUYER_PAY.getValue())
                .update();
    }


    private PayOrder checkIdempotent(PayApplyDTO applyDTO) {
        // 1.首先查询支付单
        PayOrder oldOrder = queryByBizOrderNo(applyDTO.getBizOrderNo());
        // 2.判断是否存在
        if (oldOrder == null) {
            // 不存在支付单，说明是第一次，写入新的支付单并返回
            PayOrder payOrder = buildPayOrder(applyDTO);
            payOrder.setPayOrderNo(IdWorker.getId());
            save(payOrder);
            return payOrder;
        }
        // 3.旧单已经存在，判断是否支付成功
        if (PayStatus.TRADE_SUCCESS.equalsValue(oldOrder.getStatus())) {
            // 已经支付成功，抛出异常
            throw new BizIllegalException("订单已经支付！");
        }
        // 4.旧单已经存在，判断是否已经关闭
        if (PayStatus.TRADE_CLOSED.equalsValue(oldOrder.getStatus())) {
            // 已经关闭，抛出异常
            throw new BizIllegalException("订单已关闭");
        }
        // 5.旧单已经存在，判断支付渠道是否一致
        if (!StringUtils.equals(oldOrder.getPayChannelCode(), applyDTO.getPayChannelCode())) {
            // 支付渠道不一致，需要重置数据，然后重新申请支付单
            PayOrder payOrder = buildPayOrder(applyDTO);
            payOrder.setId(oldOrder.getId());
            payOrder.setQrCodeUrl("");
            updateById(payOrder);
            payOrder.setPayOrderNo(oldOrder.getPayOrderNo());
            return payOrder;
        }
        // 6.旧单已经存在，且可能是未支付或未提交，且支付渠道一致，直接返回旧数据
        return oldOrder;
    }

    private PayOrder buildPayOrder(PayApplyDTO payApplyDTO) {
        // 1.数据转换
        PayOrder payOrder = BeanUtils.toBean(payApplyDTO, PayOrder.class);
        // 2.初始化数据
        payOrder.setPayOverTime(LocalDateTime.now().plusMinutes(120L));
        payOrder.setStatus(PayStatus.WAIT_BUYER_PAY.getValue());
        payOrder.setBizUserId(UserContext.getUser());
        return payOrder;
    }
    public PayOrder queryByBizOrderNo(Long bizOrderNo) {
        return lambdaQuery()
                .eq(PayOrder::getBizOrderNo, bizOrderNo)
                .one();
    }
}
