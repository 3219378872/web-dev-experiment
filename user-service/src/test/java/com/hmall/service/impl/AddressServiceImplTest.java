package com.hmall.service.impl;

import com.hmall.UserServiceTestBase;
import com.hmall.domain.po.Address;
import com.hmall.mapper.AddressMapper;
import com.hmall.service.IAddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AddressServiceImplTest extends UserServiceTestBase {

    @Autowired
    private IAddressService addressService;

    @Autowired
    private AddressMapper addressMapper;

    private Address insertAddress(Long userId, String name, int isDefault) {
        Address addr = new Address();
        addr.setUserId(userId);
        addr.setReceiverName(name);
        addr.setPhone("13800000000");
        addr.setProvince("浙江省");
        addr.setCity("杭州市");
        addr.setDistrict("西湖区");
        addr.setDetail("文一路" + name);
        addr.setIsDefault(isDefault);
        addressMapper.insert(addr);
        return addr;
    }

    // ───────────────────── listByUserId ─────────────────────

    @Nested
    @DisplayName("listByUserId")
    class ListByUserIdTests {

        @Test
        @DisplayName("有地址-返回按isDefault降序排列的列表")
        void withAddresses_returnsSortedList() {
            insertAddress(10L, "普通地址", 0);
            insertAddress(10L, "默认地址", 1);

            List<Address> result = addressService.listByUserId(10L);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getIsDefault()).isEqualTo(1);
            assertThat(result.get(1).getIsDefault()).isEqualTo(0);
        }

        @Test
        @DisplayName("无地址-返回空列表")
        void noAddresses_returnsEmptyList() {
            List<Address> result = addressService.listByUserId(999L);

            assertThat(result).isEmpty();
        }
    }

    // ───────────────────── setDefault ─────────────────────

    @Nested
    @DisplayName("setDefault")
    class SetDefaultTests {

        @Test
        @DisplayName("设置默认地址-先清除所有默认再设置新的")
        void setDefault_clearsOldAndSetsNew() {
            Address addr1 = insertAddress(10L, "地址1", 1);
            Address addr2 = insertAddress(10L, "地址2", 0);

            addressService.setDefault(addr2.getId(), 10L);

            Address updated1 = addressMapper.selectById(addr1.getId());
            Address updated2 = addressMapper.selectById(addr2.getId());
            assertThat(updated1.getIsDefault()).isEqualTo(0);
            assertThat(updated2.getIsDefault()).isEqualTo(1);
        }

        @Test
        @DisplayName("设置不存在的地址-id不匹配-不影响其他地址")
        void setDefault_nonExistentId_noEffect() {
            Address addr = insertAddress(10L, "地址1", 1);

            addressService.setDefault(999L, 10L);

            Address unchanged = addressMapper.selectById(addr.getId());
            assertThat(unchanged.getIsDefault()).isEqualTo(0); // 被清除为0，但新地址未设置
        }
    }
}
