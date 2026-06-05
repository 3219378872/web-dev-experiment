package com.hmall.cart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.cart.config.CartProperties;
import com.hmall.cart.domain.dto.CartFormDTO;
import com.hmall.cart.domain.po.Cart;
import com.hmall.cart.domain.vo.CartVO;
import com.hmall.cart.mapper.CartMapper;
import com.hmall.cart.service.ICartService;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.CollUtils;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单详情表 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

//    private final RestTemplate restTemplate;
//    private final DiscoveryClient discoveryClient;

    private final ItemClient itemClient;

    private final CartProperties cartProperties;

    @Override
    public void addItem2Cart(CartFormDTO cartFormDTO) {
        // 1.获取登录用户
        Long userId = UserContext.getUser();

        // 2.判断是否已经存在
        if(checkItemExists(cartFormDTO.getItemId(), userId)){
            // 2.1.存在，则更新数量
            Integer num = cartFormDTO.getNum() != null ? cartFormDTO.getNum() : 1;
            baseMapper.updateNum(cartFormDTO.getItemId(), userId, num);
            return;
        }
        // 2.2.不存在，判断是否超过购物车数量
        checkCartsFull(userId);

        // 3.新增购物车条目
        // 3.1.转换PO
        Cart cart = BeanUtils.copyBean(cartFormDTO, Cart.class);
        // 3.2.保存当前用户
        cart.setUserId(userId);
        // 3.3.设置数量，默认为1
        if (cart.getNum() == null) {
            cart.setNum(1);
        }
        // 3.4.填充商品元数据（名称、图片、价格、规格）
        List<ItemDTO> items = itemClient.queryItemByIds(java.util.Collections.singletonList(cartFormDTO.getItemId()));
        if (!CollUtils.isEmpty(items)) {
            ItemDTO item = items.get(0);
            cart.setName(item.getName());
            cart.setImage(item.getImage());
            cart.setPrice(item.getPrice());
            if (StrUtil.isBlank(cart.getSpec())) {
                cart.setSpec(item.getSpec());
            }
        }
        // 3.5.保存到数据库
        save(cart);
    }

    @Override
    public List<CartVO> queryMyCarts() {
        // 1.查询我的购物车列表

        List<Cart> carts = lambdaQuery().eq(Cart::getUserId, UserContext.getUser() ).list();
        if (CollUtils.isEmpty(carts)) {
            return CollUtils.emptyList();
        }

        // 2.转换VO
        List<CartVO> vos = BeanUtils.copyList(carts, CartVO.class);

        // 3.处理VO中的商品信息
        handleCartItems(vos);

        // 4.返回
        return vos;
    }

    /**
     * 查询购物车商品，根据快照和当前数值返回降价结果
     * @param vos
     */
    private void handleCartItems(List<CartVO> vos) {
        // 1.获取商品id
        Set<Long> itemIds = vos.stream().map(CartVO::getItemId).collect(Collectors.toSet());
        // 2.查询商品
        /*
        //根据服务获取实例列表
        List<ServiceInstance> instances = discoveryClient.getInstances("item-service");
        if(instances.isEmpty()){
           return;
        }
        //负载均衡
        ServiceInstance instance = instances.get(RandomUtil.randomInt(instances.size()));

        // 2.1 发出请求
        ResponseEntity<List<ItemDTO>> response = restTemplate.exchange(
                instance.getUri() + "/items?ids={ids}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ItemDTO>>() {},
                Map.of("ids", CollectionUtil.join(itemIds, ","))
        );
        // 2.2 解析响应
        if(!response.getStatusCode().is2xxSuccessful()){
            return;

        }
        List<ItemDTO> items = response.getBody();
        if (CollUtils.isEmpty(items)) {
            return;
        }
        */
        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
        // 3.转为 id 到 item的map
        Map<Long, ItemDTO> itemMap = items.stream().collect(Collectors.toMap(ItemDTO::getId, Function.identity()));
        // 4.写入vo
        for (CartVO v : vos) {
            ItemDTO item = itemMap.get(v.getItemId());
            if (item == null) {
                continue;
            }
            v.setName(item.getName());
            v.setSpec(item.getSpec());
            v.setImage(item.getImage());
            v.setNewPrice(item.getPrice());
            v.setStatus(item.getStatus());
            v.setStock(item.getStock());
        }
    }

    @Override
    public void removeByItemIds(Collection<Long> itemIds) {
        // 1.构建删除条件，userId和itemId
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<Cart>();
        queryWrapper.lambda()
                .eq(Cart::getUserId, UserContext.getUser())
                .in(Cart::getItemId, itemIds);
        // 2.删除
        remove(queryWrapper);
    }

    private void checkCartsFull(Long userId) {
        long count = lambdaQuery().eq(Cart::getUserId, userId).count();
        if (count >= cartProperties.getMaxItems()) {
            throw new BizIllegalException(StrUtil.format(
                    "用户购物车课程不能超过{}", cartProperties.getMaxItems()));
        }
    }

    private boolean checkItemExists(Long itemId, Long userId) {
        long count = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getItemId, itemId)
                .count();
        return count > 0;
    }
}
