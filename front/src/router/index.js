import { createRouter, createWebHistory } from 'vue-router'
import { Auth } from '@/utils/auth'
import Layout from '@/components/layout/Layout.vue'
import Dashboard from '@/views/Dashboard.vue'
import ProjectManagement from '@/views/ProjectManagement.vue'
import TestCase from '@/views/TestCase.vue'
import TestRecord from '@/views/TestRecord.vue'
import ScriptManagement from '@/views/ScriptManagement.vue'

// 懒加载登录和用户管理页面
const Login = () => import('@/views/Login.vue')
const UserManagement = () => import('@/views/UserManagement.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { requiresAuth: true }
      },
      {
        path: 'project',
        name: 'ProjectManagement',
        component: ProjectManagement,
        meta: { requiresAuth: true }
      },
      {
        path: 'testcase',
        name: 'TestCase',
        component: TestCase,
        meta: { requiresAuth: true }
      },
      {
        path: 'script',
        name: 'ScriptManagement',
        component: ScriptManagement,
        meta: { requiresAuth: true }
      },
      {
        path: 'testrecord',
        name: 'TestRecord',
        component: TestRecord,
        meta: { requiresAuth: true }
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: UserManagement,
        meta: { requiresAuth: true, requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  // 开发环境使用 '/'，生产环境使用 '/api/'
  history: createWebHistory(import.meta.env.MODE === 'production' ? '/api/' : '/'),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const isLoggedIn = Auth.isLoggedIn()
  const isAdmin = Auth.isAdmin()

  // 需要认证的路由
  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 需要管理员权限的路由
  if (to.meta.requiresAdmin && !isAdmin) {
    next({ name: 'Dashboard' })
    return
  }

  // 已登录用户访问登录页，跳转到首页
  if (to.name === 'Login' && isLoggedIn) {
    next({ name: 'Dashboard' })
    return
  }

  next()
})

export default router
