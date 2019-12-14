package com.ziyin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: TheBigBlue
 * @Description:
 * @Date: 2019/8/27
 */
@RestController
public class IndexController {

    @RequestMapping("/index")
    public Object index() {
        return "success";
    }
}
