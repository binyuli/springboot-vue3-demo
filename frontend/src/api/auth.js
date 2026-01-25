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
  }
}

export default authApi
