<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
    </div>

    <div class="login-card fade-in-up">
      <div class="login-header">
        <div class="login-logo">
          <el-icon :size="40"><Cpu /></el-icon>
        </div>
        <h1>智能论坛</h1>
        <p>基于 AI 的智能社区交流平台</p>
      </div>

      <!-- 切换 Tab -->
      <div class="tab-switch">
        <button :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
        <button :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
        <div class="tab-indicator" :style="{ left: mode === 'login' ? '4px' : 'calc(50% + 2px)' }"></div>
      </div>

      <!-- 登录表单 -->
      <el-form v-if="mode === 'login'" :model="loginForm" :rules="loginRules" ref="loginRef"
               @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"
                    size="large" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleLogin"
                   style="width:100%;height:48px;font-size:16px;border-radius:12px">
          登 录
        </el-button>
      </el-form>

      <!-- 注册表单 -->
      <el-form v-else :model="registerForm" :rules="registerRules" ref="registerRef"
               @keyup.enter="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="registerForm.nickname" placeholder="请输入昵称（选填）" size="large" :prefix-icon="UserFilled" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码（至少6位）"
                    size="large" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码"
                    size="large" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleRegister"
                   style="width:100%;height:48px;font-size:16px;border-radius:12px">
          注 册
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Cpu } from '@element-plus/icons-vue'
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
    ElMessage.success('登录成功')
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
  opacity: 0.4;
  animation: float 8s ease-in-out infinite;
}

.bg-orb-1 {
  width: 400px; height: 400px;
  background: #6366f1;
  top: -100px; left: -100px;
}

.bg-orb-2 {
  width: 300px; height: 300px;
  background: #8b5cf6;
  bottom: -50px; right: -50px;
  animation-delay: 2s;
}

.bg-orb-3 {
  width: 200px; height: 200px;
  background: #a78bfa;
  top: 50%; left: 60%;
  animation-delay: 4s;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  width: 72px; height: 72px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 20px;
  color: white;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.35);
}

.login-header h1 {
  font-size: 28px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.login-header p {
  color: var(--text-muted);
  font-size: 14px;
  margin-top: 6px;
}

.tab-switch {
  display: flex;
  position: relative;
  background: var(--bg);
  border-radius: var(--radius-sm);
  padding: 4px;
  margin-bottom: 28px;
}

.tab-switch button {
  flex: 1;
  padding: 10px;
  border: none;
  background: none;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  color: var(--text-secondary);
  position: relative;
  z-index: 1;
  transition: var(--transition);
}

.tab-switch button.active {
  color: var(--primary);
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
</style>
