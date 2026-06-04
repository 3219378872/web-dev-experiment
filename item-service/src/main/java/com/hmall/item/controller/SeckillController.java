package com.hmall.item.controller;

import com.hmall.item.domain.vo.SeckillVO;
import com.hmall.item.service.ISeckillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "限时秒杀")
@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final ISeckillService seckillService;

    @ApiOperation("查询进行中的秒杀活动")
    @GetMapping("/active")
    public List<SeckillVO> listActiveSeckills() {
        return seckillService.listActiveSeckills();
    }
}