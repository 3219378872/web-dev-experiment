package com.hmall.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.item.domain.po.ItemReview;
import java.util.List;

public interface IItemReviewService extends IService<ItemReview> {
    List<ItemReview> listByItemId(Long itemId);
}
