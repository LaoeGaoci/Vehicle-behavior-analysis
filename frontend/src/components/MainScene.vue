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
      <vue3VideoPlay class="animation" v-bind="options" poster='https://cdn.jsdelivr.net/gh/xdlumia/files/video-play/ironMan.jpg'/>
      <div class="show-index">
        <Checkbox v-model="index" inputId="ingredient" name="index" value="index"/>
        <label for="ingredient"> 显示指标 </label>
        <!-- 当 index 为 true 时，显示指标数据 -->
        <div v-if="index" class="index-content">
          <Textarea v-model="indexValue" rows="25" cols="35" />
        </div>
      </div>
    </div>
    <div class="answer">
      <Button label="完全安全" severity="success" @click="openDialog('完全安全')" />
      <Button label="低风险" severity="info" @click="openDialog('低风险')" />
      <Button label="中等风险" severity="secondary" @click="openDialog('中等风险')" />
      <Button label="极度危险" severity="danger" @click="openDialog('极度危险')" />
    </div>
  </div>
  <!-- 弹窗对话框 -->
  <Dialog v-model:visible="Dialog_visible" modal header="评价选择难度" :style="{ width: '25rem' }">
    <span class="text-surface-500 dark:text-surface-400 block mb-8">
      0-3分：难度较低，错误率较少<br>
      4-5分：中等难度<br>
      6-7分：较难，需要认真分析<br>
      8-10分：非常难，需要深入研究<br>
    </span>
    <div class="horizontal-layout">
      <div class="star">
        <Rating v-model="star_value" :stars="10" />
      </div>
      <Button label="提交" @click="submit" />
    </div>
  </Dialog>
</template>

<script setup>
import {reactive, ref} from 'vue';
import axios from 'axios';
import { useToast } from 'primevue/usetoast';
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
const segmentCount = ref('');
const videos = ref([]);


const options = reactive({
  width: '75%', //播放器高度
  height: '80%', //播放器高度
  color: "#409eff", //主题色
  title: '', //视频名称
  src: "https://cdn.jsdelivr.net/gh/xdlumia/files/video-play/IronMan.mp4", //视频源
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
//获取视频
async function fetchVideos() {
  if (!segmentCount.value) {
    toast.add({ severity: 'warn', summary: 'Warn Message', detail: '请输入片段个数',group: 'tr', life: 3000 });
    return;
  }
  try {

    const response = await axios.get('/api/getVideoFiles', {
      params: { count: segmentCount.value }
    });
    // 假设后端返回的数据格式为：["video1.mp4", "video2.mp4", ...]
    videos.value = response.data.map(fileName => `http://localhost/video/${fileName}`);
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
  }
}

// 下一个视频
function nextVideo() {
  if (currentIndex.value < videos.value.length - 1) {
    currentIndex.value++;
  }
}

// 点击风险按钮时调用，同时记录风险等级，并弹出对话框
function openDialog(riskLabel) {
  selectedRisk.value = riskLabel;
  Dialog_visible.value = true;
}

//提交答案
async function submit() {
  console.log("选择选项为：" + selectedRisk.value + " 评分：" +star_value.value + " 文件为：" + videos.value[currentIndex.value])
  const payload = {
    rating: star_value.value,
    // 这里传入当前视频的文件名，如果需要传文件名部分，可以进一步处理字符串
    videoFile: videos.value[currentIndex.value],
    risk: selectedRisk.value
  };

  try {
    // 向后端提交数据（根据实际接口地址调整）
    await axios.post('/api/submitRating', payload);
    console.log('提交成功：', payload);
    toast.add({ severity: 'success', summary: 'Success Message',detail: '提交成功',group: 'tr', life: 3000 });
    // 提交成功后，可以重置打分或其他状态
    star_value.value = 0;
    Dialog_visible.value = false;
  } catch (error) {
    console.error('提交失败：', error);
    toast.add({ severity: 'error', summary: 'Error Message', group: 'tr', detail: error, life: 3000 });
  }
}
</script>
