package com.cms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cms.backend.pojo.Frame;
import com.cms.backend.pojo.Vehicle;
import com.cms.backend.service.FrameService;
import com.cms.backend.service.VehicleService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Validated
@RestController
public class VehicleController {

    private final VehicleService vehicleService;

    private final FrameService frameService;

    public VehicleController(VehicleService vehicleService, FrameService frameService) {
        this.vehicleService = vehicleService;
        this.frameService = frameService;
    }

    @GetMapping(value = "/vehicle-data-list")
    public ResponseEntity<List<DataList>> getUserInfo() {
        // 查询所有 Vehicle 的需要列（需要的列有vehicleId，vClass，vLength，vWidth）
        List<Vehicle> vehicles = vehicleService.list(
                new LambdaQueryWrapper<Vehicle>().select(Vehicle::getVehicleId, Vehicle::getVClass, Vehicle::getVLength, Vehicle::getVWidth)
        );

        // 查询所有 Vehicle 的需要列（需要的列有vehicleId，globalTime，laneId，velocity,acceleration,globalX,globalY）
        List<Frame> frames = frameService.list(
                new LambdaQueryWrapper<Frame>().select(Frame::getVehicleId, Frame::getGlobalTime, Frame::getLaneId, Frame::getVelocity, Frame::getAcceleration, Frame::getGlobalX, Frame::getGlobalY)
        );

        // 构建 DataList 列表
        List<DataList> dataLists = new ArrayList<>();

        // 将所有车辆与对应的帧组合
        for (Vehicle vehicle : vehicles) {
            for (Frame frame : frames) {
                if (frame.getVehicleId().equals(vehicle.getVehicleId())) {
                    DataList dataList = new DataList(vehicle, frame);
                    dataLists.add(dataList);
                }
            }
        }

        return ResponseEntity.ok(dataLists);
    }

    @Data
    public static class FrameData {
        private String globalTime;
        private Integer laneId;
        private float velocity;
        private float acceleration;
    }

    @Data
    public static class PathData {
        private float globalX;
        private float globalY;
    }


    @Data
    public static class DataList {
        public DataList(Vehicle vehicle, Frame frame) {
            this.vehicleId = vehicle.getVehicleId();
            this.vClass = vehicle.getVClass();
            this.vLength = vehicle.getVLength();
            this.vWidth = vehicle.getVWidth();

            // 创建 FrameData 实例并添加到 frame 列表
            FrameData frameData = new FrameData();
            frameData.setGlobalTime(frame.getGlobalTime());
            frameData.setLaneId(frame.getLaneId());
            frameData.setVelocity(frame.getVelocity());
            frameData.setAcceleration(frame.getAcceleration());
            this.frame = new ArrayList<>(); // 初始化列表
            this.frame.add(frameData);

            // 创建 PathData 实例并添加到 path 列表
            PathData pathData = new PathData();
            pathData.setGlobalX(frame.getGlobalX());
            pathData.setGlobalY(frame.getGlobalY());
            this.path = new ArrayList<>(); // 初始化列表
            this.path.add(pathData);
        }

        private Integer vehicleId;

        private Integer vClass;

        private float vLength;

        private float vWidth;

        private List<FrameData> frame;

        private List<PathData> path;
    }
}
