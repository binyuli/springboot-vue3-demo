import request from '../utils/axios'

// 认证API
const authApi = {
  /**
   * 用户登录
   * @param {Object} loginData 登录数据
   * @param {string} loginData.username 用户名
   * @param {string} loginData.password 密码
   * @returns {Promise} 返回Promise对象
   */
  login: (loginData) => {
    return request({
      url: '/auth/login',
      method: 'post',
      data: loginData
    })
  },

  /**
   * 刷新token
   * @returns {Promise} 返回Promise对象
   * 注意：Refresh Token通过HttpOnly Cookie自动发送
   */
  refreshToken: () => {
    return request({
      url: '/auth/refresh',
      method: 'post',
      data: {} // 不需要传递refreshToken，通过Cookie自动发送
    })
  },

  /**
   * 用户登出
   * @returns {Promise} 返回Promise对象
   * 注意：Refresh Token通过HttpOnly Cookie自动发送
   */
  logout: () => {
    return request({
      url: '/auth/logout',
      method: 'post',
      data: {} // 不需要传递refreshToken，通过Cookie自动发送
    })
  }
}

export default authApi
