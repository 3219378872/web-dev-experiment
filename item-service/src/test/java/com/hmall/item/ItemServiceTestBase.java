package com.hmall.item;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public abstract class ItemServiceTestBase {
    protected static final Long TEST_USER_ID = 1L;
}
