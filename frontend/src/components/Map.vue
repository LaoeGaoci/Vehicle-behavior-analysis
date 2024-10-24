<!-- eslint-disable vue/multi-word-component-names -->
<script setup>
import { ref, onMounted } from 'vue';
import L from 'leaflet';
import 'leaflet-ant-path';
import * as d3 from 'd3';
import axios from 'axios';
import { useToast } from "primevue/usetoast";

const map = ref(null);
const mapContainer = ref(null);
const vehicles = ref([]);
const selected_date = ref(null);
const filtered_date = ref([]);
const operationVisible = ref(true);

const toast = useToast();

const trajectories = ref([]);
onMounted(() => {
  // 初始化 Leaflet 地图
  map.value = L.map(mapContainer.value, {
    zoomControl: false,
  }).setView([31.2304, 121.4737], 15);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
  }).addTo(map.value);
});

const searchDailyCarTrail = async () => {
  if (!selected_date.value) {
    // 如果没有选择日期，则弹出提示
    console.error('请选择日期');
    return;
  }

  try {
    // 发起请求，请求指定日期的车辆轨迹数据
    const response = await axios.get(`http://localhost:8080/vehicle/trajectories`, {
      params: {
        date: selected_date.value
      }
    });

    // 将返回的轨迹数据中的路径赋值给 trajectories，并按日期排序
    trajectories.value = response.data.vehicleDataList.sort((a, b) => a.frame[0].globalTime - b.frame[0].globalTime)
      .map(vehicle => ({
        path: vehicle.path.map(point => [point.globalX, point.globalY]),
        globalTime: vehicle.frame[0].globalTime
      }));

    // 使用返回的数据逐个更新 Leaflet 地图上的车辆轨迹
    animateVehicles();
  } catch (error) {
    console.error('获取车辆轨迹失败:', error);
  }
};

const animateVehicles = () => {
  if (!map.value) {
    console.error('地图尚未初始化');
    return;
  }

  // 使用 Leaflet SVG 图层来绘制路径
  L.svg().addTo(map.value);
  const svgLayer = d3.select(map.value.getPanes().overlayPane).select('svg');
  const g = svgLayer.append('g');

  // 按顺序逐个车辆绘制动画
  const animateNextVehicle = (index = 0) => {
    if (index >= trajectories.value.length) return;

    const { path: trajectory, globalTime } = trajectories.value[index];

    // 添加车辆路径
    const path = g.append('path')
      .datum(trajectory)
      .attr('fill', 'none')
      .attr('stroke', d3.schemeCategory10[index % 10])
      .attr('stroke-width', 4)
      .attr('stroke-opacity', 0.8)
      .attr('filter', 'drop-shadow(0 0 5px #ff0)')
      .attr('class', 'vehicle-path');

    function updatePath() {
      const line = d3.line()
        .x((d) => map.value.latLngToLayerPoint(new L.LatLng(d[0], d[1])).x)
        .y((d) => map.value.latLngToLayerPoint(new L.LatLng(d[0], d[1])).y)
        .curve(d3.curveLinear);
      path.attr('d', line);
    }

    map.value.on('zoomend', updatePath);
    map.value.on('moveend', updatePath);
    updatePath();

    // 添加车辆标记并进行动画
    const vehicleMarker = g.append('circle')
      .attr('r', 6)
      .attr('fill', d3.schemeCategory10[index % 10])
      .attr('class', 'vehicle-marker');

    let i = 0;
    const moveMarker = () => {
      if (i >= trajectory.length) {
        // 动画完成后，开始下一个车辆的动画
        setTimeout(() => animateNextVehicle(index + 1), 1000); // 使用 globalTime 控制车辆动画间隔
        return;
      }

      const projectedPoint = map.value.latLngToLayerPoint(new L.LatLng(trajectory[i][0], trajectory[i][1]));
      vehicleMarker
        .attr('cx', projectedPoint.x)
        .attr('cy', projectedPoint.y);
      i++;
      setTimeout(moveMarker, 500); // 调整每个点的动画间隔时间
    };

    moveMarker();
  };

  animateNextVehicle();
};






const addCar = () => {
  vehicles.value.push({
    selectedItem: null,
    filteredItems: [],
  });
};

const removeVehicle = (index) => {
  vehicles.value.splice(index, 1);
};

const resetFields = () => {
  selected_date.value = null;
  vehicles.value = [];
};

const search_date = (event) => {
  // Simulate searching available dates
  filtered_date.value = ['2023-10-10', '2023-10-11', '2023-10-12'].filter(date =>
    date.includes(event.query)
  );
};

const search_carItems = (event, index) => {
  // Simulate searching vehicle numbers
  vehicles.value[index].filteredItems = ['Car-001', 'Car-002', 'Car-003'].filter(item =>
    item.includes(event.query)
  );
};


//获取所有历史记录
const historyData = ref([]);
const historyVisible = ref(false);
const showHistory = async () => {
  try {
    // Request historical records
    const response = await axios.get('http://localhost:8080/history/getall');
    historyData.value = response.data;

    console.log('Vehicle History:', historyData);
    historyVisible.value = true; // 显示Dialog
  } catch (error) {
    console.error('Error fetching history records:', error);
  }
};
// 删除单条历史记录
const deleteHistoryRecord = async (record) => {
  try {
    await axios.delete(`http://localhost:8080/history/delete`, {
      data: {
        tree_line_search_id: record.tree_line_search_id,
      }
    });
    showHistory();
    toast.add({ severity: 'success', summary: 'Success Message', detail: '删除成功', group: 'br', life: 2000 });
  } catch (error) {
    console.error('删除错误:', error);
    toast.add({ severity: 'error', summary: 'Error', detail: '删除失败', group: 'br', life: 2000 });
  }
};

// 清空所有历史记录
const clearHistory = async () => {
  console.log("执行数据清空");
  try {
    await axios.delete(`http://localhost:8080/history/deleteAll`, {

    });
    historyData.value = [];
    toast.add({ severity: 'success', summary: 'Success Message', detail: '清空成功', group: 'br', life: 2000 });
  } catch (error) {
    console.error('清空错误:', error);
    toast.add({ severity: 'error', summary: 'Error', detail: '清空失败', group: 'br', life: 2000 });
  }
};
</script>

<template>
  <div class="layout-map-container">
    <div id="map" ref="mapContainer" style="height: 100vh; position: relative;"></div>
    <div class="panel-container">
      <Panel header="操作" toggleable v-model:collapsed="operationVisible">
          <div class="car-search">
            <div class="date">
              <i class="pi pi-calendar" style="color: #00af577f"></i>
              <FloatLabel>
                <AutoComplete v-model="selected_date" :suggestions="filtered_date" @complete="search_date"
                  :virtualScrollerOptions="{ itemSize: 38 }" dropdown />
                <label for="date">日期</label>
              </FloatLabel>
            </div>
            <div class="vehicles">
              <div v-for="(vehicle, index) in vehicles" :key="index" class="vehicle">
                <Button icon="pi pi-trash" class="p-button-rounded" @click="removeVehicle(index)" />
                <FloatLabel>
                  <AutoComplete v-model="vehicle.selectedItem" :suggestions="vehicle.filteredItems"
                    @complete="(e) => search_carItems(e, index)" :virtualScrollerOptions="{ itemSize: 38 }"
                    style="width: 235px;" dropdown />
                  <label :for="'vehicle-' + index">车号 {{ index + 1 }}</label>
                </FloatLabel>
              </div>
            </div>
            <div class="buttons">
              <Button label="搜索迹踪" icon="pi pi-search" style="width: 120.33px;" @click="searchDailyCarTrail" />
              <Button label="历史记录" icon="pi pi-warehouse" style="width: 120.33px;" @click="showHistory" />
              <Toast />
            </div>
            <div class="functionButtons">
              <Button label="重置" icon="pi pi-refresh" style="width: 120.33px;" @click="resetFields" />
              <Button label="增加车号" icon="pi pi-plus" @click="addCar" />
            </div>
          </div>
      </Panel>
    </div>
  </div>
  <Dialog v-model:visible="historyVisible" header="历史记录" :style="{ width: '50vw' }" maximizable modal
      :contentStyle="{ height: '300px' }">
      <DataTable :value="historyData.value" scrollable scrollHeight="flex" tableStyle="min-width: 50rem">
        <Column field="tree_line_name" header="道路名称"></Column>
        <Column header="操作">
          <template #body="slotProps">
            <Button icon="pi pi-trash" class="p-button-danger p-button-rounded"
              @click="deleteHistoryRecord(slotProps.data)" />
          </template>
        </Column>
      </DataTable>
      <template #footer>
        <Button label="清空所有历史记录" icon="pi pi-times" class="p-button-danger" @click="clearHistory" />
        <Button label="Ok" icon="pi pi-check" @click="historyVisible = false" />
      </template>
    </Dialog>
</template>

<style>
.layout-map-container {
  width: 100%;
  height: 100vh;
}
#map {
  width: 100%;
  height: 100%;
}
.vehicle-path {
  fill: none;
  stroke-width: 4;
  stroke-opacity: 0.8;
  filter: drop-shadow(0 0 5px #ff0);
}
.vehicle-marker {
  fill: red;
  stroke: black;
  stroke-width: 1;
}
</style>
