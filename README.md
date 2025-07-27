# rinha-backend-2025-java-spring


#### Comando para criação do banco de dados para teste local
docker run --name rinha-pg-test -e POSTGRES_DB=rinha_db -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=123456 -p 5432:5432 -d postgres:16-alpine