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

export function aiChat(question) {
  return request.post('/ai/chat', { question })
}