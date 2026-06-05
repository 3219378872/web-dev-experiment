package com.hmall.it;

import com.hmall.api.client.ItemClient;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.Address;
import com.hmall.service.IAddressService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.cloud.bootstrap.enabled=false"})
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/sql/data-address.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AddressServiceImplIT {

    @MockBean
    private ItemClient itemClient;

    @BeforeEach
    void setUp() { UserContext.setUser(1L); }

    @AfterEach
    void tearDown() { UserContext.removeUser(); }

    @Autowired
    private IAddressService addressService;

    @Test
    void listByUserId_shouldReturnAddressesWithDefaultFirst() {
        List<Address> result = addressService.listByUserId(1L);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIsDefault()).isEqualTo(1);
    }

    @Test
    void setDefault_shouldClearOldDefault() {
        addressService.setDefault(2L, 1L);
        List<Address> result = addressService.listByUserId(1L);
        assertThat(result).filteredOn(a -> a.getIsDefault() == 1).hasSize(1);
        Address newDefault = result.stream()
                .filter(a -> a.getIsDefault() == 1).findFirst().orElseThrow();
        assertThat(newDefault.getId()).isEqualTo(2L);
    }
}
