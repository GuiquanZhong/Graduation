import request from './index'

// ============ 统计 ============
export function getAdminStats() {
    return request.get('/admin/stats')
}

// ============ 用户管理 ============
export function getAdminUsers(page = 1, size = 10, keyword = '') {
    return request.get('/admin/users', { params: { page, size, keyword } })
}

export function banUser(userId) {
    return request.put(`/admin/user/${userId}/ban`)
}

export function unbanUser(userId) {
    return request.put(`/admin/user/${userId}/unban`)
}

// ============ 帖子管理 ============
export function getAdminPosts(page = 1, size = 10, keyword = '') {
    return request.get('/admin/posts', { params: { page, size, keyword } })
}

export function adminDeletePost(postId) {
    return request.delete(`/admin/post/${postId}`)
}

export function toggleTopPost(postId) {
    return request.put(`/admin/post/${postId}/top`)
}
