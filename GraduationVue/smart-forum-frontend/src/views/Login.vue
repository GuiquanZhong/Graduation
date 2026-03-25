<template>
  <div class="login-container">
    <!-- 背景 -->
    <div class="login-bg">
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="login-card fade-in-up">
      <!-- Logo & 标题 -->
      <div class="login-header">
        <div class="login-logo">
          <el-icon :size="28">
            <Cpu />
          </el-icon>
        </div>
        <h1>智能论坛</h1>
        <p>基于 AI 的智能社区交流平台</p>
      </div>

      <!-- 切换 Tab -->
      <div class="tab-switch">
        <button :class="{ active: mode === 'login' }" @click="mode = 'login'">
          <el-icon>
            <Key />
          </el-icon>
          登录
        </button>
        <button :class="{ active: mode === 'register' }" @click="mode = 'register'">
          <el-icon>
            <UserFilled />
          </el-icon>
          注册
        </button>
        <div class="tab-indicator" :style="{ left: mode === 'login' ? '4px' : 'calc(50% + 2px)' }"></div>
      </div>

      <!-- 登录表单 -->
      <el-form v-if="mode === 'login'" :model="loginForm" :rules="loginRules" ref="loginRef" @keyup.enter="handleLogin"
        class="auth-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large" :prefix-icon="User"
            class="form-input" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" :prefix-icon="Lock"
            show-password class="form-input" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleLogin" class="submit-btn">
          <el-icon v-if="!loading">
            <Key />
          </el-icon>
          {{ loading ? '登录中...' : '登 录' }}
        </el-button>
      </el-form>

      <!-- 注册表单 -->
      <el-form v-else :model="registerForm" :rules="registerRules" ref="registerRef" @keyup.enter="handleRegister"
        class="auth-form">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名（3-20位）" size="large" :prefix-icon="User"
            class="form-input" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="registerForm.nickname" placeholder="请输入昵称（选填）" size="large" :prefix-icon="UserFilled"
            class="form-input" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码（至少6位）" size="large"
            :prefix-icon="Lock" show-password class="form-input" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" size="large"
            :prefix-icon="Lock" show-password class="form-input" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleRegister" class="submit-btn">
          <el-icon v-if="!loading">
            <UserFilled />
          </el-icon>
          {{ loading ? '注册中...' : '注 册' }}
        </el-button>
      </el-form>

      <!-- 底部提示 -->
      <div class="login-footer">
        <template v-if="mode === 'login'">
          还没有账号？
          <span class="switch-link" @click="mode = 'register'">立即注册</span>
        </template>
        <template v-else>
          已有账号？
          <span class="switch-link" @click="mode = 'login'">立即登录</span>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Cpu, Key } from '@element-plus/icons-vue'
import { login, register } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const mode = ref('login')
const loading = ref(false)
const loginRef = ref()
const registerRef = ref()

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', nickname: '', password: '', confirmPassword: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const validateConfirm = (rule, value, callback) => {
  if (value !== registerForm.password) callback(new Error('两次密码不一致'))
  else callback()
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  await loginRef.value?.validate()
  loading.value = true
  try {
    const res = await login(loginForm)
    userStore.setLogin(res.data)
    ElMessage.success('登录成功，欢迎回来！')
    router.push(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  await registerRef.value?.validate()
  loading.value = true
  try {
    await register(registerForm)
    ElMessage.success('注册成功，请登录')
    mode.value = 'login'
    loginForm.username = registerForm.username
    loginForm.password = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-hero);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  animation: float 10s ease-in-out infinite;
}

.bg-orb-1 {
  width: 500px;
  height: 500px;
  background: rgba(99, 102, 241, 0.3);
  top: -160px;
  left: -120px;
}

.bg-orb-2 {
  width: 380px;
  height: 380px;
  background: rgba(139, 92, 246, 0.25);
  bottom: -100px;
  right: -80px;
  animation-delay: 2.5s;
}

.bg-orb-3 {
  width: 220px;
  height: 220px;
  background: rgba(167, 139, 250, 0.2);
  top: 45%;
  left: 58%;
  animation-delay: 5s;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size: 48px 48px;
}

/* Login Card */
.login-card {
  position: relative;
  z-index: 1;
  width: 440px;
  padding: 48px 44px;
  background: rgba(255, 255, 255, 0.97);
  backdrop-filter: blur(24px);
  border-radius: var(--radius-2xl);
  box-shadow: 0 32px 80px rgba(0, 0, 0, 0.2), 0 8px 24px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

/* Header */
.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  width: 68px;
  height: 68px;
  margin: 0 auto 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 20px;
  color: white;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4), 0 2px 8px rgba(99, 102, 241, 0.2);
  animation: float 6s ease-in-out infinite;
}

.login-header h1 {
  font-size: 26px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.5px;
  margin-bottom: 6px;
}

.login-header p {
  color: var(--text-muted);
  font-size: 14px;
}

/* Tab Switch */
.tab-switch {
  display: flex;
  position: relative;
  background: var(--bg-subtle);
  border-radius: var(--radius-sm);
  padding: 4px;
  margin-bottom: 28px;
  border: 1px solid var(--border-light);
}

.tab-switch button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  border: none;
  background: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  color: var(--text-secondary);
  position: relative;
  z-index: 1;
  transition: var(--transition);
  border-radius: var(--radius-xs);
}

.tab-switch button.active {
  color: var(--primary);
  font-weight: 600;
}

.tab-indicator {
  position: absolute;
  top: 4px;
  width: calc(50% - 6px);
  height: calc(100% - 8px);
  background: white;
  border-radius: 6px;
  box-shadow: var(--shadow-sm);
  transition: var(--transition);
}

/* Form */
.auth-form {
  margin-bottom: 4px;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.form-input :deep(.el-input__wrapper) {
  border-radius: 10px !important;
  padding: 4px 12px !important;
  box-shadow: 0 0 0 1px var(--border) !important;
  transition: var(--transition) !important;
}

.form-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-light) !important;
}

.form-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2), 0 0 0 1px var(--primary) !important;
}

.submit-btn {
  width: 100% !important;
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: 12px !important;
  letter-spacing: 1px;
  margin-top: 8px;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.35) !important;
}

.submit-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.45) !important;
}

/* Footer */
.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--text-muted);
}

.switch-link {
  color: var(--primary);
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s;
}

.switch-link:hover {
  color: var(--primary-dark);
  text-decoration: underline;
  text-underline-offset: 2px;
}
</style>
