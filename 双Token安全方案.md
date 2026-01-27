# 双Token安全方案

## 核心架构

```
┌─────────────────────────────────────────────────────────────┐
│  客户端                                              │
│  ┌───────────────────────────────────────────────┐   │
│  │ Access Token (短期)                   │   │
│  │ - 存储位置: localStorage           │   │
│  │ - 有效期: 15-30分钟              │   │
│  │ - 用途: API请求认证                │   │
│  └───────────────────────────────────────────────┘   │
│  ┌───────────────────────────────────────────────┐   │
│  │ Refresh Token (长期)                  │   │
│  │ - 存储位置: HttpOnly Cookie        │   │
│  │ - 有效期: 7-30天                │   │
│  │ - 安全属性: HttpOnly + Secure +    │   │
│  │             SameSite=Strict            │   │
│  └───────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────┐
│  服务端                                              │
│  ┌───────────────────────────────────────────────┐   │
│  │ Redis存储                               │   │
│  │ - refresh_token:{userId}             │   │
│  │ - user_security:{userId}              │   │
│  └───────────────────────────────────────────────┘   │
│  ┌───────────────────────────────────────────────┐   │
│  │ 安全监控                               │   │
│  │ - IP/设备变化检测                   │   │
│  │ - 异常访问记录                       │   │
│  └───────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

## 认证流程

### 登录流程

```
用户输入账号密码
    │
    ▼
┌─────────────────┐
│  后端验证    │
│  - 生成Access Token (15分钟)  │
│  - 生成Refresh Token (7天)   │
│  - 保存到Redis               │
│  - 设置HttpOnly Cookie         │
└─────────────────┘
    │
    ▼
┌─────────────────┐
│  前端存储    │
│  - Access Token → localStorage  │
│  - Refresh Token → Cookie (自动) │
└─────────────────┘
    │
    ▼
  登录成功
```

### Token刷新流程

```
Access Token即将过期
    │
    ▼
┌─────────────────┐
│  检测异常访问  │
│  - IP变化?                         │
│  - 设备变化?                       │
│  - 记录异常日志                   │
└─────────────────┘
    │
    ▼
┌─────────────────┐
│  验证Refresh Token  │
│  - 从Cookie读取                │
│  - 对比Redis存储               │
│  - 检查是否过期               │
└─────────────────┘
    │
    ▼
┌─────────────────┐
│  生成新Token    │
│  - 新Access Token (15分钟)   │
│  - 新Refresh Token (7天)     │
│  - 更新Cookie                   │
└─────────────────┘
    │
    ▼
  刷新成功
```

## 安全特性

### Access Token (短期)

| 特性 | 说明 |
|------|------|
| **存储位置** | localStorage |
| **有效期** | 15-30分钟 |
| **用途** | API请求认证 |
| **传输方式** | Authorization: Bearer {token} |
| **安全性** | 短期降低泄露风险 |

### Refresh Token (长期)

| 特性 | 说明 |
|------|------|
| **存储位置** | HttpOnly Cookie |
| **有效期** | 7-30天 |
| **用途** | 刷新Access Token |
| **HttpOnly** | ✅ 防止XSS窃取 |
| **Secure** | ✅ 仅HTTPS传输 |
| **SameSite** | ✅ Strict模式防CSRF |

## 异常检测机制

### IP/设备监控

```
正常访问:
用户A (IP: 192.168.1.100) → 服务器 → ✅ 允许

异常访问:
用户A (IP: 192.168.1.100) → 服务器 → ✅ 允许 (仅记录)
用户A (IP: 203.0.113.1) → 服务器 → ⚠️ 检测到IP变化
                                       → 记录异常日志
                                       → 下次刷新Token时强制重新登录
```

### 触发场景

| 场景 | 行为 |
|------|------|
| **Access Token请求** | 仅记录异常，不阻止 |
| **Refresh Token请求** | 检测到异常则强制失效 |
| **正常切换网络** | 记录但不阻止 |
| **账号被盗用** | 刷新时强制重新登录 |

## 技术实现

### 前端关键代码

```javascript
// 1. 登录时存储Token
userStore.login({
  token: response.data.token  // Access Token → localStorage
})
// Refresh Token由后端通过HttpOnly Cookie自动设置，前端无法获取

// 2. 请求时携带Access Token
axios.interceptors.request.use(config => {
  const token = userStore.token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 3. 自动刷新Token
axios.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      // Access Token过期，自动刷新
      await userStore.refreshToken()
    }
    return Promise.reject(error)
  }
)
```

### 后端关键代码

```java
// 1. 登录时设置Cookie
@PostMapping("/login")
public ResultVO<?> login(@RequestBody LoginRequest request) {
    // 生成Token
    String token = jwtUtil.generateToken(user.getUsername());
    String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
    
    // 存储Refresh Token到Redis
    refreshTokenService.createRefreshToken(user.getId(), user.getUsername());
    
    // 设置HttpOnly Cookie
    cookieUtil.addRefreshTokenCookie(response, refreshToken, 7 * 24 * 3600);
    
    return ResultVO.success(Map.of(
        "token", token,
        "refreshToken", refreshToken
    ));
}

// 2. 刷新时检查异常
@PostMapping("/refresh")
public ResultVO<?> refresh(@RequestBody RefreshRequest request) {
    // 从Cookie获取Refresh Token
    String refreshToken = cookieUtil.getRefreshTokenFromRequest(request)
        .orElseThrow(() -> new RuntimeException("无效的刷新令牌"));
    
    // 检查访问异常
    boolean hasAnomaly = securityMonitorUtil.checkAccessAnomaly(userId, request);
    if (hasAnomaly) {
        // 强制Token失效
        securityMonitorUtil.invalidateUserTokens(userId);
        throw new RuntimeException("检测到异常访问，请重新登录");
    }
    
    // 生成新Token
    String newToken = jwtUtil.generateToken(username);
    String newRefreshToken = jwtUtil.generateRefreshToken(username);
    
    // 更新Cookie
    cookieUtil.addRefreshTokenCookie(response, newRefreshToken, 7 * 24 * 3600);
    
    return ResultVO.success(Map.of("token", newToken));
}

// 3. 登出时清理
@PostMapping("/logout")
public ResultVO<?> logout(HttpServletRequest request, HttpServletResponse response) {
    // 清除用户安全信息（包含Refresh Token）
    securityMonitorUtil.invalidateUserTokens(userId);
    
    // 清除Cookie
    cookieUtil.clearRefreshTokenCookie(response);
    
    return ResultVO.success("登出成功");
}
```

## 安全优势

### 传统方案 vs 双Token方案

| 对比项 | 传统方案 | 双Token方案 |
|--------|----------|------------|
| **Refresh Token存储** | localStorage | HttpOnly Cookie |
| **XSS防护** | ❌ 容易被窃取 | ✅ HttpOnly保护 |
| **CSRF防护** | ❌ 需额外配置 | ✅ SameSite自动防护 |
| **异常检测** | ❌ 无 | ✅ IP/设备监控 |
| **强制登出** | ❌ 无法实现 | ✅ 异常时强制失效 |

## XSS防护集成

### 前端输入验证

```javascript
import { validateInput } from '../utils/security'

// 登录时清理用户名
const cleanedUsername = validateInput(loginForm.username, { maxLength: 20 })

// 提交表单时清理所有字段
const cleanedData = {
  username: validateInput(formData.username, { maxLength: 20 }),
  nickname: validateInput(formData.nickname, { maxLength: 50 }),
  email: validateInput(formData.email, { maxLength: 100 }),
  phone: validateInput(formData.phone, { maxLength: 20 })
}
```

### 防护功能

| 功能 | 说明 |
|------|------|
| **HTML转义** | 自动转义 `< > " ' /` 等特殊字符 |
| **输入验证** | 长度限制 + 正则验证 |
| **URL安全** | 检查危险协议 (javascript:等) |
| **JSON安全** | 防止原型污染攻击 |

## 总结

### 核心优势

1. **安全性提升**
   - ✅ HttpOnly Cookie防止XSS窃取
   - ✅ SameSite防止CSRF攻击
   - ✅ IP/设备监控检测异常
   - ✅ 异常时强制Token失效

2. **用户体验**
   - ✅ Access Token短期降低泄露风险
   - ✅ Refresh Token长期保持登录状态
   - ✅ 自动刷新无需频繁登录
   - ✅ 异常检测不影响正常使用

3. **架构清晰**
   - ✅ 短期Token用于API认证
   - ✅ 长期Token用于刷新机制
   - ✅ 职责分离，易于维护

### 技术栈

- **后端**: Spring Boot 3.1.0 + Java 17
- **前端**: Vue 3 + Element Plus
- **存储**: Redis (Token + 安全信息)
- **安全**: JWT + HttpOnly Cookie + XSS防护
