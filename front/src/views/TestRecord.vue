<template>
  <section>
    <!-- 标题栏 -->
    <div class="mb-6">
      <h2 class="text-xl font-bold text-dark">测试记录</h2>
    </div>

    <!-- 数据概览卡片 - 仿照测试用例样式 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded-lg p-4 border border-borderColor card-hover card-shadow">
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-light text-sm">执行总数</h3>
          <i class="fa fa-history text-light hover:text-primary cursor-pointer"></i>
        </div>
        <div class="flex items-end gap-2">
          <span class="text-4xl font-bold text-dark">{{ statistics.total || 0 }}</span>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 border border-borderColor card-hover card-shadow">
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-light text-sm">成功</h3>
          <div class="w-6 h-6 bg-success/20 rounded-full flex items-center justify-center text-success">
            <i class="fa fa-check"></i>
          </div>
        </div>
        <div class="flex items-end gap-2">
          <span class="text-4xl font-bold text-dark">{{ statistics.success || 0 }}</span>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 border border-borderColor card-hover card-shadow">
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-light text-sm">失败</h3>
          <div class="w-6 h-6 bg-danger/20 rounded-full flex items-center justify-center text-danger">
            <i class="fa fa-times"></i>
          </div>
        </div>
        <div class="flex items-end gap-2">
          <span class="text-4xl font-bold text-dark">{{ statistics.failed || 0 }}</span>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 border border-borderColor card-hover card-shadow">
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-light text-sm">跳过</h3>
          <div class="w-6 h-6 bg-info/20 rounded-full flex items-center justify-center text-info">
            <i class="fa fa-forward"></i>
          </div>
        </div>
        <div class="flex items-end gap-2">
          <span class="text-4xl font-bold text-dark">{{ statistics.skipped || 0 }}</span>
        </div>
      </div>
    </div>

    <!-- 筛选栏和列表 -->
    <div class="bg-white p-6 rounded-lg card-shadow">
      <!-- 筛选栏 -->
      <div class="bg-neutral rounded-lg p-4 mb-6 flex flex-col md:flex-row gap-4 items-start md:items-center border border-borderColor">
        <div class="flex flex-wrap gap-3 flex-1">
          <select v-model="filters.projectId" @change="handleSearch" class="w-40 border border-borderColor bg-white text-dark rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary">
            <option value="">全部项目</option>
            <option v-for="project in projects" :key="project.uniqueId" :value="project.uniqueId">
              {{ project.name }}
            </option>
          </select>

          <select v-model="filters.status" @change="handleSearch" class="w-40 border border-borderColor bg-white text-dark rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary">
            <option value="">全部状态</option>
            <option value="SUCCESS">成功</option>
            <option value="FAILED">失败</option>
            <option value="SKIPPED">跳过</option>
          </select>

          <select v-model="filters.executorId" @change="handleSearch" class="w-40 border border-borderColor bg-white text-dark rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary">
            <option value="">全部执行人</option>
            <option v-for="user in executors" :key="user.id" :value="user.id">
              {{ user.name }}
            </option>
          </select>
        </div>

        <div class="relative flex-shrink-0">
          <input
            v-model="filters.search"
            type="text"
            placeholder="搜索用例名称或编号..."
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

      <!-- 记录列表表格 -->
      <div v-else class="bg-white rounded-lg border border-borderColor overflow-hidden card-shadow">
        <table class="w-full text-left">
          <thead>
            <tr class="border-b border-borderColor bg-neutral/50">
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">项目名称</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">用例编号</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">用例名称</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">执行状态</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">执行人</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">耗时</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">执行时间</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="records.length === 0">
              <td colspan="8" class="px-4 py-10 text-center">
                <i class="fa fa-inbox text-4xl text-light mb-2"></i>
                <p class="text-light">暂无测试记录</p>
              </td>
            </tr>
            <tr v-else v-for="record in records" :key="record.uniqueId" class="border-b border-borderColor table-row-hover">
              <td class="px-4 py-4 text-sm whitespace-nowrap">{{ record.projectName || '-' }}</td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">{{ record.caseNumber || '-' }}</td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                <div>
                  <div class="font-medium">{{ record.caseName }}</div>
                  <div class="text-xs text-light truncate max-w-xs">{{ record.executionUrl }}</div>
                </div>
              </td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                <span class="px-2 py-1 rounded text-xs" :class="getStatusClass(record.status)">
                  {{ getStatusText(record.status) }}
                </span>
              </td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                {{ record.executorName || '-' }}
              </td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                {{ formatDuration(record.duration) }}
              </td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                {{ formatDate(record.createdTime) }}
              </td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                <span class="flex items-center gap-3">
                  <button @click="viewDetail(record)" class="text-info hover:text-primary relative group">
                    <i class="fa fa-eye text-lg"></i>
                    <span class="absolute -top-8 left-1/2 -translate-x-1/2 bg-white border border-borderColor rounded px-2 py-1 text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 transition shadow-sm z-10">查看详情</span>
                  </button>
                  <button @click="deleteRecord(record)" class="text-danger hover:text-dangerRed relative group">
                    <i class="fa fa-trash text-lg"></i>
                    <span class="absolute -top-8 left-1/2 -translate-x-1/2 bg-white border border-borderColor rounded px-2 py-1 text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 transition shadow-sm z-10">删除</span>
                  </button>
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div v-if="records.length > 0" class="flex items-center justify-between mt-4">
        <div class="text-sm text-light">
          共 {{ total }} 条记录
        </div>
        <div class="flex gap-2">
          <button
            @click="prevPage"
            :disabled="currentPage === 1"
            class="px-3 py-1 border border-borderColor rounded hover:bg-neutral disabled:opacity-50 disabled:cursor-not-allowed text-sm"
          >
            上一页
          </button>
          <span class="px-3 py-1 text-sm">{{ currentPage }} / {{ totalPages }}</span>
          <button
            @click="nextPage"
            :disabled="currentPage === totalPages"
            class="px-3 py-1 border border-borderColor rounded hover:bg-neutral disabled:opacity-50 disabled:cursor-not-allowed text-sm"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 查看详情弹窗 -->
    <div v-if="showDetailModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" @click.self="showDetailModal = false">
      <div class="bg-white rounded-lg w-full max-w-4xl max-h-[90vh] overflow-hidden shadow-xl" @click.stop>
        <!-- 标题栏 -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-borderColor">
          <h3 class="text-lg font-bold">测试记录详情 - {{ currentDetail.caseNumber }} - {{ currentDetail.caseName }}</h3>
          <button @click="showDetailModal = false" class="text-light hover:text-dark transition-colors">
            <i class="fa fa-times text-lg"></i>
          </button>
        </div>

        <!-- 内容区域 -->
        <div class="p-6 overflow-y-auto max-h-[calc(90vh-140px)]">
          <div v-if="detailLoading" class="flex items-center justify-center py-12">
            <i class="fa fa-spinner fa-spin text-4xl text-primary"></i>
          </div>

          <div v-else class="space-y-6">
            <!-- 基本信息 -->
            <div class="space-y-4">
              <div class="flex items-center gap-2">
                <span class="text-sm font-medium text-light">执行状态：</span>
                <span class="px-2 py-1 rounded text-xs" :class="getStatusClass(currentDetail.status)">
                  {{ getStatusText(currentDetail.status) }}
                </span>
              </div>

              <div class="flex gap-4">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-medium text-light">执行耗时：</span>
                  <span class="text-sm text-dark">{{ formatDuration(currentDetail.duration) }}</span>
                </div>
                <div class="flex items-center gap-2">
                  <span class="text-sm font-medium text-light">执行时间：</span>
                  <span class="text-sm text-dark">{{ formatDate(currentDetail.createdTime) }}</span>
                </div>
              </div>

              <div class="flex items-center gap-2">
                <span class="text-sm font-medium text-light">执行URL：</span>
                <span class="text-sm text-dark flex-1 break-all">{{ currentDetail.executionUrl }}</span>
              </div>

              <div class="flex items-center gap-2">
                <span class="text-sm font-medium text-light">执行人：</span>
                <span class="text-sm text-dark">{{ currentDetail.executorName || '-' }}</span>
              </div>

              <div v-if="currentDetail.errorMessage" class="flex items-center gap-2">
                <span class="text-sm font-medium text-light">错误信息：</span>
                <span class="text-sm text-danger flex-1">{{ currentDetail.errorMessage }}</span>
              </div>
            </div>

            <hr class="border-borderColor">

            <!-- 执行步骤 -->
            <div>
              <h4 class="text-base font-semibold mb-3">执行步骤</h4>
              <div class="space-y-4">
                <div
                  v-for="(step, index) in currentDetail.stepsResult"
                  :key="index"
                  class="border border-borderColor rounded-lg p-4"
                  :class="step.success ? 'border-success/30' : 'border-danger/30'"
                >
                  <div class="flex items-start justify-between mb-2">
                    <div class="flex-1">
                      <div class="flex items-center gap-2 mb-1">
                        <span class="font-medium text-sm">Step {{ index + 1 }}:</span>
                        <span class="text-sm text-dark">{{ step.description }}</span>
                      </div>
                      <div class="flex items-center gap-4 text-xs">
                        <span class="flex items-center gap-1" :class="step.success ? 'text-success' : 'text-danger'">
                          <i :class="step.success ? 'fa fa-check-circle' : 'fa fa-times-circle'"></i>
                          {{ step.success ? '成功' : '失败' }}
                        </span>
                        <span class="text-light">耗时：{{ step.duration || 0 }}ms</span>
                        <span v-if="step.error" class="text-danger">错误：{{ step.error }}</span>
                      </div>
                    </div>
                  </div>

                  <!-- 每一步的截图 -->
                  <div v-if="step.screenshot" class="mt-3">
                    <div
                      class="inline-block cursor-pointer border border-borderColor rounded overflow-hidden hover:border-primary transition"
                      @click="previewScreenshot(step.screenshot)"
                    >
                      <img
                        :src="step.screenshot"
                        :alt="`Step ${index + 1} 截图`"
                        class="w-32 h-24 object-cover"
                        @error="handleImageError"
                        @load="handleImageLoad"
                      >
                    </div>
                    <p class="text-xs text-light mt-1">点击放大</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 生成的脚本 -->
            <div v-if="currentDetail.generatedScript">
              <h4 class="text-base font-semibold mb-3">生成的脚本</h4>
              <div class="bg-neutral/50 rounded-lg p-4 border border-borderColor">
                <div class="flex items-center justify-between mb-2">
                  <button
                    @click="toggleScriptExpand"
                    class="text-xs text-primary hover:underline flex items-center gap-1"
                  >
                    <i :class="scriptExpanded ? 'fa fa-angle-down' : 'fa fa-angle-right'"></i>
                    {{ scriptExpanded ? '收起' : '展开' }}
                  </button>
                  <button
                    @click="copyScript"
                    class="text-xs text-primary hover:underline flex items-center gap-1"
                  >
                    <i class="fa fa-copy"></i>
                    复制
                  </button>
                </div>
                <div
                  v-show="scriptExpanded"
                  class="bg-dark text-success text-xs p-3 rounded font-mono whitespace-pre-wrap overflow-x-auto"
                >
                  {{ currentDetail.generatedScript }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部按钮 -->
        <div class="flex justify-end gap-3 px-6 py-4 border-t border-borderColor bg-neutral/30">
          <button @click="showDetailModal = false" class="btn-outline">返回</button>
          <button @click="deleteCurrentRecord" class="bg-danger hover:bg-red-600 text-white px-4 py-2 rounded-lg text-sm">删除记录</button>
        </div>
      </div>
    </div>

    <!-- 截图预览弹窗 -->
    <div v-if="showScreenshotModal" class="fixed inset-0 bg-black/80 flex items-center justify-center z-[100] p-4" @click.self="showScreenshotModal = false">
      <div class="relative max-w-5xl max-h-[90vh] bg-white rounded-lg shadow-2xl">
        <img
          :src="previewScreenshotUrl"
          alt="截图预览"
          class="max-w-full max-h-[90vh] object-contain rounded-lg"
          style="background: repeating-conic-gradient(#f0f0f0 0% 25%, #fff 0% 50%) 50% / 20px 20px;"
        >
        <button
          @click="showScreenshotModal = false"
          class="absolute top-4 right-4 text-gray-600 text-2xl hover:text-danger bg-white rounded-full w-10 h-10 flex items-center justify-center shadow-lg hover:shadow-xl transition"
        >
          <i class="fa fa-times"></i>
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTestRecords, getTestRecordDetail, deleteTestRecord, getTestRecordStatistics } from '@/api/testrecord'
import MessageBox from '@/utils/messageBox'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 筛选条件
const filters = ref({
  projectId: '',
  status: '',
  executorId: '',
  search: ''
})

// 项目列表
const projects = ref([])

// 获取项目列表
const fetchProjects = async () => {
  try {
    const { getProjects } = await import('@/api/project')
    const data = await getProjects()
    projects.value = data || []
  } catch (error) {
    console.error('获取项目列表失败:', error)
    projects.value = []
  }
}

// 统计数据
const statistics = ref({
  total: 0,
  success: 0,
  failed: 0,
  skipped: 0
})

// 执行人列表（模拟数据）
const executors = ref([
  { id: 1, name: '系统管理员' },
  { id: 2, name: '测试工程师A' },
  { id: 3, name: '测试工程师B' }
])

// 详情弹窗
const showDetailModal = ref(false)
const detailLoading = ref(false)
const currentDetail = ref({
  uniqueId: null,
  caseNumber: '',
  caseName: '',
  executionUrl: '',
  status: '',
  duration: null,
  executedBy: null,
  executorName: '',
  createdTime: null,
  generatedScript: '',
  stepsResult: [],
  errorMessage: ''
})

// 脚本展开状态
const scriptExpanded = ref(false)

// 截图预览
const showScreenshotModal = ref(false)
const previewScreenshotUrl = ref('')

// 总页数
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 获取测试记录列表
const fetchRecords = async () => {
  loading.value = true
  try {
    const params = {
      projectId: filters.value.projectId || undefined,
      status: filters.value.status || undefined,
      executorId: filters.value.executorId || undefined,
      search: filters.value.search || undefined,
      page: currentPage.value,
      size: pageSize.value
    }

    const response = await getTestRecords(params)
    records.value = response.content || []
    total.value = response.totalElements || 0
  } catch (error) {
    console.error('获取测试记录失败:', error)
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const data = await getTestRecordStatistics()
    statistics.value = data || { total: 0, success: 0, failed: 0, skipped: 0 }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchRecords()
}

// 分页处理
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    fetchRecords()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    fetchRecords()
  }
}

// 查看详情
const viewDetail = async (record) => {
  detailLoading.value = true
  showDetailModal.value = true
  scriptExpanded.value = false

  try {
    const data = await getTestRecordDetail(record.uniqueId)
    currentDetail.value = {
      uniqueId: data.uniqueId,
      caseNumber: data.caseNumber || '',
      caseName: data.caseName || '',
      executionUrl: data.executionUrl || '',
      status: data.status || '',
      duration: data.duration,
      executedBy: data.executedBy,
      executorName: data.executorName || '',
      createdTime: data.createdTime,
      generatedScript: data.generatedScript || '',
      stepsResult: data.stepsResult || [],
      errorMessage: data.errorMessage || ''
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    MessageBox.error('获取详情失败')
    showDetailModal.value = false
  } finally {
    detailLoading.value = false
  }
}

// 删除记录
const deleteRecord = async (record) => {
  try {
    const result = await MessageBox.confirm(`确定要删除测试记录"${record.caseName}"吗？`, '确认删除', {
      type: 'warning',
      confirmButtonType: 'danger'
    })

    if (result === 'cancel') return

    try {
      await deleteTestRecord(record.uniqueId)
      MessageBox.success('删除成功')
      // 刷新列表
      await fetchRecords()
      await fetchStatistics()
    } catch (error) {
      console.error('删除失败:', error)
      MessageBox.error('删除失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 删除当前详情记录
const deleteCurrentRecord = async () => {
  try {
    await deleteTestRecord(currentDetail.value.uniqueId)
    MessageBox.success('删除成功')
    showDetailModal.value = false
    // 刷新列表
    await fetchRecords()
    await fetchStatistics()
  } catch (error) {
    console.error('删除失败:', error)
    MessageBox.error('删除失败')
  }
}

// 获取状态样式
const getStatusClass = (status) => {
  const classMap = {
    SUCCESS: 'bg-success/20 text-success',
    FAILED: 'bg-danger/20 text-danger',
    SKIPPED: 'bg-info/20 text-info'
  }
  return classMap[status] || 'bg-gray-100 text-gray-600'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    SUCCESS: '成功',
    FAILED: '失败',
    SKIPPED: '跳过'
  }
  return textMap[status] || status
}

// 格式化耗时
const formatDuration = (duration) => {
  if (!duration) return '-'
  if (duration < 1000) return `${duration}ms`
  return `${(duration / 1000).toFixed(2)}s`
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 切换脚本展开/收起
const toggleScriptExpand = () => {
  scriptExpanded.value = !scriptExpanded.value
}

// 复制脚本
const copyScript = () => {
  navigator.clipboard.writeText(currentDetail.value.generatedScript).then(() => {
    MessageBox.success('脚本已复制到剪贴板')
  }).catch(() => {
    MessageBox.error('复制失败')
  })
}

// 预览截图
const previewScreenshot = (screenshot) => {
  console.log('预览截图:', screenshot.substring(0, 100)) // 输出前100个字符用于调试
  previewScreenshotUrl.value = screenshot
  showScreenshotModal.value = true
}

// 图片加载错误处理
const handleImageError = (event) => {
  console.error('图片加载失败:', event.target.src.substring(0, 100))
  event.target.style.display = 'none'
}

// 图片加载成功处理
const handleImageLoad = (event) => {
  console.log('图片加载成功')
}

onMounted(() => {
  fetchProjects()
  fetchRecords()
  fetchStatistics()
})
</script>
