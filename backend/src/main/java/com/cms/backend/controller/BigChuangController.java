package com.cms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cms.backend.pojo.Judge;
import com.cms.backend.pojo.Video;
import com.cms.backend.pojo.User;
import com.cms.backend.service.JudgeService;
import com.cms.backend.service.UserService;
import com.cms.backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BigChuangController {
    @Autowired
    private final JudgeService judgeService;

    @Autowired
    private final VideoService videoService;

    @Autowired
    private final UserService userService;

    public BigChuangController(JudgeService judgeService, VideoService videoService, UserService userService) {
        this.judgeService = judgeService;
        this.videoService = videoService;
        this.userService = userService;
    }

    /**
     * 获取视频
     *
     * @param number 视频个数
     * @return 视频列表
     */
    @GetMapping("/getVideos")
    public ResponseEntity<List<Video>> check(@RequestParam Integer number){
        System.out.println(number);
        List<Video> videos = videoService.list();
        System.out.println("2");
        return ResponseEntity.ok(videos);
    }

    /**
     * 用户信息
     *
     * @param user 用户信息
     * @return 收藏结果
     */
    @PostMapping("/user")
    public String collect(@RequestBody User user) {
        userService.save(user);
        return "";
    }

    /**
     * 用户提交判断
     *
     * @param judge 用户判断
     * @return 用户提交判断
     */
    @PostMapping("/submitRating")
    public String collect(@RequestBody Judge judge) {
        judgeService.save(judge);
        return "";
    }
}
