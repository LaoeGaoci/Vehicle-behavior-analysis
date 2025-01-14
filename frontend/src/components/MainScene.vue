<template>
  <div>
    <!-- 控制面板 -->
    <div class="control-panel">
      <button @click="loadPreviousEvent" :disabled="currentEventIndex === 0">Previous Event</button>
      <button @click="loadNextEvent" :disabled="currentEventIndex === events.length - 1">Next Event</button>
    </div>
    <!-- Three.js Canvas -->
    <canvas ref="canvas"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import * as THREE from 'three';

const canvas = ref(null);
const currentEventIndex = ref(0); // 当前变道事件索引
const events = ref([]); // 存储后端返回的变道事件列表
const animations = []; // 动画逻辑

// 初始化 Three.js 场景
const initSceneForEvent = (vehicleDataList) => {
  const scene = new THREE.Scene();
  const renderer = new THREE.WebGLRenderer({ canvas: canvas.value });
  renderer.setSize(window.innerWidth, window.innerHeight);

  const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
  camera.position.set(0, 300, 0);
  camera.lookAt(0, 0, 0);

  // 添加环境光和方向光
  scene.add(new THREE.AmbientLight(0xffffff, 0.5));
  const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
  directionalLight.position.set(100, 200, 100);
  scene.add(directionalLight);

  // 解析后端数据，创建车辆和轨迹
  vehicleDataList.forEach((vehicleData) => {
    const pathPoints = vehicleData.path.map((point) => new THREE.Vector3(point.globalX, 0, point.globalY));
    const curve = new THREE.CatmullRomCurve3(pathPoints);

    const carGeometry = new THREE.BoxGeometry(vehicleData.vLength, 5, vehicleData.vWidth);
    const carMaterial = new THREE.MeshStandardMaterial({ color: 0x00ff00 });
    const carMesh = new THREE.Mesh(carGeometry, carMaterial);
    scene.add(carMesh);

    // 动画逻辑
    const animateCar = (progress) => {
      const position = curve.getPoint(progress);
      carMesh.position.set(position.x, position.y, position.z);

      // 对齐车辆方向
      const tangent = curve.getTangent(progress);
      carMesh.rotation.y = Math.atan2(tangent.z, tangent.x);
    };

    animations.push({ animateCar, progress: 0 });
  });

  return { scene, renderer, camera };
};

// 渲染场景
const renderScene = (scene, renderer, camera) => {
  const animate = () => {
    requestAnimationFrame(animate);

    animations.forEach((animation) => {
      animation.progress += 0.01;
      if (animation.progress > 1) animation.progress = 0;
      animation.animateCar(animation.progress);
    });

    renderer.render(scene, camera);
  };
  animate();
};

// 切换到下一个事件
const loadNextEvent = () => {
  if (currentEventIndex.value < events.value.length - 1) {
    currentEventIndex.value += 1;
    const { scene, renderer, camera } = initSceneForEvent(events.value[currentEventIndex.value].vehicleDataList);
    renderScene(scene, renderer, camera);
  }
};

// 切换到上一个事件
const loadPreviousEvent = () => {
  if (currentEventIndex.value > 0) {
    currentEventIndex.value -= 1;
    const { scene, renderer, camera } = initSceneForEvent(events.value[currentEventIndex.value].vehicleDataList);
    renderScene(scene, renderer, camera);
  }
};

// 加载后端数据
const fetchChangeEventList = async (distanceThreshold, number) => {
  try {
    const response = await axios.post('/vehicle/change-list', {
      distanceThreshold,
      number,
    });
    events.value = response.data.changeEventList;
    // 初始化第一个事件
    const { scene, renderer, camera } = initSceneForEvent(events.value[0].vehicleDataList);
    renderScene(scene, renderer, camera);
  } catch (error) {
    console.error('Error fetching change event list:', error);
  }
};

// 加载事件数据
onMounted(() => {
  const distanceThreshold = 50; // 示例参数
  const number = 5;
  fetchChangeEventList(distanceThreshold, number);

  // 监听窗口尺寸变化
  window.addEventListener('resize', () => {
    const { renderer, camera } = initSceneForEvent(events.value[currentEventIndex.value].vehicleDataList);
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
</style>
