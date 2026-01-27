import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../store/user'
import authApi from '../api/auth'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // 接口基础路径
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 是否正在刷新token
let isRefreshing = false

// 待重试的请求队列
let requests = []

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
  async error => {
    // 响应错误处理
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, config } = error.response
      const userStore = useUserStore()
      
      // 处理401错误（token过期）
      if (status === 401 && !config.url.includes('/auth/refresh')) {
        // 如果正在刷新token，将请求加入队列
        if (isRefreshing) {
          return new Promise((resolve) => {
            requests.push((token) => {
              config.headers.Authorization = `Bearer ${token}`
              resolve(service(config))
            })
          })
        }
        
        // 标记正在刷新token
        isRefreshing = true
        
        try {
          // 尝试刷新token（不需要传递refreshToken，因为它会自动通过HttpOnly Cookie发送）
          const response = await authApi.refreshToken({})
          
          // 更新加密的token
          userStore.updateToken(response.data.token)
          
          // 执行队列中的请求
          requests.forEach(callback => callback(response.data.token))
          requests = []
          
          // 重试当前请求
          config.headers.Authorization = `Bearer ${response.data.token}`
          return service(config)
          
        } catch (refreshError) {
          console.error('刷新token失败:', refreshError)
          // 刷新失败，跳转登录页
          handleLogout()
          return Promise.reject(refreshError)
        } finally {
          // 刷新完成
          isRefreshing = false
        }
      } else {
        // 处理其他错误（包括refresh请求返回401的情况）
        switch (status) {
          case 401:
            // token过期或无效（但不是在刷新时）
            handleLogout()
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

// 处理登出
function handleLogout() {
  const userStore = useUserStore()
  
  // 调用登出接口（Refresh Token通过HttpOnly Cookie自动发送）
  authApi.logout({}).catch(err => {
    console.error('登出接口调用失败:', err)
  })
  
  // 清除用户信息
  userStore.logout()
  
  // 跳转到登录页
  ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    window.location.href = '/login'
  }).catch(() => {
    window.location.href = '/login'
  })
}

export default service