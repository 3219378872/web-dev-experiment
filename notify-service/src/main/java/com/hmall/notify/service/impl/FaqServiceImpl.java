package com.hmall.notify.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.notify.domain.po.Faq;
import com.hmall.notify.domain.vo.FaqVO;
import com.hmall.notify.mapper.FaqMapper;
import com.hmall.notify.service.IFaqService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FaqServiceImpl extends ServiceImpl<FaqMapper, Faq>
        implements IFaqService {

    @Override
    public List<FaqVO> getActiveFaqs() {
        return lambdaQuery()
                .eq(Faq::getStatus, 1)
                .orderByAsc(Faq::getSort)
                .orderByDesc(Faq::getCreateTime)
                .list()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private FaqVO toVO(Faq faq) {
        FaqVO vo = new FaqVO();
        vo.setId(faq.getId());
        vo.setQuestion(faq.getQuestion());
        vo.setAnswer(faq.getAnswer());
        return vo;
    }
}
