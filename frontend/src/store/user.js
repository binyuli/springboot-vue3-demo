import { defineStore } from 'pinia'

// 用户状态管理
const useUserStore = defineStore('user', {
  state: () => ({
    // token
    token: localStorage.getItem('token') || '',
    // refresh token
    refreshToken: localStorage.getItem('refreshToken') || '',
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
    
    // 设置refresh token
    setRefreshToken(refreshToken) {
      this.refreshToken = refreshToken
      localStorage.setItem('refreshToken', refreshToken)
    },
    
    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    
    // 登录
    login(userInfo) {
      // 设置token和refresh token
      this.setToken(userInfo.token)
      this.setRefreshToken(userInfo.refreshToken)
      this.setUserInfo(userInfo.user)
    },
    
    // 登出
    logout() {
      // 清除token、refresh token和用户信息
      this.token = ''
      this.refreshToken = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
    },
    
    // 更新token（刷新token时使用）
    updateTokens(newToken, newRefreshToken) {
      this.setToken(newToken)
      if (newRefreshToken) {
        this.setRefreshToken(newRefreshToken)
      }
    }
  }
})

export { useUserStore }