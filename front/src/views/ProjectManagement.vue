<template>
  <section>
    <div class="mb-6 flex items-center justify-between">
      <h2 class="text-xl font-bold text-dark">项目管理</h2>
      <button @click="showModal = true" class="btn-primary text-sm flex items-center justify-center gap-2">
        <i class="fa fa-plus"></i> 新增项目
      </button>
    </div>
    <div class="bg-white p-6 rounded-lg card-shadow">
      <!-- 筛选栏 -->
      <div class="bg-neutral rounded-lg p-4 mb-6 flex flex-col md:flex-row gap-4 items-start md:items-center border border-borderColor">
        <div class="flex flex-wrap gap-3 flex-1 items-center">
          <div class="flex items-center gap-2">
            <label class="text-sm text-light whitespace-nowrap">创建时间:</label>
            <input
              v-model="filters.startDate"
              type="date"
              class="w-36 border border-borderColor bg-white text-dark rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary"
              @change="handleSearch"
            >
            <span class="text-light">至</span>
            <input
              v-model="filters.endDate"
              type="date"
              class="w-36 border border-borderColor bg-white text-dark rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary"
              @change="handleSearch"
            >
          </div>
          <button
            @click="clearDateFilter"
            class="px-3 py-2 text-sm text-light hover:text-primary border border-borderColor rounded-lg hover:border-primary transition-colors"
            title="清除日期筛选"
          >
            <i class="fa fa-times"></i> 清除
          </button>
        </div>
        <div class="relative flex-shrink-0">
          <input
            v-model="filters.search"
            type="text"
            placeholder="搜索项目名称或代码..."
            class="w-64 py-2 px-4 pr-10 rounded-lg border border-borderColor bg-white text-dark focus:outline-none focus:border-primary text-sm"
            @keyup.enter="handleSearch"
          >
          <i
            class="fa fa-search absolute right-3 top-1/2 -translate-y-1/2 text-light cursor-pointer hover:text-primary transition-colors duration-200"
            @click="handleSearch"
            title="点击搜索"
          ></i>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-10">
        <i class="fa fa-spinner fa-spin text-4xl text-primary"></i>
        <p class="text-light mt-2">加载中...</p>
      </div>

      <!-- 项目卡片列表 -->
      <div v-else>
        <!-- 空数据状态 -->
        <div v-if="displayedProjects.length === 0" class="text-center py-10">
          <i class="fa fa-inbox text-6xl text-light"></i>
          <p class="text-light mt-4">暂无项目数据</p>
        </div>

        <!-- 项目列表 -->
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div v-for="project in displayedProjects" :key="project.uniqueId" class="test-card">
          <div class="card-header">
            <div class="card-icon">
              <svg viewBox="0 0 24 24" class="w-4 h-4 fill-primary">
                <path d="M19 5v14H5V5h14m0-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z"/>
                <path d="M7 18v-2h10v2H7zm0-4v-2h10v2H7zm0-4V8h10v2H7z"/>
              </svg>
            </div>
            <div class="card-title">
              <h3 class="text-base font-semibold">{{ project.code }}</h3>
              <p class="text-xs text-cardTextLight truncate w-28">{{ project.name }}</p>
            </div>
            <div class="status-badge text-xs text-successGreen flex items-center">
              <span class="w-2 h-2 bg-successGreen rounded-full mr-1"></span>
              ACTIVE
            </div>
          </div>
          <div class="card-body">
            <div class="test-count text-2xl font-bold">{{ project.testCaseCount || 0 }}</div>
            <div class="pass-rate text-right">
              <div class="text-xs text-cardTextLight">测试用例</div>
            </div>
          </div>
          <div class="progress-container">
            <div class="progress-label text-xs text-cardTextLight flex justify-between mb-1">
              <span>项目状态</span>
              <span>运行中</span>
            </div>
            <div class="progress-bar h-1 bg-cardBorder rounded-md overflow-hidden">
              <div class="progress-fill bg-warningYellow" style="width: 100%"></div>
            </div>
          </div>
          <div class="card-footer pt-2 border-t border-cardBorder flex justify-between items-center text-xs text-cardTextLight">
            <div>{{ formatDate(project.createdTime) }}</div>
            <div class="card-actions flex gap-2">
              <router-link :to="`/testcase?projectId=${project.uniqueId}`" class="text-linkBlue hover:underline">查看</router-link>
              <a href="#" @click.prevent="deleteProject(project)" class="text-dangerRed hover:underline">删除</a>
            </div>
          </div>
        </div>
        </div>
      </div>
    </div>

    <!-- 新增项目弹窗 -->
    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showModal = false">
      <div class="bg-white rounded-lg w-full max-w-2xl p-6 transform transition-all">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-bold">新增项目</h3>
          <button @click="showModal = false" class="text-light hover:text-dark transition-colors">
            <i class="fa fa-times text-lg"></i>
          </button>
        </div>
        <form @submit.prevent="createProject" class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="md:col-span-2">
            <label class="block text-sm font-medium mb-1">项目名称 <span class="text-red-500">*</span></label>
            <input v-model="newProject.name" type="text" class="w-full border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:border-primary" placeholder="请输入项目名称" required>
          </div>
          <div class="md:col-span-2">
            <label class="block text-sm font-medium mb-1">项目代码 <span class="text-red-500">*</span></label>
            <input v-model="newProject.code" type="text" class="w-full border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:border-primary" placeholder="请输入项目代码（如：RA、ACMS）" required>
          </div>
          <div class="md:col-span-2">
            <label class="block text-sm font-medium mb-1">项目描述</label>
            <textarea v-model="newProject.description" class="w-full border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:border-primary min-h-[80px]" placeholder="请输入项目描述（选填）"></textarea>
          </div>
          <div class="md:col-span-2 flex justify-end gap-3 mt-4">
            <button type="button" @click="showModal = false" class="btn-outline">取消</button>
            <button type="submit" class="btn-primary">确认创建</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProjects, createProject as apiCreateProject, deleteProject as apiDeleteProject } from '@/api/project'
import MessageBox from '@/utils/messageBox'

const showModal = ref(false)
const loading = ref(false)
const projects = ref([])
const displayedProjects = ref([])

const filters = ref({
  search: '',
  startDate: '',
  endDate: ''
})

const newProject = ref({
  name: '',
  code: '',
  description: ''
})

// 获取项目数据
const fetchProjects = async () => {
  loading.value = true
  try {
    const data = await getProjects()
    projects.value = data || []
    displayedProjects.value = data || []
  } catch (error) {
    console.error('获取项目失败:', error)
    projects.value = []
  } finally {
    loading.value = false
  }
}

// 创建项目
const createProject = async () => {
  try {
    await apiCreateProject(newProject.value)
    // 重置表单
    newProject.value = {
      name: '',
      code: '',
      description: ''
    }
    showModal.value = false
    MessageBox.success('项目创建成功')
    // 刷新列表
    await fetchProjects()
  } catch (error) {
    console.error('创建项目失败:', error)
    MessageBox.error('创建项目失败')
  }
}

// 删除项目
const deleteProject = async (project) => {
  try {
    const result = await MessageBox.confirm(`确定要删除项目"${project.name}"吗？`, '确认删除', {
      type: 'warning',
      confirmButtonType: 'danger'
    })

    // 如果用户取消，不执行删除
    if (result === 'cancel') return

    try {
      await apiDeleteProject(project.uniqueId)
      MessageBox.success('删除成功')
      // 刷新列表
      await fetchProjects()
    } catch (error) {
      console.error('删除项目失败:', error)
      MessageBox.error('删除项目失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 搜索处理
const handleSearch = () => {
  let result = projects.value

  // 按关键词搜索
  if (filters.value.search && filters.value.search.trim() !== '') {
    const search = filters.value.search.toLowerCase().trim()
    result = result.filter(project =>
      project.name.toLowerCase().includes(search) ||
      project.code.toLowerCase().includes(search)
    )
  }

  // 按创建时间区间过滤
  if (filters.value.startDate) {
    const startDate = new Date(filters.value.startDate)
    startDate.setHours(0, 0, 0, 0) // 设置为当天0点
    result = result.filter(project => {
      const projectDate = new Date(project.createdTime)
      return projectDate >= startDate
    })
  }

  if (filters.value.endDate) {
    const endDate = new Date(filters.value.endDate)
    endDate.setHours(23, 59, 59, 999) // 设置为当天23:59:59
    result = result.filter(project => {
      const projectDate = new Date(project.createdTime)
      return projectDate <= endDate
    })
  }

  displayedProjects.value = result
}

// 清除日期筛选
const clearDateFilter = () => {
  filters.value.startDate = ''
  filters.value.endDate = ''
  handleSearch()
}

onMounted(() => {
  fetchProjects()
})
</script>
