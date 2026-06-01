import { describe, it, expect, vi } from 'vitest'

describe('axios instance', () => {
  it('is configured with correct baseURL and timeout', async () => {
    const mod = await import('axios')
    const axios = mod.default

    const createSpy = vi.spyOn(axios, 'create')

    // Re-import triggers the module-level axios.create() call
    await import('@/api/request')

    expect(createSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        baseURL: '/api',
        timeout: 10000
      })
    )

    createSpy.mockRestore()
  })
})
