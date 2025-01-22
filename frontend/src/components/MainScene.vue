<template>
  <div>
    <!-- 控制面板 -->
    <div class="control-panel">
      <button @click="loadPreviousEvent" :disabled="currentEventIndex === 0">Previous Event</button>
      <button @click="loadNextEvent" :disabled="currentEventIndex === totalLength - 1">Next Event</button>
      <Button type="button" label="Search" icon="pi pi-search" :loading="loading" @click="load" />
    </div>
    <!-- Three.js Canvas -->
    <canvas ref="canvas"></canvas>
  </div>
</template>

<script setup>
import {ref, onMounted, toRaw} from 'vue';
import axios from 'axios';
import * as THREE from 'three';

import Button from 'primevue/button';
import 'primeicons/primeicons.css';

const canvas = ref(null);
const currentEventIndex = ref(0); // 当前事件索引
const events = ref([]); // 事件列表
const animations = []; // 动画列表
const labels = []; // 标签列表
let totalLength = 0;//动画数量
let totalevents;

const initSceneForEvent = (vehicleDataList) => {
  const scene = new THREE.Scene();
  const renderer = new THREE.WebGLRenderer({ canvas: canvas.value });
  renderer.setSize(window.innerWidth*0.8, window.innerHeight);

  // 设置摄像机为俯视视角
  const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
  const roadWidth = window.innerWidth * 0.6; // 道路宽度占场景 80%
  console.log(roadWidth);
  const roadLength = window.innerHeight*1.5; // 道路长度占场景 80%
  camera.position.set(roadWidth / 2, 800, -roadLength / 2); // 摄像机居中于道路
  camera.lookAt(roadWidth / 2, 0, -roadLength / 2);

  // 添加光源
  scene.add(new THREE.AmbientLight(0xffffff, 0.5));
  const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
  directionalLight.position.set(100, 200, 100);
  scene.add(directionalLight);

  // 绘制道路
  const laneCount = 10; // 车道数量
  const laneWidth = roadWidth / laneCount; // 每个车道的宽度
  const roadGeometry = new THREE.PlaneGeometry(roadWidth, roadLength);
  const roadMaterial = new THREE.MeshStandardMaterial({ color: 0x333333, side: THREE.DoubleSide });
  const roadMesh = new THREE.Mesh(roadGeometry, roadMaterial);
  roadMesh.rotation.x = -Math.PI / 2;
  roadMesh.position.set(roadWidth / 2, 0, -roadLength / 2);
  scene.add(roadMesh);

  // 绘制车道分隔线
  for (let i = 0; i <= laneCount; i++) {
    const lineGeometry = new THREE.PlaneGeometry(2, roadLength);
    const lineMaterial = new THREE.MeshStandardMaterial({
      color: i === 5 ? 0xffff00 : 0xffffff, // 中心线为黄色，其余为白色
      side: THREE.DoubleSide,
    });
    const lineMesh = new THREE.Mesh(lineGeometry, lineMaterial);

    const xPosition = i * laneWidth; // 从道路起点计算车道线位置
    lineMesh.position.set(xPosition, 0.1, -roadLength / 2);
    lineMesh.rotation.x = -Math.PI / 2;
    scene.add(lineMesh);
  }

  vehicleDataList.forEach((vehicleData) => {
    const { vehicleId, frame, path } = vehicleData;

    const initialPathPoint = path[0];
    const initialX = initialPathPoint.localX + roadWidth / 2;
    const initialY = -roadLength / 2 + initialPathPoint.localY;

    console.log(vehicleId + ": " + "(" + initialX + "," + initialY + ")");

    // 创建车辆
    const sphereGeometry = new THREE.SphereGeometry(8, 32, 32);
    const sphereMaterial = new THREE.MeshStandardMaterial({ color: 0x00ff00 });
    const carMesh = new THREE.Mesh(sphereGeometry, sphereMaterial);

    carMesh.position.set(initialX, 0, initialY);
    scene.add(carMesh);

    // 创建标签
    const carLabel = document.createElement('div');
    carLabel.className = 'car-label';
    document.body.appendChild(carLabel);
    labels.push(carLabel);

    // 动画控制
    let currentProgress = 0;
    const totalTime = 50000; // 动画总时间（毫秒）
    const speedFactor = 1 / (totalTime / 16.67); // 动画每帧的进度增量

    const animateCar = () => {
      if (currentProgress < 1) {
        const positionIndex = Math.floor(currentProgress * (path.length - 1));
        const position = path[positionIndex];
        const local_X = position.localX * 8;
        const local_Y = position.localY * -1;
        // 更新车辆位置
        carMesh.position.set(local_X, 0, local_Y);

        // 获取当前车道号
        const laneId = frame[positionIndex].laneId;

        // 转换为屏幕坐标
        const screenPosition = new THREE.Vector3(local_X, 0, local_Y ).project(camera);
        const screenX = (screenPosition.x+1.1) / 2 * window.innerWidth;
        const screenY = (-screenPosition.y+0.8) / 2 * window.innerHeight;

        carLabel.style.left = `${screenX}px`;
        carLabel.style.top = `${screenY}px`;
        carLabel.style.transform = 'translate(-50%, -50%)';
        carLabel.style.display = screenPosition.z < 1 ? 'block' : 'none';
        carLabel.innerHTML = `
        车号: ${vehicleId} <br>
        车道号: ${laneId}
      `;
        currentProgress += speedFactor;
      }
    };

    animations.push(animateCar);
  });


  return { scene, renderer, camera };
};

const renderScene = (scene, renderer, camera) => {
  const animate = () => {
    requestAnimationFrame(animate);
    animations.forEach((animateCar) => animateCar());
    renderer.render(scene, camera);
  };
  animate();
};

//后端数据获取
const fetchChangeEventList = async (distanceThreshold, number) => {
  try {
    const response = await axios.get('http://localhost:8080/vehicle/change-list', {
      headers: { "Cache-Control": "no-cache" },
      params: { number, distanceThreshold },
    });

    events.value = response.data.changeEventList.map(event => event.vehicleDataList);
    totalevents = toRaw(events.value);
    console.log(totalevents);
    totalLength =totalevents.length;
    console.log('Total length:', totalLength);

    if (totalLength > 0) {
      const { scene, renderer, camera } = initSceneForEvent(totalevents[currentEventIndex.value]);
      renderScene(scene, renderer, camera);
    }
  } catch (error) {
    console.error('Error fetching change event list:', error);
  }
};

const loadNextEvent = () => {
  console.log("当前动画"+currentEventIndex.value+"转到下一个动画");
  if (currentEventIndex.value < totalLength - 1) {
    currentEventIndex.value += 1;
    console.log(totalevents[currentEventIndex.value]);
    const { scene, renderer, camera } = initSceneForEvent(totalevents[currentEventIndex.value]);
    renderScene(scene, renderer, camera);
  }
};

const loadPreviousEvent = () => {
  console.log("当前动画"+currentEventIndex.value+"转到上一个动画");
  if (currentEventIndex.value > 0) {
    currentEventIndex.value -= 1;
    const { scene, renderer, camera } = initSceneForEvent(totalevents[currentEventIndex.value]);
    renderScene(scene, renderer, camera);
  }
};

onMounted(() => {
  const distanceThreshold = 30.0;
  const number = 3;

  fetchChangeEventList(distanceThreshold, number);

  window.addEventListener('resize', () => {
    const { renderer, camera } = initSceneForEvent(totalevents[currentEventIndex.value]);
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
  });
});
</script>

<style>
canvas {
  display: block;
  width: 100%;
  height: 100vh;
}

.control-panel {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 100;
}

button {
  margin: 5px;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.car-label {
  position: absolute;
  color: white;
  padding: 2px 5px;
  background-color: rgba(0, 0, 0, 0.7);
  border-radius: 3px;
  font-size: 12px;
  pointer-events: none;
  z-index: 200;
  transform: translate(-50%, -50%);
}
</style>
