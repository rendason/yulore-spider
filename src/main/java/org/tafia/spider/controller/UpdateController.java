package org.tafia.spider.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tafia.spider.component.ApiResponse;
import org.tafia.spider.service.DataUpdateService;
import org.tafia.spider.service.ScheduleService;

/**
 * Created by Dason on 2017/6/17.
 */
@RestController
@RequestMapping("/update")
public class UpdateController {

    @Autowired
    private ScheduleService scheduleService;

    @RequestMapping("/manual")
    public ApiResponse manual() {
        scheduleService.refresh();
        return ApiResponse.custom(0, "操作成功");
    }

    @RequestMapping("/status")
    public ApiResponse status() {
        return ApiResponse.custom(0, "查询成功", scheduleService.status());
    }
}
