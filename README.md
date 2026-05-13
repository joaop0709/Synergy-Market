# 🛒 Synergy Market - Backend API

Sistema de Gestão Comercial para mini supermercado autônomo (honesty market).

## 🚀 Tecnologias

- Java 21
- Spring Boot 3.2
- Spring Security + JWT
- Spring Data JPA / Hibernate
- MySQL 8
- Maven
- Lombok

## 📁 Estrutura do Projeto

```
src/main/java/com/synergymarket/
├── config/          # Configurações de segurança (SecurityConfig)
├── controller/      # Endpoints REST
├── dto/             # Data Transfer Objects
├── entity/          # Entidades JPA
├── enums/           # Enumerações (PerfilUsuario)
├── exception/       # Exceções customizadas + Handler global
├── repository/      # Interfaces JPA Repository
├── security/        # JWT Service + Filtro de autenticação
└── service/         # Regras de negócio
```

## ⚙️ Como rodar

### 1. Pré-requisitos
- Java 21+
- MySQL 8 rodando localmente
- Maven

### 2. Configure o banco
Edite `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=SUA_SENHA
```

### 3. Execute
```bash
mvn spring-boot:run
```
A API estará disponível em `http://localhost:8080`

---

## 🔐 Autenticação

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "senha": "123456"
}
```

Resposta:
```json
{
  "token": "eyJhbGci...",
  "username": "admin",
  "perfil": "ROLE_ADMIN"
}
```

Use o token nas demais requisições:
```
Authorization: Bearer <token>
```

---

## 📌 Endpoints

### Clientes
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/clientes` | Listar todos |
| GET | `/api/clientes/{id}` | Buscar por ID |
| POST | `/api/clientes` | Criar cliente |
| PUT | `/api/clientes/{id}` | Atualizar cliente |
| DELETE | `/api/clientes/{id}` | Deletar cliente |

### Produtos
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/produtos` | Listar todos |
| GET | `/api/produtos/{id}` | Buscar por ID |
| POST | `/api/produtos` | Criar produto |
| PUT | `/api/produtos/{id}` | Atualizar produto |
| DELETE | `/api/produtos/{id}` | Deletar produto |

### Vendas
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/vendas` | Listar todas |
| GET | `/api/vendas/{id}` | Buscar por ID |
| POST | `/api/vendas` | Registrar venda |

---

## 🏗️ Arquitetura

Arquitetura em Camadas (Layered Architecture):

```
Controller → Service → Repository → MySQL
```

**Design Patterns aplicados:**
- **DTO (Data Transfer Object):** Desacopla entidades da API pública
- **Repository Pattern:** Abstrai o acesso ao banco de dados

---

## 🧪 Testes

```bash
mvn test
```

Testes unitários dos Services com JUnit 5 + Mockito.

---

## 👥 Autores

João Paulo Costa, Henrique Guimarães, Miguel Marques
