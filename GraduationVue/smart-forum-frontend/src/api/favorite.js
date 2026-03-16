import request from './index'

export function toggleFavorite(postId) {
    return request.post(`/post/favorite/${postId}`)
}

export function getUserFavoritePosts(page = 1, size = 10) {
    return request.get('/post/favorite/user', { params: { page, size } })
}
