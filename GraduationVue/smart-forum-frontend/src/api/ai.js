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

export function aiChat(history) {
  return request.post('/ai/chat', { history })
}

export function aiPostChat(postTitle, postContent, history) {
  return request.post('/ai/post-chat', { postTitle, postContent, history })
}