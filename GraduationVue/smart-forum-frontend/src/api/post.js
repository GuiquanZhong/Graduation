import request from './index'

export function getPostList(page = 1, size = 10, sort = 'latest') {
    return request.get('/post/list', { params: { page, size, sort } })
}

export function getPostDetail(id) {
    return request.get(`/post/detail/${id}`)
}

export function createPost(data) {
    return request.post('/post/create', data)
}

export function searchPosts(keyword, page = 1, size = 10) {
    return request.get('/post/search', { params: { keyword, page, size } })
}

export function updatePost(id, data) {
    return request.put(`/post/update/${id}`, data)
}

export function deletePost(id) {
    return request.delete(`/post/delete/${id}`)
}

export function getHotSearch() {
    return request.get('/post/hot-search')
}

