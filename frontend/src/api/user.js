import request from '../utils/axios'

// 用户API
const userApi = {
  /**
   * 分页查询用户列表
   * @param {Object} params 查询参数
   * @param {number} params.pageNum 页码
   * @param {number} params.pageSize 每页条数
   * @param {string} params.username 用户名（模糊查询）
   * @returns {Promise} 返回Promise对象
   */
  pageUser: (params) => {
    return request({
      url: '/user/page',
      method: 'post',
      data: params
    })
  },
  
  /**
   * 查询用户详情
   * @param {number} id 用户ID
   * @returns {Promise} 返回Promise对象
   */
  getUserById: (id) => {
    return request({
      url: `/user/${id}`,
      method: 'get'
    })
  },
  
  /**
   * 根据用户名查询用户
   * @param {string} username 用户名
   * @returns {Promise} 返回Promise对象
   */
  getUserByUsername: (username) => {
    return request({
      url: `/user/username/${username}`,
      method: 'get'
    })
  },
  
  /**
   * 新增用户
   * @param {Object} user 用户信息
   * @returns {Promise} 返回Promise对象
   */
  addUser: (user) => {
    return request({
      url: '/user',
      method: 'post',
      data: user
    })
  },
  
  /**
   * 更新用户
   * @param {Object} user 用户信息
   * @returns {Promise} 返回Promise对象
   */
  updateUser: (user) => {
    return request({
      url: '/user',
      method: 'put',
      data: user
    })
  },
  
  /**
   * 删除用户
   * @param {number} id 用户ID
   * @returns {Promise} 返回Promise对象
   */
  deleteUser: (id) => {
    return request({
      url: `/user/${id}`,
      method: 'delete'
    })
  },
  
  /**
   * 批量删除用户
   * @param {Array<number>} ids 用户ID列表
   * @returns {Promise} 返回Promise对象
   */
  batchDeleteUser: (ids) => {
    return request({
      url: '/user/batch',
      method: 'delete',
      data: ids
    })
  }
}

export default userApi