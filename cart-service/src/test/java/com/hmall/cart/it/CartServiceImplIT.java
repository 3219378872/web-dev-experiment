package com.hmall.cart.it;

import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.cart.domain.dto.CartFormDTO;
import com.hmall.cart.domain.po.Cart;
import com.hmall.cart.domain.vo.CartVO;
import com.hmall.cart.service.ICartService;
import com.hmall.common.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"spring.cloud.bootstrap.enabled=false"})
@ActiveProfiles("test")
@Transactional
class CartServiceImplIT {

    @AfterEach
    void tearDown() { UserContext.removeUser(); }

    @Autowired
    private ICartService cartService;

    @MockBean
    private ItemClient itemClient;

    @BeforeEach
    void setUp() { UserContext.setUser(1L); }

    @BeforeEach
    void setUpMock() {
        ItemDTO item = new ItemDTO();
        item.setId(100L);
        item.setPrice(15000);
        item.setStatus(1);
        item.setStock(50);
        when(itemClient.queryItemByIds(anySet())).thenReturn(List.of(item));
    }

    @Test
    void addItem2Cart_newItem_shouldCreateEntry() {
        CartFormDTO form = new CartFormDTO();
        form.setItemId(100L);
        form.setName("测试商品");
        form.setPrice(15000);
        form.setSpec("标准");
        form.setImage("/img/test.png");
        cartService.addItem2Cart(form);

        List<Cart> carts = cartService.lambdaQuery()
                .eq(Cart::getUserId, 1L)
                .eq(Cart::getItemId, 100L).list();
        assertThat(carts).hasSize(1);
        assertThat(carts.get(0).getNum()).isEqualTo(1);
    }

    @Test
    void addItem2Cart_existingItem_shouldIncrementNum() {
        CartFormDTO form = new CartFormDTO();
        form.setItemId(100L);
        form.setName("测试商品");
        form.setPrice(15000);
        cartService.addItem2Cart(form);
        cartService.addItem2Cart(form);

        Cart cart = cartService.lambdaQuery()
                .eq(Cart::getUserId, 1L)
                .eq(Cart::getItemId, 100L).one();
        assertThat(cart.getNum()).isEqualTo(2);
    }

    @Test
    void removeByItemIds_shouldDeleteEntries() {
        CartFormDTO form = new CartFormDTO();
        form.setItemId(100L);
        form.setName("测试");
        form.setPrice(1000);
        cartService.addItem2Cart(form);

        cartService.removeByItemIds(Set.of(100L));

        long count = cartService.lambdaQuery()
                .eq(Cart::getUserId, 1L)
                .eq(Cart::getItemId, 100L).count();
        assertThat(count).isZero();
    }

    @Test
    void queryMyCarts_shouldReturnWithFreshItemInfo() {
        CartFormDTO form = new CartFormDTO();
        form.setItemId(100L);
        form.setName("旧名称");
        form.setPrice(9999);
        cartService.addItem2Cart(form);

        List<CartVO> result = cartService.queryMyCarts();
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getNewPrice()).isEqualTo(15000);
        assertThat(result.get(0).getStatus()).isEqualTo(1);
    }
}
