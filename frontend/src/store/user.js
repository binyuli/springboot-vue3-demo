import { defineStore } from 'pinia'

// 用户状态管理
const useUserStore = defineStore('user', {
  state: () => ({
    // Access Token（存储在localStorage中）
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
      // 设置token和用户信息
      // 注意：现在Refresh Token通过HttpOnly Cookie存储，前端不直接访问
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
    
    // 更新token（刷新token时使用）
    updateToken(newToken) {
      this.setToken(newToken)
    }
  }
})

export { useUserStore }