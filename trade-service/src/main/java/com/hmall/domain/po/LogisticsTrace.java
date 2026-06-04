package com.hmall.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流轨迹表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("logistics_trace")
public class LogisticsTrace implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 物流节点
     */
    private String node;

    /**
     * 描述
     */
    private String description;

    /**
     * 轨迹时间
     */
    private LocalDateTime traceTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
