package com.cms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cms.backend.pojo.Frame;
import com.cms.backend.pojo.Vehicle;
import com.cms.backend.service.FrameService;
import com.cms.backend.service.VehicleService;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Validated
@RestController
@RequestMapping("/vehicle")
public class VehicleController {

    private final VehicleService vehicleService;

    private final FrameService frameService;

    public VehicleController(VehicleService vehicleService, FrameService frameService) {
        this.vehicleService = vehicleService;
        this.frameService = frameService;
    }

    @GetMapping(value = "/data-list")
    public ResponseEntity<VehicleDataList> getUserInfo() {

        // 查询所有 Vehicle 的需要列（需要的列有vehicleId，vClass，vLength，vWidth）
        List<Vehicle> vehicles = vehicleService.list(
                new LambdaQueryWrapper<Vehicle>().select(Vehicle::getVehicleId, Vehicle::getVClass, Vehicle::getVLength, Vehicle::getVWidth)
        );

        // 查询所有 Vehicle 的需要列（需要的列有vehicleId，globalTime，laneId，velocity,acceleration,globalX,globalY）
        List<Frame> frames = frameService.list(
                new LambdaQueryWrapper<Frame>().select(Frame::getVehicleId, Frame::getGlobalTime, Frame::getLaneId, Frame::getVelocity, Frame::getAcceleration, Frame::getGlobalX, Frame::getGlobalY)
        );

        List<DataList> dataLists = new ArrayList<>();

        // 将所有车辆与对应的帧组合
        for (Vehicle vehicle : vehicles) {
            List<FrameData> frameDataList = new ArrayList<>();
            List<PathData> pathDataList = new ArrayList<>();

            for (Frame frame : frames) {
                if (frame.getVehicleId().equals(vehicle.getVehicleId())) {
                    FrameData frameData = new FrameData(frame.getGlobalTime(), frame.getLaneId(), frame.getVelocity(), frame.getAcceleration());
                    frameDataList.add(frameData);
                    PathData pathData = new PathData(frame.getGlobalX(), frame.getGlobalY());
                    pathDataList.add(pathData);
                }
            }

            DataList dataList = new DataList(vehicle.getVehicleId(), vehicle.getVClass(), vehicle.getVLength(), vehicle.getVWidth(), frameDataList, pathDataList);
            dataLists.add(dataList);
        }

        VehicleDataList vehicleDataList = new VehicleDataList(dataLists);

        return ResponseEntity.ok(vehicleDataList);
    }

    @Data
    @AllArgsConstructor
    public static class FrameData {

        private String globalTime;

        private Integer laneId;

        private float velocity;

        private float acceleration;

    }

    @Data
    @AllArgsConstructor
    public static class PathData {

        private float globalX;

        private float globalY;

    }

    @Data
    @AllArgsConstructor
    public static class DataList {

        @JsonProperty("vehicleId")
        private Integer vehicleId;

        @JsonProperty("vClass")
        private Integer vClass;

        @JsonProperty("vLength")
        private float vLength;

        @JsonProperty("vWidth")
        private float vWidth;

        @JsonProperty("frame")
        private List<FrameData> frame;

        @JsonProperty("path")
        private List<PathData> path;

    }

    @Data
    @AllArgsConstructor
    public static class VehicleDataList {

        private List<DataList> vehicleDataList;

    }

}
