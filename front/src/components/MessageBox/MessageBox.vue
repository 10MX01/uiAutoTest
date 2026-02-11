<template>
  <Transition name="fade">
    <div v-if="visible" class="fixed inset-0 z-[9999] flex items-center justify-center">
      <!-- 遮罩层 -->
      <div class="absolute inset-0 bg-black/50" @click="handleMaskClick"></div>

      <!-- 消息框 -->
      <div
        class="relative bg-white rounded-lg shadow-xl max-w-md w-full mx-4 transform transition-all"
        :class="[
          !isAlert && messageType === 'error' && 'border-l-4 border-danger',
          !isAlert && messageType === 'warning' && 'border-l-4 border-warning',
          !isAlert && messageType === 'success' && 'border-l-4 border-success',
          !isAlert && messageType === 'info' && 'border-l-4 border-info'
        ]"
      >
        <!-- 标题栏 -->
        <div v-if="!isAlert" class="flex items-center justify-between px-6 py-4 border-b border-borderColor">
          <div class="flex items-center gap-2">
            <i
              class="text-lg"
              :class="[
                messageType === 'error' && 'fa fa-times-circle text-danger',
                messageType === 'warning' && 'fa fa-exclamation-triangle text-warning',
                messageType === 'success' && 'fa fa-check-circle text-success',
                messageType === 'info' && 'fa fa-info-circle text-info'
              ]"
            ></i>
            <h3 class="text-base font-bold text-dark">{{ title || '温馨提示' }}</h3>
          </div>
          <button
            v-if="showClose"
            @click="handleClose"
            class="text-light hover:text-dark transition-colors"
          >
            <i class="fa fa-times text-lg"></i>
          </button>
        </div>

        <!-- 内容区 -->
        <div class="px-6 py-4">
          <div class="flex items-start gap-3">
            <i
              v-if="isAlert"
              class="text-xl mt-0.5 flex-shrink-0"
              :class="[
                messageType === 'error' && 'fa fa-times-circle text-danger',
                messageType === 'warning' && 'fa fa-exclamation-triangle text-warning',
                messageType === 'success' && 'fa fa-check-circle text-success',
                messageType === 'info' && 'fa fa-info-circle text-info'
              ]"
            ></i>
            <p class="text-sm text-dark leading-relaxed whitespace-pre-wrap">{{ message }}</p>
          </div>
        </div>

        <!-- 按钮区 -->
        <div class="flex items-center justify-end gap-3 px-6 py-4" :class="!isAlert && 'border-t border-borderColor'">
          <button
            v-if="!isAlert && showCancelButton"
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
            {{ confirmButtonText || (isAlert ? '知道了' : '确定') }}
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  // 消息框类型：confirm（确认框）、alert（提示框）
  type: {
    type: String,
    default: 'confirm'
  },
  // 提示类型：error、warning、success、info
  messageType: {
    type: String,
    default: 'warning'
  },
  // 标题
  title: String,
  // 消息内容
  message: {
    type: String,
    required: true
  },
  // 是否显示关闭按钮
  showClose: {
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
  // 确认按钮类型：danger、warning、primary
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
  }
})

const emit = defineEmits(['confirm', 'cancel', 'close'])

const visible = ref(false)
let resolve = null
let reject = null

// 是否为 alert 类型
const isAlert = computed(() => props.type === 'alert')

// 显示消息框
const show = () => {
  visible.value = true
  return new Promise((res, rej) => {
    resolve = res
    reject = rej
  })
}

// 确认
const handleConfirm = () => {
  visible.value = false
  emit('confirm')
  resolve?.('confirm')
}

// 取消
const handleCancel = () => {
  visible.value = false
  emit('cancel')
  resolve?.('cancel')
}

// 关闭
const handleClose = () => {
  visible.value = false
  emit('close')
  if (isAlert.value) {
    handleConfirm()
  } else {
    resolve?.('close')
  }
}

// 点击遮罩
const handleMaskClick = () => {
  if (props.closeOnClickModal) {
    handleCancel()
  }
}

// ESC 键关闭
const handleEscape = (e) => {
  if (props.closeOnPressEscape && e.key === 'Escape' && visible.value) {
    handleCancel()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape)
})

// 暴露方法
defineExpose({
  show
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
