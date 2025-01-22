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

    private final static double timeWindow = 10; // 时间窗口

    private final static double distanceThreshold = 50; // 距离限制
    float changingVehicleLocalY;
    public VehicleController(FrameService frameService) {
        this.frameService = frameService;
    }

    @GetMapping(value = "/change-list")
    public ResponseEntity<ChangeEventList> getChangeEventList(@RequestParam Integer number) {
        // 查询所有帧数据并按 globalTime 排序
        List<Frame> frames = frameService.list(
                new LambdaQueryWrapper<Frame>()
                        .select(Frame::getVehicleId, Frame::getFrameId, Frame::getTotalFrame, Frame::getGlobalTime, Frame::getLocalX,
                                Frame::getLocalY, Frame::getGlobalX, Frame::getGlobalY, Frame::getVLength, Frame::getVWidth,
                                Frame::getVClass, Frame::getVelocity, Frame::getAcceleration, Frame::getLaneId, Frame::getPreceding,
                                Frame::getFollowing, Frame::getSpaceHead, Frame::getTimeHead, Frame::getLocation)
        ).stream().sorted(Comparator.comparing(Frame::getGlobalTime)).toList();

        // 按 VehicleKey 分组
        Map<VehicleKey, List<Frame>> groupedFrames = frames.stream()
                .collect(Collectors.groupingBy(frame -> new VehicleKey(
                        frame.getVehicleId(), frame.getVLength(), frame.getVWidth(), frame.getVClass())));

        List<ChangeEvent> changeEventList = new ArrayList<>();

        // 遍历分组数据，检测变道事件
        for (Map.Entry<VehicleKey, List<Frame>> entry : groupedFrames.entrySet()) {
            List<Frame> vehicleFrames = entry.getValue();
            for (int i = 1; i < vehicleFrames.size(); i++) {
                Frame current = vehicleFrames.get(i);
                Frame previous = vehicleFrames.get(i - 1);

                // 检测变道
                if (!Objects.equals(current.getLaneId(), previous.getLaneId())) {
                    String changeTime = current.getGlobalTime();
                    long changeTimestamp = parseTimestamp(changeTime);
                    if (changeTimestamp == -1) continue; // 无效时间，跳过

                    // 获取变道后的第一帧
                    Frame firstFrameAfterChange = vehicleFrames.stream()
                            .filter(frame -> parseTimestamp(frame.getGlobalTime()) > changeTimestamp)
                            .findFirst()
                            .orElse(null);

                    // 检查在这一帧的时间是否满足前后车条件
                    if (firstFrameAfterChange != null) {
                        changingVehicleLocalY = firstFrameAfterChange.getLocalY();
                        int changingVehicleLaneId = firstFrameAfterChange.getLaneId();
                        int changingVehicleId = firstFrameAfterChange.getVehicleId();

                        // 是否存在一辆车的 localY 小于变道车辆的 localY 并在范围内，且车道号相同，排除变道车辆本身
                        boolean hasPrecedingVehicle = frames.stream()
                                .filter(frame -> parseTimestamp(frame.getGlobalTime()) == parseTimestamp(firstFrameAfterChange.getGlobalTime())) // 时间匹配
                                .filter(frame -> frame.getVehicleId() != changingVehicleId) // 排除变道车辆本身
                                .anyMatch(frame -> frame.getLaneId() == changingVehicleLaneId && // 车道号相同
                                        frame.getLocalY() < changingVehicleLocalY &&
                                        Math.abs(frame.getLocalY() - changingVehicleLocalY) <= distanceThreshold);

                        // 是否存在一辆车的 localY 大于变道车辆的 localY 并在范围内，且车道号相同，排除变道车辆本身
                        boolean hasFollowingVehicle = frames.stream()
                                .filter(frame -> parseTimestamp(frame.getGlobalTime()) == parseTimestamp(firstFrameAfterChange.getGlobalTime())) // 时间匹配
                                .filter(frame -> frame.getVehicleId() != changingVehicleId) // 排除变道车辆本身
                                .anyMatch(frame -> frame.getLaneId() == changingVehicleLaneId && // 车道号相同
                                        frame.getLocalY() > changingVehicleLocalY &&
                                        Math.abs(frame.getLocalY() - changingVehicleLocalY) <= distanceThreshold);

                        // 如果不满足条件，则跳过此变道事件
                        if (!hasPrecedingVehicle || !hasFollowingVehicle) {
                            continue;
                        }
                    }

                    // 获取变道前后的车道号
                    int currentLaneId = current.getLaneId();
                    int previousLaneId = previous.getLaneId();

                    // 构建变道车辆数据
                    VehicleList changingVehicle = buildVehicleData(vehicleFrames, changeTimestamp);

                    // 检查变道车辆的轨迹时间是否满足时间窗口要求
                    long startTime = changeTimestamp - (long) (timeWindow * 1000); // 时间窗口开始时间
                    long endTime = changeTimestamp + (long) (timeWindow * 1000);   // 时间窗口结束时间

                    // 获取变道车辆轨迹中的最早和最晚时间戳
                    Optional<Long> minTimestamp = vehicleFrames.stream()
                            .map(frame -> parseTimestamp(frame.getGlobalTime()))
                            .min(Long::compare);

                    Optional<Long> maxTimestamp = vehicleFrames.stream()
                            .map(frame -> parseTimestamp(frame.getGlobalTime()))
                            .max(Long::compare);

                    // 如果轨迹时间不足以覆盖时间窗口，则跳过该变道事件
                    if (minTimestamp.isEmpty() || maxTimestamp.isEmpty() ||
                            minTimestamp.get() > startTime || maxTimestamp.get() < endTime) {
                        continue;
                    }

                    // 查找周围车辆
                    List<VehicleList> surroundingVehicles = findSurroundingVehicles(frames, changingVehicle, changeTimestamp, currentLaneId, previousLaneId,changingVehicleLocalY);

                    // 如果变道时周围没有车辆，则跳过此变道事件
                    if (surroundingVehicles.isEmpty()) {
                        continue;
                    }

                    // 将数据组合
                    List<VehicleList> allVehicles = new ArrayList<>();
                    allVehicles.add(changingVehicle);
                    allVehicles.addAll(surroundingVehicles);

                    ChangeEvent changeEvent = new ChangeEvent(allVehicles);
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

    // 解析时间戳算法
    private long parseTimestamp(String globalTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.UTC);
            return Instant.from(formatter.parse(globalTime)).toEpochMilli();
        } catch (DateTimeParseException e) {
            System.err.println("不合规时间：" + globalTime);
            return -1;
        }
    }

    private List<VehicleList> findSurroundingVehicles(List<Frame> frames, VehicleList changingVehicle,
                                                      long changeTimestamp, int currentLaneId, int previousLaneId,
                                                      float changingVehicleLocalY) {
        // 构建周围车辆集合
        List<VehicleList> surroundingVehicles = new ArrayList<>();

        // 查找变道前后一刻所在车道和变道后车道的车辆
        List<Frame> relevantFramesByLaneId = frames.stream()
                .filter(frame ->
                        (frame.getLaneId() == currentLaneId || frame.getLaneId() == previousLaneId) &&
                                (parseTimestamp(frame.getGlobalTime()) == changeTimestamp)
                )
                .toList();

        // 筛选出与变道车辆的 localY 距离差不超过限定范围的车辆
        List<Frame> relevantFramesByLocalY = relevantFramesByLaneId.stream()
                .filter(frame -> Math.abs(frame.getLocalY() - changingVehicleLocalY) <= distanceThreshold)
                .toList();

        // 排除变道车辆本身
        List<Frame> relevantFrames = relevantFramesByLocalY.stream()
                .filter(frame -> !Objects.equals(frame.getVehicleId(), changingVehicle.getVehicleId()))
                .toList();

        // 提取车辆的 VehicleKey
        Set<VehicleKey> vehicleKeys = relevantFrames.stream()
                .map(frame -> new VehicleKey(
                        frame.getVehicleId(), frame.getVLength(), frame.getVWidth(), frame.getVClass()))
                .collect(Collectors.toSet());

        // 查找车辆的轨迹数据
        Map<VehicleKey, List<Frame>> groupedFrames = frames.stream()
                .filter(frame -> vehicleKeys.contains(new VehicleKey(
                        frame.getVehicleId(), frame.getVLength(), frame.getVWidth(), frame.getVClass())))
                .collect(Collectors.groupingBy(frame -> new VehicleKey(
                        frame.getVehicleId(), frame.getVLength(), frame.getVWidth(), frame.getVClass())));

        // 构建车辆的轨迹数据
        for (Map.Entry<VehicleKey, List<Frame>> entry : groupedFrames.entrySet()) {
            List<Frame> vehicleFrames = entry.getValue();
            VehicleList vehicleData = buildVehicleData(vehicleFrames, changeTimestamp);
            surroundingVehicles.add(vehicleData);
        }

        return surroundingVehicles;
    }


    // 构建车辆数据算法
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
    public static class ChangeEventList {
        private List<ChangeEvent> changeEventList;
    }

    @Data
    @AllArgsConstructor
    public static class ChangeEvent {
        private List<VehicleList> vehicleDataList;
    }

    @Data
    public static class VehicleList {
        private Integer vehicleId;
        @JsonProperty("vClass")
        private Integer vClass;
        @JsonProperty("vLength")
        private float vLength;
        @JsonProperty("vWidth")
        private float vWidth;
        private List<FrameData> frame;
        private List<PathData> path;
    }

    @Data
    public static class FrameData {
        private String globalTime;
        private Integer laneId;
        private float velocity;
        private float acceleration;

        public FrameData(String globalTime, Integer laneId, float velocity, float acceleration) {
            this.globalTime = globalTime;
            this.laneId = laneId;
            this.velocity = velocity;
            this.acceleration = acceleration;
        }
    }

    @Data
    public static class PathData {
        private float localX;
        private float localY;

        public PathData(float localX, float localY) {
            this.localX = localX;
            this.localY = localY;
        }
    }
}
