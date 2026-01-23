<template>
  <div class="home-container">
    <el-card class="welcome-card" shadow="hover">
      <div class="card-header">
        <h3>欢迎使用系统管理平台</h3>
      </div>
      
      <div class="welcome-content">
        <p>这是一个基于 Vue3 + Spring Boot 的前后端分离系统管理平台，提供了用户管理、订单管理等功能。</p>
        
        <div class="stats-container">
          <el-statistic title="用户数" :value="userCount" prefix=""><el-icon><User /></el-icon></el-statistic>
          <el-statistic title="订单数" :value="orderCount" prefix=""><el-icon><Document /></el-icon></el-statistic>
          <el-statistic title="商品数" :value="productCount" prefix=""><el-icon><Goods /></el-icon></el-statistic>
          <el-statistic title="销售额" :value="salesAmount" prefix="￥"><el-icon><Money /></el-icon></el-statistic>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, Document, Goods, Money } from '@element-plus/icons-vue'

// 统计数据
const userCount = ref(0)
const orderCount = ref(0)
const productCount = ref(0)
const salesAmount = ref(0)

// 模拟获取统计数据
const getStats = () => {
  // 这里可以调用API获取真实数据
  // 模拟数据
  userCount.value = 100
  orderCount.value = 500
  productCount.value = 200
  salesAmount.value = 100000
}

// 页面挂载时获取数据
onMounted(() => {
  getStats()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-card {
  max-width: 1200px;
}

.card-header {
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.welcome-content {
  margin-top: 20px;
}

.welcome-content p {
  margin-bottom: 30px;
  font-size: 16px;
  color: #666;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}
</style>