<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <div class="card-header">
        <h3>系统登录</h3>
      </div>
      
      <el-form
        :model="loginForm"
        :rules="loginRules"
        ref="loginRef"
        label-width="80px"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="loginForm.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-btn">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import authApi from '../api/auth'
import { validateInput } from '../utils/security'

// 路由实例
const router = useRouter()

// 用户状态管理
const userStore = useUserStore()

// 表单引用
const loginRef = ref(null)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 登录表单校验规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 登录
const handleLogin = async () => {
  if (!loginRef.value) return
  try {
    // 表单验证
    await loginRef.value.validate()
    
    // 清理用户名，防止XSS攻击
    const cleanedUsername = validateInput(loginForm.username, { maxLength: 20 })
    
    // 调用登录接口
    const response = await authApi.login({
      username: cleanedUsername,
      password: loginForm.password
    })
    
    // 设置用户信息和token
    userStore.login(response.data)
    
    // 提示登录成功
    ElMessage.success('登录成功')
    
    // 跳转到首页
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.response?.data?.msg || '登录失败')
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.login-card {
  width: 420px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
  padding-top: 10px;
}

.card-header h3 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
  letter-spacing: 1px;
}

.login-form {
  margin-top: 20px;
  padding: 0 30px 30px 30px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.login-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.login-form :deep(.el-input__wrapper) {
  margin-bottom: 8px;
}

.login-form :deep(.el-input__inner) {
  border-radius: 6px;
  padding: 12px 15px;
  font-size: 14px;
}

.login-form :deep(.el-input__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.login-btn:active {
  transform: translateY(0);
}
</style>