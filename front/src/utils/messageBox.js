import { createApp, h } from 'vue'
import MessageBoxComponent from '@/components/MessageBox/MessageBox.vue'

// 创建消息框实例
const createMessageBox = (options) => {
  // 创建容器
  const container = document.createElement('div')
  document.body.appendChild(container)

  // 创建 Vue 应用
  const app = createApp({
    render() {
      return h(MessageBoxComponent, {
        ...options,
        onConfirm: () => {
          options.onConfirm?.()
          cleanup()
        },
        onCancel: () => {
          options.onCancel?.()
          cleanup()
        },
        onClose: () => {
          options.onClose?.()
          if (options.type === 'alert') {
            cleanup()
          }
        }
      })
    }
  })

  // 挂载应用
  const vm = app.mount(container)

  // 获取组件实例并调用 show 方法
  const componentInstance = vm.$.subTree.component
  const promise = componentInstance?.exposed?.show?.() || Promise.resolve()

  // 清理函数
  const cleanup = () => {
    setTimeout(() => {
      app.unmount()
      if (document.body.contains(container)) {
        document.body.removeChild(container)
      }
    }, 300) // 等待动画完成
  }

  return promise
}

// MessageBox 工具对象
const MessageBox = {
  // 确认框
  confirm(message, title, options = {}) {
    // 支持重载
    if (typeof title === 'object') {
      options = title
      title = ''
    }

    return createMessageBox({
      type: 'confirm',
      messageType: options.type || 'warning',
      title: title || '温馨提示',
      message,
      showClose: options.showClose !== false,
      showCancelButton: options.showCancelButton !== false,
      showConfirmButton: options.showConfirmButton !== false,
      cancelButtonText: options.cancelButtonText || '取消',
      confirmButtonText: options.confirmButtonText || '确定',
      confirmButtonType: options.confirmButtonType || 'primary',
      closeOnClickModal: options.closeOnClickModal !== false,
      closeOnPressEscape: options.closeOnPressEscape !== false
    })
  },

  // 提示框
  alert(message, title, options = {}) {
    // 支持重载
    if (typeof title === 'object') {
      options = title
      title = ''
    }

    return createMessageBox({
      type: 'alert',
      messageType: options.type || 'info',
      title: title || '提示',
      message,
      showClose: options.showClose !== false,
      showConfirmButton: true,
      confirmButtonText: options.confirmButtonText || '知道了',
      closeOnClickModal: options.closeOnClickModal !== false,
      closeOnPressEscape: options.closeOnPressEscape !== false
    })
  },

  // 成功提示
  success(message, options = {}) {
    return this.alert(message, {
      type: 'success',
      ...options
    })
  },

  // 警告提示
  warning(message, options = {}) {
    return this.alert(message, {
      type: 'warning',
      ...options
    })
  },

  // 错误提示
  error(message, options = {}) {
    return this.alert(message, {
      type: 'error',
      title: '错误',
      ...options
    })
  },

  // 信息提示
  info(message, options = {}) {
    return this.alert(message, {
      type: 'info',
      ...options
    })
  }
}

export default MessageBox
