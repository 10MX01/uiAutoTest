<template>
  <div class="p-6">
    <div class="mb-6 flex items-center justify-between">
      <h2 class="text-xl font-bold text-dark">账号管理</h2>
      <button v-if="Auth.isAdmin()" class="btn-primary text-sm flex items-center gap-1" @click="openCreateModal">
        <i class="fa fa-plus"></i> 新增用户
      </button>
    </div>

    <div class="bg-white p-6 rounded-lg card-shadow">
      <!-- 筛选栏 -->
      <div class="mb-6 flex flex-col md:flex-row gap-4 items-start md:items-center">
        <div class="flex flex-wrap gap-3 flex-1">
          <select v-model="filters.role" @change="loadUsers" class="w-40 border border-gray-200 bg-white text-dark rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary">
            <option value="">全部角色</option>
            <option value="ADMIN">管理员</option>
            <option value="USER">普通用户</option>
          </select>

          <select v-model="filters.status" @change="loadUsers" class="w-40 border border-gray-200 bg-white text-dark rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary">
            <option value="">全部状态</option>
            <option value="ACTIVE">启用</option>
            <option value="DISABLED">禁用</option>
          </select>
        </div>
        <div class="relative flex-shrink-0">
          <input
            v-model="filters.search"
            @keyup.enter="loadUsers"
            type="text"
            placeholder="搜索用户名或姓名..."
            class="w-64 py-2 px-4 pr-10 rounded-lg border border-gray-200 bg-white text-dark focus:outline-none focus:border-primary text-sm"
          >
          <i
            class="fa fa-search absolute right-3 top-1/2 -translate-y-1/2 text-light cursor-pointer hover:text-primary transition-colors duration-200"
            @click="loadUsers"
            title="点击搜索"
          ></i>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-10">
        <i class="fa fa-spinner fa-spin text-4xl text-primary"></i>
        <p class="text-light mt-2">加载中...</p>
      </div>

      <!-- 空数据状态 -->
      <div v-else-if="users.length === 0" class="text-center py-10">
        <i class="fa fa-users text-6xl text-light"></i>
        <p class="text-light mt-4">暂无用户数据</p>
      </div>

      <!-- 用户表格 -->
      <div v-else class="bg-white rounded-lg border border-gray-200 overflow-hidden card-shadow">
        <table class="w-full text-left">
          <thead>
            <tr class="border-b border-gray-200 bg-gray-50">
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">用户名</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">真实姓名</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">角色</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">邮箱</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">手机号</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">状态</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">最后登录</th>
              <th v-if="Auth.isAdmin()" class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.uniqueId" class="border-b border-gray-200 table-row-hover">
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                <span class="font-medium">{{ user.username }}</span>
              </td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">{{ user.realName }}</td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                <span class="px-2 py-1 rounded text-xs" :class="user.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-gray-100 text-gray-700'">
                  {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
                </span>
              </td>
              <td class="px-4 py-4 text-sm whitespace-nowrap text-light">{{ user.email || '-' }}</td>
              <td class="px-4 py-4 text-sm whitespace-nowrap text-light">{{ user.phone || '-' }}</td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                <span class="px-2 py-1 rounded text-xs" :class="user.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'">
                  {{ user.status === 'ACTIVE' ? '启用' : '禁用' }}
                </span>
              </td>
              <td class="px-4 py-4 text-sm whitespace-nowrap text-light">{{ formatDate(user.lastLoginTime) }}</td>
              <td v-if="Auth.isAdmin()" class="px-4 py-4 text-sm whitespace-nowrap">
                <div class="flex items-center gap-2">
                  <button @click="openEditModal(user)" class="text-primary hover:text-primary/80" title="编辑">
                    <i class="fa fa-edit"></i>
                  </button>
                  <button
                    v-if="user.status === 'ACTIVE'"
                    @click="disableUser(user.uniqueId)"
                    class="text-orange-500 hover:text-orange-600"
                    title="禁用"
                  >
                    <i class="fa fa-ban"></i>
                  </button>
                  <button
                    v-else
                    @click="enableUser(user.uniqueId)"
                    class="text-green-500 hover:text-green-600"
                    title="启用"
                  >
                    <i class="fa fa-check"></i>
                  </button>
                  <button @click="deleteUser(user.uniqueId)" class="text-red-500 hover:text-red-600" title="删除">
                    <i class="fa fa-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-lg w-full max-w-md">
        <div class="p-6 border-b border-gray-200">
          <h3 class="text-lg font-bold text-dark">{{ isEdit ? '编辑用户' : '新增用户' }}</h3>
        </div>
        <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
          <div v-if="!isEdit">
            <label class="block text-sm font-medium text-dark mb-1">用户名 *</label>
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-primary"
              :class="{ 'border-red-500': errors.username }"
            />
            <p v-if="errors.username" class="text-red-500 text-xs mt-1">{{ errors.username }}</p>
          </div>

          <div v-if="!isEdit">
            <label class="block text-sm font-medium text-dark mb-1">密码 *</label>
            <input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-primary"
              :class="{ 'border-red-500': errors.password }"
            />
            <p v-if="errors.password" class="text-red-500 text-xs mt-1">{{ errors.password }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-dark mb-1">真实姓名 *</label>
            <input
              v-model="form.realName"
              type="text"
              placeholder="请输入真实姓名"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-primary"
              :class="{ 'border-red-500': errors.realName }"
            />
            <p v-if="errors.realName" class="text-red-500 text-xs mt-1">{{ errors.realName }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-dark mb-1">邮箱</label>
            <input
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-primary"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-dark mb-1">手机号</label>
            <input
              v-model="form.phone"
              type="text"
              placeholder="请输入手机号"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-primary"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-dark mb-1">角色 *</label>
            <select
              v-model="form.role"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-primary"
            >
              <option value="USER">普通用户</option>
              <option value="ADMIN">管理员</option>
            </select>
          </div>

          <div class="flex gap-3 pt-4">
            <button
              type="button"
              @click="closeModal"
              class="flex-1 px-4 py-2 border border-gray-200 rounded-lg hover:bg-neutral transition-colors"
            >
              取消
            </button>
            <button
              type="submit"
              :disabled="submitting"
              class="flex-1 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors disabled:bg-gray-300"
            >
              {{ submitting ? '提交中...' : '确定' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserList, createUser, updateUser, enableUser as enableUserApi, disableUser as disableUserApi, deleteUser as deleteUserApi } from '@/api/auth'
import { Auth } from '@/utils/auth'
import MessageBox from '@/utils/messageBox'

const users = ref([])
const loading = ref(false)

const filters = reactive({
  role: '',
  status: '',
  search: ''
})

const showModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)

const form = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  role: 'USER'
})

const errors = reactive({
  username: '',
  password: '',
  realName: ''
})

let currentUserId = null

const loadUsers = async () => {
  loading.value = true
  try {
    const data = await getUserList(filters)
    users.value = data
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  isEdit.value = false
  currentUserId = null
  Object.assign(form, {
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    role: 'USER'
  })
  Object.assign(errors, { username: '', password: '', realName: '' })
  showModal.value = true
}

const openEditModal = (user) => {
  isEdit.value = true
  currentUserId = user.uniqueId
  Object.assign(form, {
    realName: user.realName,
    email: user.email || '',
    phone: user.phone || '',
    role: user.role
  })
  Object.assign(errors, { username: '', password: '', realName: '' })
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const validateForm = () => {
  let isValid = true
  Object.assign(errors, { username: '', password: '', realName: '' })

  if (!isEdit.value) {
    if (!form.username.trim()) {
      errors.username = '请输入用户名'
      isValid = false
    }
    if (!form.password) {
      errors.password = '请输入密码'
      isValid = false
    } else if (form.password.length < 6) {
      errors.password = '密码长度不能少于6位'
      isValid = false
    }
  }

  if (!form.realName.trim()) {
    errors.realName = '请输入真实姓名'
    isValid = false
  }

  return isValid
}

const handleSubmit = async () => {
  if (!validateForm()) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateUser(currentUserId, form)
    } else {
      await createUser(form)
    }
    closeModal()
    loadUsers()
  } catch (error) {
    alert(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const enableUser = async (id) => {
  if (!await MessageBox.confirm('确认启用该用户吗？')) return
  try {
    await enableUserApi(id)
    loadUsers()
  } catch (error) {
    alert(error.message || '操作失败')
  }
}

const disableUser = async (id) => {
  if (!await MessageBox.confirm('确认禁用该用户吗？')) return
  try {
    await disableUserApi(id)
    loadUsers()
  } catch (error) {
    alert(error.message || '操作失败')
  }
}

const deleteUser = async (id) => {
  if (!await MessageBox.confirm('确认删除该用户吗？删除后用户将被禁用。')) return
  try {
    await deleteUserApi(id)
    loadUsers()
  } catch (error) {
    alert(error.message || '操作失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.substring(0, 19).replace('T', ' ')
}

onMounted(() => {
  loadUsers()
})
</script>
