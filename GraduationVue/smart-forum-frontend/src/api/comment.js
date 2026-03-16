import request from './index'

export function getComments(postId) {
    return request.get(`/comment/list/${postId}`)
}

export function addComment(data) {
    return request.post('/comment/add', data)
}

export function deleteComment(id) {
    return request.delete(`/comment/delete/${id}`)
}
