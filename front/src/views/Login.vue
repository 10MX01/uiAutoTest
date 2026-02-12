<template>
  <div class="min-h-screen bg-neutral flex items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-xl w-full max-w-md p-10">
      <!-- Logo和标题 -->
      <div class="text-center mb-10">
        <!-- Logo图标 -->
        <div class="flex items-center justify-center mb-6">
          <div class="w-16 h-16 bg-primary rounded-xl flex items-center justify-center shadow-lg">
            <i class="fa fa-key text-white text-3xl"></i>
          </div>
        </div>

        <!-- 平台标题 -->
        <h1 class="text-2xl font-bold text-dark mb-1">密码服务平台</h1>
        <p class="text-sm text-light mb-6">自动化测试工具</p>

        <!-- 分隔线 -->
        <div class="flex items-center gap-4 mb-6">
          <div class="flex-1 h-px bg-gray-200"></div>
          <span class="text-xs text-light">用户登录</span>
          <div class="flex-1 h-px bg-gray-200"></div>
        </div>
      </div>

      <!-- 登录表单 -->
      <form @submit.prevent="handleLogin" class="space-y-5">
        <!-- 用户名 -->
        <div>
          <label class="block text-sm font-medium text-dark mb-2">用户名</label>
          <div class="relative">
            <i class="fa fa-user absolute left-3 top-1/2 -translate-y-1/2 text-light"></i>
            <input
              v-model="loginForm.username"
              type="text"
              placeholder="请输入用户名"
              class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
              :class="{ 'border-red-500': errors.username }"
            />
          </div>
          <p v-if="errors.username" class="text-red-500 text-xs mt-1">{{ errors.username }}</p>
        </div>

        <!-- 密码 -->
        <div>
          <label class="block text-sm font-medium text-dark mb-2">密码</label>
          <div class="relative">
            <i class="fa fa-lock absolute left-3 top-1/2 -translate-y-1/2 text-light"></i>
            <input
              v-model="loginForm.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              class="w-full pl-10 pr-12 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
              :class="{ 'border-red-500': errors.password }"
            />
            <button
              type="button"
              @click="showPassword = !showPassword"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-light hover:text-primary transition-colors"
            >
              <i :class="showPassword ? 'fa-eye-slash' : 'fa-eye'"></i>
            </button>
          </div>
          <p v-if="errors.password" class="text-red-500 text-xs mt-1">{{ errors.password }}</p>
        </div>

        <!-- 记住我 -->
        <div class="flex items-center justify-between">
          <label class="flex items-center gap-2 cursor-pointer">
            <input v-model="loginForm.remember" type="checkbox" class="w-4 h-4 text-primary rounded border-gray-300 focus:ring-primary">
            <span class="text-sm text-dark">记住我</span>
          </label>
          <a href="#" class="text-sm text-primary hover:underline">忘记密码？</a>
        </div>

        <!-- 错误提示 -->
        <div v-if="errorMessage" class="bg-red-50 border border-red-200 rounded-lg p-3 text-red-600 text-sm">
          <i class="fa fa-exclamation-circle mr-2"></i>
          {{ errorMessage }}
        </div>

        <!-- 登录按钮 -->
        <button
          type="submit"
          :disabled="loading"
          class="w-full bg-primary text-white py-3 rounded-lg font-medium hover:bg-primary/90 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed flex items-center justify-center gap-2"
        >
          <i v-if="loading" class="fa fa-spinner fa-spin"></i>
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { login } from '@/api/auth'
import { Auth } from '@/utils/auth'

const router = useRouter()
const route = useRoute()

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const errors = reactive({
  username: '',
  password: ''
})

const errorMessage = ref('')
const loading = ref(false)
const showPassword = ref(false)

const validateForm = () => {
  let isValid = true
  errors.username = ''
  errors.password = ''

  if (!loginForm.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  }

  if (!loginForm.password) {
    errors.password = '请输入密码'
    isValid = false
  }

  return isValid
}

const handleLogin = async () => {
  if (!validateForm()) {
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const response = await login({
      username: loginForm.username.trim(),
      password: loginForm.password
    })

    // 保存Token和用户信息
    Auth.setToken(response.token)
    Auth.setUser({
      userId: response.userId,
      username: response.username,
      realName: response.realName,
      role: response.role
    })

    // 跳转到目标页面或首页
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (error) {
    errorMessage.value = error.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>
