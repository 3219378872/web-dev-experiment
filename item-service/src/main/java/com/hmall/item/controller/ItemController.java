package com.hmall.item.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.PageQuery;
import com.hmall.common.domain.R;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.dto.ItemIdsDTO;
import com.hmall.item.domain.dto.ItemStatusDTO;
import com.hmall.item.domain.dto.OrderDetailDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.vo.ItemVO;
import com.hmall.item.service.IItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "商品管理相关接口")
@RestController
@RequiredArgsConstructor
public class ItemController {

    private final IItemService itemService;

    // ===== 客户端接口 =====
    @ApiOperation("分页查询商品")
    @GetMapping("/items/page")
    public PageDTO<ItemDTO> queryItemByPage(PageQuery query) {
        Page<Item> result = itemService.page(query.toMpPage("update_time", false));
        return PageDTO.of(result, ItemDTO.class);
    }

    @ApiOperation("根据id批量查询商品")
    @GetMapping("/items")
    public List<ItemDTO> queryItemByIds(@RequestParam("ids") List<Long> ids) {
        return itemService.queryItemByIds(ids);
    }

    @ApiOperation("根据id查询商品")
    @GetMapping("/items/{id}")
    public ItemDTO queryItemById(@PathVariable("id") Long id) {
        return BeanUtils.copyBean(itemService.getById(id), ItemDTO.class);
    }

    @ApiOperation("查询相关推荐商品")
    @GetMapping("/items/{id}/related")
    public List<ItemVO> queryRelatedItems(
            @ApiParam("商品ID") @PathVariable Long id,
            @ApiParam("推荐数量") @RequestParam(defaultValue = "5") Integer count) {
        return itemService.queryRelatedItems(id, count);
    }

    // ===== 管理端接口 =====
    @ApiOperation("管理端分页查询商品（含状态筛选）")
    @GetMapping("/admin/items")
    public PageDTO<ItemDTO> adminQueryItemByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "minPrice", required = false) Integer minPrice,
            @RequestParam(value = "maxPrice", required = false) Integer maxPrice) {
        Page<Item> p = new Page<>(page, size);
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) wrapper.like(Item::getName, name);
        if (category != null && !category.isEmpty()) wrapper.eq(Item::getCategory, category);
        if (status != null) wrapper.eq(Item::getStatus, status);
        if (minPrice != null) wrapper.ge(Item::getPrice, minPrice);
        if (maxPrice != null) wrapper.le(Item::getPrice, maxPrice);
        wrapper.ne(Item::getStatus, 3);
        wrapper.orderByDesc(Item::getUpdateTime);
        return PageDTO.of(itemService.page(p, wrapper), ItemDTO.class);
    }

    @ApiOperation("新增商品")
    @PostMapping("/items")
    public R<Void> saveItem(@RequestBody ItemDTO item) {
        itemService.save(BeanUtils.copyBean(item, Item.class));
        return R.ok();
    }

    @ApiOperation("更新商品")
    @PutMapping("/admin/items/{id}")
    public R<Void> updateItem(@PathVariable("id") Long id, @RequestBody ItemDTO item) {
        Item entity = BeanUtils.copyBean(item, Item.class);
        entity.setId(id);
        itemService.updateById(entity);
        return R.ok();
    }

    @ApiOperation("更新商品状态（上架/下架）")
    @PutMapping("/admin/items/{id}/status")
    public R<Void> updateItemStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        Item item = new Item();
        item.setId(id);
        item.setStatus(status);
        itemService.updateById(item);
        return R.ok();
    }

    @ApiOperation("批量更新商品状态")
    @PutMapping("/admin/items/status")
    public R<Void> batchUpdateItemStatus(@RequestBody ItemStatusDTO dto) {
        itemService.batchUpdateStatus(dto.getIds(), dto.getStatus());
        return R.ok();
    }

    @ApiOperation("删除商品（逻辑删除）")
    @DeleteMapping("/admin/items/{id}")
    public R<Void> deleteItemById(@PathVariable("id") Long id) {
        itemService.batchUpdateStatus(List.of(id), 3);
        return R.ok();
    }

    @ApiOperation("批量删除商品（逻辑删除）")
    @DeleteMapping("/admin/items")
    public R<Void> batchDeleteItems(@RequestBody ItemIdsDTO dto) {
        itemService.batchUpdateStatus(dto.getIds(), 3);
        return R.ok();
    }

    @ApiOperation("批量扣减库存")
    @PutMapping("/items/stock/deduct")
    public void deductStock(@RequestBody List<OrderDetailDTO> items) {
        itemService.deductStock(items);
    }

    @ApiOperation("管理端商品统计")
    @GetMapping("/admin/items/stats")
    public Map<String, Object> adminItemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", itemService.count());
        stats.put("onSale", itemService.lambdaQuery().eq(Item::getStatus, 1).count());
        stats.put("offSale", itemService.lambdaQuery().eq(Item::getStatus, 2).count());
        stats.put("lowStock", itemService.lambdaQuery().lt(Item::getStock, 20).eq(Item::getStatus, 1).count());
        return stats;
    }
}
