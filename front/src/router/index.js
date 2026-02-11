import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/layout/Layout.vue'
import Dashboard from '@/views/Dashboard.vue'
import ProjectManagement from '@/views/ProjectManagement.vue'
import TestCase from '@/views/TestCase.vue'
import TestRecord from '@/views/TestRecord.vue'
import ScriptManagement from '@/views/ScriptManagement.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard
      },
      {
        path: 'project',
        name: 'ProjectManagement',
        component: ProjectManagement
      },
      {
        path: 'testcase',
        name: 'TestCase',
        component: TestCase
      },
      {
        path: 'script',
        name: 'ScriptManagement',
        component: ScriptManagement
      },
      {
        path: 'testrecord',
        name: 'TestRecord',
        component: TestRecord
      }
    ]
  }
]

const router = createRouter({
  // 开发环境使用 '/'，生产环境使用 '/api/'
  history: createWebHistory(import.meta.env.MODE === 'production' ? '/api/' : '/'),
  routes
})

export default router
