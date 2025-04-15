package com.cms.backend.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.backend.mapper.JudgeMapper;
import com.cms.backend.pojo.Judge;
import com.cms.backend.service.JudgeService;
import org.springframework.stereotype.Service;

@Service
public class JudgeServiceImpl extends ServiceImpl<JudgeMapper, Judge> implements JudgeService {
}
