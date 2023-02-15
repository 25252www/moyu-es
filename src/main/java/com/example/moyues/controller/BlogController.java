package com.example.moyues.controller;


import com.example.moyues.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlogController {
    @Autowired
    private BlogService blogService;

    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public Object search(@PathVariable("keyword") String keyword,
                         @PathVariable("pageNo") int pageNo,
                         @PathVariable("pageSize") int pageSize) throws Exception {
        if (pageNo <= 1) {
            pageNo = 1;
        }
        return blogService.searchPage(keyword, pageNo-1, pageSize);
    }
}
