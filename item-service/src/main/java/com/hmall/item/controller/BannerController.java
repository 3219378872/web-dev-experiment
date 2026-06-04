package com.hmall.item.controller;

import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.item.domain.po.Banner;
import com.hmall.item.domain.vo.BannerVO;
import com.hmall.item.service.IBannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "轮播图/广告位管理")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class BannerController {

    private final IBannerService bannerService;

    @ApiOperation("前台查询轮播图列表")
    @GetMapping("/banners")
    public List<BannerVO> listCarousels() {
        return bannerService.listActiveCarousels();
    }

    @ApiOperation("前台查询广告位列表")
    @GetMapping("/ads")
    public List<BannerVO> listAds(
            @ApiParam("位置") @RequestParam(required = false) String position) {
        return bannerService.listActiveAds("ad", position);
    }

    @ApiOperation("管理端分页查询轮播图/广告位列表")
    @GetMapping("/admin/banners")
    public PageDTO<BannerVO> queryBannersPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("类型") @RequestParam(required = false) String type) {
        return bannerService.queryBannersPage(page, size, type);
    }

    @ApiOperation("管理端创建轮播图/广告位")
    @PostMapping("/admin/banners")
    public R<Void> createBanner(@RequestBody Banner banner) {
        bannerService.createBanner(banner);
        return R.ok();
    }

    @ApiOperation("管理端更新轮播图/广告位")
    @PutMapping("/admin/banners/{id}")
    public R<Void> updateBanner(
            @ApiParam("ID") @PathVariable Long id,
            @RequestBody Banner banner) {
        banner.setId(id);
        bannerService.updateBanner(banner);
        return R.ok();
    }

    @ApiOperation("管理端删除轮播图/广告位")
    @DeleteMapping("/admin/banners/{id}")
    public R<Void> deleteBanner(
            @ApiParam("ID") @PathVariable Long id) {
        bannerService.deleteBanner(id);
        return R.ok();
    }

    @ApiOperation("管理端更新轮播图/广告位状态")
    @PutMapping("/admin/banners/{id}/status")
    public R<Void> updateBannerStatus(
            @ApiParam("ID") @PathVariable Long id,
            @ApiParam("状态") @RequestBody Integer status) {
        bannerService.updateBannerStatus(id, status);
        return R.ok();
    }

    @ApiOperation("管理端更新轮播图/广告位排序")
    @PutMapping("/admin/banners/{id}/sort")
    public R<Void> updateBannerSort(
            @ApiParam("ID") @PathVariable Long id,
            @ApiParam("排序值") @RequestBody Integer sort) {
        bannerService.updateBannerSort(id, sort);
        return R.ok();
    }
}