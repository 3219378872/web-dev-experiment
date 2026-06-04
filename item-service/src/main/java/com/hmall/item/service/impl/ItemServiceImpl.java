package com.hmall.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.dto.OrderDetailDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.vo.ItemVO;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.service.IItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author 虎哥
 */
@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    @Override
    @Transactional
    public void deductStock(List<OrderDetailDTO> items) {
        String sqlStatement = "com.hmall.item.mapper.ItemMapper.updateStock";
        boolean r = false;
        try {
            r = executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
        } catch (Exception e) {
            throw new BizIllegalException("更新库存异常，可能是库存不足!", e);
        }
        if (!r) {
            throw new BizIllegalException("库存不足！");
        }
    }

    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }

    @Override
    public List<ItemVO> queryRelatedItems(Long itemId, Integer count) {
        // 查询当前商品
        Item item = getById(itemId);
        if (item == null) {
            return List.of();
        }

        // 查询同分类商品（排除自身）
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getCategory, item.getCategory())
               .ne(Item::getId, itemId)
               .eq(Item::getStatus, 1) // 只查询上架商品
               .last("LIMIT " + count);

        List<Item> items = list(wrapper);
        return items.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        List<Item> items = listByIds(ids);
        for (Item item : items) {
            item.setStatus(status);
        }
        updateBatchById(items);
    }

    private ItemVO convertToVO(Item item) {
        ItemVO vo = new ItemVO();
        vo.setId(item.getId());
        vo.setName(item.getName());
        vo.setPrice(item.getPrice());
        vo.setStock(item.getStock());
        vo.setImage(item.getImage());
        vo.setCategory(item.getCategory());
        vo.setBrand(item.getBrand());
        vo.setSpec(item.getSpec());
        vo.setSold(item.getSold());
        vo.setCommentCount(item.getCommentCount());
        vo.setIsAD(item.getIsAD());
        vo.setStatus(item.getStatus());
        vo.setCreateTime(item.getCreateTime());
        vo.setUpdateTime(item.getUpdateTime());
        return vo;
    }
}
