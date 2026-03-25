<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="navbar-inner">
        <div class="navbar-left">
          <router-link to="/" class="logo">
            <div class="logo-icon-wrapper">
              <el-icon class="logo-icon">
                <Cpu />
              </el-icon>
            </div>
            <span class="logo-text">智能论坛</span>
          </router-link>
          <nav class="nav-links">
            <router-link to="/" class="nav-link" active-class="active">
              <el-icon>
                <House />
              </el-icon>
              <span>首页</span>
            </router-link>
            <router-link to="/ai-chat" class="nav-link" active-class="active">
              <el-icon>
                <ChatDotRound />
              </el-icon>
              <span>AI 问答</span>
              <span class="nav-badge">AI</span>
            </router-link>
            <router-link v-if="userStore.isAdmin" to="/admin" class="nav-link" active-class="active">
              <el-icon>
                <Setting />
              </el-icon>
              <span>管理后台</span>
              <span class="nav-badge admin-badge">管理</span>
            </router-link>
          </nav>
        </div>

        <div class="navbar-right">
          <template v-if="userStore.isLoggedIn">
            <el-button type="primary" round @click="$router.push('/post/create')" class="create-btn">
              <el-icon>
                <Edit />
              </el-icon>
              <span>发布文章</span>
            </el-button>
            <el-dropdown trigger="click" @command="handleCommand" popper-class="user-dropdown">
              <div class="user-avatar-wrapper">
                <el-avatar :size="38"
                  :style="{ background: 'var(--gradient-primary)', fontSize: '15px', fontWeight: '700' }">
                  {{ userStore.nickname?.charAt(0) }}
                </el-avatar>
                <span class="online-dot"></span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <div class="dropdown-user-info">
                    <el-avatar :size="40"
                      :style="{ background: 'var(--gradient-primary)', fontSize: '16px', fontWeight: '700' }">
                      {{ userStore.nickname?.charAt(0) }}
                    </el-avatar>
                    <div>
                      <div class="dropdown-name">{{ userStore.nickname }}</div>
                      <div class="dropdown-username">@{{ userStore.userInfo?.username }}</div>
                    </div>
                  </div>
                  <el-dropdown-item command="profile">
                    <el-icon>
                      <User />
                    </el-icon>
                    个人主页
                  </el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin">
                    <el-icon>
                      <Setting />
                    </el-icon>
                    管理后台
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout" class="logout-item">
                    <el-icon>
                      <SwitchButton />
                    </el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button plain round @click="$router.push('/login')" class="login-btn">
              登录
            </el-button>
            <el-button type="primary" round @click="$router.push('/login')">
              注册
            </el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 页面内容 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-logo">
          <el-icon>
            <Cpu />
          </el-icon>
          <span>智能论坛</span>
        </div>
        <p class="footer-text">© 2026 智能论坛 · 基于 Spring Boot & Vue 3 & AI 构建</p>
        <div class="footer-links">
          <span>关于我们</span>
          <span>·</span>
          <span>隐私政策</span>
          <span>·</span>
          <span>帮助中心</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { User, Setting } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

const handleCommand = (cmd) => {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'admin') {
    router.push('/admin')
  } else if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/')
    }).catch(() => { })
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ===== Navbar ===== */
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(24px) saturate(200%);
  -webkit-backdrop-filter: blur(24px) saturate(200%);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.04);
}

.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 28px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 800;
  color: var(--text-primary);
  text-decoration: none;
  letter-spacing: -0.5px;
}

.logo-icon-wrapper {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 10px;
  color: white;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  transition: var(--transition);
}

.logo:hover .logo-icon-wrapper {
  transform: rotate(-5deg) scale(1.05);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
}

.logo-icon {
  font-size: 20px;
}

.logo-text {
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* Nav Links */
.nav-links {
  display: flex;
  gap: 2px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 14px;
  transition: var(--transition);
  position: relative;
}

.nav-link:hover {
  background: var(--primary-bg);
  color: var(--primary);
}

.nav-link.active {
  background: var(--primary-bg);
  color: var(--primary);
  font-weight: 600;
}

.nav-badge {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 5px;
  background: var(--gradient-primary);
  color: white;
  border-radius: 4px;
  letter-spacing: 0.3px;
}

.admin-badge {
  background: linear-gradient(135deg, #ef4444, #f97316) !important;
}

/* Right Area */
.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.create-btn {
  font-weight: 600 !important;
  padding: 0 20px !important;
  height: 36px !important;
  font-size: 13px !important;
}

.login-btn {
  color: var(--text-secondary) !important;
  border-color: var(--border) !important;
  font-weight: 500 !important;
}

.login-btn:hover {
  color: var(--primary) !important;
  border-color: var(--primary-light) !important;
}

/* User Avatar */
.user-avatar-wrapper {
  position: relative;
  cursor: pointer;
  transition: var(--transition);
}

.user-avatar-wrapper:hover {
  transform: scale(1.06);
}

.online-dot {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 9px;
  height: 9px;
  background: var(--success);
  border: 2px solid white;
  border-radius: 50%;
}

/* Dropdown */
.dropdown-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px 12px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 4px;
  min-width: 180px;
}

.dropdown-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.dropdown-username {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 1px;
}

/* Main Content */
.main-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 28px 24px;
}

/* Footer */
.footer {
  border-top: 1px solid var(--border-light);
  padding: 28px 24px;
  background: white;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--primary);
  font-weight: 700;
  font-size: 15px;
}

.footer-text {
  color: var(--text-muted);
  font-size: 13px;
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-muted);
  font-size: 12px;
}

.footer-links span:not(:nth-child(2n)) {
  cursor: pointer;
  transition: color 0.2s;
}

.footer-links span:not(:nth-child(2n)):hover {
  color: var(--primary);
}
</style>

<style>
/* Dropdown 全局样式覆盖 */
.user-dropdown .el-dropdown-menu {
  border-radius: 12px !important;
  padding: 4px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.06) !important;
  border: 1px solid var(--border-light) !important;
  min-width: 180px !important;
}

.user-dropdown .el-dropdown-menu__item {
  border-radius: 8px !important;
  font-size: 14px !important;
  padding: 9px 12px !important;
  margin: 2px 4px !important;
  transition: all 0.15s ease !important;
}

.user-dropdown .el-dropdown-menu__item:hover {
  background: var(--primary-bg) !important;
  color: var(--primary) !important;
}

.logout-item:hover {
  background: var(--danger-bg) !important;
  color: var(--danger) !important;
}
</style>
