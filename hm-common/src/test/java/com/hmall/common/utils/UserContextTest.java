package com.hmall.common.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

class UserContextTest {

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    @Test
    @DisplayName("未 setUser 时 getUser 返回 null")
    void getUser_withoutSet_returnsNull() {
        assertThat(UserContext.getUser()).isNull();
    }

    @Test
    @DisplayName("setUser / getUser / removeUser 完整生命周期")
    void userLifecycle_setGetRemove() {
        UserContext.setUser(42L);
        assertThat(UserContext.getUser()).isEqualTo(42L);
        UserContext.removeUser();
        assertThat(UserContext.getUser()).isNull();
    }

    @Test
    @DisplayName("setRole / getRole / removeRole 完整生命周期")
    void roleLifecycle_setGetRemove() {
        UserContext.setRole("admin");
        assertThat(UserContext.getRole()).isEqualTo("admin");
        UserContext.removeRole();
        assertThat(UserContext.getRole()).isNull();
    }

    @Test
    @DisplayName("clear() 同时清除 user 与 role")
    void clear_removesBothUserAndRole() {
        UserContext.setUser(7L);
        UserContext.setRole("user");
        UserContext.clear();
        assertThat(UserContext.getUser()).isNull();
        assertThat(UserContext.getRole()).isNull();
    }

    @Test
    @DisplayName("UserContext 是 ThreadLocal，跨线程不共享")
    void threadLocal_isolatesAcrossThreads() throws ExecutionException, InterruptedException {
        UserContext.setUser(100L);
        UserContext.setRole("admin");

        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<Long> userInOtherThread = pool.submit(UserContext::getUser);
            Future<String> roleInOtherThread = pool.submit(UserContext::getRole);
            assertThat(userInOtherThread.get()).isNull();
            assertThat(roleInOtherThread.get()).isNull();
        } finally {
            pool.shutdownNow();
        }

        // 当前线程仍保留自己的值
        assertThat(UserContext.getUser()).isEqualTo(100L);
        assertThat(UserContext.getRole()).isEqualTo("admin");
    }
}
