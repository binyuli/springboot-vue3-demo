import { defineStore } from 'pinia'

// 用户状态管理
const useUserStore = defineStore('user', {
  state: () => ({
    // token
    token: localStorage.getItem('token') || '',
    // 用户信息
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null
  }),
  
  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.token
  },
  
  actions: {
    // 设置token
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },
    
    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    
    // 登录
    login(userInfo) {
      // 这里应该调用登录接口，获取token和用户信息
      // 示例代码，实际项目中需要替换为真实接口调用
      this.setToken(userInfo.token)
      this.setUserInfo(userInfo.user)
    },
    
    // 登出
    logout() {
      // 清除token和用户信息
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
    
    // 刷新token
    refreshToken(newToken) {
      this.setToken(newToken)
    }
  }
})

export { useUserStore }