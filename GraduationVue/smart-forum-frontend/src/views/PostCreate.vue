<template>
  <div class="post-create-page">
    <div class="page-header fade-in-up">
      <h1>
        <el-icon><Edit /></el-icon>
        发布文章
      </h1>
      <p>分享你的想法，AI 将助你一臂之力</p>
    </div>

    <div class="create-form fade-in-up" style="animation-delay:0.1s">
      <!-- 标题 -->
      <div class="form-group">
        <label class="form-label">文章标题</label>
        <div class="title-row">
          <el-input v-model="title" placeholder="请输入文章标题" size="large" maxlength="100" show-word-limit />
          <el-tooltip content="根据文章内容 AI 生成标题" placement="top">
            <el-button type="primary" plain :loading="titleLoading" @click="handleGenerateTitle"
                       :disabled="!content.trim()">
              <el-icon><MagicStick /></el-icon>
              AI 生成标题
            </el-button>
          </el-tooltip>
        </div>
      </div>

      <!-- 内容 -->
      <div class="form-group">
        <div class="content-header">
          <label class="form-label">文章内容</label>
          <el-tooltip content="AI 润色优化文章内容" placement="top">
            <el-button type="success" plain size="small" :loading="polishLoading"
                       @click="handlePolish" :disabled="!content.trim()">
              <el-icon><Brush /></el-icon>
              AI 润色内容
            </el-button>
          </el-tooltip>
        </div>
        <el-input v-model="content" type="textarea" :rows="16"
                  placeholder="请输入文章内容，支持详细描述你的观点..."
                  resize="vertical" />
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button size="large" @click="$router.push('/')">取消</el-button>
        <el-button type="primary" size="large" :loading="submitLoading"
                   @click="handleSubmit" :disabled="!title.trim() || !content.trim()">
          <el-icon><Promotion /></el-icon>
          发布文章
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
import { Edit, MagicStick, Brush, Promotion } from '@element-plus/icons-vue'

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
    ElMessage.success('标题生成成功')
  } finally {
    titleLoading.value = false
  }
}

const handlePolish = async () => {
  try {
    await ElMessageBox.confirm('AI 将对当前内容进行润色优化，是否继续？', '确认润色', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  polishLoading.value = true
  try {
    const res = await polishContent(content.value)
    content.value = res.data
    ElMessage.success('润色完成')
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
    ElMessage.success('发布成功')
    router.push(`/post/${res.data.id}`)
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.post-create-page {
  max-width: 820px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 28px;
}

.page-header h1 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  font-weight: 700;
}

.page-header p {
  color: var(--text-secondary);
  margin-top: 6px;
  font-size: 14px;
}

.create-form {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 32px;
}

.form-group {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.title-row {
  display: flex;
  gap: 12px;
}

.title-row .el-input {
  flex: 1;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}
</style>
