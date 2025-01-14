<template>
    <div />
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import * as THREE from 'three';
  
  const props = defineProps({
    width: Number,
    length: Number,
    color: Number,
    borderColor: Number,
  });
  
  const roadMesh = ref(null);
  
  const createRoad = () => {
    const geometry = new THREE.PlaneGeometry(props.width, props.length);
    const material = new THREE.MeshBasicMaterial({ color: props.color, side: THREE.DoubleSide });
    roadMesh.value = new THREE.Mesh(geometry, material);
    roadMesh.value.rotation.x = -Math.PI / 2;
  };
  
  createRoad();
  
  </script>
  