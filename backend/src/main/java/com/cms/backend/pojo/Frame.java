package com.cms.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Frame {

    private Integer vehicleId; // 车辆编号

    private Integer frameId; // 帧编号

    private Integer totalFrame; // 总帧数

    private String globalTime; // 时间

    private float localX; // 坐标x

    private float localY; // 坐标y

    private float globalX; // 坐标x

    private float globalY; // 坐标y

    private float vLength; // 车长

    private float vWidth; // 车宽

    private Integer vClass; // 车辆类别

    private float velocity; // 速度

    private float acceleration; // 加速度

    private Integer laneId; // 车道号

    private Integer preceding; // 前车编号

    private Integer following; // 后车编号

    private float spaceHead;

    private float timeHead;

    private String location; // 地点

}
