package com.hmall.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.common.domain.PageDTO;
import com.hmall.item.domain.po.ItemReview;
import com.hmall.item.domain.vo.ReviewVO;

import java.util.List;

public interface IItemReviewService extends IService<ItemReview> {
    List<ItemReview> listByItemId(Long itemId);

    /**
     * 管理端分页查询评价列表
     * @param page 页码
     * @param size 每页大小
     * @param rating 评分（可选）
     * @return 分页数据
     */
    PageDTO<ReviewVO> queryReviewsPage(Integer page, Integer size, Integer rating);
}
