<template>
  <section>
    <div class="mb-6">
      <h2 class="text-xl font-bold text-dark">脚本管理</h2>
    </div>

    <!-- 数据概览卡片 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded-lg p-4 border border-borderColor card-hover card-shadow">
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-light text-sm">脚本总数</h3>
          <i class="fa fa-file-code-o text-light hover:text-primary cursor-pointer"></i>
        </div>
        <div class="flex items-end gap-2">
          <span class="text-4xl font-bold text-dark">{{ statistics.total }}</span>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 border border-borderColor card-hover card-shadow">
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-light text-sm">已启用</h3>
          <div class="w-6 h-6 bg-success/20 rounded-full flex items-center justify-center text-success">
            <i class="fa fa-check"></i>
          </div>
        </div>
        <div class="flex items-end gap-2">
          <span class="text-4xl font-bold text-dark">{{ statistics.enabled }}</span>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 border border-borderColor card-hover card-shadow">
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-light text-sm">已禁用</h3>
          <div class="w-6 h-6 bg-warning/20 rounded-full flex items-center justify-center text-warning">
            <i class="fa fa-pause"></i>
          </div>
        </div>
        <div class="flex items-end gap-2">
          <span class="text-4xl font-bold text-dark">{{ statistics.disabled }}</span>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 border border-borderColor card-hover card-shadow">
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-light text-sm">AI 生成失败</h3>
          <div class="w-6 h-6 bg-danger/20 rounded-full flex items-center justify-center text-danger">
            <i class="fa fa-exclamation-triangle"></i>
          </div>
        </div>
        <div class="flex items-end gap-2">
          <span class="text-4xl font-bold text-dark">{{ statistics.failed }}</span>
        </div>
      </div>
    </div>

    <div class="bg-white p-6 rounded-lg card-shadow">
      <!-- 筛选栏 -->
      <div class="bg-neutral rounded-lg p-4 mb-6 flex flex-col md:flex-row gap-4 items-start md:items-center border border-borderColor">
        <div class="flex flex-wrap gap-3 flex-1">
          <select v-model="filters.enabled" @change="handleSearch" class="w-40 border border-borderColor bg-white text-dark rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary">
            <option value="">全部状态</option>
            <option value="true">已启用</option>
            <option value="false">已禁用</option>
          </select>
        </div>
        <div class="relative flex-shrink-0">
          <input
            v-model="filters.search"
            type="text"
            placeholder="搜索脚本名称..."
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

      <!-- 脚本表格 -->
      <div v-else class="bg-white rounded-lg border border-borderColor overflow-hidden card-shadow">
        <table class="w-full text-left">
          <thead>
            <tr class="border-b border-borderColor bg-neutral/50">
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">ID</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">脚本名称</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">状态</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">执行次数</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">最后执行</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredScripts.length === 0">
              <td colspan="6" class="px-4 py-10 text-center">
                <i class="fa fa-inbox text-4xl text-light mb-2"></i>
                <p class="text-light">暂无脚本数据</p>
              </td>
            </tr>
            <tr v-else v-for="script in filteredScripts" :key="script.uniqueId" class="border-b border-borderColor table-row-hover">
              <td class="px-4 py-4 text-sm whitespace-nowrap">#{{ script.uniqueId }}</td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                <div>
                  <div class="font-medium">{{ script.scriptName }}</div>
                  <div class="text-xs text-light truncate max-w-xs">{{ script.scriptDescription || '无描述' }}</div>
                </div>
              </td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                <div class="flex items-center gap-2">
                  <button
                    @click="toggleEnable(script)"
                    :class="['w-10 h-5 rounded-full relative transition-colors', script.enabled ? 'bg-primary' : 'bg-gray-300']"
                  >
                    <div :class="['w-4 h-4 bg-white rounded-full absolute top-0.5 transition-transform', script.enabled ? 'translate-x-5' : 'translate-x-0.5']"></div>
                  </button>
                  <span class="text-xs">{{ script.enabled ? '已启用' : '已禁用' }}</span>
                </div>
              </td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">{{ script.executionCount || 0 }}</td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">{{ formatDate(script.lastExecutionTime) }}</td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                <span class="flex items-center gap-3">
                  <button @click="editScript(script)" class="text-info hover:text-primary relative group">
                    <i class="fa fa-edit text-lg"></i>
                    <span class="absolute -top-8 left-1/2 -translate-x-1/2 bg-white border border-borderColor rounded px-2 py-1 text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 transition shadow-sm z-10">编辑</span>
                  </button>
                  <button @click="viewCode(script)" class="text-info hover:text-primary relative group">
                    <i class="fa fa-code text-lg"></i>
                    <span class="absolute -top-8 left-1/2 -translate-x-1/2 bg-white border border-borderColor rounded px-2 py-1 text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 transition shadow-sm z-10">查看代码</span>
                  </button>
                  <button @click="deleteScriptData(script)" class="text-danger hover:text-dangerRed relative group">
                    <i class="fa fa-trash text-lg"></i>
                    <span class="absolute -top-8 left-1/2 -translate-x-1/2 bg-white border border-borderColor rounded px-2 py-1 text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 transition shadow-sm z-10">删除</span>
                  </button>
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 编辑脚本弹窗 -->
    <Dialog
      v-model:visible="showEditDialog"
      title="编辑脚本"
      icon="fa-edit"
      :show-footer="true"
      confirm-button-text="保存"
      :loading="saving"
      @confirm="handleSaveScript"
      @cancel="showEditDialog = false"
    >
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium mb-1">脚本名称 <span class="text-red-500">*</span></label>
          <input
            v-model="editingScript.scriptName"
            type="text"
            class="w-full border border-borderColor rounded-lg px-3 py-2 focus:outline-none focus:border-primary"
            placeholder="请输入脚本名称"
          >
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">脚本描述</label>
          <textarea
            v-model="editingScript.scriptDescription"
            class="w-full border border-borderColor rounded-lg px-3 py-2 focus:outline-none focus:border-primary min-h-[80px]"
            placeholder="请输入脚本描述"
          ></textarea>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">分类</label>
          <input
            v-model="editingScript.category"
            type="text"
            class="w-full border border-borderColor rounded-lg px-3 py-2 focus:outline-none focus:border-primary"
            placeholder="请输入分类"
          >
        </div>
      </div>
    </Dialog>

    <!-- 查看代码弹窗 -->
    <Dialog
      v-model:visible="showCodeDialog"
      title="脚本代码"
      icon="fa-code"
      :show-footer="false"
    >
      <CodeViewer :code="viewingScript.scriptContent" />
    </Dialog>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getScripts, deleteScript, toggleScriptEnabled, updateScript } from '@/api/script'
import MessageBox from '@/utils/messageBox'
import Dialog from '@/components/Dialog/Dialog.vue'
import CodeViewer from '@/components/CodeViewer/CodeViewer.vue'

const loading = ref(false)
const scripts = ref([])

// 编辑弹窗
const showEditDialog = ref(false)
const editingScript = ref({
  uniqueId: null,
  scriptName: '',
  scriptDescription: '',
  category: ''
})
const saving = ref(false)

// 查看代码弹窗
const showCodeDialog = ref(false)
const viewingScript = ref({
  scriptContent: ''
})

const filters = ref({
  enabled: '',
  search: ''
})

// 统计数据
const statistics = computed(() => {
  return {
    total: scripts.value.length,
    enabled: scripts.value.filter(s => s.enabled).length,
    disabled: scripts.value.filter(s => !s.enabled).length,
    failed: scripts.value.filter(s => s.aiGenerationStatus === 'FAILED').length
  }
})

// 过滤后的脚本
const filteredScripts = computed(() => {
  let result = scripts.value

  if (filters.value.enabled !== '') {
    const isEnabled = filters.value.enabled === 'true'
    result = result.filter(s => s.enabled === isEnabled)
  }

  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    result = result.filter(s =>
      s.scriptName.toLowerCase().includes(search)
    )
  }

  return result
})

// 获取脚本数据
const fetchScripts = async () => {
  loading.value = true
  try {
    const data = await getScripts()
    scripts.value = data || []
  } catch (error) {
    console.error('获取脚本失败:', error)
    scripts.value = []
  } finally {
    loading.value = false
  }
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

// 切换启用/禁用
const toggleEnable = async (script) => {
  try {
    await toggleScriptEnabled(script.uniqueId)
    MessageBox.success('状态切换成功')
    // 刷新列表
    await fetchScripts()
  } catch (error) {
    console.error('切换状态失败:', error)
    MessageBox.error('切换状态失败')
  }
}

// 编辑脚本
const editScript = (script) => {
  editingScript.value = {
    uniqueId: script.uniqueId,
    scriptName: script.scriptName,
    scriptDescription: script.scriptDescription || '',
    category: script.category || ''
  }
  showEditDialog.value = true
}

// 查看代码
const viewCode = (script) => {
  viewingScript.value = {
    scriptContent: script.scriptContent || '暂无代码'
  }
  showCodeDialog.value = true
}

// 保存脚本
const handleSaveScript = async () => {
  // 验证
  if (!editingScript.value.scriptName.trim()) {
    MessageBox.warning('请输入脚本名称')
    return
  }

  saving.value = true
  try {
    await updateScript(editingScript.value.uniqueId, {
      uniqueId: editingScript.value.uniqueId,
      scriptName: editingScript.value.scriptName,
      scriptDescription: editingScript.value.scriptDescription,
      category: editingScript.value.category
    })

    MessageBox.success('保存成功')
    showEditDialog.value = false
    // 刷新列表
    await fetchScripts()
  } catch (error) {
    console.error('保存失败:', error)
    MessageBox.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 删除脚本
const deleteScriptData = async (script) => {
  try {
    const result = await MessageBox.confirm(`确定要删除脚本"${script.scriptName}"吗？`, '确认删除', {
      type: 'warning',
      confirmButtonType: 'danger'
    })

    // 如果用户取消，result 为 'cancel'，不执行删除
    if (result === 'cancel') return

    try {
      await deleteScript(script.uniqueId)
      MessageBox.success('删除成功')
      // 刷新列表
      await fetchScripts()
    } catch (error) {
      console.error('删除失败:', error)
      MessageBox.error('删除失败')
    }
  } catch (error) {
    // 其他错误
    console.error('操作失败:', error)
  }
}

// 搜索处理
const handleSearch = () => {
  // 搜索由 computed 自动处理
}

onMounted(() => {
  fetchScripts()
})
</script>
