<template>
  <div class="user-container">
    <!-- 查询表单 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <!-- 用户名 -->
          <el-col :span="6">
            <el-form-item label="用户名">
              <el-input
                v-model="searchForm.username"
                placeholder="请输入用户名"
                clearable
              ></el-input>
            </el-form-item>
          </el-col>
          
          <!-- 操作按钮 -->
          <el-col :span="18" class="search-buttons">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><RefreshRight /></el-icon>
              重置
            </el-button>
            <el-button type="success" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增
            </el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    
    <!-- 用户列表 -->
    <el-card class="table-card" shadow="hover" style="margin-top: 20px;">
      <div class="table-header">
        <h3>用户列表</h3>
      </div>
      
      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="username" label="用户名" width="180"></el-table-column>
        <el-table-column prop="nickname" label="昵称" width="180"></el-table-column>
        <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
        <el-table-column prop="phone" label="手机号" width="180"></el-table-column>
        <el-table-column prop="gender" label="性别" width="100" align="center">
          <template #default="scope">
            <el-tag
              :type="scope.row.gender === 1 ? 'primary' : scope.row.gender === 2 ? 'success' : 'info'"
            >
              {{ scope.row.gender === 1 ? '男' : scope.row.gender === 2 ? '女' : '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="200" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="isAdd">
          <el-input type="password" v-model="formData.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="formData.gender" placeholder="请选择性别">
            <el-option label="未知" :value="0"></el-option>
            <el-option label="男" :value="1"></el-option>
            <el-option label="女" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
          ></el-switch>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, RefreshRight, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import userApi from '../../api/user'

// 加载状态
const loading = ref(false)

// 用户列表
const userList = ref([])

// 搜索表单
const searchForm = reactive({
  username: ''
})

// 分页配置
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 弹窗配置
const dialogVisible = ref(false)
const dialogTitle = computed(() => isAdd.value ? '新增用户' : '编辑用户')
const isAdd = ref(true)

// 表单数据
const formData = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  status: 1
})

// 表单校验规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 表单引用
const formRef = ref(null)

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString()
}

// 获取用户列表
const getUserList = async () => {
  loading.value = true
  try {
    // 构建请求参数
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      username: searchForm.username
    }
    
    // 调用API
    const response = await userApi.pageUser(params)
    
    // 设置数据
    userList.value = response.data.list || []
    pagination.total = response.data.total || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
    userList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  // 重置页码为1
  pagination.pageNum = 1
  // 获取用户列表
  getUserList()
}

// 重置
const handleReset = () => {
  // 重置搜索表单
  Object.assign(searchForm, {
    username: ''
  })
  
  // 重置页码为1
  pagination.pageNum = 1
  
  // 获取用户列表
  getUserList()
}

// 新增
const handleAdd = () => {
  // 设置为新增模式
  isAdd.value = true
  
  // 重置表单数据
  Object.assign(formData, {
    id: null,
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    gender: 0,
    status: 1
  })
  
  // 打开弹窗
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  // 设置为编辑模式
  isAdd.value = false
  
  // 设置表单数据
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    password: '', // 编辑时不显示密码
    nickname: row.nickname,
    email: row.email,
    phone: row.phone,
    gender: row.gender,
    status: row.status
  })
  
  // 打开弹窗
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API删除用户
    await userApi.deleteUser(row.id)
    
    // 提示成功
    ElMessage.success('删除成功')
    
    // 重新获取用户列表
    getUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    // 调用API更新用户状态
    await userApi.updateUser(row)
    
    // 提示成功
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
    
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 提交表单
const handleSubmit = async () => {
  // 表单校验
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isAdd.value) {
          // 新增用户
          await userApi.addUser(formData)
          ElMessage.success('新增成功')
        } else {
          // 编辑用户
          await userApi.updateUser(formData)
          ElMessage.success('编辑成功')
        }
        
        // 关闭弹窗
        dialogVisible.value = false
        
        // 重新获取用户列表
        getUserList()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 分页大小变更
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getUserList()
}

// 当前页码变更
const handleCurrentChange = (current) => {
  pagination.pageNum = current
  getUserList()
}

// 页面挂载时获取用户列表
onMounted(() => {
  getUserList()
})
</script>

<style scoped>
.user-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 0;
}

.search-buttons {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 10px;
}

.table-card {
  margin-top: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>