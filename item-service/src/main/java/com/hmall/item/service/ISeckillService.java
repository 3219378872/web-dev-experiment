package com.hmall.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.item.domain.po.Seckill;
import com.hmall.item.domain.vo.SeckillVO;

import java.util.List;

public interface ISeckillService extends IService<Seckill> {

    /**
     * 查询进行中的秒杀活动列表
     * @return 秒杀活动列表
     */
    List<SeckillVO> listActiveSeckills();
}