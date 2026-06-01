import { describe, it, expect } from 'vitest'

describe('router', () => {
  it('is configured with routes', async () => {
    const { default: router } = await import('@/router')
    expect(router).toBeDefined()
    expect(router.hasRoute('Login')).toBe(true)
    expect(router.hasRoute('Dashboard')).toBe(true)
  })
})
