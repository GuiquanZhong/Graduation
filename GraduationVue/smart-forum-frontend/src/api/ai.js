import request from './index'

export function generateSummary(content) {
  return request.post('/ai/summary', { content })
}

export function generateTitle(content) {
  return request.post('/ai/title', { content })
}

export function polishContent(content) {
  return request.post('/ai/polish', { content })
}

export function aiChat(history, model = null) {
  return request.post('/ai/chat', { history, ...(model ? { model } : {}) })
}

export function aiPostChat(postTitle, postContent, history) {
  return request.post('/ai/post-chat', { postTitle, postContent, history })
}

export function listAiSessions() {
  return request.get('/ai/sessions')
}

export function getAiSession(id) {
  return request.get(`/ai/sessions/${id}`)
}

export function createAiSession(data) {
  return request.post('/ai/sessions', data)
}

export function updateAiSession(id, data) {
  return request.put(`/ai/sessions/${id}`, data)
}

export function deleteAiSession(id) {
  return request.delete(`/ai/sessions/${id}`)
}

export function aiCommentSuggest(postTitle, postContent, existingComments = '') {
  return request.post('/ai/comment-suggest', { postTitle, postContent, existingComments })
}

export function aiSemanticSearch(query) {
  return request.get('/ai/search', { params: { query } })
}
