import request from './index'

export function submitReport(postId, reason, description = '') {
    return request.post(`/report/post/${postId}`, { reason, description })
}

export function checkReported(postId) {
    return request.get(`/report/check/${postId}`)
}
