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
const vehicleSpeed = 1000; // 车辆移动速度，单位为毫秒
const toast = useToast();
onMounted(() => {
  // 初始化 Leaflet 地图
  map.value = L.map(mapContainer.value, {
    zoomControl: false,
  }).setView([31.2304, 121.4737], 15);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
  }).addTo(map.value);

  // 添加初始标记
  let marker = L.marker([31.2304, 121.4737])
    .addTo(map.value)
    .bindPopup('这里是上海市中心')
    .openPopup();

  // 点击地图时更新标记位置
  map.value.on('click', (e) => {
    const { lat, lng } = e.latlng;
    marker.setLatLng([lat, lng]);
    marker.bindPopup(`纬度: ${lat}, 经度: ${lng}`).openPopup();
  });

  // 为 Leaflet 地图添加 D3.js SVG 覆盖层以绘制路径
  const svg = d3.select(map.value.getPanes().overlayPane).append("svg")
    .attr("class", "leaflet-zoom-hide");
  const g = svg.append("g").attr("class", "leaflet-zoom-hide");

  // 更新 SVG 大小和位置
  function resetSvgBounds() {
    const bounds = map.value.getBounds();
    const topLeft = map.value.latLngToLayerPoint(bounds.getNorthWest());
    const bottomRight = map.value.latLngToLayerPoint(bounds.getSouthEast());
    svg.style("width", `${bottomRight.x - topLeft.x}px`)
       .style("height", `${bottomRight.y - topLeft.y}px`)
       .style("left", `${topLeft.x}px`)
       .style("top", `${topLeft.y}px`);
    g.attr("transform", `translate(${-topLeft.x}, ${-topLeft.y})`);
  }

  // 示例车辆轨迹数据
  const trajectories = [
    [
      [31.2304, 121.4737],
      [31.2310, 121.4740],
      [31.2320, 121.4750],
    ],
    [
      [31.2304, 121.4737],
      [31.2295, 121.4725],
      [31.2280, 121.4700],
    ],
  ];

  // 将纬度/经度投影到 Leaflet 地图的图层点的函数
  function projectPoint(lat, lng) {
    return map.value.latLngToLayerPoint(new L.LatLng(lat, lng));
  }

  // 使用 D3 绘制车辆轨迹
  trajectories.forEach((trajectory, index) => {
    const path = g
      .append("path")
      .datum(trajectory)
      .attr("fill", "none")
      .attr("stroke", d3.schemeCategory10[index % 10])
      .attr("stroke-width", 4)
      .attr("stroke-opacity", 0.8)
      .attr("filter", "drop-shadow(0 0 5px #ff0)")
      .attr("class", "vehicle-path");

    // 在地图视图重置时更新路径坐标
    function updatePath() {
      const line = d3
        .line()
        .x((d) => projectPoint(d[0], d[1]).x)
        .y((d) => projectPoint(d[0], d[1]).y)
        .curve(d3.curveLinear);
      path.attr("d", line);
    }

    map.value.on("moveend", () => {
      updatePath();
      resetSvgBounds();
    });
    map.value.on("zoomend", () => {
      updatePath();
      resetSvgBounds();
    });
    updatePath();
    resetSvgBounds();

    // 动画显示车辆沿路径移动
    function animateVehicle() {
      const totalLength = path.node().getTotalLength();
      path
        .attr("stroke-dasharray", totalLength + " " + totalLength)
        .attr("stroke-dashoffset", totalLength)
        .transition()
        .duration(vehicleSpeed * 5) // 使用车辆速度调整动画时间
        .ease(d3.easeLinear)
        .attr("stroke-dashoffset", 0)
        .on("end", () => {
          setTimeout(animateVehicle, 1000); // 动画结束后延迟重新开始
        });
    }

    // 添加车辆标记并进行动画
    const vehicleMarker = g.append("circle")
      .attr("r", 6)
      .attr("fill", d3.schemeCategory10[index % 10])
      .attr("class", "vehicle-marker");

    function animateMarker() {
      const points = trajectory;
      let i = 0;

      function move() {
        if (i >= points.length) {
          i = 0;
          setTimeout(move, 1000);
          return;
        }

        const projectedPoint = map.value.latLngToLayerPoint([points[i][0], points[i][1]]);
        vehicleMarker
          .attr("cx", projectedPoint.x)
          .attr("cy", projectedPoint.y);
        i++;
        setTimeout(move, vehicleSpeed / 2); // 使用车辆速度调整标记移动速度
      }
      move();
    }

    animateVehicle();
    animateMarker();
  });
});
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
  console.log("删除第" + record.tree_line_search_id + "号林荫道数据")
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
  console.log("执行林荫道数据清空");
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
              <Button label="搜索迹踪" icon="pi pi-search" style="width: 120.33px;" @click="searchCarTrail" />
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
