package com.hmall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.domain.po.Address;
import com.hmall.mapper.AddressMapper;
import com.hmall.service.IAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address>
        implements IAddressService {

    @Override
    public List<Address> listByUserId(Long userId) {
        return lambdaQuery().eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault).list();
    }

    @Override
    @Transactional
    public void setDefault(Long id, Long userId) {
        lambdaUpdate().eq(Address::getUserId, userId).set(Address::getIsDefault, 0).update();
        lambdaUpdate().eq(Address::getId, id).eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 1).update();
    }
}
