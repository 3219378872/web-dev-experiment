import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
const request = axios.create({ baseURL: '/api', timeout: 10000 })
request.interceptors.request.use(config => {
  const token = localStorage.getItem('adminToken')
  if (token) config.headers.authorization = token
  return config
})
request.interceptors.response.use(
  r => r.data,
  error => {
    if (error.response?.status === 401) { localStorage.removeItem('adminToken'); router.push('/login') }
    if (error.response?.status === 403) { ElMessage.error('无权限访问') }
    ElMessage.error(error.response?.data?.msg || '请求失败')
    return Promise.reject(error)
  }
)
export default request
