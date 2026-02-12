const TOKEN_KEY = 'uiaut_token'
const USER_KEY = 'uiaut_user'

/**
 * Token管理工具
 */
export const Auth = {
  /**
   * 获取Token
   */
  getToken() {
    return localStorage.getItem(TOKEN_KEY)
  },

  /**
   * 设置Token
   */
  setToken(token) {
    localStorage.setItem(TOKEN_KEY, token)
  },

  /**
   * 移除Token
   */
  removeToken() {
    localStorage.removeItem(TOKEN_KEY)
  },

  /**
   * 获取用户信息
   */
  getUser() {
    const userStr = localStorage.getItem(USER_KEY)
    if (userStr) {
      try {
        return JSON.parse(userStr)
      } catch (e) {
        return null
      }
    }
    return null
  },

  /**
   * 设置用户信息
   */
  setUser(user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  },

  /**
   * 移除用户信息
   */
  removeUser() {
    localStorage.removeItem(USER_KEY)
  },

  /**
   * 清除所有认证信息
   */
  clear() {
    this.removeToken()
    this.removeUser()
  },

  /**
   * 检查是否已登录
   */
  isLoggedIn() {
    return !!this.getToken()
  },

  /**
   * 检查是否是管理员
   */
  isAdmin() {
    const user = this.getUser()
    return user && user.role === 'ADMIN'
  }
}

export default Auth
