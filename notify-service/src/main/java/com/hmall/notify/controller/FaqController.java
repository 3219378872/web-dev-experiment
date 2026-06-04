package com.hmall.notify.controller;

import com.hmall.notify.domain.vo.FaqVO;
import com.hmall.notify.service.IFaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class FaqController {

    private final IFaqService faqService;

    @GetMapping("/faqs")
    public List<FaqVO> list() {
        return faqService.getActiveFaqs();
    }
}
