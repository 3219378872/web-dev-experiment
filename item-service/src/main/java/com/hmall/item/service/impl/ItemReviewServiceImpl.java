package com.hmall.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.domain.PageDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemReview;
import com.hmall.item.domain.vo.ReviewVO;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.mapper.ItemReviewMapper;
import com.hmall.item.service.IItemReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemReviewServiceImpl extends ServiceImpl<ItemReviewMapper, ItemReview>
        implements IItemReviewService {

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public List<ItemReview> listByItemId(Long itemId) {
        return lambdaQuery().eq(ItemReview::getItemId, itemId)
                .orderByDesc(ItemReview::getCreateTime).list();
    }

    @Override
    public PageDTO<ReviewVO> queryReviewsPage(Integer page, Integer size, Integer rating) {
        LambdaQueryWrapper<ItemReview> wrapper = new LambdaQueryWrapper<>();
        if (rating != null) {
            wrapper.eq(ItemReview::getRating, rating);
        }
        wrapper.orderByDesc(ItemReview::getCreateTime);

        Page<ItemReview> pageParam = new Page<>(page, size);
        Page<ItemReview> result = page(pageParam, wrapper);

        List<ReviewVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageDTO<>(result.getTotal(), result.getPages(), voList);
    }

    private ReviewVO convertToVO(ItemReview review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setUserId(review.getUserId());
        vo.setItemId(review.getItemId());
        vo.setContent(review.getContent());
        vo.setImages(review.getImages());
        vo.setRating(review.getRating());
        vo.setCreateTime(review.getCreateTime());

        // 查询商品信息
        Item item = itemMapper.selectById(review.getItemId());
        if (item != null) {
            vo.setItemName(item.getName());
        }

        return vo;
    }
}
