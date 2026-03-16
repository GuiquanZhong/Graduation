import request from './index'

export function toggleLike(postId) {
    return request.post(`/post/like/${postId}`)
}

export function getUserLikedPosts(page = 1, size = 10) {
    return request.get('/post/like/user', { params: { page, size } })
}
