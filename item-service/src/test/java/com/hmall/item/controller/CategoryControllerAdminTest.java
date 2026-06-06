package com.hmall.item.controller;

import com.hmall.item.ItemServiceTestBase;
import com.hmall.item.domain.po.Category;
import com.hmall.item.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CategoryControllerAdminTest extends ItemServiceTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    void list_onlyReturnsEnabledCategoriesForCustomerFacingEndpoint() throws Exception {
        categoryMapper.insert(category("启用分类", 1, 10));
        categoryMapper.insert(category("禁用分类", 0, 20));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("启用分类"));
    }

    @Test
    void adminList_returnsDisabledCategoriesForLifecycleManagement() throws Exception {
        categoryMapper.insert(category("启用分类", 1, 10));
        categoryMapper.insert(category("禁用分类", 0, 20));

        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("启用分类"))
                .andExpect(jsonPath("$[1].name").value("禁用分类"));
    }

    private Category category(String name, Integer status, Integer sortOrder) {
        Category category = new Category();
        category.setName(name);
        category.setParentId(0L);
        category.setStatus(status);
        category.setSortOrder(sortOrder);
        return category;
    }
}
