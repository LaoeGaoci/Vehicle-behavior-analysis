package com.cms.backend.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Judge {

    @TableField("video_id")
    private String video_id;

    @TableField("username")
    private String username;

    @TableField("risk_level")
    private String risk_level;

    @TableField("selection_difficulty")
    private Integer selection_difficulty;
}
