package com.hmall.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.Seckill;
import com.hmall.item.domain.vo.SeckillVO;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.mapper.SeckillMapper;
import com.hmall.item.service.ISeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl extends ServiceImpl<SeckillMapper, Seckill>
        implements ISeckillService {

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public List<SeckillVO> listActiveSeckills() {
        LocalDateTime now = LocalDateTime.now();
        List<Seckill> seckills = lambdaQuery()
                .eq(Seckill::getStatus, 1)
                .le(Seckill::getStartTime, now)
                .ge(Seckill::getEndTime, now)
                .list();
        return seckills.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private SeckillVO convertToVO(Seckill seckill) {
        SeckillVO vo = new SeckillVO();
        vo.setId(seckill.getId());
        vo.setItemId(seckill.getItemId());
        vo.setSeckillPrice(seckill.getSeckillPrice());
        vo.setStartTime(seckill.getStartTime());
        vo.setEndTime(seckill.getEndTime());
        vo.setStock(seckill.getStock());
        vo.setSold(seckill.getSold());
        vo.setStatus(seckill.getStatus());
        vo.setCreateTime(seckill.getCreateTime());
        vo.setUpdateTime(seckill.getUpdateTime());

        // 计算库存百分比
        if (seckill.getStock() != null && seckill.getStock() > 0) {
            int percent = (int) ((seckill.getSold() * 100.0) / seckill.getStock());
            vo.setStockPercent(percent);
        } else {
            vo.setStockPercent(0);
        }

        // 查询商品信息
        Item item = itemMapper.selectById(seckill.getItemId());
        if (item != null) {
            vo.setItemName(item.getName());
            vo.setItemImage(item.getImage());
            vo.setOriginalPrice(item.getPrice());
        }

        return vo;
    }
}