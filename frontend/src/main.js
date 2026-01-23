import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './style.css'

// 创建Vue应用
const app = createApp(App)

// 使用Element Plus
app.use(ElementPlus)

// 使用Pinia
app.use(createPinia())

// 使用路由
app.use(router)

// 挂载应用
app.mount('#app')
