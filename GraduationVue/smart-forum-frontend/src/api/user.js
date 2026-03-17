import request from './index'

export function register(data) {
    return request.post('/user/register', data)
}

export function login(data) {
    return request.post('/user/login', data)
}

export function getUserInfo() {
    return request.get('/user/info')
}

export function getUserProfile(userId) {
    return request.get(`/user/profile/${userId}`)
}

export function getUserPosts(userId, page = 1, size = 10) {
    return request.get(`/user/${userId}/posts`, { params: { page, size } })
}

export function getUserComments(userId, page = 1, size = 10) {
    return request.get(`/user/${userId}/comments`, { params: { page, size } })
}

export function getUserFavorites(userId, page = 1, size = 10) {
    return request.get(`/user/${userId}/favorites`, { params: { page, size } })
}

export function getUserLikes(userId, page = 1, size = 10) {
    return request.get(`/user/${userId}/likes`, { params: { page, size } })
}

export function getUserFollowing(userId, page = 1, size = 10) {
    return request.get(`/user/${userId}/following`, { params: { page, size } })
}

export function getUserFollowers(userId, page = 1, size = 10) {
    return request.get(`/user/${userId}/followers`, { params: { page, size } })
}

export function removeFavorite(postId) {
    return request.delete(`/user/favorites/${postId}`)
}

export function removeLike(postId) {
    return request.delete(`/user/likes/${postId}`)
}

export function removeFollow(targetUserId) {
    return request.delete(`/user/following/${targetUserId}`)
}

export function updateNickname(nickname) {
    return request.put('/user/nickname', { nickname })
}

export function changePassword(oldPassword, newPassword) {
    return request.put('/user/password', { oldPassword, newPassword })
}
