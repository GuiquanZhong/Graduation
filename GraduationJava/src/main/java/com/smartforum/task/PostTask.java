package com.smartforum.task;

import com.smartforum.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PostTask {

    @Autowired
    private PostMapper postMapper;

    /**
     * 每天凌晨0点执行，重置所有帖子的今日浏览量
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyViewCount() {
        log.info("开始重置帖子今日浏览量...");
        try {
            postMapper.resetDailyViewCount();
            log.info("重置帖子今日浏览量完成！");
        } catch (Exception e) {
            log.error("重置帖子今日浏览量失败：", e);
        }
    }
}
