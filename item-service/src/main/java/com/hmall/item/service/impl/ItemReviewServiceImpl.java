package com.hmall.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.item.domain.po.ItemReview;
import com.hmall.item.mapper.ItemReviewMapper;
import com.hmall.item.service.IItemReviewService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemReviewServiceImpl extends ServiceImpl<ItemReviewMapper, ItemReview>
        implements IItemReviewService {

    @Override
    public List<ItemReview> listByItemId(Long itemId) {
        return lambdaQuery().eq(ItemReview::getItemId, itemId)
                .orderByDesc(ItemReview::getCreateTime).list();
    }
}
