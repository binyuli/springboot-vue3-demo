# Spring Boot + Vue3 全栈项目

一个基于 Spring Boot 3.x 和 Vue 3 的前后端分离项目，专注于用户管理和认证授权功能。

## 技术栈

### 后端
- **Java 17**
- **Spring Boot 3.1.0**
- **Spring Security** - 安全认证框架
- **MyBatis Plus 3.5.7** - ORM框架
- **Redis** - 缓存
- **MySQL 8.0** - 关系型数据库
- **JWT** - Token认证
- **Docker** - 容器化部署

### 前端
- **Vue 3** - 渐进式JavaScript框架
- **Vite** - 构建工具
- **Pinia** - 状态管理
- **Vue Router** - 路由管理
- **Element Plus** - UI组件库
- **Axios** - HTTP客户端

## 项目架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         客户端                                │
│                   ┌───────────────┐                          │
│                   │   Vue3 前端   │                          │
│                   │  (localhost:  │                          │
│                   │   3002)       │                          │
│                   └───────┬───────┘                          │
└───────────────────────────┼───────────────────────────────────┘
                            │ HTTP/HTTPS
                            │ JWT Token
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
│   │  Redis    │   │  MySQL    │   │  业务逻辑  │          │
│   │  缓存     │   │  数据库   │   │           │          │
│   └───────────┘   └───────────┘   └───────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

## 数据流

```
用户登录流程：
┌─────────┐      1. 登录请求      ┌──────────────┐
│  前端   │ ───────────────────▶ │ Spring Boot  │
│  Vue3   │                     │   后端       │
└─────────┘                     └──────┬───────┘
                                       │
                              2. 验证用户名密码
                                       │
                              3. 生成JWT Token
                                       │
┌─────────┐      4. 返回Token      ┌──────▼───────┐
│  前端   │ ◀─────────────────── │ Spring Boot  │
│  Vue3   │                     │   后端       │
└─────────┘                     └──────────────┘
        │
        │ 5. 存储Token到localStorage
        │
        │ 6. 后续请求携带Token
        ▼
┌─────────┐      7. 请求+Token     ┌──────────────┐
│  前端   │ ───────────────────▶ │ Spring Boot  │
│  Vue3   │                     │   后端       │
└─────────┘                     └──────┬───────┘
                                       │
                              8. JWT过滤器验证Token
                                       │
                              9. 查询Redis缓存
                                       │
                              10. 缓存未命中，查询MySQL
                                       │
                              11. 返回数据
                                       │
┌─────────┐      12. 返回数据       ┌──────▼───────┐
│  前端   │ ◀─────────────────── │ Spring Boot  │
│  Vue3   │                     │   后端       │
└─────────┘                     └──────────────┘
```

## 项目结构

```
springboot-vue3-demo/
├── backend/                          # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── config/         # 配置类
│   │   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   └── RedisConfig.java
│   │   │   │   ├── controller/     # 控制器
│   │   │   │   │   ├── UserController.java
│   │   │   │   │   └── AuthController.java
│   │   │   │   ├── entity/         # 实体类
│   │   │   │   │   └── User.java
│   │   │   │   ├── mapper/         # 数据访问层
│   │   │   │   │   └── UserMapper.java
│   │   │   │   ├── service/        # 业务逻辑层
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   └── impl/
│   │   │   │   │       └── UserServiceImpl.java
│   │   │   │   ├── util/           # 工具类
│   │   │   │   │   ├── JwtUtil.java
│   │   │   │   │   └── PasswordUtil.java
│   │   │   │   ├── vo/             # 视图对象
│   │   │   │   │   ├── ResultVO.java
│   │   │   │   │   └── LoginVO.java
│   │   │   │   └── SpringbootVue3DemoApplication.java
│   │   │   └── resources/
│   │   │       ├── application.yml  # 应用配置
│   │   │       └── logback.xml     # 日志配置
│   │   └── test/
│   ├── pom.xml                     # Maven配置
│   ├── Dockerfile                  # Docker镜像构建
│   └── .dockerignore              # Docker忽略文件
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
│   │   ├── stores/                # Pinia状态管理
│   │   │   ├── user.js
│   │   │   └── index.js
│   │   ├── utils/                 # 工具函数
│   │   │   ├── request.js         # Axios封装
│   │   │   └── auth.js           # Token管理
│   │   ├── views/                 # 页面组件
│   │   │   ├── Login.vue
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
    "userInfo": {
      "id": 1,
      "username": "admin",
      "nickname": "Admin",
      "email": "admin@example.com",
      "phone": "13800138000",
      "gender": 1
    }
  }
}
```

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
- 前端：http://localhost:3002
- 后端API：http://localhost:8080/api
- MySQL：localhost:3306
- Redis：localhost:6379

### 生产环境部署

1. 修改配置文件
   - `application.yml` - 修改数据库、Redis连接信息
   - `docker-compose.yml` - 修改端口映射、环境变量

2. 构建镜像
```bash
docker-compose build
```

3. 启动服务
```bash
docker-compose up -d
```

## 核心功能

### 1. JWT认证
- 基于Token的无状态认证
- 安全的Base64编码密钥
- 支持HS256算法
- 自动处理过期Token

### 2. 用户管理
- 用户CRUD操作
- 分页查询
- 用户状态管理
- 密码BCrypt加密存储
- 批量删除功能

### 3. 安全特性
- Spring Security集成
- 基于角色的访问控制
- CORS预检请求支持
- 密码强度验证

### 4. 前端特性
- Vue 3 Composition API
- Element Plus UI组件库
- Pinia状态管理
- 响应式布局
- 暗黑模式支持

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