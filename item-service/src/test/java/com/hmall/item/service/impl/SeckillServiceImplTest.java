package com.hmall.item.service.impl;

import com.hmall.item.ItemServiceTestBase;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.Seckill;
import com.hmall.item.domain.vo.SeckillVO;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.mapper.SeckillMapper;
import com.hmall.item.service.ISeckillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeckillServiceImplTest extends ItemServiceTestBase {

    @Autowired
    private ISeckillService seckillService;

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private ItemMapper itemMapper;

    private Item testItem;

    @BeforeEach
    void setUp() {
        // 创建测试商品
        testItem = new Item();
        testItem.setName("测试商品");
        testItem.setPrice(10000);
        testItem.setStock(100);
        testItem.setStatus(1);
        itemMapper.insert(testItem);

        // 创建进行中的秒杀活动
        Seckill seckill1 = new Seckill();
        seckill1.setItemId(testItem.getId());
        seckill1.setSeckillPrice(5000);
        seckill1.setStartTime(LocalDateTime.now().minusHours(1));
        seckill1.setEndTime(LocalDateTime.now().plusHours(1));
        seckill1.setStock(50);
        seckill1.setSold(20);
        seckill1.setStatus(1);
        seckillMapper.insert(seckill1);

        // 创建已结束的秒杀活动
        Seckill seckill2 = new Seckill();
        seckill2.setItemId(testItem.getId());
        seckill2.setSeckillPrice(6000);
        seckill2.setStartTime(LocalDateTime.now().minusHours(3));
        seckill2.setEndTime(LocalDateTime.now().minusHours(1));
        seckill2.setStock(30);
        seckill2.setSold(30);
        seckill2.setStatus(1);
        seckillMapper.insert(seckill2);

        // 创建禁用的秒杀活动
        Seckill seckill3 = new Seckill();
        seckill3.setItemId(testItem.getId());
        seckill3.setSeckillPrice(7000);
        seckill3.setStartTime(LocalDateTime.now().minusHours(1));
        seckill3.setEndTime(LocalDateTime.now().plusHours(1));
        seckill3.setStock(20);
        seckill3.setSold(10);
        seckill3.setStatus(0);
        seckillMapper.insert(seckill3);
    }

    @Nested
    @DisplayName("listActiveSeckills")
    class ListActiveSeckillsTests {

        @Test
        @DisplayName("查询进行中的秒杀活动")
        void listActiveSeckills_returnsActiveSeckills() {
            List<SeckillVO> result = seckillService.listActiveSeckills();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSeckillPrice()).isEqualTo(5000);
            assertThat(result.get(0).getStock()).isEqualTo(50);
            assertThat(result.get(0).getSold()).isEqualTo(20);
        }

        @Test
        @DisplayName("秒杀活动包含商品信息")
        void listActiveSeckills_includesItemInfo() {
            List<SeckillVO> result = seckillService.listActiveSeckills();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getItemName()).isEqualTo("测试商品");
            assertThat(result.get(0).getOriginalPrice()).isEqualTo(10000);
        }

        @Test
        @DisplayName("秒杀活动包含库存百分比")
        void listActiveSeckills_includesStockPercent() {
            List<SeckillVO> result = seckillService.listActiveSeckills();

            assertThat(result).hasSize(1);
            // 20/50 = 40%
            assertThat(result.get(0).getStockPercent()).isEqualTo(40);
        }

        @Test
        @DisplayName("无进行中的秒杀活动返回空列表")
        void listActiveSeckills_noActive_returnsEmpty() {
            // 删除所有进行中的秒杀活动
            seckillMapper.delete(null);

            List<SeckillVO> result = seckillService.listActiveSeckills();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("秒杀活动-库存为0时库存百分比为0")
        void listActiveSeckills_stockZero_stockPercentZero() {
            // 创建库存为0的秒杀活动
            Seckill seckill = new Seckill();
            seckill.setItemId(testItem.getId());
            seckill.setSeckillPrice(4000);
            seckill.setStartTime(LocalDateTime.now().minusHours(1));
            seckill.setEndTime(LocalDateTime.now().plusHours(1));
            seckill.setStock(0);
            seckill.setSold(0);
            seckill.setStatus(1);
            seckillMapper.insert(seckill);

            List<SeckillVO> result = seckillService.listActiveSeckills();

            assertThat(result).hasSize(2);
            // 找到库存为0的秒杀活动
            SeckillVO zeroStockSeckill = result.stream()
                    .filter(s -> s.getStock() == 0)
                    .findFirst()
                    .orElse(null);
            assertThat(zeroStockSeckill).isNotNull();
            assertThat(zeroStockSeckill.getStockPercent()).isEqualTo(0);
        }

        @Test
        @DisplayName("秒杀活动-库存为1时库存百分比正确")
        void listActiveSeckills_stockOne_stockPercentCorrect() {
            // 创建库存为1的秒杀活动
            Seckill seckill = new Seckill();
            seckill.setItemId(testItem.getId());
            seckill.setSeckillPrice(4000);
            seckill.setStartTime(LocalDateTime.now().minusHours(1));
            seckill.setEndTime(LocalDateTime.now().plusHours(1));
            seckill.setStock(1);
            seckill.setSold(1);
            seckill.setStatus(1);
            seckillMapper.insert(seckill);

            List<SeckillVO> result = seckillService.listActiveSeckills();

            assertThat(result).hasSize(2);
            // 找到库存为1的秒杀活动
            SeckillVO oneStockSeckill = result.stream()
                    .filter(s -> s.getStock() == 1)
                    .findFirst()
                    .orElse(null);
            assertThat(oneStockSeckill).isNotNull();
            // 1/1 = 100%
            assertThat(oneStockSeckill.getStockPercent()).isEqualTo(100);
        }

        @Test
        @DisplayName("秒杀活动-商品不存在时商品信息为null")
        void listActiveSeckills_itemNotExists_itemInfoNull() {
            // 创建商品
            Item item = new Item();
            item.setName("临时商品");
            item.setPrice(5000);
            item.setStock(50);
            item.setStatus(1);
            itemMapper.insert(item);

            // 创建秒杀活动
            Seckill seckill = new Seckill();
            seckill.setItemId(item.getId());
            seckill.setSeckillPrice(3000);
            seckill.setStartTime(LocalDateTime.now().minusHours(1));
            seckill.setEndTime(LocalDateTime.now().plusHours(1));
            seckill.setStock(20);
            seckill.setSold(10);
            seckill.setStatus(1);
            seckillMapper.insert(seckill);

            // 删除商品
            itemMapper.deleteById(item.getId());

            List<SeckillVO> result = seckillService.listActiveSeckills();

            assertThat(result).hasSize(2);
            // 找到刚才创建的秒杀活动
            SeckillVO seckillVO = result.stream()
                    .filter(s -> s.getItemId().equals(item.getId()))
                    .findFirst()
                    .orElse(null);
            assertThat(seckillVO).isNotNull();
            assertThat(seckillVO.getItemName()).isNull();
            assertThat(seckillVO.getItemImage()).isNull();
            assertThat(seckillVO.getOriginalPrice()).isNull();
        }
    }
}