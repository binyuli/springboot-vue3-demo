import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../store/user'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // 接口基础路径
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从Pinia中获取token
    const userStore = useUserStore()
    const token = userStore.token
    
    // 添加token到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  error => {
    // 请求错误处理
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 统一处理响应结果
    if (res.code === 200) {
      return res
    } else {
      // 错误提示
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  error => {
    // 响应错误处理
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status } = error.response
      
      switch (status) {
        case 401:
          // token过期或无效，清除用户信息并跳转到登录页
          const userStore = useUserStore()
          ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            userStore.logout()
            window.location.href = '/login'
          })
          break
        case 403:
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(`请求失败，错误码：${status}`)
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      ElMessage.error('网络异常，请检查网络连接')
    } else {
      // 请求配置错误
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default service