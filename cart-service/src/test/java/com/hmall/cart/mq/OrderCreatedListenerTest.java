package com.hmall.cart.mq;

import com.hmall.cart.CartServiceTestBase;
import com.hmall.cart.domain.po.Cart;
import com.hmall.cart.mapper.CartMapper;
import com.hmall.common.mq.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCreatedListenerTest extends CartServiceTestBase {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private OrderCreatedListener listener;

    @Test
    void handleOrderCreated_removesOnlyCurrentUsersOrderedItems() {
        Cart ordered = new Cart();
        ordered.setUserId(TEST_USER_ID);
        ordered.setItemId(100L);
        cartMapper.insert(ordered);
        Cart otherItem = new Cart();
        otherItem.setUserId(TEST_USER_ID);
        otherItem.setItemId(200L);
        cartMapper.insert(otherItem);
        Cart otherUser = new Cart();
        otherUser.setUserId(999L);
        otherUser.setItemId(100L);
        cartMapper.insert(otherUser);

        listener.handleOrderCreated(new OrderCreatedEvent(10L, TEST_USER_ID, List.of(100L)));

        List<Cart> remaining = cartMapper.selectList(null);
        assertThat(remaining).extracting(Cart::getItemId).containsExactlyInAnyOrder(200L, 100L);
        assertThat(remaining).filteredOn(cart -> cart.getItemId().equals(100L))
                .singleElement()
                .extracting(Cart::getUserId)
                .isEqualTo(999L);
    }
}
