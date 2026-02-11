<template>
  <div class="code-viewer">
    <pre class="bg-neutral rounded-lg p-4 overflow-x-auto text-sm"><code>{{ code }}</code></pre>
  </div>
</template>

<script setup>
defineProps({
  code: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
code {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  color: #333;
  line-height: 1.6;
  white-space: pre;
}
</style>
