# API Contract: Authentication

**Base Path**: `/ecommerce/api/v1/auth`

All endpoints in this contract are **public** (no token required).

---

## POST /auth/register (Create Account)

**Request Body** (`RegisterRequest`):
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePass123"
}
```

**Validation**:
- `username`: required, non-blank, 3-100 chars, unique
- `email`: required, valid email format, unique
- `password`: required, 8-100 chars

**Response** (201 Created):
```json
{
  "success": true,
  "status": 201,
  "message": "Account created successfully",
  "data": {
    "accessToken": "eyJhbG...",
    "refreshToken": "eyJhbG..."
  }
}
```

**Error** (409): Username or email already taken

---

## POST /auth/login (Authenticate)

**Request Body** (`LoginRequest`):
```json
{
  "username": "johndoe",
  "password": "securePass123"
}
```

**Validation**:
- `username`: required, non-blank
- `password`: required, non-blank

**Response** (200 OK):
```json
{
  "success": true,
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbG...",
    "refreshToken": "eyJhbG..."
  }
}
```

**Error** (401): Invalid credentials

---

## POST /auth/refresh-token (Refresh Access Token)

**Request Body** (`RefreshTokenRequest`):
```json
{
  "refreshToken": "eyJhbG..."
}
```

**Validation**:
- `refreshToken`: required, non-blank, must be a valid non-revoked refresh token

**Response** (200 OK):
```json
{
  "success": true,
  "status": 200,
  "message": "Token refreshed",
  "data": {
    "accessToken": "eyJhbG...",
    "refreshToken": "eyJhbG..."
  }
}
```

**Error** (401): Invalid or expired refresh token

---

## Token Specification

**Access Token**:
- Expiry: 15 minutes
- Claims: `sub` (username), `role` (ADMIN/CUSTOMER), `iat`, `exp`
- Header: `Authorization: Bearer <access-token>`

**Refresh Token**:
- Expiry: 7 days
- Used only at `/auth/refresh-token` endpoint
- Rotating: old refresh token is revoked when a new one is issued

---

## Endpoint Security Summary

| Endpoint Pattern | Auth Required | Roles |
|-----------------|---------------|-------|
| `POST /auth/**` | No | Public |
| `GET /products/**` | No | Public |
| `GET /categories/**` | No | Public |
| `POST/PATCH/DELETE /products/**` | Yes | ADMIN |
| `POST/PATCH/DELETE /categories/**` | Yes | ADMIN |
| `POST /orders` | Yes | ADMIN, CUSTOMER |
| `GET /orders` | Yes | ADMIN (all), CUSTOMER (own) |
| `GET /orders/{id}` | Yes | ADMIN, CUSTOMER (own) |
| `PATCH /orders/{id}` | Yes | ADMIN |
| `GET /actuator/health` | No | Public |
| `GET /actuator/metrics/**` | Yes | ADMIN |
| `GET /swagger-ui/**` | No | Public |
