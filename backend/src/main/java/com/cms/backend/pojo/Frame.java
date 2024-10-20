package com.cms.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Frame {

//    private Integer frameId;
//
//    private Integer vehicle_id;
//
//    private Integer total_frame;
//
//    //frame
//    private String global_time;
//
//    private float local_x;
//
//    private float local_y;
//
//    //path
//    private Long global_x;
//
//    //path
//    private Long global_y;
//
//    //frame
//    private float velocity;
//
//    //frame
//    private float acceleration;
//
//    //frame
//    private Integer lane_id;
//
//    private Integer preceding;
//
//    private Integer following;
//
//    private float space_head;
//
//    private float time_head;


    private Integer vehicleId;   // vehicle_id
    private String globalTime; // global_time
    private Integer laneId;    // lane_id
    private float velocity;    // velocity
    private float acceleration; // acceleration
    private float globalX;     // global_x
    private float globalY;     // global_y


}
