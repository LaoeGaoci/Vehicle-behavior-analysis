package com.cms.backend.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.backend.mapper.VideoMapper;
import com.cms.backend.pojo.Video;
import com.cms.backend.service.VideoService;
import org.springframework.stereotype.Service;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {
}
