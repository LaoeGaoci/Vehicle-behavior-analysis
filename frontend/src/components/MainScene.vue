<template>
  <Toast position="top-right" group="tr" />
  <div class="card">
    <div class="title">换道片段安全测评</div>
    <div class="slide-button">
      <div class="input-group">
        <!-- 用户输入片段个数 -->
        <InputText
          v-model="segmentCount"
          name="road-number"
          type="text"
          placeholder="片段个数"
          fluid
        />
        <!-- 点击按钮触发获取视频 -->
        <Button @click="fetchVideos">获取视频</Button>
      </div>
      <!-- 上一个按钮：在当前视频为第一个时禁用 -->
      <Button
        class="prov"
        label="上一个"
        @click="prevVideo"
        :disabled="currentIndex === 0"
      />
      <!-- 下一个按钮：在当前视频为最后一个时禁用 -->
      <Button
        class="next"
        label="下一个"
        @click="nextVideo"
        :disabled="currentIndex === videos.length - 1"
      />
    </div>
    <div class="view">
      <vue3VideoPlay
        class="animation"
        v-bind:src="videoSrc"/>
<!--        v-bind:src="currentVideoOptions" />-->
<!--        poster='https://cdn.jsdelivr.net/gh/xdlumia/files/video-play/ironMan.jpg'/>-->
      <div class="show-index">
        <Checkbox v-model="index" inputId="ingredient" name="index" value="index"/>
        <label for="ingredient"> 显示指标 </label>
        <!-- 当 index 为 true 时，显示指标数据 -->
        <div v-if="index" class="index-content">
          <Textarea v-model="indexValue" rows="25" cols="50" />
        </div>
      </div>
    </div>
    <div class="answer">
      <Button label="完全安全" severity="success" @click="openDialog('完全安全')" />
      <Button label="低风险" severity="info" @click="openDialog('低风险')" />
      <Button class="warn" label="中等风险"  @click="openDialog('中等风险')" />
      <Button label="极度危险" severity="danger" @click="openDialog('极度危险')" />
    </div>
    <span class="text-surface-500 dark:text-surface-400 block mb-8">
      0-3分：完全安全，符合所有安全标准<br>
      4-5分：中等风险，存在风险，但可接受<br>
      6-7分：低风险，换道较安全，风险可控<br>
      8-10分：较高风险，换道会导致碰撞<br>
    </span>
  </div>
  <!-- 弹窗对话框 -->
  <Dialog v-model:visible="Dialog_visible" modal header="评价选择难度" :style="{ width: '25rem' }">
    <span class="text-surface-500 dark:text-surface-400 block mb-8">
      一星是一分
    </span>
    <div class="horizontal-layout">
      <div class="star">
        <Rating v-model="star_value" :stars="3" />
      </div>
      <Button label="提交" @click="submit" />
    </div>
  </Dialog>
</template>

<script setup>
import {reactive, ref, computed, toRaw, watch} from 'vue';
import axios from 'axios';
import { useToast } from 'primevue/usetoast';
import { useStore } from 'vuex';

const store = useStore();
const accountName = localStorage.getItem('username');

//提示
const toast = useToast();

// 定义响应式变量
const Dialog_visible = ref(false); // 控制 Dialog 的显示
const star_value = ref(0);       // 评分组件的值
// 用户选择的风险按钮对应的标签
const selectedRisk = ref('');

const index = ref(false); //是否显示指标
const indexValue = ref(""); // 用于存储指标数据

const currentIndex = ref(0);  // 当前视频索引
const segmentCount = ref();//片段个数
const videoSrc = ref("http://localhost:65/i-80~1435.mp4");
let videos = [];//视频列表


const options = reactive({
  width: '60%', //播放器宽度
  height: '80%', //播放器高度
  color: "#409eff", //主题色
  title: '', //视频名称
  src: "i-80~1435.mp4", //视频源
  muted: false, //静音
  webFullScreen: false,
  speedRate: ["0.75", "1.0", "1.25", "1.5", "2.0"], //播放倍速
  autoPlay: false, //自动播放
  loop: false, //循环播放
  mirror: false, //镜像画面
  ligthOff: false,  //关灯模式
  volume: 0.3, //默认音量大小
  control: true, //是否显示控制
  controlBtns:['audioTrack', 'quality', 'speedRate', 'volume', 'setting', 'pip', 'pageFullScreen', 'fullScreen'] //显示所有按钮,
})
function replaceVideo() {
  videoSrc.value = `http://localhost:65/${videos.length > 0 ? videos[currentIndex.value].video_id : options.src}`;
}

//获取视频
async function fetchVideos() {
  console.log(accountName);
  if (!segmentCount.value) {
    toast.add({ severity: 'warn', summary: 'Warn Message', detail: '请输入片段个数',group: 'tr', life: 3000 });
    return;
  }
  try {

    const response = await axios.get('http://localhost:8080/getVideos', {
      params: { number:  parseInt(segmentCount.value) }
    });
    // 后端返回的数据格式为：["i-80~1.mp4", "i-80~2.mp4", ...]
    videos = response.data;
    console.log(videos);
    //加入提示
    toast.add({ severity: 'success', summary: 'Success Message', detail: '视频获取成功', group: 'tr',life: 3000 });
  } catch (error) {
    console.error('获取视频文件失败:', error);
    toast.add({ severity: 'error', summary: 'Error Message', detail: error,group: 'tr', life: 3000 });
  }
}

// 上一个视频
function prevVideo() {
  if (currentIndex.value > 0) {
    currentIndex.value--;
    replaceVideo();
    console.log(`After Prev: ${currentIndex.value}`);
  } else {
    console.warn("Failed to prev");
  }
}

// 下一个视频
function nextVideo() {
  if (currentIndex.value < videos.length - 1) {
    currentIndex.value++;
    replaceVideo();
    console.log(`After Next: ${currentIndex.value}`);
  } else {
    console.warn("Failed to next");
  }
}

// 点击风险按钮时调用，同时记录风险等级，并弹出对话框
function openDialog(riskLabel) {
  selectedRisk.value = riskLabel;
  Dialog_visible.value = true;
}

//提交答案
async function submit() {
  const videoId = videos[currentIndex.value].video_id;  // 获取原始的 video_id
  const riskLevel = selectedRisk.value;  // 获取 selectedRisk 的值
  const starValue = star_value.value;  // 获取 star_value 的值

  console.log("用户" + accountName.value + "选择选项为：" + riskLevel + " 评分：" + starValue + " 文件为：" + videoId);

  try {
    // 向后端提交数据（根据实际接口地址调整）
    await axios.post('http://localhost:8080/submitRating', {
      video_id: videoId,
      username: accountName,
      risk_level: riskLevel,
      selection_difficulty: starValue
    });

    toast.add({ severity: 'success', summary: 'Success Message', detail: '提交成功', group: 'tr', life: 3000 });

    // 提交成功后，可以重置打分或其他状态
    star_value.value = 0;
    Dialog_visible.value = false;
  } catch (error) {
    console.error('提交失败：', error);
    toast.add({ severity: 'error', summary: 'Error Message', group: 'tr', detail: error, life: 3000 });
  }
}

watch(currentIndex, () => {
  const currentData = videos[currentIndex.value];
  console.log(index.value);
  indexValue.value = `
    当前换道车辆(红车)的速度: ${currentData.g} km/h
    目标车道后方车辆的速度: ${currentData.l2} km/h
    目标车道后方车辆与前方车辆的距离: ${currentData.l3} m
    当前换道车辆(红车)与原车道前方车辆的距离: ${currentData.l4} m
    当前换道车辆(红车)与目标车道后方车辆的距离: ${currentData.v1} m
    当前换道车辆(红车)与目标车道前方车辆的距离: ${currentData.v3} m
  `;
});
</script>
