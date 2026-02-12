<template>
  <section>
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h2 class="text-xl font-bold text-dark">用例列表</h2>
        <p v-if="currentProjectName" class="text-sm text-light mt-1">
          项目：{{ currentProjectName }}
        </p>
      </div>
      <div class="flex gap-2">
        <div class="flex items-center border border-borderColor rounded-md">
          <button :class="['px-3 py-1.5 rounded-l-md text-sm flex items-center gap-1', mode === 'smart' ? 'bg-primary text-white' : 'bg-white text-light']" @click="mode = 'smart'">
            <i class="fa fa-bolt"></i>智能
          </button>
          <button :class="['px-3 py-1.5 rounded-r-md text-sm', mode === 'script' ? 'bg-primary text-white' : 'bg-white text-light']" @click="mode = 'script'">
            脚本模式
          </button>
        </div>
        <button class="btn-primary text-sm flex items-center gap-1" @click="executeAll">
          <i class="fa fa-play"></i> 执行全部
        </button>
        <button class="btn-outline text-sm flex items-center gap-1" @click="downloadTemplate">
          <i class="fa fa-download"></i> 下载模板
        </button>
        <button class="btn-primary text-sm flex items-center gap-1" @click="importCases">
          <i class="fa fa-upload"></i> 导入用例
        </button>
        <button class="btn-primary text-sm flex items-center gap-1" @click="openCreateModal">
          <i class="fa fa-plus"></i> 新建用例
        </button>
      </div>
    </div>

    <!-- 数据概览卡片 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded-lg p-4 border border-borderColor card-hover card-shadow">
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-light text-sm">用例总数</h3>
          <i class="fa fa-file-text-o text-light hover:text-primary cursor-pointer"></i>
        </div>
        <div class="flex items-end gap-2">
          <span class="text-4xl font-bold text-dark">{{ statistics.total }}</span>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 border border-borderColor card-hover card-shadow">
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-light text-sm">已通过</h3>
          <div class="w-6 h-6 bg-success/20 rounded-full flex items-center justify-center text-success">
            <i class="fa fa-check"></i>
          </div>
        </div>
        <div class="flex items-end gap-2">
          <span class="text-4xl font-bold text-dark">{{ statistics.passed }}</span>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 border border-borderColor card-hover card-shadow">
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-light text-sm">未通过</h3>
          <div class="w-6 h-6 bg-danger/20 rounded-full flex items-center justify-center text-danger">
            <i class="fa fa-times"></i>
          </div>
        </div>
        <div class="flex items-end gap-2">
          <span class="text-4xl font-bold text-dark">{{ statistics.failed }}</span>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 border border-borderColor card-hover card-shadow">
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-light text-sm">未执行</h3>
          <div class="w-6 h-6 bg-info/20 rounded-full flex items-center justify-center text-info">
            <i class="fa fa-clock-o"></i>
          </div>
        </div>
        <div class="flex items-end gap-2">
          <span class="text-4xl font-bold text-dark">{{ statistics.notExecuted }}</span>
        </div>
      </div>
    </div>

    <div class="bg-white p-6 rounded-lg card-shadow">
      <!-- 筛选栏 -->
      <div class="bg-neutral rounded-lg p-4 mb-6 flex flex-col md:flex-row gap-4 items-start md:items-center border border-borderColor">
        <div class="flex flex-wrap gap-3 flex-1">
          <select v-model="filters.status" class="w-40 border border-borderColor bg-white text-dark rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary">
            <option value="">全部状态</option>
            <option value="NOT_EXECUTED">未执行</option>
            <option value="PASSED">通过</option>
            <option value="FAILED">未通过</option>
          </select>
          <select v-model="filters.executor" class="w-40 border border-borderColor bg-white text-dark rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary">
            <option value="">全部执行人</option>
            <option v-for="user in executors" :key="user.id" :value="user.id">{{ user.name }}</option>
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

      <!-- 空数据状态 -->
      <div v-else-if="filteredTestCases.length === 0" class="text-center py-10">
        <i class="fa fa-inbox text-6xl text-light"></i>
        <p class="text-light mt-4">暂无测试用例数据</p>
      </div>

      <!-- 用例表格 -->
      <div v-else class="bg-white rounded-lg border border-borderColor overflow-hidden card-shadow">
        <table class="w-full text-left">
          <thead>
            <tr class="border-b border-borderColor bg-neutral/50">
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">用例编号</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">用例名称</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">状态</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">执行人</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">最后执行时间</th>
              <th class="px-4 py-3 text-light text-sm font-medium whitespace-nowrap">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="testCase in filteredTestCases" :key="testCase.uniqueId" class="border-b border-borderColor table-row-hover">
              <td class="px-4 py-4 text-sm whitespace-nowrap">{{ testCase.caseNumber || '-' }}</td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                <div>
                  <div class="font-medium">{{ testCase.name }}</div>
                  <div class="text-xs text-light truncate max-w-xs">{{ testCase.description }}</div>
                </div>
              </td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                <span class="px-2 py-1 rounded text-xs" :class="getStatusClass(testCase.status)">{{ getStatusText(testCase.status) }}</span>
              </td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                <span v-if="testCase.executedBy">{{ getExecutorName(testCase.executedBy) }}</span>
                <span v-else class="text-light">-</span>
              </td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">{{ formatDate(testCase.executionTime) }}</td>
              <td class="px-4 py-4 text-sm whitespace-nowrap">
                <span class="flex items-center gap-3">
                  <button @click="executeCase(testCase)" class="text-info hover:text-primary relative group">
                    <i class="fa fa-play text-lg"></i>
                    <span class="absolute -top-8 left-1/2 -translate-x-1/2 bg-white border border-borderColor rounded px-2 py-1 text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 transition shadow-sm z-10">执行</span>
                  </button>
                  <button @click="viewDetail(testCase)" class="text-info hover:text-primary relative group">
                    <i class="fa fa-eye text-lg"></i>
                    <span class="absolute -top-8 left-1/2 -translate-x-1/2 bg-white border border-borderColor rounded px-2 py-1 text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 transition shadow-sm z-10">查看详情</span>
                  </button>
                  <button @click="deleteCase(testCase)" class="text-danger hover:text-dangerRed relative group">
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

    <!-- 导入用例弹窗 -->
    <div v-if="showImportModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="closeImportModal">
      <div class="bg-white rounded-lg w-full max-w-md p-6 transform transition-all">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-bold">导入用例</h3>
          <button @click="closeImportModal" class="text-light hover:text-dark transition-colors">
            <i class="fa fa-times text-lg"></i>
          </button>
        </div>

        <div class="space-y-4">
          <!-- 项目信息显示 -->
          <div v-if="currentProjectName">
            <label class="block text-sm font-medium mb-2">导入到项目</label>
            <div class="w-full border border-borderColor bg-neutral/30 text-dark rounded-lg px-3 py-2 text-sm">
              {{ currentProjectName }}
            </div>
          </div>

          <!-- 文件上传区域 -->
          <div>
            <label class="block text-sm font-medium mb-2">上传Excel文件</label>
            <div
              class="border-2 border-dashed rounded-lg p-6 text-center transition-colors cursor-pointer"
              :class="dragOver ? 'border-primary bg-primary/5' : 'border-borderColor hover:border-primary'"
              @click="triggerFileInput"
              @dragover.prevent="dragOver = true"
              @dragleave.prevent="dragOver = false"
              @drop.prevent="handleDrop"
            >
              <input
                ref="fileInput"
                type="file"
                accept=".xlsx,.xls"
                class="hidden"
                @change="handleFileChange"
              >
              <div class="space-y-2">
                <i class="fa fa-cloud-upload text-4xl text-primary"></i>
                <p class="text-sm text-dark">点击上传或将文件拖拽到此处</p>
                <p class="text-xs text-light">支持 .xlsx 和 .xls 格式，单个文件不超过 10MB</p>
              </div>
            </div>
          </div>

          <!-- 已选文件显示 -->
          <div v-if="fileList.length > 0" class="border border-borderColor rounded-lg">
            <div
              class="flex items-center justify-between px-4 py-3"
            >
              <div class="flex items-center gap-3 flex-1 min-w-0">
                <i class="fa fa-file-excel-o text-success text-xl flex-shrink-0"></i>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium truncate">{{ fileList[0].name }}</p>
                  <p class="text-xs text-light">{{ formatFileSize(fileList[0].size) }}</p>
                </div>
              </div>
              <button
                @click="removeFile"
                class="text-danger hover:text-dangerRed p-1 flex-shrink-0"
                title="删除"
              >
                <i class="fa fa-times"></i>
              </button>
            </div>
          </div>

          <!-- 导入进度条 -->
          <div v-if="importing" class="mt-4">
            <div class="flex items-center justify-between mb-2">
              <span class="text-sm text-light">正在导入中...</span>
              <span class="text-sm font-medium text-primary">{{ importProgress }}%</span>
            </div>
            <div class="w-full bg-neutral rounded-full h-2 overflow-hidden">
              <div
                class="bg-primary h-2 transition-all duration-300 ease-out"
                :style="{ width: importProgress + '%' }"
              ></div>
            </div>
            <p class="text-xs text-light mt-2">{{ currentImportStep }}</p>
          </div>
        </div>

        <div class="flex justify-end gap-3 mt-6">
          <button @click="closeImportModal" class="btn-outline">取消</button>
          <button
            @click="handleImport"
            :disabled="importing"
            class="btn-primary"
          >
            <i v-if="importing" class="fa fa-spinner fa-spin mr-1"></i>
            {{ importing ? '导入中...' : '开始导入' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 新建用例弹窗 -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" @click.self="closeCreateModal">
      <div class="bg-white rounded-lg w-full max-w-7xl max-h-[90vh] overflow-hidden shadow-xl" @click.stop>
        <!-- 标题栏 -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-borderColor">
          <h3 class="text-lg font-bold">新建用例</h3>
          <button @click="closeCreateModal" class="text-light hover:text-dark transition-colors">
            <i class="fa fa-times text-lg"></i>
          </button>
        </div>

        <!-- 内容区域 -->
        <div class="p-6 overflow-y-auto max-h-[calc(90vh-140px)]">
          <!-- 用例表格 -->
          <div class="border border-borderColor rounded-lg overflow-visible">
            <table class="w-full text-left table-fixed">
              <thead class="bg-neutral/50">
                <tr>
                  <th class="px-3 py-2 text-xs font-medium text-light border-r border-borderColor w-40">用例编号*</th>
                  <th class="px-3 py-2 text-xs font-medium text-light border-r border-borderColor w-40">用例名称*</th>
                  <th class="px-3 py-2 text-xs font-medium text-light border-r border-borderColor w-40">用例描述</th>
                  <th class="px-3 py-2 text-xs font-medium text-light border-r border-borderColor w-44">前置条件</th>
                  <th class="px-3 py-2 text-xs font-medium text-light border-r border-borderColor w-64">测试步骤</th>
                  <th class="px-3 py-2 text-xs font-medium text-light border-r border-borderColor w-40">预期结果</th>
                  <th class="px-3 py-2 text-xs font-medium text-light w-20">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, index) in createFormRows" :key="index" class="border-t border-borderColor">
                  <td class="px-2 py-2 border-r border-borderColor">
                    <input
                      v-model="row.caseNumber"
                      type="text"
                      class="w-full px-2 py-1.5 border border-borderColor rounded-none text-sm focus:outline-none focus:border-primary h-10 box-border"
                      placeholder="用例编号"
                      :class="{ 'border-red-500': !row.caseNumber }"
                    >
                  </td>
                  <td class="px-2 py-2 border-r border-borderColor">
                    <input
                      v-model="row.name"
                      type="text"
                      class="w-full px-2 py-1.5 border border-borderColor rounded-none text-sm focus:outline-none focus:border-primary h-10 box-border"
                      placeholder="用例名称"
                      :class="{ 'border-red-500': !row.name }"
                    >
                  </td>
                  <td class="px-2 py-2 border-r border-borderColor">
                    <textarea
                      v-model="row.description"
                      class="w-full px-2 py-1.5 border border-borderColor rounded-none text-sm focus:outline-none focus:border-primary resize-none overflow-y-auto"
                      style="height: 40px; box-sizing: border-box; display: block;"
                      placeholder="用例描述"
                    ></textarea>
                  </td>
                  <td class="px-2 py-2 border-r border-borderColor relative">
                    <div class="relative prerequisite-dropdown">
                      <button
                        :ref="(el) => { if (el) prerequisiteButtons[index] = el }"
                        @click.stop="togglePrerequisiteDropdown(index)"
                        class="w-full px-2 py-1.5 border border-borderColor rounded-none text-sm focus:outline-none focus:border-primary bg-white flex items-center justify-between"
                        style="height: 40px; box-sizing: border-box;"
                        :class="{ 'border-primary': row.showPrerequisiteDropdown }"
                      >
                        <span v-if="row.prerequisite" class="text-dark truncate flex-1 text-left">
                          {{ row.prerequisite.caseNumber || '-' }}
                        </span>
                        <span v-else-if="row.prerequisite === null" class="text-dark truncate flex-1 text-left">
                          无
                        </span>
                        <span v-else class="text-light truncate flex-1 text-left">选择前置用例</span>
                        <i class="fa fa-caret-down text-light ml-1 flex-shrink-0"></i>
                      </button>
                      <teleport to="body">
                        <div
                          v-if="row.showPrerequisiteDropdown"
                          :style="getDropdownStyle(index)"
                          class="fixed bg-white border border-borderColor rounded-none shadow-lg max-h-48 overflow-y-auto"
                        >
                          <div
                            class="px-2 py-1.5 hover:bg-neutral/50 cursor-pointer flex items-center gap-2 whitespace-nowrap"
                            @click="selectPrerequisite(index, null)"
                          >
                            <input
                              type="radio"
                              :name="`prerequisite-${index}`"
                              :checked="!row.prerequisite"
                              class="rounded-none border-borderColor text-primary focus:ring-primary flex-shrink-0"
                              @click.stop
                            >
                            <span class="text-sm">无</span>
                          </div>
                          <div
                            v-for="testCase in availableTestCases"
                            :key="testCase.uniqueId"
                            class="px-2 py-1.5 hover:bg-neutral/50 cursor-pointer flex items-center gap-2 whitespace-nowrap"
                            @click="selectPrerequisite(index, testCase)"
                          >
                            <input
                              type="radio"
                              :name="`prerequisite-${index}`"
                              :checked="row.prerequisite && row.prerequisite.uniqueId === testCase.uniqueId"
                              class="rounded-none border-borderColor text-primary focus:ring-primary flex-shrink-0"
                              @click.stop
                            >
                            <span class="text-sm truncate">{{ testCase.caseNumber || '-' }} - {{ testCase.name }}</span>
                          </div>
                          <div v-if="availableTestCases.length === 0" class="px-2 py-1.5 text-sm text-light">
                            暂无可选前置用例
                          </div>
                        </div>
                      </teleport>
                    </div>
                  </td>
                  <td class="px-2 py-2 border-r border-borderColor">
                    <textarea
                      v-model="row.stepsText"
                      class="w-full px-2 py-1.5 border border-borderColor rounded-none text-sm focus:outline-none focus:border-primary resize-none overflow-y-auto"
                      style="height: 40px; box-sizing: border-box; display: block;"
                      placeholder="测试步骤"
                    ></textarea>
                  </td>
                  <td class="px-2 py-2 border-r border-borderColor">
                    <textarea
                      v-model="row.expectedResult"
                      class="w-full px-2 py-1.5 border border-borderColor rounded-none text-sm focus:outline-none focus:border-primary resize-none overflow-y-auto"
                      style="height: 40px; box-sizing: border-box; display: block;"
                      placeholder="预期结果"
                    ></textarea>
                  </td>
                  <td class="px-2 py-2 text-center">
                    <button
                      @click="removeCreateRow(index)"
                      :disabled="createFormRows.length === 1"
                      class="text-danger hover:text-dangerRed disabled:text-light disabled:cursor-not-allowed"
                      title="删除"
                    >
                      <i class="fa fa-trash"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 添加行按钮 -->
          <div class="mt-3">
            <button
              @click="addCreateRow"
              class="text-primary hover:text-primaryDark text-sm flex items-center gap-1"
            >
              <i class="fa fa-plus-circle"></i> 添加一行
            </button>
          </div>

          <!-- 提示信息 -->
          <div class="mt-4 p-3 bg-info/10 border border-info/30 rounded-lg">
            <p class="text-xs text-info">
              <i class="fa fa-info-circle mr-1"></i>
              标有 * 的字段为必填项。用例编号和用例名称为必填字段。
            </p>
          </div>
        </div>

        <!-- 底部按钮 -->
        <div class="flex justify-end gap-3 px-6 py-4 border-t border-borderColor bg-neutral/30">
          <button @click="closeCreateModal" class="btn-outline">取消</button>
          <button
            @click="handleCreate"
            :disabled="creating"
            class="btn-primary"
          >
            <i v-if="creating" class="fa fa-spinner fa-spin mr-1"></i>
            {{ creating ? '创建中...' : '创建用例' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 查看详情弹窗 -->
    <div v-if="showDetailModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" @click.self="showDetailModal = false">
      <div class="bg-white rounded-lg w-full max-w-3xl max-h-[90vh] overflow-hidden shadow-xl" @click.stop>
        <!-- 标题栏 -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-borderColor">
          <h3 class="text-lg font-bold">测试用例详情</h3>
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
                <span class="text-sm font-medium text-light">用例名称：</span>
                <span class="text-sm font-semibold">{{ currentDetail.name }}</span>
                <span class="px-2 py-1 rounded text-xs" :class="getStatusClass(currentDetail.status)">
                  {{ getStatusText(currentDetail.status) }}
                </span>
              </div>

              <div v-if="currentDetail.description" class="flex gap-2">
                <span class="text-sm font-medium text-light whitespace-nowrap">用例描述：</span>
                <span class="text-sm text-dark flex-1">{{ currentDetail.description }}</span>
              </div>
            </div>

            <hr class="border-borderColor">

            <!-- 测试步骤 -->
            <div>
              <h4 class="text-base font-semibold mb-3">测试步骤</h4>
              <div class="bg-neutral/50 rounded-lg p-4">
                <div v-if="currentDetail.stepsText" class="text-sm text-dark leading-relaxed whitespace-pre-wrap font-mono">
                  {{ currentDetail.stepsText }}
                </div>
                <div v-else class="text-sm text-light italic">暂无测试步骤</div>
              </div>
            </div>

            <!-- 预期结果 -->
            <div v-if="currentDetail.expectedResult">
              <h4 class="text-base font-semibold mb-3">预期结果</h4>
              <div class="bg-success/5 border border-success/20 rounded-lg p-4">
                <div class="text-sm text-dark leading-relaxed">
                  {{ currentDetail.expectedResult }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部按钮 -->
        <div class="flex justify-end px-6 py-4 border-t border-borderColor bg-neutral/30">
          <button @click="showDetailModal = false" class="btn-primary">关闭</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { getTestCases, deleteTestCase, executeTestCase, executeAllTestCases, importTestCases, getTestCaseDetail, getTestCasesGroupedByProject, createTestCase } from '@/api/testcase'
import { getProjects } from '@/api/project'
import { getExecutors } from '@/api/auth'
import MessageBox from '@/utils/messageBox'

const route = useRoute()
const mode = ref('smart')
const loading = ref(false)
const testCases = ref([])
const projects = ref([])

// 当前项目信息
const currentProjectId = ref(null)
const currentProjectName = ref('')

// 导入相关
const showImportModal = ref(false)
const importing = ref(false)
const importProgress = ref(0)
const currentImportStep = ref('')
const fileList = ref([])
const dragOver = ref(false)
const fileInput = ref(null)
const selectedProjectId = ref('')

// 详情弹窗
const showDetailModal = ref(false)
const detailLoading = ref(false)
const currentDetail = ref({
  name: '',
  description: '',
  stepsText: '',
  expectedResult: '',
  priority: '',
  status: ''
})

// 新建用例弹窗
const showCreateModal = ref(false)
const creating = ref(false)
const createFormRows = ref([])
const prerequisiteButtons = ref([])

// 计算下拉列表位置
const getDropdownStyle = (rowIndex) => {
  const button = prerequisiteButtons.value?.[rowIndex]
  if (!button) {
    return {}
  }

  const rect = button.getBoundingClientRect()
  // 固定宽度为按钮宽度，防止下拉框宽度变化
  return {
    position: 'fixed',
    top: `${rect.bottom + 4}px`,
    left: `${rect.left}px`,
    width: `${rect.width}px`,
    minWidth: `${rect.width}px`,
    maxWidth: `${rect.width}px`,
    zIndex: 99999
  }
}

// 支持的文件类型
const fileTypes = ['xlsx', 'xls']
const maxSize = 10 * 1024 * 1024 // 10MB

// 执行人列表
const executors = ref([])

// 加载执行人列表
const loadExecutors = async () => {
  try {
    const data = await getExecutors()
    executors.value = data.map(user => ({
      id: user.uniqueId,
      name: user.realName
    }))
  } catch (error) {
    console.error('加载执行人列表失败:', error)
  }
}

const filters = ref({
  status: '',
  executor: '',
  search: ''
})

// 统计数据
const statistics = computed(() => {
  return {
    total: testCases.value.length,
    passed: testCases.value.filter(tc => tc.status === 'PASSED').length,
    failed: testCases.value.filter(tc => tc.status === 'FAILED').length,
    notExecuted: testCases.value.filter(tc => tc.status === 'NOT_EXECUTED').length
  }
})

// 过滤后的测试用例
const filteredTestCases = computed(() => {
  let result = testCases.value

  // 根据当前项目ID过滤
  if (currentProjectId.value) {
    result = result.filter(tc => {
      const projectId = getProjectId(tc)
      return projectId === currentProjectId.value
    })
  }

  if (filters.value.status) {
    result = result.filter(tc => tc.status === filters.value.status)
  }

  if (filters.value.executor) {
    result = result.filter(tc => tc.executedBy === parseInt(filters.value.executor))
  }

  if (filters.value.search) {
    const search = filters.value.search.toLowerCase().trim()
    result = result.filter(tc => {
      // 搜索用例名称（不区分大小写）
      const nameMatch = tc.name && tc.name.toLowerCase().includes(search)
      // 搜索用例编号（不区分大小写）
      const caseNumberMatch = tc.caseNumber && tc.caseNumber.toLowerCase().includes(search)
      // 搜索唯一ID（精确匹配）
      const idMatch = tc.uniqueId && tc.uniqueId.toString() === search

      return nameMatch || caseNumberMatch || idMatch
    })
  }

  return result
})

// 可选的前置用例列表（当前项目的用例）
const availableTestCases = computed(() => {
  if (!currentProjectId.value) {
    return []
  }
  // 返回当前项目的所有用例，按创建时间倒序
  return testCases.value
    .filter(tc => {
      const projectId = getProjectId(tc)
      return projectId === currentProjectId.value
    })
    .sort((a, b) => {
      const timeA = new Date(a.createdTime || 0).getTime()
      const timeB = new Date(b.createdTime || 0).getTime()
      return timeB - timeA
    })
})

// 切换前置条件下拉框显示
const togglePrerequisiteDropdown = (rowIndex) => {
  const row = createFormRows.value[rowIndex]
  // 关闭其他行的所有下拉框
  createFormRows.value.forEach((r, i) => {
    if (i !== rowIndex) {
      r.showPrerequisiteDropdown = false
    }
  })
  // 切换当前行的下拉框
  row.showPrerequisiteDropdown = !row.showPrerequisiteDropdown
}

// 选择前置条件（单选，包含"无"选项）
const selectPrerequisite = (rowIndex, testCase) => {
  const row = createFormRows.value[rowIndex]
  if (testCase === null) {
    // 选择"无"
    row.prerequisite = null
  } else {
    // 选择具体用例
    if (row.prerequisite && row.prerequisite.uniqueId === testCase.uniqueId) {
      // 点击已选中的，切换为"无"
      row.prerequisite = null
    } else {
      row.prerequisite = {
        uniqueId: testCase.uniqueId,
        caseNumber: testCase.caseNumber,
        name: testCase.name
      }
    }
  }
  // 关闭下拉框
  row.showPrerequisiteDropdown = false
}

// 获取项目ID
const getProjectId = (testCase) => {
  if (testCase.projects && testCase.projects.length > 0) {
    return testCase.projects[0].uniqueId
  }
  return null
}

// 初始化当前项目信息
const initCurrentProject = async () => {
  // 从URL参数获取项目ID
  const projectIdFromUrl = route.query.projectId
  if (projectIdFromUrl) {
    currentProjectId.value = parseInt(projectIdFromUrl)
  }

  // 获取项目列表以找到项目名称
  await fetchProjects()

  // 如果有项目ID，查找并设置项目名称
  if (currentProjectId.value) {
    const project = projects.value.find(p => p.uniqueId === currentProjectId.value)
    if (project) {
      currentProjectName.value = project.name
    }
  }
}

// 获取测试用例数据（按项目分组）
const fetchTestCases = async () => {
  loading.value = true
  try {
    const data = await getTestCasesGroupedByProject()
    console.log('前端接收到的数据:', data)
    console.log('数据类型:', typeof data)
    console.log('是否为数组:', Array.isArray(data))
    console.log('数据长度:', data?.length)
    testCases.value = data || []
  } catch (error) {
    console.error('获取测试用例失败:', error)
    testCases.value = []
  } finally {
    loading.value = false
  }
}

// 获取项目列表
const fetchProjects = async () => {
  try {
    const data = await getProjects()
    projects.value = data || []
  } catch (error) {
    console.error('获取项目列表失败:', error)
    projects.value = []
  }
}

// 获取状态样式
const getStatusClass = (status) => {
  const classMap = {
    NOT_EXECUTED: 'bg-info/20 text-info',
    PASSED: 'bg-success/20 text-success',
    FAILED: 'bg-danger/20 text-danger'
  }
  return classMap[status] || 'bg-gray-100 text-gray-600'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    NOT_EXECUTED: '未执行',
    PASSED: '通过',
    FAILED: '未通过'
  }
  return textMap[status] || status
}

// 获取执行人名称
const getExecutorName = (id) => {
  const executor = executors.value.find(e => e.id === id)
  return executor ? executor.name : `用户${id}`
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

// 执行全部
const executeAll = async () => {
  try {
    const result = await MessageBox.confirm('确定要执行所有测试用例吗？', '确认执行', {
      type: 'warning'
    })

    // 如果用户取消，不执行
    if (result === 'cancel') return

    await executeAllTestCases()
    MessageBox.success('已开始执行所有测试用例')
    // 刷新列表
    await fetchTestCases()
  } catch (error) {
    console.error('执行失败:', error)
    MessageBox.error('执行失败')
  }
}

// 导入用例
const importCases = async () => {
  // 使用当前项目ID
  selectedProjectId.value = currentProjectId.value
  fileList.value = []
  showImportModal.value = true
}

// 下载模板
const downloadTemplate = () => {
  // 创建一个隐藏的a标签来下载文件
  const link = document.createElement('a')
  link.href = '/api/file/download/test-case-template'
  link.download = '测试用例导入模板.xlsx'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 关闭导入弹窗
const closeImportModal = () => {
  showImportModal.value = false
  fileList.value = []
  selectedProjectId.value = ''
  dragOver.value = false
  importProgress.value = 0
  currentImportStep.value = ''
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value?.click()
}

// 处理文件选择
const handleFileChange = (event) => {
  const file = event.target.files?.[0]
  if (file) {
    processFile(file)
  }
  // 清空input，允许重复选择同一文件
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 处理拖拽上传
const handleDrop = (event) => {
  dragOver.value = false
  const file = event.dataTransfer?.files?.[0]
  if (file) {
    processFile(file)
  }
}

// 处理单个文件（验证并添加）
const processFile = (file) => {
  // 检查文件类型
  const fileType = file.name.split('.').pop()?.toLowerCase()
  const isSupported = fileTypes.length === 0 || fileTypes.includes(fileType)
  if (!isSupported) {
    MessageBox.warning(`文件类型不支持！仅支持 ${fileTypes.join(', ')} 格式`)
    return
  }

  // 检查文件大小
  const isLtMax = file.size < maxSize
  if (!isLtMax) {
    MessageBox.warning(`文件超过 ${maxSize / 1024 / 1024}MB 限制！`)
    return
  }

  // 验证通过，替换当前文件
  fileList.value = [file]
}

// 删除文件
const removeFile = () => {
  fileList.value = []
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 处理导入
const handleImport = async () => {
  // 验证项目选择
  if (!selectedProjectId.value) {
    MessageBox.warning('请先选择所属项目')
    return
  }

  if (fileList.value.length === 0) {
    MessageBox.warning('请选择要导入的文件')
    return
  }

  importing.value = true
  importProgress.value = 0
  currentImportStep.value = '正在准备导入...'

  // 模拟进度更新
  const progressSteps = [
    { progress: 10, step: '正在读取Excel文件...' },
    { progress: 20, step: '正在解析测试用例数据...' },
    { progress: 30, step: '正在调用AI解析测试步骤...' },
    { progress: 50, step: 'AI正在生成测试用例脚本...' },
    { progress: 70, step: '正在保存测试用例到数据库...' },
    { progress: 90, step: '正在处理前置条件关联...' },
    { progress: 95, step: '正在完成导入...' }
  ]

  let stepIndex = 0
  const progressInterval = setInterval(() => {
    if (stepIndex < progressSteps.length && importProgress.value < 95) {
      importProgress.value = progressSteps[stepIndex].progress
      currentImportStep.value = progressSteps[stepIndex].step
      stepIndex++
    }
  }, 2000) // 每2秒更新一次进度

  try {
    const result = await importTestCases(fileList.value[0], selectedProjectId.value)

    // 完成导入
    clearInterval(progressInterval)
    importProgress.value = 100
    currentImportStep.value = '导入完成！'

    setTimeout(() => {
      MessageBox.success(result || '导入成功！')
      closeImportModal()
      importProgress.value = 0
      currentImportStep.value = ''
    }, 500)

    // 刷新列表
    await fetchTestCases()
  } catch (error) {
    clearInterval(progressInterval)
    importProgress.value = 0
    currentImportStep.value = ''

    console.error('导入失败:', error)
    // 显示详细错误信息
    const errorMsg = error.response?.data?.message || error.message || '未知错误'
    MessageBox.error('导入失败：' + errorMsg)
  } finally {
    importing.value = false
  }
}

// 打开新建用例弹窗
const openCreateModal = () => {
  // 初始化一行空白表单
  createFormRows.value = [createEmptyRow()]
  showCreateModal.value = true
}

// 点击其他地方关闭下拉框
const handleClickOutside = (event) => {
  // 检查点击是否在下拉框内
  const inDropdown = event.target.closest('.prerequisite-dropdown')

  // 如果点击的不是下拉框内部，关闭所有下拉框
  if (!inDropdown) {
    createFormRows.value.forEach(row => {
      row.showPrerequisiteDropdown = false
    })
  }
}

// 监听全局点击事件
onMounted(async () => {
  await initCurrentProject()
  fetchTestCases()
  loadExecutors()
  // 添加全局点击监听
  document.addEventListener('click', handleClickOutside)
})

// 清理监听器
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

// 创建空白的表单行
const createEmptyRow = () => {
  return {
    caseNumber: '',
    name: '',
    description: '',
    prerequisite: null,  // null表示"无"，对象表示具体用例
    showPrerequisiteDropdown: false,
    stepsText: '',
    expectedResult: ''
  }
}

// 添加一行
const addCreateRow = () => {
  createFormRows.value.push(createEmptyRow())
}

// 删除一行
const removeCreateRow = (index) => {
  if (createFormRows.value.length > 1) {
    createFormRows.value.splice(index, 1)
  }
}

// 关闭新建弹窗
const closeCreateModal = () => {
  showCreateModal.value = false
  createFormRows.value = []
}

// 处理创建用例
const handleCreate = async () => {
  // 验证表单
  for (let i = 0; i < createFormRows.value.length; i++) {
    const row = createFormRows.value[i]
    if (!row.caseNumber || row.caseNumber.trim() === '') {
      MessageBox.warning(`第${i + 1}行：用例编号不能为空`)
      return
    }
    if (!row.name || row.name.trim() === '') {
      MessageBox.warning(`第${i + 1}行：用例名称不能为空`)
      return
    }
  }

  creating.value = true
  let successCount = 0
  let failCount = 0
  const errors = []

  try {
    // 逐个创建用例
    for (let i = 0; i < createFormRows.value.length; i++) {
      const row = createFormRows.value[i]
      try {
        // 构建前置依赖ID列表（单选）
        const prerequisiteIds = row.prerequisite
          ? [row.prerequisite.uniqueId]
          : []

        const request = {
          caseNumber: row.caseNumber.trim(),
          name: row.name.trim(),
          description: row.description || null,
          projectId: currentProjectId.value,
          stepsText: row.stepsText || '',
          expectedResult: row.expectedResult || null,
          prerequisiteIds: prerequisiteIds,
          priority: 'P2',
          status: 'NOT_EXECUTED',
          automationStatus: 'MANUAL'
        }

        await createTestCase(request)
        successCount++
        console.log(`第${i + 1}行创建成功`)
      } catch (error) {
        console.error(`第${i + 1}行创建失败:`, error)
        failCount++
        // 收集错误信息
        const errorMsg = error.response?.data?.message || error.message || '未知错误'
        errors.push(`第${i + 1}行（${row.caseNumber}）：${errorMsg}`)
      }
    }

    // 显示结果
    if (failCount === 0) {
      MessageBox.success(`成功创建${successCount}条测试用例！`)
      closeCreateModal()
      // 刷新列表
      await fetchTestCases()
    } else if (successCount === 0) {
      // 全部失败，显示详细错误
      MessageBox.error(`创建失败！\n${errors.join('\n')}`)
    } else {
      // 部分成功
      MessageBox.warning(`创建完成：成功${successCount}条，失败${failCount}条\n${errors.join('\n')}`)
      // 刷新列表显示成功的
      await fetchTestCases()
    }
  } catch (error) {
    console.error('创建失败:', error)
    MessageBox.error('创建失败：' + (error.message || '未知错误'))
  } finally {
    creating.value = false
  }
}

// 执行单个用例
const executeCase = async (testCase) => {
  try {
    const result = await MessageBox.confirm(`确定要执行测试用例"${testCase.name}"吗？`, '确认执行', {
      type: 'warning'
    })

    // 如果用户取消，不执行
    if (result === 'cancel') return

    await executeTestCase(testCase.uniqueId)
    MessageBox.success('已开始执行测试用例')
    // 刷新列表
    await fetchTestCases()
  } catch (error) {
    console.error('执行失败:', error)
    MessageBox.error('执行失败')
  }
}

// 查看详情
const viewDetail = async (testCase) => {
  detailLoading.value = true
  showDetailModal.value = true

  try {
    const data = await getTestCaseDetail(testCase.uniqueId)
    currentDetail.value = {
      name: data.name || '',
      description: data.description || '',
      stepsText: data.stepsText || '',
      expectedResult: data.expectedResult || '',
      priority: data.priority || 'P2',
      status: data.status || 'NOT_EXECUTED'
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    MessageBox.error('获取详情失败')
    showDetailModal.value = false
  } finally {
    detailLoading.value = false
  }
}

// 删除用例
const deleteCase = async (testCase) => {
  try {
    const result = await MessageBox.confirm(`确定要删除测试用例"${testCase.name}"吗？`, '确认删除', {
      type: 'warning',
      confirmButtonType: 'danger'
    })

    // 如果用户取消，不执行删除
    if (result === 'cancel') return

    try {
      await deleteTestCase(testCase.uniqueId)
      MessageBox.success('删除成功')
      // 刷新列表
      await fetchTestCases()
    } catch (error) {
      console.error('删除失败:', error)
      MessageBox.error('删除失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 搜索处理
const handleSearch = () => {
  // 搜索由 computed 自动处理
}
</script>
