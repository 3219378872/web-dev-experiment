package com.hmall.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.dto.OrderDetailDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.vo.ItemVO;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
public interface IItemService extends IService<Item> {

    void deductStock(List<OrderDetailDTO> items);

    List<ItemDTO> queryItemByIds(Collection<Long> ids);

    /**
     * 查询相关推荐商品（同分类，排除自身）
     * @param itemId 商品ID
     * @param count 推荐数量
     * @return 相关商品列表
     */
    List<ItemVO> queryRelatedItems(Long itemId, Integer count);

    /**
     * 批量更新商品状态
     * @param ids 商品ID列表
     * @param status 状态值
     */
    void batchUpdateStatus(List<Long> ids, Integer status);
}
