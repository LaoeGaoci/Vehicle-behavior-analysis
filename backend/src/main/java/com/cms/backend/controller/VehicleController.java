package com.cms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cms.backend.pojo.Frame;
import com.cms.backend.service.FrameService;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/vehicle")
public class VehicleController {

    private final FrameService frameService;
    private final static double timeWindow = 2;

    public VehicleController(FrameService frameService) {
        this.frameService = frameService;
    }

    @GetMapping(value = "/change-list")
    public ResponseEntity<ChangeEventList> getChangeEventList(@RequestParam Integer number, @RequestParam float distanceThreshold) {
        System.out.println("Number: " + number);
        System.out.println("DistanceThreshold: " + distanceThreshold);

        // 查询所有帧数据并按 globalTime 排序
        List<Frame> frames = frameService.list(
                new LambdaQueryWrapper<Frame>().select(Frame::getVehicleId, Frame::getFrameId, Frame::getTotalFrame, Frame::getGlobalTime, Frame::getLocalX, Frame::getLocalY,
                        Frame::getGlobalX, Frame::getGlobalY, Frame::getVLength, Frame::getVWidth, Frame::getVClass, Frame::getVelocity, Frame::getAcceleration, Frame::getLaneId,
                        Frame::getPreceding, Frame::getFollowing, Frame::getSpaceHead, Frame::getTimeHead, Frame::getLocation)
        ).stream().sorted(Comparator.comparing(Frame::getGlobalTime)).toList();

        // 按 VehicleKey 分组
        Map<VehicleKey, List<Frame>> groupedFrames = frames.stream()
                .collect(Collectors.groupingBy(frame -> new VehicleKey(
                        frame.getVehicleId(),
                        frame.getVLength(),
                        frame.getVWidth(),
                        frame.getVClass()
                )));

        List<ChangeEvent> changeEventList = new ArrayList<>();

        // 遍历分组数据，检测变道事件
        for (Map.Entry<VehicleKey, List<Frame>> entry : groupedFrames.entrySet()) {
            List<Frame> vehicleFrames = entry.getValue();

            for (int i = 1; i < vehicleFrames.size(); i++) {
                Frame current = vehicleFrames.get(i);
                Frame previous = vehicleFrames.get(i - 1);

                // 检测变道
                if (!Objects.equals(current.getLaneId(), previous.getLaneId())) {
                    String changeTime = current.getGlobalTime(); // 换道时间
                    long changeTimestamp = parseTimestamp(changeTime); // 转换为时间戳

                    if (changeTimestamp == -1) continue; // 无效时间，跳过

                    ChangeEvent changeEvent = new ChangeEvent();

                    // 构建变道车辆数据
                    VehicleList changingVehicle = buildVehicleData(vehicleFrames, changeTimestamp);

                    // 构建时间窗口内的周围车辆数据，加入距离过滤
                    List<VehicleList> surroundingVehicles = groupedFrames.entrySet().stream()
                            .filter(neighborEntry -> {
                                return !neighborEntry.getKey().getVehicleId().equals(changingVehicle.getVehicleId()); // 排除变道车辆自己
                            })
                            .map(neighborEntry -> buildFilteredVehicleData(
                                    neighborEntry.getValue(), changeTimestamp, changingVehicle, distanceThreshold
                            ))
                            .filter(Objects::nonNull)
                            .toList();

                    // 组合变道事件
                    List<VehicleList> allVehicles = new ArrayList<>();
                    allVehicles.add(changingVehicle);
                    allVehicles.addAll(surroundingVehicles);

                    changeEvent.setVehicleDataList(allVehicles);
                    changeEventList.add(changeEvent);

                    // 达到指定数量后返回
                    if (changeEventList.size() >= number) {
                        System.out.println("变道事件个数：" + changeEventList.size());
                        return ResponseEntity.ok(new ChangeEventList(changeEventList));
                    }
                }
            }
        }

        System.out.println("变道事件个数：" + changeEventList.size());
        return ResponseEntity.ok(new ChangeEventList(changeEventList));
    }

    private long parseTimestamp(String globalTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.UTC);
            return Instant.from(formatter.parse(globalTime)).toEpochMilli();
        } catch (DateTimeParseException e) {
            System.err.println("不合规时间：" + globalTime);
            return -1;
        }
    }

    private VehicleList buildFilteredVehicleData(List<Frame> frames, long baseTimestamp, VehicleList changingVehicle, float distanceThreshold) {
        VehicleList vehicleData = buildVehicleData(frames, baseTimestamp);

        // 如果车辆无有效帧，直接返回 null
        if (vehicleData.getFrame().isEmpty() || vehicleData.getPath().isEmpty()) {
            return null;
        }

        // 计算与变道车辆的最近距离
        double minDistance = changingVehicle.getPath().stream()
                .flatMap(changingPath -> vehicleData.getPath().stream()
                        .map(neighborPath -> calculateDistance(
                                changingPath.getLocalX(), changingPath.getLocalY(),
                                neighborPath.getLocalX(), neighborPath.getLocalY()
                        )))
                .min(Double::compareTo)
                .orElse(Double.MAX_VALUE);

        return minDistance <= distanceThreshold ? vehicleData : null;
    }

    private double calculateDistance(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private VehicleList buildVehicleData(List<Frame> frames, long baseTimestamp) {
        VehicleList vehicleData = new VehicleList();
        if (frames == null || frames.isEmpty()) {
            vehicleData.setFrame(new ArrayList<>());
            vehicleData.setPath(new ArrayList<>());
            return vehicleData;
        }

        Frame referenceFrame = frames.getFirst();
        vehicleData.setVehicleId(referenceFrame.getVehicleId());
        vehicleData.setVClass(referenceFrame.getVClass());
        vehicleData.setVLength(referenceFrame.getVLength());
        vehicleData.setVWidth(referenceFrame.getVWidth());

        long startTime = (long) (baseTimestamp - timeWindow * 1000); // 时间窗口开始
        long endTime = (long) (baseTimestamp + timeWindow * 1000); // 时间窗口结束

        // 筛选时间范围内的帧
        List<Frame> filteredFrames = frames.stream()
                .filter(frame -> {
                    long frameTimestamp = parseTimestamp(frame.getGlobalTime());
                    return frameTimestamp >= startTime && frameTimestamp <= endTime;
                })
                .toList();

        vehicleData.setFrame(filteredFrames.stream()
                .map(frame -> new FrameData(frame.getGlobalTime(), frame.getLaneId(), frame.getVelocity(), frame.getAcceleration()))
                .collect(Collectors.toList()));

        vehicleData.setPath(filteredFrames.stream()
                .map(frame -> new PathData(frame.getLocalX(), frame.getLocalY()))
                .collect(Collectors.toList()));

        return vehicleData;
    }

    @Data
    @AllArgsConstructor
    public static class VehicleKey {
        private Integer vehicleId;
        private float vLength;
        private float vWidth;
        private Integer vClass;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VehicleKey that = (VehicleKey) o;
            return Float.compare(that.vLength, vLength) == 0 &&
                    Float.compare(that.vWidth, vWidth) == 0 &&
                    Objects.equals(vehicleId, that.vehicleId) &&
                    Objects.equals(vClass, that.vClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vehicleId, vLength, vWidth, vClass);
        }
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
        private float localX;
        private float localY;
    }

    @Data
    @AllArgsConstructor
    public static class VehicleList {
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

        public VehicleList() {
        }
    }

    @Data
    @AllArgsConstructor
    public static class ChangeEvent {
        private List<VehicleList> vehicleDataList;

        public ChangeEvent() {
        }
    }

    @Data
    @AllArgsConstructor
    public static class ChangeEventList {
        private List<ChangeEvent> changeEventList;
    }
}
