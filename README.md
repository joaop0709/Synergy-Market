# Relatório Técnico-Acadêmico: Projeto Synergy Market 

O projeto Synergy Market consiste no desenvolvimento de um sistema de "mini supermercado autônomo", fundamentado no modelo de negócio conhecido como *honesty market*. Este conceito é amplamente aplicado em condomínios e espaços corporativos, onde a conveniência e a automação permitem que o consumidor realize suas compras de forma independente, sem a necessidade de intermediários humanos no ponto de venda. Os objetivos centrais desta solução de Sistema de Gestão Comercial (SGC) incluem a gestão rigorosa de produtos e clientes, o registro automatizado de transações e um controle preciso de estoque e segurança operacional.

---

## 🚀 1. Descrição do Sistema e Escopo  
O Synergy Market é uma aplicação de gestão comercial projetada para operar em cenários reais de automação residencial e comercial. Sua proposta de valor reside na integração entre hardware (terminais de autoatendimento) e software para garantir uma operação fluida e segura.

### Fluxo Operacional
* O fluxo operacional básico inicia-se com a identificação do morador ou usuário no sistema.
* Durante o processo de compra, o morador seleciona os itens desejados, e o sistema valida em tempo real a disponibilidade física no inventário.
* Ao concluir a transação, o SGC assegura a integridade dos dados, atualizando instantaneamente o saldo do estoque e registrando a venda sob o perfil do usuário autenticado.

---

## 📋 2. Requisitos do Sistema 

### 2.1. Requisitos Funcionais  
As funcionalidades do sistema foram agrupadas por categorias de domínio, contendo atributos específicos e regras de negócio obrigatórias:

**Gestão de Clientes**
* Atributos: ID (PK), Nome, CPF (Unique), E-mail, Telefone e Endereço.
* O CPF deve ser único no banco de dados.
* O e-mail deve passar por validação de formato.
* O sistema deve impedir a exclusão de clientes que possuam histórico de compras vinculado.

**Gestão de Produtos**
* Atributos: ID (PK), Nome, Descrição, Preço e Quantidade em Estoque.
* O preço não pode ser negativo.
* O sistema deve controlar o estoque mínimo.
* A venda deve ser bloqueada na camada de serviço se a quantidade solicitada for superior ao estoque disponível.

**Registro de Venda**
* Atributos: ID (PK), Data, Cliente (FK), Lista de Itens (ItemVenda), Valor Total e Usuário Responsável (FK).
* O valor total deve ser calculado automaticamente pelo sistema.
* A confirmação da venda dispara a atualização automática do estoque.
* É proibido o registro de transações sem ao menos um item selecionado.

**Autenticação de Usuários**
* Atributos: ID (PK), Username (Unique), Senha (criptografada) e Perfil (ADMIN, FUNCIONARIO).
* O acesso deve ocorrer obrigatoriamente via endpoint de login, utilizando proteção de rotas por token e controle de acesso baseado em perfis (RBAC).

**Relatórios**
* Geração de demonstrativos de vendas por período e por cliente.
* Criação de uma representação gráfica das vendas anuais para análise de tendência.

### 2.2. Requisitos Não Funcionais  
* **Comunicação:** Interface baseada em API REST, utilizando JSON como padrão de intercâmbio de dados.
* **Segurança:** Implementação de autenticação via Token (JWT) com expiração definida; criptografia de senhas utilizando o algoritmo BCrypt.
* **Stack Tecnológica:** Java 21+, Spring Boot 3+, Spring Data JPA, Maven para automação de build e MySQL como sistema gerenciador de banco de dados relacional.
* **Interface de Usuário:** Front-end desenvolvido em Swing ou ambiente Web, agindo como cliente da API REST.

---

## 🏛️ 3. Arquitetura do Sistema  
O Synergy Market utiliza uma Arquitetura em Camadas (Layered Architecture), o que promove a separação de preocupações e facilita a manutenibilidade.

### 3.1. Estrutura em Camadas 
1. **Camada de Apresentação:** Interface gráfica (Swing ou Web) responsável pela interação com o usuário e pelo envio de requisições HTTP para o backend.
2. **Camada Controller:** Porta de entrada da API; gerencia os endpoints REST, valida os dados de entrada e direciona as chamadas para a camada de serviço.
3. **Camada de Aplicação/Serviço:** Onde reside a inteligência do sistema, contendo as regras de negócio, validações de estoque e orquestração de transações.
4. **Camada de Domínio:** Representa as entidades do mundo real, incluindo classes de modelo e lógica intrínseca às regras de negócio e persistência.
5. **Camada de Persistência (Repository):** Abstrai a complexidade do acesso a dados, utilizando interfaces do Spring Data JPA para realizar operações no MySQL.

---

## 🎨 4. Padrões de Projeto (Design Patterns)  

* **Padrão DTO (Data Transfer Object):** Aplicado na interface de comunicação entre as camadas Controller e Service. É utilizado para desacoplar as entidades de banco de dados da interface pública da API, evitando a exposição de campos sensíveis. Melhora a segurança ao ocultar campos como a senha do usuário e otimiza o tráfego de rede ao enviar apenas os dados estritamente necessários.
* **Padrão Repository:** Aplicado na camada de persistência, estendendo as interfaces do Spring Data JPA. Adotado para abstrair a lógica de acesso a dados e centralizar as consultas SQL. Promove alta coesão e permite a manutenção ou troca do banco de dados com impacto mínimo nas regras de negócio da aplicação.

---

## 📊 5. Modelagem Estrutural e de Dados  

### 5.1. Modelagem de Domínio e Classes  
| Classe | Atributos Principais | Relacionamentos |
| :--- | :--- | :--- |
| **Cliente**  | id, nome, cpf, email, telefone, endereco  | Possui 0 ou muitas Vendas (1:N)  |
| **Produto**  | id, nome, descricao, preco, quantidadeEmEstoque  | Referenciado em ItemVenda (1:N)  |
| **Venda**  | id, data, valor Total, cliente_id, usuario_id  | Contém 1 ou muitos Itens Venda (1:N)  |
| **ItemVenda**  | id, venda_id, produto_id, quantidade, precoUnitario  | Pertence a uma Venda e um Produto  |
| **Usuario**  | id, username, senha, perfil (ADMIN/FUNCIONARIO)  | Registra 0 ou muitas Vendas (1:N)  |

### 5.2. Modelo Lógico do Banco de Dados 
* **usuarios:** Tabela de credenciais (PK: id, UK: username).
* **clientes:** Tabela de moradores (PK: id, UK: cpf).
* **produtos:** Cadastro de mercadorias (PK: id).
* **vendas:** Transações de saída (PK: id, FK: cliente_id, FK: usuario_id).
* **itens_venda:** Detalhamento da venda (PK: id, FK: venda_id, FK: produto_id).

---

## 📅 6. Planejamento de Entregas (Roadmap Acadêmico)  

| Entrega | Foco / Objetivo | Principais Entregáveis | Data Limite |
| :--- | :--- | :--- | :--- |
| **Entrega 1** | Modelagem e Arquitetura  | PDF Descritivo, Diagramas (Domínio e Classes), Script SQL e GitHub  | 02/04  |
| **Entrega 2** | Backend e API  | API funcional, CRUDs, Integração MySQL, Autenticação JWT e Tratamento de Exceções  | 07/05  |
| **Entrega 3** | Sistema Completo  | Integração UI (Swing/Web), Controle de Estoque, Relatórios e Documentação Final  | 25/06  |

---

## 🛠️ 7. Instruções de Implementação e Segurança  
O projeto deve seguir rigorosamente a estrutura de diretórios padrão Maven para garantir a organização do código-fonte no pacote `br.com.sgc`:
* **config/**: Deve conter `SecurityConfig.java`, `JwtAuthenticationFilter.java` e `JwtService.java`.
* **controller/**: Classes como `AuthController.java` e `VendaController.java`.
* **service/**: Lógica central em `VendaService.java` e `AuthService.java`.
* **domain/model/**: Entidades JPA como `Cliente.java` e `Venda.java`.
* **dto/**: Objetos de transferência como `AuthRequestDTO.java` e `VendaDTO.java`.
* **exception/**: `GlobalExceptionHandler.java` para tratamento centralizado de erros.
* **util/**: `MapperUtil.java` para conversão entre Entidades e DTOS.

### Segurança
* A autenticação é provida via JWT.
* Todas as senhas de usuários devem ser processadas pelo `PasswordEncoder` com algoritmo BCrypt antes da persistência.
* O acesso aos recursos é filtrado pelo perfil (ADMIN para funções gerenciais e FUNCIONARIO para operações de PDV).
