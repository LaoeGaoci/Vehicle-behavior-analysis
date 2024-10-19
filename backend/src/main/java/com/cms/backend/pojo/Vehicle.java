package com.cms.backend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Vehicle {

    @TableId
    private Integer vehicleId;

    private float vLength;

    private float vWidth;

    private Integer vClass;

}
