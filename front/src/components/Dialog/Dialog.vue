<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="fixed inset-0 z-[9998] flex items-center justify-center">
        <!-- 遮罩层 -->
        <div class="absolute inset-0 bg-black/50" @click="handleMaskClick"></div>

        <!-- 对话框 -->
        <div
          class="relative bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 transform transition-all max-h-[90vh] flex flex-col"
        >
          <!-- 标题栏 -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-borderColor flex-shrink-0">
            <div class="flex items-center gap-2">
              <i v-if="icon" :class="`fa ${icon} ${iconColor}`"></i>
              <h3 class="text-base font-bold text-dark">{{ title }}</h3>
            </div>
            <button
              @click="handleClose"
              class="text-light hover:text-dark transition-colors"
            >
              <i class="fa fa-times text-lg"></i>
            </button>
          </div>

          <!-- 内容区 -->
          <div class="px-6 py-4 overflow-y-auto flex-1">
            <slot></slot>
          </div>

          <!-- 按钮区 -->
          <div v-if="showFooter" class="flex items-center justify-end gap-3 px-6 py-4 border-t border-borderColor flex-shrink-0">
            <slot name="footer">
              <button
                v-if="showCancelButton"
                @click="handleCancel"
                class="px-4 py-2 text-sm rounded-lg border border-borderColor text-dark hover:bg-neutral transition-colors"
              >
                {{ cancelButtonText || '取消' }}
              </button>
              <button
                v-if="showConfirmButton"
                @click="handleConfirm"
                class="px-4 py-2 text-sm rounded-lg text-white transition-colors"
                :class="[
                  confirmButtonType === 'danger' && 'bg-danger hover:bg-dangerRed',
                  confirmButtonType === 'warning' && 'bg-warning hover:bg-warningYellow',
                  confirmButtonType !== 'danger' && confirmButtonType !== 'warning' && 'bg-primary hover:bg-primaryDark'
                ]"
              >
                <i v-if="loading" class="fa fa-spinner fa-spin mr-1"></i>
                {{ loading ? loadingText : confirmButtonText }}
              </button>
            </slot>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  // 是否显示
  visible: {
    type: Boolean,
    default: false
  },
  // 标题
  title: {
    type: String,
    required: true
  },
  // 图标
  icon: String,
  // 图标颜色
  iconColor: {
    type: String,
    default: 'text-primary'
  },
  // 是否显示底部
  showFooter: {
    type: Boolean,
    default: true
  },
  // 是否显示取消按钮
  showCancelButton: {
    type: Boolean,
    default: true
  },
  // 是否显示确认按钮
  showConfirmButton: {
    type: Boolean,
    default: true
  },
  // 取消按钮文本
  cancelButtonText: {
    type: String,
    default: '取消'
  },
  // 确认按钮文本
  confirmButtonText: {
    type: String,
    default: '确定'
  },
  // 确认按钮类型
  confirmButtonType: {
    type: String,
    default: 'primary'
  },
  // 是否可通过点击遮罩关闭
  closeOnClickModal: {
    type: Boolean,
    default: true
  },
  // 是否可通过按 ESC 关闭
  closeOnPressEscape: {
    type: Boolean,
    default: true
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 加载文本
  loadingText: {
    type: String,
    default: '处理中...'
  }
})

const emit = defineEmits(['update:visible', 'confirm', 'cancel', 'close'])

// 确认
const handleConfirm = () => {
  emit('confirm')
}

// 取消
const handleCancel = () => {
  emit('cancel')
  emit('update:visible', false)
}

// 关闭
const handleClose = () => {
  emit('close')
  emit('update:visible', false)
}

// 点击遮罩
const handleMaskClick = () => {
  if (props.closeOnClickModal) {
    handleClose()
  }
}

// ESC 键关闭
const handleEscape = (e) => {
  if (props.closeOnPressEscape && e.key === 'Escape' && props.visible) {
    handleClose()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape)
})

// 监听 visible 变化，禁止/恢复页面滚动
watch(() => props.visible, (val) => {
  if (val) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-enter-active .relative,
.fade-leave-active .relative {
  transition: transform 0.3s;
}

.fade-enter-from .relative,
.fade-leave-to .relative {
  transform: scale(0.9);
}
</style>
