package com.hmall.item.controller;

import com.hmall.common.domain.R;
import com.hmall.item.domain.po.Category;
import com.hmall.item.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping
    public List<Category> list() {
        return categoryService.lambdaQuery()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder)
                .list();
    }

    @PostMapping
    public R<Void> save(@RequestBody Category category) {
        category.setCreateTime(LocalDateTime.now());
        categoryService.save(category);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        category.setUpdateTime(LocalDateTime.now());
        categoryService.updateById(category);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return R.ok();
    }
}
