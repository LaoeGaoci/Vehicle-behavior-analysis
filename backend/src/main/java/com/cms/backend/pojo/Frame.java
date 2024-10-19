package com.cms.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Frame {

    private Integer frameId;

    private Integer vehicle_id;

    private Integer total_frame;

    private Long global_time;

    private float local_x;

    private float local_y;

    private Long global_x;

    private Long global_y;

    private float velocity;

    private float acceleration;

    private Integer lane_id;

    private Integer preceding;

    private Integer following;

    private float space_head;

    private float time_head;

}
