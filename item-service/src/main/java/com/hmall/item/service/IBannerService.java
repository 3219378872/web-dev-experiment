package com.hmall.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.common.domain.PageDTO;
import com.hmall.item.domain.po.Banner;
import com.hmall.item.domain.vo.BannerVO;

import java.util.List;

public interface IBannerService extends IService<Banner> {

    /**
     * 前台查询启用的轮播图列表（按 sort 排序）
     * @return 轮播图列表
     */
    List<BannerVO> listActiveCarousels();

    /**
     * 前台查询启用的广告位列表（按 type/position 过滤）
     * @param type 类型
     * @param position 位置
     * @return 广告位列表
     */
    List<BannerVO> listActiveAds(String type, String position);

    /**
     * 管理端分页查询轮播图/广告位列表
     * @param page 页码
     * @param size 每页大小
     * @param type 类型（可选）
     * @return 分页数据
     */
    PageDTO<BannerVO> queryBannersPage(Integer page, Integer size, String type);

    /**
     * 创建轮播图/广告位
     * @param banner 轮播图/广告位信息
     */
    void createBanner(Banner banner);

    /**
     * 更新轮播图/广告位
     * @param banner 轮播图/广告位信息
     */
    void updateBanner(Banner banner);

    /**
     * 删除轮播图/广告位
     * @param id ID
     */
    void deleteBanner(Long id);

    /**
     * 更新轮播图/广告位状态
     * @param id ID
     * @param status 状态
     */
    void updateBannerStatus(Long id, Integer status);

    /**
     * 更新轮播图/广告位排序
     * @param id ID
     * @param sort 排序值
     */
    void updateBannerSort(Long id, Integer sort);
}