# Relatório Técnico-Acadêmico: Projeto Synergy Market [cite: 1]

[cite_start]O projeto Synergy Market consiste no desenvolvimento de um sistema de "mini supermercado autônomo", fundamentado no modelo de negócio conhecido como *honesty market*[cite: 3]. [cite_start]Este conceito é amplamente aplicado em condomínios e espaços corporativos, onde a conveniência e a automação permitem que o consumidor realize suas compras de forma independente, sem a necessidade de intermediários humanos no ponto de venda[cite: 4]. [cite_start]Os objetivos centrais desta solução de Sistema de Gestão Comercial (SGC) incluem a gestão rigorosa de produtos e clientes, o registro automatizado de transações e um controle preciso de estoque e segurança operacional[cite: 5].

---

## [cite_start]🚀 1. Descrição do Sistema e Escopo [cite: 7]
[cite_start]O Synergy Market é uma aplicação de gestão comercial projetada para operar em cenários reais de automação residencial e comercial[cite: 8]. [cite_start]Sua proposta de valor reside na integração entre hardware (terminais de autoatendimento) e software para garantir uma operação fluida e segura[cite: 9].

### Fluxo Operacional
* [cite_start]O fluxo operacional básico inicia-se com a identificação do morador ou usuário no sistema[cite: 10].
* [cite_start]Durante o processo de compra, o morador seleciona os itens desejados, e o sistema valida em tempo real a disponibilidade física no inventário[cite: 11].
* [cite_start]Ao concluir a transação, o SGC assegura a integridade dos dados, atualizando instantaneamente o saldo do estoque e registrando a venda sob o perfil do usuário autenticado[cite: 12].

---

## [cite_start]📋 2. Requisitos do Sistema [cite: 14]

### 2.1. [cite_start]Requisitos Funcionais [cite: 15]
[cite_start]As funcionalidades do sistema foram agrupadas por categorias de domínio, contendo atributos específicos e regras de negócio obrigatórias[cite: 16]:

[cite_start]**Gestão de Clientes [cite: 17]**
* [cite_start]Atributos: ID (PK), Nome, CPF (Unique), E-mail, Telefone e Endereço[cite: 18].
* [cite_start]O CPF deve ser único no banco de dados[cite: 20].
* [cite_start]O e-mail deve passar por validação de formato[cite: 20].
* [cite_start]O sistema deve impedir a exclusão de clientes que possuam histórico de compras vinculado[cite: 21].

[cite_start]**Gestão de Produtos [cite: 22]**
* [cite_start]Atributos: ID (PK), Nome, Descrição, Preço e Quantidade em Estoque[cite: 23].
* [cite_start]O preço não pode ser negativo[cite: 25].
* [cite_start]O sistema deve controlar o estoque mínimo[cite: 25].
* [cite_start]A venda deve ser bloqueada na camada de serviço se a quantidade solicitada for superior ao estoque disponível[cite: 26].

[cite_start]**Registro de Venda [cite: 27]**
* [cite_start]Atributos: ID (PK), Data, Cliente (FK), Lista de Itens (ItemVenda), Valor Total e Usuário Responsável (FK)[cite: 28].
* [cite_start]O valor total deve ser calculado automaticamente pelo sistema[cite: 29].
* [cite_start]A confirmação da venda dispara a atualização automática do estoque[cite: 29].
* [cite_start]É proibido o registro de transações sem ao menos um item selecionado[cite: 30].

[cite_start]**Autenticação de Usuários [cite: 31]**
* [cite_start]Atributos: ID (PK), Username (Unique), Senha (criptografada) e Perfil (ADMIN, FUNCIONARIO)[cite: 32].
* [cite_start]O acesso deve ocorrer obrigatoriamente via endpoint de login, utilizando proteção de rotas por token e controle de acesso baseado em perfis (RBAC)[cite: 34].

[cite_start]**Relatórios [cite: 35]**
* [cite_start]Geração de demonstrativos de vendas por período e por cliente[cite: 36].
* [cite_start]Criação de uma representação gráfica das vendas anuais para análise de tendência[cite: 37].

### 2.2. [cite_start]Requisitos Não Funcionais [cite: 38]
* [cite_start]**Comunicação:** Interface baseada em API REST, utilizando JSON como padrão de intercâmbio de dados[cite: 41].
* [cite_start]**Segurança:** Implementação de autenticação via Token (JWT) com expiração definida; criptografia de senhas utilizando o algoritmo BCrypt[cite: 42].
* [cite_start]**Stack Tecnológica:** Java 21+, Spring Boot 3+, Spring Data JPA, Maven para automação de build e MySQL como sistema gerenciador de banco de dados relacional[cite: 43].
* [cite_start]**Interface de Usuário:** Front-end desenvolvido em Swing ou ambiente Web, agindo como cliente da API REST[cite: 44].

---

## [cite_start]🏛️ 3. Arquitetura do Sistema [cite: 45]
[cite_start]O Synergy Market utiliza uma Arquitetura em Camadas (Layered Architecture), o que promove a separação de preocupações e facilita a manutenibilidade[cite: 46].

### 3.1. [cite_start]Estrutura em Camadas [cite: 47]
1. [cite_start]**Camada de Apresentação:** Interface gráfica (Swing ou Web) responsável pela interação com o usuário e pelo envio de requisições HTTP para o backend[cite: 48].
2. [cite_start]**Camada Controller:** Porta de entrada da API; gerencia os endpoints REST, valida os dados de entrada e direciona as chamadas para a camada de serviço[cite: 49].
3. [cite_start]**Camada de Aplicação/Serviço:** Onde reside a inteligência do sistema, contendo as regras de negócio, validações de estoque e orquestração de transações[cite: 50].
4. [cite_start]**Camada de Domínio:** Representa as entidades do mundo real, incluindo classes de modelo e lógica intrínseca às regras de negócio e persistência[cite: 51].
5. [cite_start]**Camada de Persistência (Repository):** Abstrai a complexidade do acesso a dados, utilizando interfaces do Spring Data JPA para realizar operações no MySQL[cite: 52].

---

## [cite_start]🎨 4. Padrões de Projeto (Design Patterns) [cite: 58]

* [cite_start]**Padrão DTO (Data Transfer Object) [cite: 59][cite_start]:** Aplicado na interface de comunicação entre as camadas Controller e Service[cite: 61]. [cite_start]É utilizado para desacoplar as entidades de banco de dados da interface pública da API, evitando a exposição de campos sensíveis[cite: 62]. [cite_start]Melhora a segurança ao ocultar campos como a senha do usuário e otimiza o tráfego de rede ao enviar apenas os dados estritamente necessários[cite: 63].
* [cite_start]**Padrão Repository [cite: 64][cite_start]:** Aplicado na camada de persistência, estendendo as interfaces do Spring Data JPA[cite: 65]. [cite_start]Adotado para abstrair a lógica de acesso a dados e centralizar as consultas SQL[cite: 66]. [cite_start]Promove alta coesão e permite a manutenção ou troca do banco de dados com impacto mínimo nas regras de negócio da aplicação[cite: 67].

---

## [cite_start]📊 5. Modelagem Estrutural e de Dados [cite: 68]

### 5.1. [cite_start]Modelagem de Domínio e Classes [cite: 69]
| Classe | Atributos Principais | Relacionamentos |
| :--- | :--- | :--- |
| [cite_start]**Cliente** [cite: 71] [cite_start]| id, nome, cpf, email, telefone, endereco [cite: 71] | [cite_start]Possui 0 ou muitas Vendas (1:N) [cite: 71] |
| [cite_start]**Produto** [cite: 71] [cite_start]| id, nome, descricao, preco, quantidadeEmEstoque [cite: 71] | [cite_start]Referenciado em ItemVenda (1:N) [cite: 71] |
| [cite_start]**Venda** [cite: 71] [cite_start]| id, data, valor Total, cliente_id, usuario_id [cite: 71] | [cite_start]Contém 1 ou muitos Itens Venda (1:N) [cite: 71] |
| [cite_start]**ItemVenda** [cite: 71] [cite_start]| id, venda_id, produto_id, quantidade, precoUnitario [cite: 71] | [cite_start]Pertence a uma Venda e um Produto [cite: 71] |
| [cite_start]**Usuario** [cite: 71] [cite_start]| id, username, senha, perfil (ADMIN/FUNCIONARIO) [cite: 71] | [cite_start]Registra 0 ou muitas Vendas (1:N) [cite: 71] |

### 5.2. [cite_start]Modelo Lógico do Banco de Dados [cite: 72]
* [cite_start]**usuarios:** Tabela de credenciais (PK: id, UK: username)[cite: 73].
* [cite_start]**clientes:** Tabela de moradores (PK: id, UK: cpf)[cite: 74].
* [cite_start]**produtos:** Cadastro de mercadorias (PK: id)[cite: 75].
* [cite_start]**vendas:** Transações de saída (PK: id, FK: cliente_id, FK: usuario_id)[cite: 76].
* [cite_start]**itens_venda:** Detalhamento da venda (PK: id, FK: venda_id, FK: produto_id)[cite: 77].

---

## [cite_start]📅 6. Planejamento de Entregas (Roadmap Acadêmico) [cite: 122]

| Entrega | Foco / Objetivo | Principais Entregáveis | Data Limite |
| :--- | :--- | :--- | :--- |
| [cite_start]**Entrega 1** [cite: 124] | [cite_start]Modelagem e Arquitetura [cite: 124] | [cite_start]PDF Descritivo, Diagramas (Domínio e Classes), Script SQL e GitHub [cite: 124] | [cite_start]02/04 [cite: 124] |
| [cite_start]**Entrega 2** [cite: 124] | [cite_start]Backend e API [cite: 124] | [cite_start]API funcional, CRUDs, Integração MySQL, Autenticação JWT e Tratamento de Exceções [cite: 124] | [cite_start]07/05 [cite: 124] |
| [cite_start]**Entrega 3** [cite: 124] | [cite_start]Sistema Completo [cite: 124] | [cite_start]Integração UI (Swing/Web), Controle de Estoque, Relatórios e Documentação Final [cite: 124] | [cite_start]25/06 [cite: 124] |

---

## [cite_start]🛠️ 7. Instruções de Implementação e Segurança [cite: 125]
[cite_start]O projeto deve seguir rigorosamente a estrutura de diretórios padrão Maven para garantir a organização do código-fonte no pacote `br.com.sgc`[cite: 126]:
* [cite_start]**config/**: Deve conter `SecurityConfig.java`, `JwtAuthenticationFilter.java` e `JwtService.java`[cite: 127, 128].
* **controller/**: Classes como `AuthController.java` e `VendaController.java`[cite: 129].
* [cite_start]**service/**: Lógica central em `VendaService.java` e `AuthService.java`[cite: 130].
* [cite_start]**domain/model/**: Entidades JPA como `Cliente.java` e `Venda.java`[cite: 130].
* **dto/**: Objetos de transferência como `AuthRequestDTO.java` e `VendaDTO.java`[cite: 131].
* [cite_start]**exception/**: `GlobalExceptionHandler.java` para tratamento centralizado de erros[cite: 131].
* [cite_start]**util/**: `MapperUtil.java` para conversão entre Entidades e DTOS[cite: 132].

### Segurança
* A autenticação é provida via JWT[cite: 133].
* [cite_start]Todas as senhas de usuários devem ser processadas pelo `PasswordEncoder` com algoritmo BCrypt antes da persistência[cite: 133].
* [cite_start]O acesso aos recursos é filtrado pelo perfil (ADMIN para funções gerenciais e FUNCIONARIO para operações de PDV)[cite: 134].
