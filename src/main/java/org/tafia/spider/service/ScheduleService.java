package org.tafia.spider.service;

import java.util.Map;

/**
 * 调度服务
 */
public interface ScheduleService {

    void schedule();

    void refresh();

    Map<String, String> status();
}
