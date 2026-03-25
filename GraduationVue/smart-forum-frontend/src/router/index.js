import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录 - 智能论坛' }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页 - 智能论坛' }
      },
      {
        path: 'post/:id',
        name: 'PostDetail',
        component: () => import('@/views/PostDetail.vue'),
        meta: { title: '文章详情 - 智能论坛' }
      },
      {
        path: 'post/create',
        name: 'PostCreate',
        component: () => import('@/views/PostCreate.vue'),
        meta: { title: '发布文章 - 智能论坛', requiresAuth: true }
      },
      {
        path: 'ai-chat',
        name: 'AIChat',
        component: () => import('@/views/AIChat.vue'),
        meta: { title: 'AI 问答 - 智能论坛', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/UserProfile.vue'),
        meta: { title: '个人主页 - 智能论坛', requiresAuth: true }
      },
      {
        path: 'user/:id',
        name: 'UserHomepage',
        component: () => import('@/views/UserHomepage.vue'),
        meta: { title: '用户主页 - 智能论坛' }
      },
      {
        path: 'user/:userId/favorites',
        name: 'UserFavorites',
        component: () => import('@/views/UserFavorites.vue'),
        meta: { title: '收藏列表 - 智能论坛' }
      },
      {
        path: 'user/:userId/likes',
        name: 'UserLikes',
        component: () => import('@/views/UserLikes.vue'),
        meta: { title: '点赞列表 - 智能论坛' }
      },
      {
        path: 'user/:userId/:type(following|followers)',
        name: 'UserFollowList',
        component: () => import('@/views/UserFollowList.vue'),
        meta: { title: '关注列表 - 智能论坛' }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/Admin.vue'),
        meta: { title: '管理后台 - 智能论坛', requiresAuth: true, requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '智能论坛'

  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }

  if (to.meta.requiresAdmin) {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
    if (!userInfo || userInfo.role !== 'admin') {
      next({ path: '/' })
      return
    }
  }

  next()
})

export default router