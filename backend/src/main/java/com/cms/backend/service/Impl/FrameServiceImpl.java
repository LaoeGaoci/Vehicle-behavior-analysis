package com.cms.backend.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.backend.mapper.FrameMapper;
import com.cms.backend.pojo.Frame;
import com.cms.backend.service.FrameService;
import org.springframework.stereotype.Service;

@Service
public class FrameServiceImpl extends ServiceImpl<FrameMapper, Frame> implements FrameService {

}
