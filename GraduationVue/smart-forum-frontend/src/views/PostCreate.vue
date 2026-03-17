<template>
  <div class="post-create-page">
    <div class="page-header fade-in-up">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="22"><Edit /></el-icon>
        </div>
        <div>
          <h1>发布文章</h1>
          <p>分享你的想法，AI 将助你一臂之力 ✨</p>
        </div>
      </div>
      <el-button text round @click="$router.push('/')" class="cancel-top-btn">
        <el-icon><Close /></el-icon> 取消
      </el-button>
    </div>

    <div class="create-form fade-in-up" style="animation-delay:0.08s">
      <!-- 标题 -->
      <div class="form-group">
        <div class="form-label-row">
          <label class="form-label">文章标题</label>
          <span class="required-badge">必填</span>
        </div>
        <div class="title-row">
          <el-input v-model="title" placeholder="输入一个吸引人的标题..."
                    size="large" maxlength="100" show-word-limit class="title-input" />
          <el-tooltip content="根据文章内容 AI 生成标题" placement="top">
            <el-button type="primary" plain :loading="titleLoading" @click="handleGenerateTitle"
                       :disabled="!content.trim()" class="ai-action-btn">
              <el-icon><MagicStick /></el-icon>
              AI 生成标题
            </el-button>
          </el-tooltip>
        </div>
      </div>

      <!-- 内容 -->
      <div class="form-group">
        <div class="form-label-row">
          <label class="form-label">文章内容</label>
          <div class="content-actions">
            <el-tooltip content="AI 润色优化文章内容" placement="top">
              <el-button type="success" plain size="small" :loading="polishLoading"
                         @click="handlePolish" :disabled="!content.trim()" class="ai-action-btn-sm">
                <el-icon><Brush /></el-icon>
                AI 润色内容
              </el-button>
            </el-tooltip>
          </div>
        </div>
        <div class="content-editor">
          <el-input v-model="content" type="textarea" :rows="18"
                    placeholder="在这里写下你的文章内容，支持 Markdown 格式..."
                    resize="none" class="content-textarea" />
          <div class="editor-footer">
            <span class="char-count">{{ content.length }} 字</span>
          </div>
        </div>
      </div>

      <!-- AI 提示卡 -->
      <div class="ai-hint-card" v-if="!title.trim() || !content.trim()">
        <div class="ai-hint-icon">
          <el-icon><MagicStick /></el-icon>
        </div>
        <div class="ai-hint-text">
          <strong>AI 功能提示</strong>
          <p>写完内容后，可以使用 AI 自动生成标题或润色内容，让你的文章更精彩！</p>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button size="large" round @click="$router.push('/')">
          <el-icon><Close /></el-icon>
          取消
        </el-button>
        <el-button type="primary" size="large" :loading="submitLoading"
                   @click="handleSubmit" :disabled="!title.trim() || !content.trim()"
                   round class="submit-btn">
          <el-icon v-if="!submitLoading"><Promotion /></el-icon>
          {{ submitLoading ? '发布中...' : '发布文章' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createPost } from '@/api/post'
import { generateTitle, polishContent } from '@/api/ai'
import { Edit, MagicStick, Brush, Promotion, Close } from '@element-plus/icons-vue'

const router = useRouter()
const title = ref('')
const content = ref('')
const titleLoading = ref(false)
const polishLoading = ref(false)
const submitLoading = ref(false)

const handleGenerateTitle = async () => {
  titleLoading.value = true
  try {
    const res = await generateTitle(content.value)
    title.value = res.data.replace(/^["'《]|["'》]$/g, '').trim()
    ElMessage.success('标题生成成功 ✨')
  } finally {
    titleLoading.value = false
  }
}

const handlePolish = async () => {
  try {
    await ElMessageBox.confirm('AI 将对当前内容进行润色优化，是否继续？', '确认润色', {
      confirmButtonText: '确定润色',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  polishLoading.value = true
  try {
    const res = await polishContent(content.value)
    content.value = res.data
    ElMessage.success('润色完成 ✨')
  } finally {
    polishLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!title.value.trim() || !content.value.trim()) {
    ElMessage.warning('标题和内容不能为空')
    return
  }
  submitLoading.value = true
  try {
    const res = await createPost({ title: title.value.trim(), content: content.value })
    ElMessage.success('发布成功！🎉')
    router.push(`/post/${res.data.id}`)
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.post-create-page {
  max-width: 860px;
  margin: 0 auto;
}

/* ===== Page Header ===== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 14px;
  color: white;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
}

.page-header h1 {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

.page-header p {
  color: var(--text-secondary);
  font-size: 14px;
  margin-top: 3px;
}

.cancel-top-btn {
  color: var(--text-secondary) !important;
}

/* ===== Create Form ===== */
.create-form {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: 36px 40px;
  box-shadow: var(--shadow-sm);
}

/* ===== Form Group ===== */
.form-group {
  margin-bottom: 28px;
}

.form-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.form-label {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.required-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  background: rgba(239, 68, 68, 0.08);
  color: var(--danger);
  border-radius: var(--radius-full);
}

.content-actions {
  display: flex;
  gap: 8px;
}

/* ===== Title Row ===== */
.title-row {
  display: flex;
  gap: 12px;
}

.title-input {
  flex: 1;
}

.title-input :deep(.el-input__wrapper) {
  border-radius: 10px !important;
  padding: 8px 14px !important;
  font-weight: 500;
}

.ai-action-btn {
  white-space: nowrap;
  border-radius: 10px !important;
  font-weight: 600 !important;
  height: 42px !important;
}

.ai-action-btn-sm {
  white-space: nowrap;
  font-weight: 600 !important;
  border-radius: 8px !important;
}

/* ===== Content Editor ===== */
.content-editor {
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
  transition: border-color 0.2s;
}

.content-editor:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.content-textarea :deep(.el-textarea__inner) {
  border: none !important;
  border-radius: 0 !important;
  padding: 16px 18px !important;
  font-size: 15px !important;
  line-height: 1.8 !important;
  box-shadow: none !important;
  font-family: 'JetBrains Mono', Consolas, 'Courier New', monospace;
  resize: none !important;
}

.editor-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 8px 16px;
  background: var(--bg-subtle);
  border-top: 1px solid var(--border-light);
}

.char-count {
  font-size: 12px;
  color: var(--text-muted);
}

/* ===== AI Hint Card ===== */
.ai-hint-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(99,102,241,0.05) 0%, rgba(139,92,246,0.05) 100%);
  border: 1px solid rgba(99, 102, 241, 0.15);
  border-radius: var(--radius-md);
  margin-bottom: 28px;
}

.ai-hint-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 10px;
  color: white;
  flex-shrink: 0;
}

.ai-hint-text strong {
  display: block;
  font-size: 14px;
  color: var(--primary);
  margin-bottom: 4px;
}

.ai-hint-text p {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* ===== Form Actions ===== */
.form-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid var(--border-light);
}

.submit-btn {
  min-width: 120px;
  font-weight: 600 !important;
  box-shadow: var(--shadow-primary) !important;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px) !important;
  box-shadow: var(--shadow-primary-lg) !important;
}
</style>
