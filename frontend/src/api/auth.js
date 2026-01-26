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
   * @param {Object} refreshData 刷新数据
   * @param {string} refreshData.refreshToken 刷新令牌
   * @returns {Promise} 返回Promise对象
   */
  refreshToken: (refreshData) => {
    return request({
      url: '/auth/refresh',
      method: 'post',
      data: refreshData
    })
  },

  /**
   * 用户登出
   * @param {Object} logoutData 登出数据
   * @param {string} logoutData.refreshToken 刷新令牌
   * @returns {Promise} 返回Promise对象
   */
  logout: (logoutData) => {
    return request({
      url: '/auth/logout',
      method: 'post',
      data: logoutData
    })
  }
}

export default authApi
