# Extrator de Dados - Gerenciamento de Clusters

## Funcionalidades

Este serviço Spring Boot 3.5 fornece APIs RESTful para gerenciar clusters de banco de dados PostgreSQL e descobrir automaticamente os bancos de dados disponíveis em cada cluster.

## Endpoints Disponíveis

### Clusters Management

- **POST /api/v1/clusters** - Criar novo cluster
- **GET /api/v1/clusters** - Listar todos os clusters
- **GET /api/v1/clusters/{id}** - Buscar cluster por ID  
- **GET /api/v1/clusters/alias/{alias}** - Buscar cluster por alias
- **PUT /api/v1/clusters/{id}** - Atualizar cluster
- **DELETE /api/v1/clusters/{id}** - Deletar cluster
- **POST /api/v1/clusters/{id}/discover** - Descobrir bancos do cluster
- **GET /api/v1/clusters/{id}/test-connection** - Testar conexão

## Exemplo de Uso

### 1. Criar um cluster
```bash
curl -X POST http://localhost:8080/api/v1/clusters \
  -H "Content-Type: application/json" \
  -d '{
    "alias": "servidorLocal",
    "host": "127.0.0.1",
    "port": 5432,
    "username": "postgres",
    "password": "123",
    "description": "Servidor local de desenvolvimento"
  }'
```

### 2. Descobrir bancos de dados
```bash
curl -X POST http://localhost:8080/api/v1/clusters/1/discover
```

### 3. Listar todos os clusters
```bash
curl -X GET http://localhost:8080/api/v1/clusters
```

### 4. Testar conexão
```bash
curl -X GET http://localhost:8080/api/v1/clusters/1/test-connection
```

## Configuração do Banco de Dados

O serviço utiliza PostgreSQL como banco de dados principal. Configure as seguintes variáveis de ambiente:

- `DB_USERNAME`: Nome do usuário do banco (default: postgres)
- `DB_PASSWORD`: Senha do banco (default: postgres)

## Estrutura das Tabelas

### clusters
- id (BIGSERIAL, PK)
- alias (VARCHAR, UNIQUE)
- host (VARCHAR)
- port (INTEGER)
- username (VARCHAR)
- password (VARCHAR)
- connection_active (BOOLEAN)
- last_discovery (TIMESTAMP)
- description (TEXT)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

### database_instances
- id (BIGSERIAL, PK)
- database_name (VARCHAR)
- database_size (BIGINT)
- database_encoding (VARCHAR)
- is_accessible (BOOLEAN)
- cluster_id (BIGINT, FK)
- discovered_at (TIMESTAMP)

## Tecnologias Utilizadas

- Spring Boot 3.5
- Spring Data JPA
- Spring Security
- PostgreSQL
- Liquibase
- Bean Validation
- SLF4J + Logback

## Executar o Projeto

1. Configure o banco PostgreSQL
2. Execute: `mvn spring-boot:run`
3. Acesse: http://localhost:8080/api/v1/clusters

## Funcionalidades Principais

- ✅ Cadastro de clusters com validação
- ✅ Descoberta automática de bancos de dados
- ✅ Teste de conectividade
- ✅ CRUD completo de clusters
- ✅ Tratamento de exceções
- ✅ Logs detalhados
- ✅ Relacionamento JPA entre clusters e databases
- ✅ Migração de banco com Liquibase
