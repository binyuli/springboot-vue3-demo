# Spring Boot + Vue3 全栈项目

一个基于 Spring Boot 3.x 和 Vue 3 的前后端分离项目，专注于用户管理和认证授权功能，采用双Token安全方案和安全监控机制。

## 技术栈

### 后端
- **Java 17**
- **Spring Boot 3.1.0**
- **Spring Security** - 安全认证框架
- **MyBatis Plus 3.5.7** - ORM框架
- **Redis 7.0** - 缓存（用于Refresh Token存储、安全监控信息）
- **MySQL 8.0** - 关系型数据库
- **JWT (jjwt 0.12.3)** - Token认证
- **Docker** - 容器化部署

### 前端
- **Vue 3.5.24** - 渐进式JavaScript框架
- **Vite 7.2.4** - 构建工具
- **Pinia 3.0.4** - 状态管理
- **Vue Router 4.6.4** - 路由管理
- **Element Plus 2.13.1** - UI组件库
- **Axios 1.13.2** - HTTP客户端（含Token自动刷新机制）

## 项目架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         客户端                                │
│                   ┌───────────────┐                          │
│                   │   Vue3 前端   │                          │
│                   │  (localhost:  │                          │
│                   │   80)         │                          │
│                   └───────┬───────┘                          │
│                           │                                  │
│           Access Token: localStorage                          │
│           Refresh Token: HttpOnly Cookie                      │
└───────────────────────────┼───────────────────────────────────┘
                            │ HTTP/HTTPS
                            │ Access Token (Authorization Header)
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Spring Boot 后端                          │
│                   ┌───────────────┐                          │
│                   │ Spring Security│                          │
│                   │  JWT过滤器     │                          │
│                   └───────┬───────┘                          │
│                           │                                  │
│         ┌─────────────────┼─────────────────┐                │
│         │                 │                 │                │
│   ┌─────▼─────┐   ┌─────▼─────┐   ┌─────▼─────┐          │
│   │ Controller│   │  Service  │   │  Mapper   │          │
│   │   层     │   │   层      │   │   层      │          │
│   └─────┬─────┘   └─────┬─────┘   └─────┬─────┘          │
│         │                 │                 │                │
│         └─────────────────┼─────────────────┘                │
│                           │                                  │
│         ┌─────────────────┼─────────────────┐                │
│         │                 │                 │                │
│   ┌─────▼─────┐   ┌─────▼─────┐   ┌─────▼─────┐          │
│   │  Redis    │   │  MySQL    │   │ 安全监控   │          │
│   │  -Refresh │   │  数据库   │   │ -IP监控   │          │
│   │   Token   │   │           │   │ -设备指纹 │          │
│   │  -安全信息 │   │           │   │ -异常检测 │          │
│   └───────────┘   └───────────┘   └───────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

## 数据流

### 用户登录流程

```
┌─────────┐      1. 登录请求      ┌──────────────┐
│  前端   │ ───────────────────▶ │ Spring Boot  │
│  Vue3   │                     │   后端       │
└─────────┘                     └──────┬───────┘
                                       │
                              2. 验证用户名密码
                                       │
                              3. 检查登录尝试次数（防暴力破解）
                                       │
                              4. 生成Access Token (1小时)
                                       │
                              5. 生成Refresh Token (24小时)
                                       │
                              6. 存储Refresh Token到Redis
                                       │
                              7. 设置HttpOnly Cookie (Refresh Token)
                                       │
                              8. 记录用户安全信息（IP、设备指纹）
                                       │
┌─────────┐      9. 返回Token      ┌──────▼───────┐
│  前端   │ ◀─────────────────── │ Spring Boot  │
│  Vue3   │   (Access Token +    │   后端       │
└─────────┘    HttpOnly Cookie)  └──────────────┘
        │
        │ 10. 存储Access Token到localStorage
        │     Refresh Token自动存储到Cookie
        │
        ▼
    登录成功
```

### API请求流程

```
┌─────────┐  1. 请求+Access Token  ┌──────────────┐
│  前端   │ ──────────────────────▶ │ Spring Boot  │
│  Vue3   │  (Authorization Header) │   后端       │
└─────────┘                         └──────┬───────┘
                                           │
                                  2. JWT过滤器验证Access Token
                                           │
                                  3. 检查访问异常（IP、设备变化）
                                           │
                                  4. 查询Redis缓存（用户信息）
                                           │
                                  5. 缓存未命中，查询MySQL
                                           │
                                  6. 返回数据
                                           │
┌─────────┐      7. 返回数据       ┌──────▼───────┐
│  前端   │ ◀─────────────────── │ Spring Boot  │
│  Vue3   │                     │   后端       │
└─────────┘                     └──────────────┘
```

### Token刷新流程

```
┌─────────┐  1. Access Token过期   ┌──────────────┐
│  前端   │ ──────────────────────▶ │ Spring Boot  │
│  Vue3   │  自动触发刷新请求       │   后端       │
└─────────┘  (携带Refresh Token)   └──────┬───────┘
                                           │
                                  2. 从Cookie读取Refresh Token
                                           │
                                  3. 验证Refresh Token（对比Redis）
                                           │
                                  4. 检查访问异常（IP、设备变化）
                                           │
                                  5. 异常时强制Token失效
                                           │
                                  6. 生成新Access Token
                                           │
                                  7. 生成新Refresh Token
                                           │
                                  8. 更新Redis存储
                                           │
                                  9. 更新HttpOnly Cookie
                                           │
┌─────────┐    10. 返回新Token     ┌──────▼───────┐
│  前端   │ ◀─────────────────── │ Spring Boot  │
│  Vue3   │                     │   后端       │
└─────────┘                     └──────────────┘
        │
        │ 11. 更新localStorage中的Access Token
        │     Cookie自动更新
        │
        ▼
    刷新成功（用户无感知）
```

### 用户登出流程

```
┌─────────┐      1. 登出请求      ┌──────────────┐
│  前端   │ ───────────────────▶ │ Spring Boot  │
│  Vue3   │                     │   后端       │
└─────────┘                     └──────┬───────┘
                                       │
                              2. 删除Redis中的Refresh Token
                                       │
                              3. 清除HttpOnly Cookie
                                       │
                              4. 清除用户安全信息
                                       │
┌─────────┐      5. 返回成功       ┌──────▼───────┐
│  前端   │ ◀─────────────────── │ Spring Boot  │
│  Vue3   │                     │   后端       │
└─────────┘                     └──────────────┘
        │
        │ 6. 清除localStorage中的Access Token
        │
        ▼
    登出成功
```

## 项目结构

```
springboot-vue3-demo/
├── src/                             # 后端项目
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── config/         # 配置类
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── RedisConfig.java
│   │   │   ├── controller/     # 控制器
│   │   │   │   ├── UserController.java
│   │   │   │   └── AuthController.java
│   │   │   ├── entity/         # 实体类
│   │   │   │   └── User.java
│   │   │   ├── mapper/         # 数据访问层
│   │   │   │   └── UserMapper.java
│   │   │   ├── service/        # 业务逻辑层
│   │   │   │   ├── UserService.java
│   │   │   │   ├── RefreshTokenService.java
│   │   │   │   └── impl/
│   │   │   │       ├── UserServiceImpl.java
│   │   │   │       └── RefreshTokenServiceImpl.java
│   │   │   ├── util/           # 工具类
│   │   │   │   ├── JwtUtil.java
│   │   │   │   ├── CookieUtil.java
│   │   │   │   └── SecurityMonitorUtil.java
│   │   │   ├── vo/             # 视图对象
│   │   │   │   └── ResultVO.java
│   │   │   └── SpringbootVue3DemoApplication.java
│   │   └── resources/
│   │       ├── application.yml  # 应用配置
│   │       └── logback.xml     # 日志配置
│   └── test/
├── pom.xml                     # Maven配置
├── Dockerfile                  # Docker镜像构建
└── .dockerignore              # Docker忽略文件
│
├── frontend/                        # 前端项目
│   ├── src/
│   │   ├── api/                   # API接口
│   │   │   ├── auth.js
│   │   │   └── user.js
│   │   ├── assets/                # 静态资源
│   │   ├── components/             # 公共组件
│   │   ├── router/                # 路由配置
│   │   │   └── index.js
│   │   ├── store/                 # Pinia状态管理
│   │   │   └── user.js
│   │   ├── utils/                 # 工具函数
│   │   │   ├── axios.js           # Axios封装（含Token自动刷新）
│   │   │   └── security.js        # 安全工具类（XSS防护等）
│   │   ├── views/                 # 页面组件
│   │   │   ├── Login.vue          # 现代化登录页面
│   │   │   ├── Layout.vue
│   │   │   └── user/
│   │   │       └── index.vue
│   │   ├── App.vue
│   │   └── main.js
│   ├── public/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── .dockerignore
│
├── mysql/
│   └── init.sql                   # 数据库初始化脚本
│
├── 双Token安全方案.md              # 双Token安全方案文档
├── docker-compose.yml              # Docker Compose配置
├── README.md                      # 项目文档
└── .gitignore                    # Git忽略文件
```

## 快速开始

### 前置要求

- Docker 和 Docker Compose
- Node.js 16+ (用于本地开发)
- Java 17+ (用于本地开发)

### 使用 Docker Compose 启动

```bash
# 启动所有服务（MySQL、Redis、Spring Boot）
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

### 本地开发

#### 后端

```bash
# 进入后端目录
cd backend

# 使用Maven构建
mvn clean package -DskipTests

# 运行应用
java -jar target/*.jar
```

#### 前端

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin  | 123456 | 管理员 |
| user1  | 123456 | 普通用户 |

## API文档

### 认证接口

#### 登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "Admin",
      "email": "admin@example.com",
      "phone": "13800138000",
      "gender": 1
    }
  }
}

Set-Cookie: refresh_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...; 
             HttpOnly; Secure; SameSite=Strict; Max-Age=86400; Path=/
```

**说明**：
- Access Token返回在响应体中，需存储到localStorage
- Refresh Token通过HttpOnly Cookie自动设置，前端无需手动处理
- Access Token有效期：1小时
- Refresh Token有效期：24小时

#### 刷新Token
```
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}

Set-Cookie: refresh_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...; 
             HttpOnly; Secure; SameSite=Strict; Max-Age=86400; Path=/
```

**说明**：
- Refresh Token从Cookie中自动读取
- 检测到IP或设备变化时，会强制Token失效
- 返回新的Access Token和Refresh Token

#### 登出
```
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response:
{
  "code": 200,
  "message": "登出成功"
}

Set-Cookie: refresh_token=; 
             HttpOnly; Secure; SameSite=Strict; Max-Age=0; Path=/
```

**说明**：
- 删除Redis中的Refresh Token
- 清除HttpOnly Cookie
- 清除用户安全信息

### 用户接口

#### 分页查询用户
```
POST /api/user/page
Authorization: Bearer {token}
Content-Type: application/json

{
  "pageNum": 1,
  "pageSize": 10,
  "username": ""
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "total": 13
  }
}
```

#### 新增用户
```
POST /api/user
Authorization: Bearer {token}
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "nickname": "New User",
  "email": "newuser@example.com",
  "phone": "13800138000",
  "gender": 1,
  "status": 1
}
```

#### 更新用户
```
PUT /api/user
Authorization: Bearer {token}
Content-Type: application/json

{
  "id": 1,
  "username": "admin",
  "nickname": "Admin",
  "email": "admin@example.com",
  "phone": "13800138000",
  "gender": 1,
  "status": 1
}
```

#### 删除用户
```
DELETE /api/user/{id}
Authorization: Bearer {token}
```

## 部署说明

### Docker部署

1. 克隆项目
```bash
git clone <repository-url>
cd springboot-vue3-demo
```

2. 启动服务
```bash
docker-compose up -d
```

3. 访问应用
- 前端：http://localhost:80
- 后端API：http://localhost:8080/api
- MySQL：localhost:3306
- Redis：localhost:6379

4. 查看服务状态
```bash
docker-compose ps
```

5. 查看日志
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f app
docker-compose logs -f frontend
docker-compose logs -f mysql
docker-compose logs -f redis
```

6. 停止服务
```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

### 生产环境部署

1. 修改配置文件
   - `application.yml` - 修改数据库、Redis连接信息
   - `docker-compose.yml` - 修改端口映射、环境变量
   - `JWT_SECRET` - 修改为生产环境的安全密钥

2. 构建镜像
```bash
docker-compose build
```

3. 启动服务
```bash
docker-compose up -d
```

4. 配置Nginx（可选）
如果需要使用Nginx作为反向代理，可以参考以下配置：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://app:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 内存优化

当前Docker Compose配置已优化内存使用：

| 服务 | 内存限制 | 说明 |
|------|---------|------|
| MySQL | 256MB | innodb_buffer_pool_size=128M |
| Redis | 64MB | maxmemory=50mb |
| Spring Boot | 256MB | JAVA_OPTS: -Xmx200m -Xms100m |
| Frontend (Nginx) | 64MB | Alpine镜像，轻量级 |

**总内存需求**：约640MB（含系统余量）

## 核心功能

### 1. 双Token安全认证方案

#### Access Token（短期）
- **存储位置**：localStorage
- **有效期**：1小时
- **用途**：API请求认证
- **传输方式**：Authorization: Bearer {token}
- **验证方式**：JWT签名验证（无状态）
- **安全性**：短期降低泄露风险

#### Refresh Token（长期）
- **存储位置**：HttpOnly Cookie
- **有效期**：24小时
- **用途**：刷新Access Token
- **安全属性**：
  - HttpOnly：防止XSS窃取
  - Secure：仅HTTPS传输
  - SameSite=Strict：防止CSRF攻击
- **验证方式**：Redis存储验证（有状态）
- **安全性**：可主动撤销，防止重放攻击

#### Token刷新机制
- Access Token即将过期时，自动触发刷新
- 前端Axios拦截器自动处理刷新逻辑
- 刷新成功后，自动更新localStorage和Cookie
- 用户无感知，无需重新登录

### 2. 安全监控与异常检测

#### IP地址监控
- 记录用户每次登录的IP地址
- 检测IP地址变化
- IP变化时记录异常日志
- 支持多级代理环境下的真实IP获取

#### 设备指纹监控
- 基于User-Agent生成设备指纹
- 检测设备变化
- 设备变化时记录异常日志
- 支持跨设备访问检测

#### 异常访问处理
- 检测到IP或设备变化时，记录异常信息
- 异常信息保存90天用于审计
- 可配置异常时是否强制登出
- 支持手动查看用户安全信息

#### 防暴力破解
- 记录登录失败次数
- 超过5次失败后锁定账户15分钟
- 登录成功后自动清除失败记录
- 支持自定义锁定时间和尝试次数

### 3. Cookie安全管理

#### HttpOnly Cookie
- 防止JavaScript访问Cookie
- 有效防止XSS攻击窃取Token
- 自动管理Cookie生命周期

#### Secure Cookie
- 仅在HTTPS环境下传输
- 生产环境必须启用
- 开发环境可通过配置禁用

#### SameSite Cookie
- Strict模式防止CSRF攻击
- 跨站请求时自动携带Cookie
- 支持灵活的SameSite策略配置

### 4. 前端安全防护

#### XSS防护
- HTML实体编码转义
- 危险标签和属性过滤
- 安全的innerHTML设置
- URL安全检查

#### 输入验证
- 长度限制
- 正则表达式验证
- 自动清理HTML内容
- 防止原型污染攻击

#### 安全工具类
- `escapeHtml()` - HTML转义
- `sanitizeHtml()` - HTML清理
- `isSafeUrl()` - URL安全检查
- `validateInput()` - 输入验证
- `safeJsonParse()` - 安全JSON解析

### 5. 用户管理
- 用户CRUD操作
- 分页查询
- 用户状态管理
- 密码BCrypt加密存储
- 用户信息缓存（Redis）

### 6. 分布式支持
- Access Token无状态，天然支持分布式
- Refresh Token存储在Redis，支持多实例共享
- 用户安全信息存储在Redis
- 所有实例使用相同的JWT密钥
- 支持负载均衡部署

### 7. 前端特性
- Vue 3 Composition API
- Element Plus UI组件库
- Pinia状态管理
- 响应式布局
- 现代化登录页面设计
- Axios拦截器自动处理Token刷新
- 自动处理401错误，触发Token刷新

### 8. 性能优化
- Redis缓存用户信息
- Redis缓存Refresh Token
- JWT无状态验证，无需查询存储
- 数据库连接池优化
- 前端资源懒加载
- Docker容器内存优化

## 常见问题

### 1. 数据库连接失败
检查MySQL容器是否正常运行：
```bash
docker ps | grep mysql
docker logs springboot-mysql
```

### 2. Redis连接失败
检查Redis容器是否正常运行：
```bash
docker ps | grep redis
docker logs springboot-redis
```

### 3. 前端无法访问后端
检查：
- 后端服务是否启动：`docker ps | grep springboot-app`
- 后端日志：`docker logs springboot-app`
- 端口是否被占用：`netstat -ano | findstr 8080`

### 4. 登录失败
检查：
- 用户名密码是否正确
- 数据库中是否有该用户
- JWT配置是否正确

## 开发规范

### 后端
- 遵循RESTful API设计规范
- 统一返回格式：`ResultVO`
- 统一异常处理：`GlobalExceptionHandler`
- 代码注释使用中文

### 前端
- 使用Vue 3 Composition API
- 统一使用Pinia进行状态管理
- 统一使用Axios进行HTTP请求
- 代码注释使用中文