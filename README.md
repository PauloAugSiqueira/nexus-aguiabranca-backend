## Nexus — Sistema de Retaguarda (Back-end Java & Spring Boot)

Repositório oficial do back-end do sistema **Nexus**, desenvolvido em **Java** com **Spring Boot** e **MongoDB** para sustentar a lógica de negócios, segurança corporativa, motor de pontuação e o ecossistema de inovação do **Grupo Águia Branca** em parceria com a **FIAP**.

---

## Visão Geral da Arquitetura

O sistema foi arquitetado em camadas bem definidas (**Clean Architecture / Layered Architecture**), priorizando a desacoplagem, imutabilidade de dados (com uso intensivo de *Java Records*), segurança robusta via tokens JWT e observabilidade por meio de uma trilha dedicada de auditoria.

---

## Stack Tecnológica & Dependências (`pom.xml`)

- **Java:** `17`
- **Spring Boot:** `3.3.4`
- **Gerenciamento de Dados:** `Spring Data MongoDB`
- **Segurança:** `Spring Security` (Stateless) com **JJWT (`0.12.6`)** para tokens JWT e criptografia BCrypt
- **Documentação de API:** `SpringDoc OpenAPI Starter WebMVC UI` (`2.5.0`)
- **Validação e Métricas:** `Spring Boot Starter Validation` & `Spring Boot Starter Actuator`

---

## Organização dos Pacotes

br.com.fiap.gabinova.backend/
├── config/        # Configuração de segurança (SecurityConfig), OpenAPI e Seeders automáticos
├── controller/    # Endpoints REST (Auth, Ideas, Projects, Gamification, Dashboard, Guidelines, Users)
├── domain/        # Entidades do MongoDB (User, Idea, Project, StrategicGuideline, AuditLog, etc.)
├── dto/           # Records imutáveis de Request e Response para o contrato com o Client Mobile
├── exception/     # Tratamento centralizado de erros (GlobalExceptionHandler) e exceções customizadas
├── mapper/        # Conversores estáticos de entidades para DTOs e formatação de datas (ISO-8601)
├── repository/    # Interfaces Spring Data MongoDB
├── security/      # Filtros JWT, provedores de token e contexto de usuário autenticado
└── service/       # Regras de negócio, motor de pontuação, gamificação, auditoria e dashboard

---

## Principais Módulos e Regras de Negócio
Autenticação e Segurança (/api/auth):
Autenticação stateless via credenciais corporativas, gerando tokens JWT criptografados com claims de perfil e expiração
configurada.
Gestão de Ideias e Motor de Pontuação (/api/ideas):
Submissão e edição restrita a colaboradores.
ScoringService: Replica exatamente o cálculo de pontuação corporativa (Nexus Score de 0 a 100) com base na categoria,
setor, urgência e impacto esperado.
Transições de status rigidamente controladas (PENDING, APPROVED, NOT_APPROVED, IMPLEMENTED).
Projetos e Iniciativas Estratégicas (/api/projects):
Promoção de ideias validadas para projetos oficiais com controle de estágios, investimentos, retornos financeiros,
prazos e progresso.
Gamificação e Ranking (/api/gamification, /api/ranking):
Atribuição automatizada de pontos por eventos do ciclo de vida da inovação (criação, preenchimento detalhado, aprovação,
conversão em projeto e conclusão).
Gestão de níveis corporativos e desbloqueio dinâmico de badges.
Dashboard Executivo (/api/dashboard):
Agregações em tempo real de métricas estratégicas para a alta liderança (ROI da carteira válida, volume de ideias, taxa
de engajamento dos colaboradores e diretriz de maior destaque).
Governança e Auditoria (AuditService):
Registro automático e imutável de todas as operações críticas de escrita no banco de dados (quem, quando, o que e o
resultado).

---

## Perfis de Acesso Automáticos (DataSeeder)
Para facilitar os testes imediatos da aplicação, na primeira inicialização do back-end, o sistema popula automaticamente
três usuários de teste padrão (desativado no profile test):

Operador (Colaborador): operador@gab.com / 123456

Gestor: gestor@gab.com / 123456

Liderança (Admin): lideranca@gab.com / 123456

*Documentação Interativa da API*
Com a aplicação em execução, acesse a documentação gerada automaticamente via OpenAPI:
Swagger UI: http://localhost:8080/swagger-ui.html
Health Check: http://localhost:8080/actuator/health