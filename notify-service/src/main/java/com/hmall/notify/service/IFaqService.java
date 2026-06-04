package com.hmall.notify.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.notify.domain.po.Faq;
import com.hmall.notify.domain.vo.FaqVO;
import java.util.List;

public interface IFaqService extends IService<Faq> {
    List<FaqVO> getActiveFaqs();
}
