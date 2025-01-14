<template>
    <div />
  </template>
  
  <script setup>
  import { TextureLoader, BoxGeometry, Mesh, MeshBasicMaterial, Group } from 'three';
  import { ref, watchEffect, onMounted } from 'vue';
  
  const props = defineProps({
    x: { type: Number, required: true },
    z: { type: Number, required: true },
    speed: { type: Number, default: 0.5 },
    texturePath: { type: String, required: true }, // 贴图路径
    width: { type: Number, default: 10 }, // 车辆宽度
    height: { type: Number, default: 5 }, // 车辆高度
    depth: { type: Number, default: 20 }, // 车辆长度
  });
  
  const carMesh = ref(null); // 车辆 Mesh
  const position = ref({ x: props.x, z: props.z }); // 车辆位置
  const emit = defineEmits(['loaded', 'updatePosition']); // 定义事件
  
  // 加载贴图并创建车辆
  const loadCar = () => {
    const textureLoader = new TextureLoader();
    const carGroup = new Group(); // 使用 Group 组织车辆
  
    textureLoader.load(
      props.texturePath,
      (texture) => {
        const geometry = new BoxGeometry(props.width, props.height, props.depth); // 车辆几何体
        const material = new MeshBasicMaterial({
          map: texture,
          transparent: true, // 启用透明度
          alphaTest: 0.5,    // 剔除完全透明像素
        });
        const car = new Mesh(geometry, material);
  
        car.position.set(props.x, props.height / 2, props.z); // 设置车辆位置
        carGroup.add(car);
        carMesh.value = carGroup;
  
        // 通知父组件加载完成
        emit('loaded', carGroup);
      },
      undefined,
      (error) => {
        console.error('Error loading car texture:', error);
      }
    );
  };
  
  onMounted(() => {
    loadCar();
  });
  
  // 动态更新位置
  watchEffect(() => {
    if (carMesh.value) {
      carMesh.value.position.set(position.value.x, props.height / 2, position.value.z);
      emit('updatePosition', position.value); // 通知父组件位置更新
    }
  });
  </script>
  