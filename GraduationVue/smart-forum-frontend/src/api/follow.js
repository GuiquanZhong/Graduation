import request from './index'

export function toggleFollow(userId) {
    return request.post(`/user/follow/${userId}`)
}
