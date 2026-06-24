# 🛒 Synergy Market — Sistema de Gestão Comercial!!

Sistema de gestão para mini supermercado autônomo (honesty market), desenvolvido como projeto acadêmico na disciplina de Desenvolvimento de Software.

**Autores:** João Paulo Costa · Henrique Guimarães · Miguel Marques  
**Instituição:** Centro Universitário de Brasília — CEUB  

---

## 🚀 Tecnologias utilizadas

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.2 |
| Segurança | Spring Security + JWT (JJWT) |
| Persistência | Spring Data JPA / Hibernate |
| Banco de dados | MySQL 8 |
| Build | Maven |
| Utilitários | Lombok |
| Testes | JUnit 5 + Mockito |
| Frontend | HTML5 + JavaScript (Vanilla) |

---

## 📁 Estrutura do projeto

```
Synergy-Market/
├── src/
│   ├── main/
│   │   ├── java/com/synergymarket/
│   │   │   ├── config/         # SecurityConfig, DataInitializer
│   │   │   ├── controller/     # AuthController, ClienteController,
│   │   │   │                   # ProdutoController, VendaController
│   │   │   ├── dto/            # Objetos de transferência de dados
│   │   │   ├── entity/         # Entidades JPA (Cliente, Produto,
│   │   │   │                   # Venda, ItemVenda, Usuario)
│   │   │   ├── enums/          # PerfilUsuario (ADMIN, FUNCIONARIO)
│   │   │   ├── exception/      # BusinessException,
│   │   │   │                   # ResourceNotFoundException,
│   │   │   │                   # GlobalExceptionHandler
│   │   │   ├── repository/     # Interfaces Spring Data JPA
│   │   │   ├── security/       # JwtService, JwtAuthFilter,
│   │   │   │                   # CustomUserDetailsService
│   │   │   └── service/        # Regras de negócio
│   │   └── resources/
│   │       └── application.properties
│   └── test/                   # Testes unitários dos Services
├── frontend/
│   └── index.html              # Interface web (HTML + JS)
├── docs/
│   ├── Script_SQL_de_implementação.sql
│   ├── Modelo_Lógico.pdf
│   └── Diagrama_engenharia_reversa.mwb
└── pom.xml
```

---

## ⚙️ Como rodar o projeto

### Pré-requisitos

- Java 21+
- MySQL 8 rodando localmente
- Maven 3.8+
- Navegador moderno (Chrome, Firefox, Edge)

### 1. Criar o banco de dados

Execute o script SQL incluído no projeto:

```sql
-- Execute no MySQL Workbench ou via terminal:
source docs/Script_SQL_de_implementação.sql
```

Ou crie manualmente:

```sql
CREATE DATABASE IF NOT EXISTS sgc_synergy_market;
```

### 2. Configurar a senha do banco

Edite o arquivo `src/main/resources/application.properties`:

```properties
spring.datasource.password=SUA_SENHA_AQUI
```

### 3. Rodar o backend

```bash
mvn spring-boot:run
```

A API estará disponível em: **http://localhost:8080**

O sistema cria automaticamente um usuário admin ao iniciar:
- **Usuário:** `admin`
- **Senha:** `123456`

### 4. Abrir o frontend

Abra o arquivo `frontend/index.html` diretamente no navegador (duplo clique).

> ⚠️ O backend precisa estar rodando antes de abrir o frontend.

---

## 🔐 Autenticação

O sistema usa **JWT (JSON Web Token)**. O fluxo é:

1. O usuário faz login enviando usuário e senha
2. A API retorna um token JWT
3. Todas as demais requisições enviam o token no header `Authorization`

### Endpoint de login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "senha": "123456"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGci...",
  "username": "admin",
  "perfil": "ROLE_ADMIN"
}
```

**Uso nas demais requisições:**
```
Authorization: Bearer <token>
```

---

## 📌 Endpoints da API

### Autenticação
| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | `/api/auth/login` | Login e geração do token | ❌ |

### Clientes
| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/clientes` | Listar todos os clientes | ✅ |
| GET | `/api/clientes/{id}` | Buscar cliente por ID | ✅ |
| POST | `/api/clientes` | Cadastrar novo cliente | ✅ |
| PUT | `/api/clientes/{id}` | Atualizar cliente | ✅ |
| DELETE | `/api/clientes/{id}` | Remover cliente | ✅ |

### Produtos
| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/produtos` | Listar todos os produtos | ✅ |
| GET | `/api/produtos/{id}` | Buscar produto por ID | ✅ |
| POST | `/api/produtos` | Cadastrar novo produto | ✅ |
| PUT | `/api/produtos/{id}` | Atualizar produto | ✅ |
| DELETE | `/api/produtos/{id}` | Remover produto | ✅ |

### Vendas
| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/vendas` | Listar todas as vendas | ✅ |
| GET | `/api/vendas/{id}` | Buscar venda por ID | ✅ |
| POST | `/api/vendas` | Registrar nova venda | ✅ |

---

## 📦 Exemplos de requisição

### Cadastrar produto
```json
POST /api/produtos
{
  "nome": "Coca-Cola 350ml",
  "descricao": "Refrigerante gelado",
  "preco": 5.50,
  "quantidadeEstoque": 100
}
```

### Registrar venda
```json
POST /api/vendas
{
  "clienteId": 1,
  "itens": [
    { "produtoId": 1, "quantidade": 2 },
    { "produtoId": 3, "quantidade": 1 }
  ]
}
```

---

## 🏗️ Arquitetura

O projeto segue a **Arquitetura em Camadas (Layered Architecture)**:

```
Frontend (HTML/JS)
       │
       ▼  HTTP + JWT
   Controller          ← Recebe requisições, valida entrada
       │
       ▼
    Service            ← Regras de negócio, validações
       │
       ▼
   Repository          ← Acesso ao banco via Spring Data JPA
       │
       ▼
     MySQL             ← Persistência dos dados
```

### Design Patterns aplicados

- **DTO (Data Transfer Object):** separa as entidades do banco dos dados expostos pela API, protegendo campos internos como senhas.
- **Repository Pattern:** abstrai o acesso ao banco de dados — o Service não sabe como os dados são salvos, só pede ao Repository.
- **Builder Pattern (Lombok):** construção de objetos complexos de forma legível.
- **Global Exception Handler:** centraliza o tratamento de erros da aplicação em um único lugar.

---

## ✅ Regras de negócio implementadas

| Regra | Onde |
|-------|------|
| CPF único por cliente | `ClienteService.criar()` |
| Não exclui cliente com histórico de vendas | `ClienteService.deletar()` |
| Venda precisa ter pelo menos 1 item | `VendaService.registrarVenda()` |
| Valida estoque antes de confirmar venda | `VendaService.registrarVenda()` |
| Desconta estoque automaticamente ao vender | `VendaService.registrarVenda()` |
| Preço não pode ser negativo | Constraint no banco (CHECK) |
| Todos os endpoints protegidos por JWT | `SecurityConfig` |

---

## 🧪 Testes

O projeto possui testes unitários para os principais serviços:

```bash
mvn test
```

Testes implementados:
- `ClienteServiceTest` — cobertura de criação, atualização e regras de CPF
- `ProdutoServiceTest` — cobertura de CRUD e validações

---

## 🗄️ Modelo de dados

```
usuarios (id, username, senha, perfil)
    │
    └──registra──► vendas (id, data_venda, valor_total, cliente_id, usuario_id)
                        │
clientes (id, nome, cpf, email, telefone, endereco)   │
    └──────────────────────────────────────────────────┘
                        │
                   itens_venda (id, venda_id, produto_id, quantidade, preco_unitario)
                        │
produtos (id, nome, descricao, preco, quantidade_estoque)
```
