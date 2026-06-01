package com.hmall.cart.service.impl;

import com.hmall.api.dto.ItemDTO;
import com.hmall.cart.CartServiceTestBase;
import com.hmall.cart.domain.dto.CartFormDTO;
import com.hmall.cart.domain.po.Cart;
import com.hmall.cart.domain.vo.CartVO;
import com.hmall.cart.mapper.CartMapper;
import com.hmall.cart.service.ICartService;
import com.hmall.common.exception.BizIllegalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

class CartServiceImplTest extends CartServiceTestBase {

    @Autowired
    private ICartService cartService;

    @Autowired
    private CartMapper cartMapper;

    @Test
    @DisplayName("新增购物车-新商品-保存成功")
    void addItem2Cart_newItem_success() {
        // Arrange
        CartFormDTO formDTO = new CartFormDTO();
        formDTO.setItemId(100L);
        formDTO.setName("测试商品");
        formDTO.setSpec("规格1");
        formDTO.setPrice(5000);
        formDTO.setImage("test.jpg");

        // Act
        cartService.addItem2Cart(formDTO);

        // Assert
        Cart saved = cartService.lambdaQuery()
                .eq(Cart::getItemId, 100L)
                .one();
        assertThat(saved).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(saved.getItemId()).isEqualTo(100L);
        assertThat(saved.getNum()).isEqualTo(1);
        assertThat(saved.getName()).isEqualTo("测试商品");
        assertThat(saved.getPrice()).isEqualTo(5000);
    }

    @Test
    @DisplayName("新增购物车-已存在商品-数量+1")
    void addItem2Cart_existingItem_incrementsNum() {
        // Arrange - 预先插入一条购物车记录
        Cart existing = new Cart();
        existing.setUserId(TEST_USER_ID);
        existing.setItemId(200L);
        existing.setNum(1);
        existing.setName("已有商品");
        existing.setPrice(3000);
        cartMapper.insert(existing);

        CartFormDTO formDTO = new CartFormDTO();
        formDTO.setItemId(200L);

        // Act
        cartService.addItem2Cart(formDTO);

        // Assert
        Cart updated = cartService.lambdaQuery()
                .eq(Cart::getItemId, 200L)
                .one();
        assertThat(updated).isNotNull();
        assertThat(updated.getNum()).isEqualTo(2);
    }

    @Test
    @DisplayName("新增购物车-购物车已满-抛出BizIllegalException")
    void addItem2Cart_cartFull_throwsException() {
        // Arrange - 插入 maxItems(10) 条购物车记录
        for (long i = 1; i <= 10; i++) {
            Cart cart = new Cart();
            cart.setUserId(TEST_USER_ID);
            cart.setItemId(i);
            cart.setNum(1);
            cartMapper.insert(cart);
        }

        CartFormDTO formDTO = new CartFormDTO();
        formDTO.setItemId(999L);

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItem2Cart(formDTO))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("不能超过");
    }

    @Test
    @DisplayName("查询购物车列表-购物车为空-返回空列表")
    void queryMyCarts_empty_returnsEmptyList() {
        // Act
        List<CartVO> result = cartService.queryMyCarts();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("查询购物车列表-有商品-返回带实时价格的CartVO")
    void queryMyCarts_withItems_returnsCartVOWithPrices() {
        // Arrange - 插入一条购物车记录
        Cart cart = new Cart();
        cart.setUserId(TEST_USER_ID);
        cart.setItemId(300L);
        cart.setNum(2);
        cart.setName("测试商品");
        cart.setPrice(5000);
        cart.setImage("test.jpg");
        cartMapper.insert(cart);

        // Mock itemClient 返回实时价格
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setId(300L);
        itemDTO.setPrice(7900);
        itemDTO.setStatus(1);
        itemDTO.setStock(50);
        when(itemClient.queryItemByIds(anySet())).thenReturn(List.of(itemDTO));

        // Act
        List<CartVO> result = cartService.queryMyCarts();

        // Assert
        assertThat(result).hasSize(1);
        CartVO vo = result.get(0);
        assertThat(vo.getNewPrice()).isEqualTo(7900);
        assertThat(vo.getStatus()).isEqualTo(1);
        assertThat(vo.getStock()).isEqualTo(50);
        // 仍保留快照价格
        assertThat(vo.getPrice()).isEqualTo(5000);
    }

    @Test
    @DisplayName("按商品ID批量删除-删除匹配条目")
    void removeByItemIds_removesMatchingItems() {
        // Arrange - 插入3条购物车记录
        for (long i = 1; i <= 3; i++) {
            Cart cart = new Cart();
            cart.setUserId(TEST_USER_ID);
            cart.setItemId(i);
            cart.setNum(1);
            cartMapper.insert(cart);
        }

        // Act - 删除 itemId 为 1 和 3 的记录
        cartService.removeByItemIds(List.of(1L, 3L));

        // Assert - 只有 itemId=2 的记录保留
        List<Cart> remaining = cartService.lambdaQuery().list();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getItemId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("removeByItemIds: 删除部分商品，其他用户商品不受影响")
    void removeByItemIds_otherUserItems_untouched() {
        Cart myCart = new Cart(); myCart.setUserId(TEST_USER_ID); myCart.setItemId(1L); cartMapper.insert(myCart);
        Cart otherCart = new Cart(); otherCart.setUserId(999L); otherCart.setItemId(1L); cartMapper.insert(otherCart);

        cartService.removeByItemIds(List.of(1L));

        // 只删除了 TEST_USER_ID 的购物车条目
        List<Cart> remaining = cartMapper.selectList(null);
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getUserId()).isEqualTo(999L);
    }
}
