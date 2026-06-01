import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import App from '@/App.vue'

describe('App.vue', () => {
  it('mounts without crashing', () => {
    const wrapper = mount(App, {
      global: {
        stubs: {
          AppHeader: true,
          AppFooter: true,
          'router-view': true
        }
      }
    })
    expect(wrapper.exists()).toBe(true)
  })
})
