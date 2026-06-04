package com.hmall.notify.service.impl;

import com.hmall.notify.domain.po.Faq;
import com.hmall.notify.domain.vo.FaqVO;
import com.hmall.notify.mapper.FaqMapper;
import com.hmall.notify.service.IFaqService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FaqServiceImplTest {

    @Autowired
    private IFaqService faqService;

    @Autowired
    private FaqMapper faqMapper;

    @Test
    @DisplayName("getActiveFaqs: 只返回状态=1的FAQ，按 sort 升序 + createTime 降序")
    void getActiveFaqs_returnsActiveOrderedBySortThenTime() {
        Faq f1 = faq("问题A", "回答A", 2, 1);
        Faq f2 = faq("问题B", "回答B", 1, 1);
        Faq f3 = faq("问题C", "回答C", 1, 0);  // 禁用
        faqMapper.insert(f1);
        faqMapper.insert(f2);
        faqMapper.insert(f3);

        List<FaqVO> result = faqService.getActiveFaqs();

        assertThat(result).hasSize(2);
        // sort=1 的排前面；同 sort 内按 createTime 降序
        assertThat(result.get(0).getQuestion()).isEqualTo("问题B");
        assertThat(result.get(1).getQuestion()).isEqualTo("问题A");
        // VO 只含 id, question, answer
        assertThat(result.get(0).getId()).isNotNull();
        assertThat(result.get(0).getAnswer()).isEqualTo("回答B");
    }

    @Test
    @DisplayName("getActiveFaqs: 无活跃FAQ时返回空列表")
    void getActiveFaqs_noActive_returnsEmpty() {
        List<FaqVO> result = faqService.getActiveFaqs();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getActiveFaqs: 所有FAQ均禁用时返回空列表")
    void getActiveFaqs_allDisabled_returnsEmpty() {
        Faq f1 = faq("问题A", "回答A", 1, 0);
        Faq f2 = faq("问题B", "回答B", 2, 0);
        faqMapper.insert(f1);
        faqMapper.insert(f2);

        List<FaqVO> result = faqService.getActiveFaqs();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save + getById: FAQ数据正常持久化")
    void saveAndGetById_works() {
        Faq f = new Faq();
        f.setQuestion("如何退款？");
        f.setAnswer("进入订单详情点击退款按钮");
        f.setSort(1);
        f.setStatus(1);
        faqService.save(f);

        Faq saved = faqService.getById(f.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getQuestion()).isEqualTo("如何退款？");
        assertThat(saved.getAnswer()).isEqualTo("进入订单详情点击退款按钮");
    }

    @Test
    @DisplayName("getById: 不存在的ID返回 null")
    void getById_nonExistent_returnsNull() {
        assertThat(faqService.getById(999L)).isNull();
    }

    @Test
    @DisplayName("getActiveFaqs: FAQ按 sort 升序排列")
    void getActiveFaqs_sortsBySortAsc() {
        Faq f1 = faq("低优先", "答1", 10, 1);
        Faq f2 = faq("高优先", "答2", 1, 1);
        Faq f3 = faq("中优先", "答3", 5, 1);
        faqMapper.insert(f1);
        faqMapper.insert(f2);
        faqMapper.insert(f3);

        List<FaqVO> result = faqService.getActiveFaqs();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getQuestion()).isEqualTo("高优先");
        assertThat(result.get(1).getQuestion()).isEqualTo("中优先");
        assertThat(result.get(2).getQuestion()).isEqualTo("低优先");
    }

    private Faq faq(String question, String answer, int sort, int status) {
        Faq f = new Faq();
        f.setQuestion(question);
        f.setAnswer(answer);
        f.setSort(sort);
        f.setStatus(status);
        return f;
    }
}
