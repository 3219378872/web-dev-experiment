package com.hmall.item.service.impl;

import com.hmall.common.domain.PageDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.item.ItemServiceTestBase;
import com.hmall.item.domain.po.Banner;
import com.hmall.item.domain.vo.BannerVO;
import com.hmall.item.mapper.BannerMapper;
import com.hmall.item.service.IBannerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BannerServiceImplTest extends ItemServiceTestBase {

    @Autowired
    private IBannerService bannerService;

    @Autowired
    private BannerMapper bannerMapper;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        Banner banner1 = new Banner();
        banner1.setImageUrl("http://example.com/image1.jpg");
        banner1.setLinkUrl("http://example.com/link1");
        banner1.setTitle("轮播图1");
        banner1.setType("carousel");
        banner1.setSort(1);
        banner1.setStatus(1);
        bannerMapper.insert(banner1);

        Banner banner2 = new Banner();
        banner2.setImageUrl("http://example.com/image2.jpg");
        banner2.setLinkUrl("http://example.com/link2");
        banner2.setTitle("轮播图2");
        banner2.setType("carousel");
        banner2.setSort(2);
        banner2.setStatus(1);
        bannerMapper.insert(banner2);

        Banner banner3 = new Banner();
        banner3.setImageUrl("http://example.com/image3.jpg");
        banner3.setLinkUrl("http://example.com/link3");
        banner3.setTitle("广告位1");
        banner3.setType("ad");
        banner3.setPosition("home_top");
        banner3.setSort(1);
        banner3.setStatus(1);
        bannerMapper.insert(banner3);

        Banner banner4 = new Banner();
        banner4.setImageUrl("http://example.com/image4.jpg");
        banner4.setLinkUrl("http://example.com/link4");
        banner4.setTitle("禁用轮播图");
        banner4.setType("carousel");
        banner4.setSort(3);
        banner4.setStatus(0);
        bannerMapper.insert(banner4);
    }

    @Nested
    @DisplayName("listActiveCarousels")
    class ListActiveCarouselsTests {

        @Test
        @DisplayName("查询启用的轮播图列表")
        void listActiveCarousels_returnsEnabledCarousels() {
            List<BannerVO> result = bannerService.listActiveCarousels();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(BannerVO::getTitle)
                    .containsExactly("轮播图1", "轮播图2");
        }

        @Test
        @DisplayName("按排序值排序")
        void listActiveCarousels_orderedBySort() {
            List<BannerVO> result = bannerService.listActiveCarousels();

            assertThat(result).extracting(BannerVO::getSort)
                    .containsExactly(1, 2);
        }
    }

    @Nested
    @DisplayName("listActiveAds")
    class ListActiveAdsTests {

        @Test
        @DisplayName("查询启用的广告位列表")
        void listActiveAds_returnsEnabledAds() {
            List<BannerVO> result = bannerService.listActiveAds("ad", "home_top");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("广告位1");
        }

        @Test
        @DisplayName("按类型过滤")
        void listActiveAds_filtersByType() {
            List<BannerVO> result = bannerService.listActiveAds("ad", null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getType()).isEqualTo("ad");
        }

        @Test
        @DisplayName("按位置过滤")
        void listActiveAds_filtersByPosition() {
            List<BannerVO> result = bannerService.listActiveAds(null, "home_top");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPosition()).isEqualTo("home_top");
        }
    }

    @Nested
    @DisplayName("queryBannersPage")
    class QueryBannersPageTests {

        @Test
        @DisplayName("分页查询轮播图列表")
        void queryBannersPage_returnsPage() {
            PageDTO<BannerVO> result = bannerService.queryBannersPage(1, 10, null);

            assertThat(result.getList()).hasSize(4);
            assertThat(result.getTotal()).isEqualTo(4L);
        }

        @Test
        @DisplayName("按类型过滤")
        void queryBannersPage_filtersByType() {
            PageDTO<BannerVO> result = bannerService.queryBannersPage(1, 10, "carousel");

            assertThat(result.getList()).hasSize(3);
            assertThat(result.getList()).allMatch(b -> "carousel".equals(b.getType()));
        }

        @Test
        @DisplayName("分页参数生效")
        void queryBannersPage_pagination_works() {
            PageDTO<BannerVO> result = bannerService.queryBannersPage(1, 2, null);

            assertThat(result.getList()).hasSize(2);
            assertThat(result.getTotal()).isEqualTo(4L);
        }
    }

    @Nested
    @DisplayName("createBanner")
    class CreateBannerTests {

        @Test
        @DisplayName("创建轮播图")
        void createBanner_success() {
            Banner banner = new Banner();
            banner.setImageUrl("http://example.com/new.jpg");
            banner.setTitle("新轮播图");
            banner.setType("carousel");
            banner.setSort(5);

            bannerService.createBanner(banner);

            assertThat(banner.getId()).isNotNull();
            assertThat(banner.getStatus()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("updateBanner")
    class UpdateBannerTests {

        @Test
        @DisplayName("更新轮播图")
        void updateBanner_success() {
            Banner banner = bannerMapper.selectList(null).get(0);
            banner.setTitle("更新后的标题");

            bannerService.updateBanner(banner);

            Banner updated = bannerMapper.selectById(banner.getId());
            assertThat(updated.getTitle()).isEqualTo("更新后的标题");
        }

        @Test
        @DisplayName("更新不存在的轮播图-抛出异常")
        void updateBanner_nonExistent_throws() {
            Banner banner = new Banner();
            banner.setId(99999L);
            banner.setTitle("不存在");

            assertThatThrownBy(() -> bannerService.updateBanner(banner))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("轮播图/广告位不存在");
        }
    }

    @Nested
    @DisplayName("deleteBanner")
    class DeleteBannerTests {

        @Test
        @DisplayName("删除轮播图")
        void deleteBanner_success() {
            Banner banner = bannerMapper.selectList(null).get(0);

            bannerService.deleteBanner(banner.getId());

            assertThat(bannerMapper.selectById(banner.getId())).isNull();
        }

        @Test
        @DisplayName("删除不存在的轮播图-抛出异常")
        void deleteBanner_nonExistent_throws() {
            assertThatThrownBy(() -> bannerService.deleteBanner(99999L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("轮播图/广告位不存在");
        }
    }

    @Nested
    @DisplayName("updateBannerStatus")
    class UpdateBannerStatusTests {

        @Test
        @DisplayName("更新轮播图状态")
        void updateBannerStatus_success() {
            Banner banner = bannerMapper.selectList(null).get(0);

            bannerService.updateBannerStatus(banner.getId(), 0);

            Banner updated = bannerMapper.selectById(banner.getId());
            assertThat(updated.getStatus()).isEqualTo(0);
        }

        @Test
        @DisplayName("更新不存在的轮播图状态-抛出异常")
        void updateBannerStatus_nonExistent_throws() {
            assertThatThrownBy(() -> bannerService.updateBannerStatus(99999L, 0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("轮播图/广告位不存在");
        }
    }

    @Nested
    @DisplayName("updateBannerSort")
    class UpdateBannerSortTests {

        @Test
        @DisplayName("更新轮播图排序")
        void updateBannerSort_success() {
            Banner banner = bannerMapper.selectList(null).get(0);

            bannerService.updateBannerSort(banner.getId(), 10);

            Banner updated = bannerMapper.selectById(banner.getId());
            assertThat(updated.getSort()).isEqualTo(10);
        }

        @Test
        @DisplayName("更新不存在的轮播图排序-抛出异常")
        void updateBannerSort_nonExistent_throws() {
            assertThatThrownBy(() -> bannerService.updateBannerSort(99999L, 10))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("轮播图/广告位不存在");
        }
    }
}