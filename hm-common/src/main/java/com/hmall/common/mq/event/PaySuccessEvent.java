package com.hmall.common.mq.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaySuccessEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long payOrderId;
    private Long orderId;
    private Long userId;
    private LocalDateTime payTime;
}
