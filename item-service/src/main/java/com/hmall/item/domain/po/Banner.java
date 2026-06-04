package com.hmall.item.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 轮播图/广告位表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("banner")
public class Banner implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 链接URL
     */
    private String linkUrl;

    /**
     * 标题
     */
    private String title;

    /**
     * 类型：carousel=轮播图，ad=广告位
     */
    private String type;

    /**
     * 广告位槽位标识
     */
    private String position;

    /**
     * 排序值（越小越靠前）
     */
    private Integer sort;

    /**
     * 状态：0=禁用，1=启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}