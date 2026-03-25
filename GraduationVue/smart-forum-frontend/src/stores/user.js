import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
    const token = ref(localStorage.getItem('token') || '')

    const isLoggedIn = computed(() => !!token.value)
    const nickname = computed(() => userInfo.value?.nickname || '游客')
    const isAdmin = computed(() => userInfo.value?.role === 'admin')

    function setLogin(data) {
        token.value = data.token
        const info = {
            userId: data.userId,
            username: data.username,
            nickname: data.nickname,
            avatar: data.avatar,
            role: data.role
        }
        userInfo.value = info
        localStorage.setItem('token', data.token)
        localStorage.setItem('userInfo', JSON.stringify(info))
    }

    function logout() {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    return { userInfo, token, isLoggedIn, nickname, isAdmin, setLogin, logout }
})

