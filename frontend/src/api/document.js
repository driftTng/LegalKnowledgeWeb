import request from './request'

export function getDocumentsApi(params) {
  return request.get('/documents', { params })
}

export function getDocumentApi(id) {
  return request.get(`/documents/${id}`)
}

export function getCategoriesApi() {
  return request.get('/documents/categories')
}

export function downloadDocumentPdf(id) {
  return `/api/documents/${id}/pdf`
}
