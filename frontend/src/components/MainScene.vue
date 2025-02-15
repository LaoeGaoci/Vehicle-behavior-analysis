<template>
  <div class="card">
    <Splitter>
      <SplitterPanel class="control-panel" :size="30" :minSize="10">
        <Card class="control">
          <template #title>控制面板</template>
          <template #content>
              <FloatLabel class="event_number">
                <InputText id="event_number" v-model="value" />
                <label for="event_number">事件数量</label>
              </FloatLabel>

            <div class="display_index">
              <RadioButton inputId="display_index" value="Cheese" />
              <label for="display_index">显示指标</label>
            </div>

            <FloatLabel class="answer">
              <InputText id="answer" v-model="value" />
              <label for="answer">你的回答</label>
            </FloatLabel>
          </template>
        </Card>
      </SplitterPanel>
      <SplitterPanel class="animation-panel" :size="70">
          <vue3VideoPlay class="animation" v-bind="options" poster='https://cdn.jsdelivr.net/gh/xdlumia/files/video-play/ironMan.jpg'/>
      </SplitterPanel>
    </Splitter>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue';

const value = ref(null);


const options = reactive({
  width: '100%', //播放器高度
  height: '100%', //播放器高度
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
</script>
