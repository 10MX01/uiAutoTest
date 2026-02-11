<template>
  <section>
    <div class="mb-6 flex items-center justify-between">
      <h2 class="text-xl font-bold text-dark">仪表盘</h2>
      <div class="flex gap-2">
        <button class="btn-outline text-sm" @click="refreshData">
          <i class="fa fa-refresh mr-1"></i> 刷新数据
        </button>
        <button class="btn-primary text-sm" @click="exportReport">
          <i class="fa fa-download mr-1"></i> 导出报表
        </button>
      </div>
    </div>

    <!-- 数据卡片统计 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
      <div v-for="stat in statistics" :key="stat.label" class="bg-white rounded-lg p-6 card-shadow">
        <div class="flex items-center justify-between mb-2">
          <h3 class="text-light text-sm">{{ stat.label }}</h3>
          <div class="w-10 h-10 bg-secondary/50 rounded-lg flex items-center justify-center text-primary">
            <i :class="stat.icon"></i>
          </div>
        </div>
        <p class="text-2xl font-bold">{{ stat.value }}</p>
        <p :class="['text-xs flex items-center mt-1', stat.trendClass]">
          <i :class="['fa mr-1', stat.trendIcon]"></i>
          {{ stat.trend }}
        </p>
      </div>
    </div>

    <!-- 图表 + 待办任务 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
      <!-- 测试趋势图表 -->
      <div class="bg-white rounded-lg p-6 card-shadow lg:col-span-2">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-semibold">近7日测试执行趋势</h3>
          <select v-model="chartType" class="text-sm border border-gray-200 rounded px-2 py-1 focus:outline-none focus:border-primary">
            <option>按用例数</option>
            <option>按通过率</option>
          </select>
        </div>
        <div class="h-64">
          <canvas id="testTrendChart"></canvas>
        </div>
      </div>
      <!-- 待处理任务 -->
      <div class="bg-white rounded-lg p-6 card-shadow">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-semibold">待处理任务</h3>
          <a href="#" class="text-primary text-sm">查看全部</a>
        </div>
        <ul class="space-y-3">
          <li v-for="task in pendingTasks" :key="task.id" class="flex items-center gap-3 p-3 bg-neutral rounded-lg">
            <div :class="['w-2 h-2 rounded-full', task.colorClass]"></div>
            <span class="text-sm flex-1">{{ task.title }}</span>
            <span class="text-xs text-light">{{ task.time }}</span>
          </li>
        </ul>
      </div>
    </div>

    <!-- 最近测试项目 -->
    <div class="bg-white rounded-lg p-6 card-shadow">
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-semibold">最近测试项目</h3>
        <router-link to="/project" class="text-primary text-sm">进入项目管理</router-link>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-gray-200">
              <th class="text-left py-3 px-4 font-semibold">项目名称</th>
              <th class="text-left py-3 px-4 font-semibold">负责人</th>
              <th class="text-left py-3 px-4 font-semibold">测试用例数</th>
              <th class="text-left py-3 px-4 font-semibold">通过率</th>
              <th class="text-left py-3 px-4 font-semibold">最近测试时间</th>
              <th class="text-left py-3 px-4 font-semibold">状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="project in recentProjects" :key="project.id" class="table-row-hover border-b border-gray-100">
              <td class="py-3 px-4">{{ project.name }}</td>
              <td class="py-3 px-4">
                <img :src="project.avatar" alt="" class="w-6 h-6 rounded-full inline-block mr-2">
                {{ project.owner }}
              </td>
              <td class="py-3 px-4">{{ project.cases }}</td>
              <td class="py-3 px-4">
                <span :class="project.passRateClass">{{ project.passRate }}</span>
              </td>
              <td class="py-3 px-4">{{ project.lastTestTime }}</td>
              <td class="py-3 px-4">
                <span :class="['px-2 py-1 rounded text-xs', project.statusClass]">{{ project.status }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const chartType = ref('按用例数')

const statistics = ref([
  {
    label: '总测试项目',
    value: '28',
    icon: 'fa fa-folder',
    trend: '12% 较上月',
    trendIcon: 'fa-arrow-up',
    trendClass: 'text-green-500'
  },
  {
    label: '今日测试用例',
    value: '156',
    icon: 'fa fa-list',
    trend: '8% 较昨日',
    trendIcon: 'fa-arrow-up',
    trendClass: 'text-green-500'
  },
  {
    label: '测试通过率',
    value: '98.2%',
    icon: 'fa fa-check-circle',
    trend: '0.5% 较昨日',
    trendIcon: 'fa-arrow-up',
    trendClass: 'text-green-500'
  },
  {
    label: '待处理任务',
    value: '7',
    icon: 'fa fa-clock-o',
    trend: '2 较昨日',
    trendIcon: 'fa-arrow-up',
    trendClass: 'text-red-500'
  }
])

const pendingTasks = ref([
  {
    id: 1,
    title: '密码加密模块测试用例评审',
    time: '今天 18:00',
    colorClass: 'bg-red-500'
  },
  {
    id: 2,
    title: '项目「电商密码服务」测试报告生成',
    time: '明天 10:00',
    colorClass: 'bg-orange-500'
  },
  {
    id: 3,
    title: '新测试人员权限分配',
    time: '明天 14:00',
    colorClass: 'bg-yellow-500'
  },
  {
    id: 4,
    title: '密码解密接口性能测试',
    time: '后天 09:00',
    colorClass: 'bg-blue-500'
  }
])

const recentProjects = ref([
  {
    id: 1,
    name: '电商平台密码服务',
    avatar: 'https://picsum.photos/24/24',
    owner: '张三',
    cases: 56,
    passRate: '99.1%',
    passRateClass: 'text-green-500',
    lastTestTime: '2026-02-03 16:20',
    status: '已完成',
    statusClass: 'bg-green-100 text-green-800'
  },
  {
    id: 2,
    name: '金融后台密码验证',
    avatar: 'https://picsum.photos/25/25',
    owner: '李四',
    cases: 89,
    passRate: '88.5%',
    passRateClass: 'text-yellow-500',
    lastTestTime: '2026-02-04 10:15',
    status: '执行中',
    statusClass: 'bg-yellow-100 text-yellow-800'
  },
  {
    id: 3,
    name: '政务系统密码加密',
    avatar: 'https://picsum.photos/26/26',
    owner: '王五',
    cases: 42,
    passRate: '75.0%',
    passRateClass: 'text-red-500',
    lastTestTime: '2026-02-04 14:30',
    status: '有异常',
    statusClass: 'bg-red-100 text-red-800'
  }
])

const refreshData = () => {
  console.log('刷新数据...')
}

const exportReport = () => {
  console.log('导出报表...')
}

onMounted(() => {
  // 初始化图表
  if (typeof Chart !== 'undefined') {
    const ctx = document.getElementById('testTrendChart')
    if (ctx) {
      new Chart(ctx, {
        type: 'line',
        data: {
          labels: ['2.1', '2.2', '2.3', '2.4', '2.5', '2.6', '2.7'],
          datasets: [{
            label: '执行用例数',
            data: [89, 102, 126, 156, 0, 0, 0],
            borderColor: '#165DFF',
            backgroundColor: 'rgba(22, 93, 255, 0.1)',
            tension: 0.3,
            fill: true
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false
            }
          },
          scales: {
            x: {
              grid: {
                display: false
              }
            },
            y: {
              beginAtZero: true,
              grid: {
                borderDash: [2, 2]
              }
            }
          }
        }
      })
    }
  }
})
</script>
