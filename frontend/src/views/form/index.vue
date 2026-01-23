<template>
  <div class="form-container">
    <el-card class="form-card" shadow="hover">
      <div class="card-header">
        <h3>复杂表单示例</h3>
      </div>
      
      <el-form
        :model="formData"
        :rules="formRules"
        ref="formRef"
        label-width="100px"
        class="demo-form"
      >
        <!-- 输入框 -->
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        
        <!-- 下拉选择 -->
        <el-form-item label="性别" prop="gender">
          <el-select v-model="formData.gender" placeholder="请选择性别">
            <el-option label="男" :value="1"></el-option>
            <el-option label="女" :value="2"></el-option>
            <el-option label="未知" :value="0"></el-option>
          </el-select>
        </el-form-item>
        
        <!-- 手机号 -->
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        
        <!-- 日期范围选择 -->
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="formData.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        
        <!-- 上传组件 -->
        <el-form-item label="上传文件" prop="files">
          <el-upload
            v-model:file-list="fileList"
            class="upload-demo"
            :action="uploadUrl"
            :auto-upload="false"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :before-remove="beforeRemove"
            :file-list="fileList"
            multiple
            accept=".jpg,.png"
            :before-upload="beforeUpload"
            list-type="picture-card"
          >
            <template #default>
              <el-icon><Plus /></el-icon>
              <div class="el-upload__text">
                点击上传或拖拽<br />
                <span class="el-upload__tip">仅支持jpg/png格式，单文件不超过2MB</span>
              </div>
            </template>
            <template #file="{ file }">
              <div class="upload-file-item">
                <img :src="file.url" alt="file" class="file-preview" />
                <div class="file-info">
                  <span>{{ file.name }}</span>
                  <el-button type="text" size="small" @click.stop="handlePreview(file)">
                    <el-icon><zoom-in /></el-icon>
                  </el-button>
                  <el-button type="text" size="small" @click.stop="handleRemove(file)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </template>
          </el-upload>
          
          <!-- 预览弹窗 -->
          <el-dialog v-model="previewVisible" title="文件预览" width="800px">
            <img v-if="previewImage" :src="previewImage" alt="预览图片" style="width: 100%" />
          </el-dialog>
        </el-form-item>
        
        <!-- 富文本编辑器（可选，演示用） -->
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注"
          ></el-input>
        </el-form-item>
        
        <!-- 操作按钮 -->
        <el-form-item>
          <div class="form-buttons">
            <el-button type="primary" @click="handleSubmit">
              <el-icon><Check /></el-icon>
              提交
            </el-button>
            <el-button @click="handleReset">
              <el-icon><RefreshRight /></el-icon>
              重置
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Plus, Check, RefreshRight, Delete, ZoomIn } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 表单引用
const formRef = ref(null)

// 上传配置
const uploadUrl = '/api/upload' // 上传接口地址，实际项目中需要替换为真实地址
const fileList = ref([]) // 上传的文件列表
const previewVisible = ref(false) // 预览弹窗可见性
const previewImage = ref('') // 预览图片地址

// 表单数据
const formData = reactive({
  name: '',
  gender: 0,
  phone: '',
  dateRange: [],
  remark: ''
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  dateRange: [
    { required: true, message: '请选择日期范围', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && value.length === 2) {
          const [start, end] = value
          if (new Date(start) > new Date(end)) {
            callback(new Error('开始日期不能晚于结束日期'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 提交表单
const handleSubmit = async () => {
  // 表单校验
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 构建表单数据，包括上传的文件
        const submitData = { ...formData }
        
        // 这里可以添加文件上传逻辑
        // 示例：将文件列表添加到提交数据中
        submitData.files = fileList.value.map(file => ({
          name: file.name,
          url: file.url,
          uid: file.uid
        }))
        
        console.log('提交的数据:', submitData)
        
        // 模拟API请求
        // await api.submitForm(submitData)
        
        // 提示成功
        ElMessage.success('表单提交成功')
        
        // 重置表单
        handleReset()
      } catch (error) {
        console.error('表单提交失败:', error)
        ElMessage.error('表单提交失败')
      }
    }
  })
}

// 重置表单
const handleReset = () => {
  // 重置表单数据
  if (formRef.value) {
    formRef.value.resetFields()
  }
  
  // 重置上传文件列表
  fileList.value = []
  
  // 重置日期范围
  formData.dateRange = []
  
  ElMessage.info('表单已重置')
}

// 预览文件
const handlePreview = (file) => {
  if (file.url || file.preview) {
    previewImage.value = file.url || file.preview
    previewVisible.value = true
  }
}

// 删除文件
const handleRemove = (file) => {
  const index = fileList.value.findIndex(item => item.uid === file.uid)
  if (index !== -1) {
    fileList.value.splice(index, 1)
  }
}

// 删除前确认
const beforeRemove = (file) => {
  return ElMessageBox.confirm(`确定要删除文件 "${file.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return true
  }).catch(() => {
    return false
  })
}

// 上传前验证
const beforeUpload = (rawFile) => {
  // 验证文件格式
  const isJPG = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
  if (!isJPG) {
    ElMessage.error('请上传 JPG/PNG 格式的图片')
    return false
  }
  
  // 验证文件大小（不超过2MB）
  const isLt2M = rawFile.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('上传文件大小不能超过 2MB')
    return false
  }
  
  // 返回 false 停止自动上传，我们将在提交表单时手动上传
  return false
}
</script>

<style scoped>
.form-container {
  padding: 20px;
}

.form-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.demo-form {
  margin-top: 20px;
}

.form-buttons {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.upload-demo {
  margin-top: 10px;
}

.upload-file-item {
  position: relative;
  margin-bottom: 10px;
}

.file-preview {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.file-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 0 0 4px 4px;
}

.file-info span {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>