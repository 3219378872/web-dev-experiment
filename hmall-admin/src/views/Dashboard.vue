<template>
  <div>
    <h2>数据看板</h2>
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="6"
        ><el-card
          ><div style="text-align: center">
            <p style="color: #999">总用户数</p>
            <h1 style="font-size: 32px; color: #409eff">{{ stats.totalUsers }}</h1>
          </div></el-card
        ></el-col
      >
      <el-col :span="6"
        ><el-card
          ><div style="text-align: center">
            <p style="color: #999">总订单数</p>
            <h1 style="font-size: 32px; color: #67c23a">{{ stats.totalOrders }}</h1>
          </div></el-card
        ></el-col
      >
      <el-col :span="6"
        ><el-card
          ><div style="text-align: center">
            <p style="color: #999">总销售额</p>
            <h1 style="font-size: 32px; color: #e6a23c">
              &yen;{{ (stats.totalSales / 100).toFixed(2) }}
            </h1>
          </div></el-card
        ></el-col
      >
      <el-col :span="6"
        ><el-card
          ><div style="text-align: center">
            <p style="color: #999">今日订单</p>
            <h1 style="font-size: 32px; color: #f56c6c">{{ stats.todayOrders }}</h1>
          </div></el-card
        ></el-col
      >
    </el-row>
    <div ref="chartRef" style="width: 100%; height: 400px; margin-top: 24px"></div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import * as echarts from 'echarts';
const stats = ref({ totalUsers: 0, totalOrders: 0, totalSales: 0, todayOrders: 0 });
const chartRef = ref(null);
onMounted(() => {
  const chart = echarts.init(chartRef.value);
  chart.setOption({
    title: { text: '订单统计概览' },
    tooltip: {},
    xAxis: { data: ['待支付', '已支付', '已发货', '已完成', '已取消'] },
    yAxis: {},
    series: [{ name: '订单数', type: 'bar', data: [5, 20, 36, 10, 7] }],
  });
});
</script>
