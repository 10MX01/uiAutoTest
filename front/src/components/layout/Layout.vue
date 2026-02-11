<template>
  <div class="bg-neutral text-dark font-sans min-h-screen flex flex-col">
    <!-- 顶部导航栏 -->
    <header class="bg-white shadow-sm sticky top-0 z-30">
      <div class="container mx-auto px-4 flex items-center justify-between h-16">
        <!-- 左侧logo + 标题 -->
        <div class="flex items-center gap-2">
          <button @click="toggleSidebar" class="text-dark hover:text-primary transition-colors">
            <i class="fa fa-bars text-lg"></i>
          </button>
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
              <i class="fa fa-key text-white"></i>
            </div>
            <h1 class="text-lg font-bold">密码服务平台-自动化测试工具</h1>
          </div>
        </div>
        <!-- 右侧功能区 -->
        <div class="flex items-center gap-4">
          <div class="relative">
            <input
              type="text"
              v-model="searchText"
              placeholder="搜索功能/项目"
              class="w-64 py-2 px-4 pr-10 rounded-lg border border-gray-200 focus:outline-none focus:border-primary"
            >
            <i class="fa fa-search absolute right-3 top-1/2 -translate-y-1/2 text-light"></i>
          </div>
          <div class="flex items-center gap-2">
            <img src="https://picsum.photos/32/32" alt="用户头像" class="w-8 h-8 rounded-full object-cover">
            <span class="font-medium">测试管理员</span>
            <i class="fa fa-caret-down text-light text-xs"></i>
          </div>
        </div>
      </div>
    </header>

    <div class="flex flex-1 overflow-hidden">
      <!-- 侧边栏 -->
      <aside
        :class="[
          'bg-white shadow-sm flex-shrink-0 transition-all duration-300',
          isSidebarCollapsed ? 'w-20 overflow-hidden' : 'w-64'
        ]"
      >
        <nav class="py-4 h-[calc(100vh-4rem)] flex flex-col overflow-y-auto">
          <!-- 主导航 -->
          <div class="px-4 mb-6">
            <h2
              :class="['text-xs text-light uppercase tracking-wider mb-2', isSidebarCollapsed && 'hidden']"
            >
              核心功能
            </h2>
            <ul class="space-y-1">
              <li>
                <router-link
                  to="/dashboard"
                  :class="[
                    'flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors',
                    $route.path === '/dashboard' ? 'sidebar-item-active' : 'hover:bg-neutral'
                  ]"
                >
                  <i class="fa fa-tachometer"></i>
                  <span :class="isSidebarCollapsed && 'hidden'">仪表盘</span>
                </router-link>
              </li>
              <li>
                <router-link
                  to="/project"
                  :class="[
                    'flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors',
                    $route.path === '/project' ? 'sidebar-item-active' : 'hover:bg-neutral'
                  ]"
                >
                  <i class="fa fa-folder-open"></i>
                  <span :class="isSidebarCollapsed && 'hidden'">项目管理</span>
                </router-link>
              </li>
              <li>
                <router-link
                  to="/script"
                  :class="[
                    'flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors',
                    $route.path === '/script' ? 'sidebar-item-active' : 'hover:bg-neutral'
                  ]"
                >
                  <i class="fa fa-code"></i>
                  <span :class="isSidebarCollapsed && 'hidden'">脚本管理</span>
                </router-link>
              </li>
              <li>
                <router-link
                  to="/testrecord"
                  :class="[
                    'flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors',
                    $route.path === '/testrecord' ? 'sidebar-item-active' : 'hover:bg-neutral'
                  ]"
                >
                  <i class="fa fa-history"></i>
                  <span :class="isSidebarCollapsed && 'hidden'">测试记录</span>
                </router-link>
              </li>
            </ul>
          </div>
          <!-- 系统管理 -->
          <div class="px-4 mb-6">
            <h2
              :class="['text-xs text-light uppercase tracking-wider mb-2', isSidebarCollapsed && 'hidden']"
            >
              系统管理
            </h2>
            <ul class="space-y-1">
              <li>
                <a href="#" class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-neutral transition-colors">
                  <i class="fa fa-user"></i>
                  <span :class="isSidebarCollapsed && 'hidden'">账号管理</span>
                </a>
              </li>
              <li>
                <a href="#" class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-neutral transition-colors">
                  <i class="fa fa-shield"></i>
                  <span :class="isSidebarCollapsed && 'hidden'">权限配置</span>
                </a>
              </li>
              <li>
                <a href="#" class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-neutral transition-colors">
                  <i class="fa fa-cog"></i>
                  <span :class="isSidebarCollapsed && 'hidden'">平台设置</span>
                </a>
              </li>
            </ul>
          </div>
          <!-- 底部占位，推到最下 -->
          <div class="flex-1"></div>
          <!-- 版本信息 -->
          <div class="px-4 py-3 border-t border-gray-100">
            <p :class="['text-xs text-light', isSidebarCollapsed && 'hidden']">V1.0.0 自动化测试版</p>
          </div>
        </nav>
      </aside>

      <!-- 主内容区 -->
      <main class="flex-1 overflow-y-auto p-6 h-[calc(100vh-4rem)]">
        <router-view></router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const searchText = ref('')
const isSidebarCollapsed = ref(false)

const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}
</script>
