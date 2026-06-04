package com.hmall.item.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.item.domain.po.Banner;
import com.hmall.item.domain.vo.BannerVO;
import com.hmall.item.mapper.BannerMapper;
import com.hmall.item.service.IBannerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner>
        implements IBannerService {

    @Override
    public List<BannerVO> listActiveCarousels() {
        List<Banner> banners = lambdaQuery()
                .eq(Banner::getStatus, 1)
                .eq(Banner::getType, "carousel")
                .orderByAsc(Banner::getSort)
                .list();
        return convertToVOList(banners);
    }

    @Override
    public List<BannerVO> listActiveAds(String type, String position) {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, 1);
        if (StrUtil.isNotBlank(type)) {
            wrapper.eq(Banner::getType, type);
        }
        if (StrUtil.isNotBlank(position)) {
            wrapper.eq(Banner::getPosition, position);
        }
        wrapper.orderByAsc(Banner::getSort);
        List<Banner> banners = list(wrapper);
        return convertToVOList(banners);
    }

    @Override
    public PageDTO<BannerVO> queryBannersPage(Integer page, Integer size, String type) {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(type)) {
            wrapper.eq(Banner::getType, type);
        }
        wrapper.orderByAsc(Banner::getSort);

        Page<Banner> pageParam = new Page<>(page, size);
        Page<Banner> result = page(pageParam, wrapper);

        List<BannerVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageDTO<>(result.getTotal(), result.getPages(), voList);
    }

    @Override
    public void createBanner(Banner banner) {
        banner.setStatus(1);
        save(banner);
    }

    @Override
    public void updateBanner(Banner banner) {
        Banner existing = getById(banner.getId());
        if (existing == null) {
            throw new BadRequestException("轮播图/广告位不存在");
        }
        updateById(banner);
    }

    @Override
    public void deleteBanner(Long id) {
        Banner existing = getById(id);
        if (existing == null) {
            throw new BadRequestException("轮播图/广告位不存在");
        }
        removeById(id);
    }

    @Override
    public void updateBannerStatus(Long id, Integer status) {
        Banner existing = getById(id);
        if (existing == null) {
            throw new BadRequestException("轮播图/广告位不存在");
        }
        existing.setStatus(status);
        updateById(existing);
    }

    @Override
    public void updateBannerSort(Long id, Integer sort) {
        Banner existing = getById(id);
        if (existing == null) {
            throw new BadRequestException("轮播图/广告位不存在");
        }
        existing.setSort(sort);
        updateById(existing);
    }

    private BannerVO convertToVO(Banner banner) {
        BannerVO vo = new BannerVO();
        vo.setId(banner.getId());
        vo.setImageUrl(banner.getImageUrl());
        vo.setLinkUrl(banner.getLinkUrl());
        vo.setTitle(banner.getTitle());
        vo.setType(banner.getType());
        vo.setPosition(banner.getPosition());
        vo.setSort(banner.getSort());
        vo.setStatus(banner.getStatus());
        vo.setCreateTime(banner.getCreateTime());
        vo.setUpdateTime(banner.getUpdateTime());
        return vo;
    }

    private List<BannerVO> convertToVOList(List<Banner> banners) {
        return banners.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
}