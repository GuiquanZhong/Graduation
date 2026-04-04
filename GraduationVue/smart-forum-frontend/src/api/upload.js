import request from './index'

/**
 * 上传图片到 OSS，返回图片 URL
 * @param {File} file
 */
export function uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/upload/image', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}
