/**
 * 前端安全工具类
 * 提供XSS过滤、输入验证、安全编码等功能
 */

/**
 * HTML实体编码映射表
 */
const htmlEntities = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#x27;',
  '/': '&#x2F;',
  '`': '&#x60;',
  '=': '&#x3D;'
}

/**
 * 转义HTML特殊字符，防止XSS攻击
 * @param {string} str 要转义的字符串
 * @returns {string} 转义后的字符串
 */
export function escapeHtml(str) {
  if (typeof str !== 'string') {
    return str
  }
  
  return str.replace(/[&<>"'`=\/]/g, char => htmlEntities[char])
}

/**
 * 清理HTML字符串，移除危险标签和属性
 * @param {string} html 要清理的HTML字符串
 * @param {Object} options 配置选项
 * @param {Array} options.allowedTags 允许的标签列表，默认安全的标签
 * @param {Array} options.allowedAttributes 允许的属性列表
 * @returns {string} 清理后的HTML字符串
 */
export function sanitizeHtml(html, options = {}) {
  if (typeof html !== 'string') {
    return html
  }
  
  const defaultAllowedTags = [
    'b', 'i', 'em', 'strong', 'a', 'p', 'br', 'span', 'div',
    'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'ul', 'ol', 'li',
    'table', 'thead', 'tbody', 'tr', 'th', 'td'
  ]
  
  const defaultAllowedAttributes = {
    'a': ['href', 'title', 'target'],
    'img': ['src', 'alt', 'title'],
    '*': ['class', 'style', 'id']
  }
  
  const allowedTags = options.allowedTags || defaultAllowedTags
  const allowedAttributes = options.allowedAttributes || defaultAllowedAttributes
  
  // 创建一个临时DOM元素来解析HTML
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = html
  
  // 递归清理节点
  function sanitizeNode(node) {
    if (node.nodeType === Node.TEXT_NODE) {
      return escapeHtml(node.textContent)
    }
    
    if (node.nodeType !== Node.ELEMENT_NODE) {
      return ''
    }
    
    const tagName = node.tagName.toLowerCase()
    
    // 检查标签是否允许
    if (!allowedTags.includes(tagName)) {
      return ''
    }
    
    // 创建新元素
    const newElement = document.createElement(tagName)
    
    // 复制允许的属性
    const attributes = node.attributes
    for (let i = 0; i < attributes.length; i++) {
      const attr = attributes[i]
      const attrName = attr.name.toLowerCase()
      
      // 检查属性是否允许
      const allowedAttrs = allowedAttributes[tagName] || allowedAttributes['*'] || []
      if (allowedAttrs.includes(attrName)) {
        // 对特定属性进行特殊处理
        if (attrName === 'href' || attrName === 'src') {
          // 验证URL
          const url = attr.value
          if (isSafeUrl(url)) {
            newElement.setAttribute(attrName, url)
          }
        } else {
          newElement.setAttribute(attrName, escapeHtml(attr.value))
        }
      }
    }
    
    // 递归处理子节点
    for (let child of node.childNodes) {
      const sanitizedChild = sanitizeNode(child)
      if (sanitizedChild) {
        if (typeof sanitizedChild === 'string') {
          newElement.appendChild(document.createTextNode(sanitizedChild))
        } else {
          newElement.appendChild(sanitizedChild)
        }
      }
    }
    
    return newElement
  }
  
  // 清理所有子节点
  const fragment = document.createDocumentFragment()
  for (let child of tempDiv.childNodes) {
    const sanitizedChild = sanitizeNode(child)
    if (sanitizedChild) {
      fragment.appendChild(sanitizedChild)
    }
  }
  
  // 返回清理后的HTML字符串
  const cleanDiv = document.createElement('div')
  cleanDiv.appendChild(fragment)
  return cleanDiv.innerHTML
}

/**
 * 检查URL是否安全（防止javascript:等危险协议）
 * @param {string} url 要检查的URL
 * @returns {boolean} 是否安全
 */
export function isSafeUrl(url) {
  if (!url) return false
  
  try {
    const parsed = new URL(url, window.location.origin)
    const protocol = parsed.protocol.toLowerCase()
    
    // 允许的协议
    const allowedProtocols = ['http:', 'https:', 'mailto:', 'tel:', 'data:image/']
    
    // 检查协议是否允许
    for (const allowed of allowedProtocols) {
      if (protocol.startsWith(allowed)) {
        return true
      }
    }
    
    // 如果是相对路径或空协议，则允许
    if (protocol === '' || protocol === window.location.protocol) {
      return true
    }
    
    return false
  } catch (e) {
    // URL解析失败，可能是不完整的相对路径
    return true
  }
}

/**
 * 验证和清理用户输入
 * @param {string} input 用户输入
 * @param {Object} options 验证选项
 * @param {number} options.maxLength 最大长度
 * @param {RegExp} options.pattern 正则表达式模式
 * @param {boolean} options.allowHtml 是否允许HTML
 * @returns {string} 清理后的输入
 */
export function validateInput(input, options = {}) {
  if (typeof input !== 'string') {
    input = String(input)
  }
  
  let result = input.trim()
  
  // 应用最大长度限制
  if (options.maxLength && result.length > options.maxLength) {
    result = result.substring(0, options.maxLength)
  }
  
  // 应用正则表达式验证
  if (options.pattern && !options.pattern.test(result)) {
    throw new Error('输入格式不正确')
  }
  
  // 清理HTML
  if (!options.allowHtml) {
    result = escapeHtml(result)
  } else if (options.allowHtml && options.sanitize !== false) {
    result = sanitizeHtml(result, options.sanitizeOptions)
  }
  
  return result
}

/**
 * 安全的JSON解析，防止原型污染攻击
 * @param {string} jsonStr JSON字符串
 * @returns {any} 解析后的对象
 */
export function safeJsonParse(jsonStr) {
  if (typeof jsonStr !== 'string') {
    return jsonStr
  }
  
  try {
    // 使用原生的JSON.parse
    const parsed = JSON.parse(jsonStr)
    
    // 防御原型污染攻击
    if (parsed && typeof parsed === 'object') {
      Object.setPrototypeOf(parsed, null)
      
      // 递归清理嵌套对象
      function cleanObject(obj) {
        if (obj && typeof obj === 'object') {
          Object.setPrototypeOf(obj, null)
          for (const key in obj) {
            if (Object.prototype.hasOwnProperty.call(obj, key)) {
              cleanObject(obj[key])
            }
          }
        }
      }
      
      cleanObject(parsed)
    }
    
    return parsed
  } catch (e) {
    console.error('JSON解析失败:', e)
    return null
  }
}

/**
 * 安全的设置innerHTML，自动清理HTML
 * @param {HTMLElement} element HTML元素
 * @param {string} html HTML内容
 * @param {Object} options 清理选项
 */
export function safeSetInnerHTML(element, html, options = {}) {
  if (!element || !(element instanceof HTMLElement)) {
    throw new Error('无效的HTML元素')
  }
  
  const cleanHtml = sanitizeHtml(html, options)
  element.innerHTML = cleanHtml
}

/**
 * 安全的设置属性，防止属性注入攻击
 * @param {HTMLElement} element HTML元素
 * @param {string} attribute 属性名
 * @param {string} value 属性值
 */
export function safeSetAttribute(element, attribute, value) {
  if (!element || !(element instanceof HTMLElement)) {
    throw new Error('无效的HTML元素')
  }
  
  const attrName = attribute.toLowerCase()
  
  // 检查属性名是否安全
  const unsafeAttributes = ['onerror', 'onload', 'onclick', 'onmouseover', 'onfocus', 'onblur']
  if (unsafeAttributes.some(unsafe => attrName.startsWith(unsafe))) {
    console.warn(`潜在的不安全属性: ${attribute}`)
    return
  }
  
  // 对特定属性进行特殊处理
  if (attrName === 'href' || attrName === 'src') {
    if (!isSafeUrl(value)) {
      console.warn(`不安全的URL: ${value}`)
      return
    }
  }
  
  element.setAttribute(attribute, escapeHtml(value))
}

/**
 * 创建安全的DOM元素
 * @param {string} tagName 标签名
 * @param {Object} attributes 属性对象
 * @param {string|HTMLElement} content 内容
 * @returns {HTMLElement} 创建的DOM元素
 */
export function createSafeElement(tagName, attributes = {}, content = '') {
  const element = document.createElement(tagName)
  
  // 设置属性
  for (const [attr, value] of Object.entries(attributes)) {
    safeSetAttribute(element, attr, value)
  }
  
  // 设置内容
  if (typeof content === 'string') {
    element.textContent = escapeHtml(content)
  } else if (content instanceof HTMLElement) {
    element.appendChild(content)
  }
  
  return element
}

/**
 * 安全的URL参数编码，防止URL注入
 * @param {Object} params 参数对象
 * @returns {string} 编码后的URL参数字符串
 */
export function safeUrlEncode(params) {
  const encodedParams = []
  
  for (const [key, value] of Object.entries(params)) {
    const encodedKey = encodeURIComponent(escapeHtml(key))
    const encodedValue = encodeURIComponent(escapeHtml(String(value)))
    encodedParams.push(`${encodedKey}=${encodedValue}`)
  }
  
  return encodedParams.join('&')
}

export default {
  escapeHtml,
  sanitizeHtml,
  isSafeUrl,
  validateInput,
  safeJsonParse,
  safeSetInnerHTML,
  safeSetAttribute,
  createSafeElement,
  safeUrlEncode
}