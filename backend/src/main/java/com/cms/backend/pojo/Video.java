package com.cms.backend.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Video {

    @TableField("video_id")
    private String video_id;

    @TableField("risk_level")
    private String risk_level;

    @TableField("selection_difficulty")
    private Integer selection_difficulty;

    @TableField("v1")
    private Integer V1;

    @TableField("v3")
    private Integer V3;

    @TableField("v4")
    private Integer V4;

    @TableField("a1")
    private Integer A1;

    @TableField("a2")
    private Integer A2;

    @TableField("a3")
    private Integer A3;

    @TableField("a4")
    private Integer A4;

    @TableField("g")
    private Integer G;

    @TableField("l2")
    private Integer L2;

    @TableField("l3")
    private Integer L3;

    @TableField("l4")
    private Integer L4;
}
