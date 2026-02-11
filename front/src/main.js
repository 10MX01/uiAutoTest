import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 导入 Tailwind CSS (CDN方式)
// 注意：生产环境建议使用 npm install tailwindcss 方式

// 导入 Font Awesome
import 'font-awesome/css/font-awesome.min.css'

const app = createApp(App)
app.use(router)
app.mount('#app')

