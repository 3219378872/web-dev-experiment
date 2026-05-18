package com.hmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.domain.po.Address;
import java.util.List;

public interface IAddressService extends IService<Address> {
    List<Address> listByUserId(Long userId);
    void setDefault(Long id, Long userId);
}
