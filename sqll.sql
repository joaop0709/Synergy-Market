
-- 1. CRIAÇÃO DO BANCO DE DADOS
CREATE DATABASE IF NOT EXISTS synergy_market;
USE synergy_market;


-- 2. TABELA DE USUÁRIOS (SISTEMA E AUTENTICAÇÃO)
-- Armazena credenciais e perfis (ADMIN, FUNCIONARIO) [cite: 55, 133]
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL, -- Suporta criptografia BCrypt [cite: 99]
    perfil ENUM('ADMIN', 'FUNCIONARIO') NOT NULL -- Controle de acesso [cite: 55]
);


-- 3. TABELA DE CLIENTES
-- Contém dados básicos e restrição de CPF único [cite: 58-64, 66]
CREATE TABLE clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    telefone VARCHAR(20),
    endereco VARCHAR(255)
);


-- 4. TABELA DE PRODUTOS
-- Inclui controle de estoque e trava de preço negativo [cite: 70-75, 77]
CREATE TABLE produtos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10, 2) NOT NULL CHECK (preco >= 0),
    quantidade_estoque INT NOT NULL DEFAULT 0
);


-- 5. TABELA DE VENDAS (CABEÇALHO)
-- Registra o valor total e vincula ao cliente e ao usuário [cite: 81-87, 136]
CREATE TABLE vendas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_venda TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valor_total DECIMAL(10, 2) NOT NULL,
    cliente_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT fk_venda_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_venda_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);


-- 6. TABELA DE ITENS DA VENDA
-- Detalhamento de cada produto vendido em uma transação [cite: 137]
CREATE TABLE itens_venda (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venda_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_item_venda FOREIGN KEY (venda_id) REFERENCES vendas(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_produto FOREIGN KEY (produto_id) REFERENCES produtos(id)
);