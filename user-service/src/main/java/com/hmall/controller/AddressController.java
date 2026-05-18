package com.hmall.controller;

import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.Address;
import com.hmall.service.IAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

    @GetMapping
    public List<Address> list() {
        return addressService.listByUserId(UserContext.getUser());
    }

    @PostMapping
    public R<Void> save(@RequestBody Address address) {
        address.setUserId(UserContext.getUser());
        address.setCreateTime(LocalDateTime.now());
        addressService.save(address);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Address address) {
        address.setId(id);
        address.setUpdateTime(LocalDateTime.now());
        addressService.updateById(address);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        addressService.removeById(id);
        return R.ok();
    }

    @PutMapping("/{id}/default")
    public R<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id, UserContext.getUser());
        return R.ok();
    }
}
