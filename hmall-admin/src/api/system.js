import request from './request'
export const getNotifications = (params) => request.get('/admin/notifications', { params })
export const saveNotification = (data) => request.post('/admin/notifications', data)
export const updateNotification = (id, data) => request.put(`/admin/notifications/${id}`, data)
export const deleteNotification = (id) => request.delete(`/admin/notifications/${id}`)
export const getFeedbacks = (params) => request.get('/admin/feedbacks', { params })
export const replyFeedback = (id, data) => request.put(`/admin/feedbacks/${id}/reply`, data)
