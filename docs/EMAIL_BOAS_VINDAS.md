# E-mail de boas-vindas

Após o cadastro de uma nova conta (`POST /api/cadastro`), o backend envia um e-mail de boas-vindas
para o endereço informado, na versão do perfil salvo:

| Perfil salvo (`perfil_uso`) | Versão | Assunto | Banner |
|---|---|---|---|
| `Cliente` | CLIENTE | Bem-vindo ao Stock4event! Seu evento começa aqui. | amarelo (`banner-cliente.jpg`) |
| `Fornecedor` | FORNECEDOR | Bem-vindo ao Stock4event! Vamos conectar seu negócio a novos eventos. | azul (`banner-fornecedor.jpg`) |
| `ClienteFornecedor` | FORNECEDOR | (igual ao fornecedor) | azul |

Qualquer outro valor de perfil não gera e-mail. Usuários adicionados por outra conta
(`POST /api/usuarios/register`) não recebem boas-vindas, porque não é um cadastro de conta nova.

## Como funciona

1. **Na transação do cadastro**, o `CadastroService` salva empresa e usuário e grava um registro na
   tabela `email_boas_vindas` (status `PENDENTE`). Se o cadastro falhar (validação, e-mail ou CNPJ
   duplicado, erro ao salvar), a transação é desfeita e o registro some junto: nada é enviado.
2. **Depois do commit**, a primeira tentativa roda em segundo plano (`@Async` +
   `@TransactionalEventListener(AFTER_COMMIT)`). A resposta do cadastro (201) não espera o envio, e uma
   falha no e-mail nunca desfaz a conta nem é mostrada como erro de cadastro.
3. **Novas tentativas**: falhas temporárias (servidor fora do ar, tempo esgotado, autenticação) voltam
   para `PENDENTE` com espera crescente (1 min, 2 min, 4 min…) até `max-tentativas` (padrão 5). O
   `BoasVindasReprocessador` roda a cada minuto e também envia registros que ficaram pendentes se o
   servidor reiniciou logo após um cadastro.
4. **Sem duplicidade**: `usuario_id` é único na tabela (um e-mail por conta), e cada tentativa reserva o
   registro com um `UPDATE … WHERE status = 'PENDENTE'`, de modo que duas execuções simultâneas nunca
   enviam a mesma mensagem. Se uma tentativa for interrompida sem resposta do servidor (ex.: queda do
   backend no meio do envio), o registro vira `INDETERMINADO` e **não** é reenviado automaticamente.

Status possíveis: `PENDENTE`, `ENVIANDO`, `ENVIADO`, `FALHOU` (falha definitiva ou limite atingido) e
`INDETERMINADO`. O campo `ultimo_erro` guarda o motivo da última falha.

> `ENVIADO` significa que o servidor SMTP **aceitou** a mensagem. Não garante entrega na caixa de
> entrada (pode haver rejeição posterior ou filtro de spam); confira no painel do provedor.

### Validação e confirmação de e-mail

- O backend valida o formato do endereço (`@Email` com domínio que contém ponto, em `UsuarioRequestDTO`),
  e o app orienta o usuário com a mesma regra na tela de cadastro.
- Um formato válido **não** prova que o endereço existe nem que pertence a quem se cadastrou.
- O projeto **ainda não tem confirmação de e-mail**. O e-mail de boas-vindas não marca o endereço como
  verificado e não substitui essa confirmação. Se ela for criada no futuro, o caminho natural é enviar o
  link de confirmação separadamente (ou mover o boas-vindas para depois da confirmação), sem reaproveitar
  este registro como prova de titularidade.

## Arquivos

| Arquivo | Função |
|---|---|
| `service/BoasVindasEmailService.java` | Registro, envio assíncrono, novas tentativas e prevenção de duplicidade |
| `service/BoasVindasTemplate.java` | Monta assunto, HTML, texto simples e banner; escapa o nome do usuário |
| `service/BoasVindasReprocessador.java` | Agendador das novas tentativas |
| `config/EmailConfig.java`, `config/EmailProperties.java` | Configuração e verificação ao subir |
| `entity/EmailBoasVindas.java`, `repository/EmailBoasVindasRepository.java` | Tabela `email_boas_vindas` |
| `resources/email/boas-vindas/cliente.html` / `.txt` | Modelo da versão cliente (HTML e texto simples) |
| `resources/email/boas-vindas/fornecedor.html` / `.txt` | Modelo da versão fornecedor |
| `resources/email/boas-vindas/banner-*.jpg` | Banners (1200×800, cerca de 145 KB cada) |

Os banners vêm da pasta de materiais "E-mail Alert" e foram otimizados e copiados para o projeto.
Eles vão **incorporados na mensagem (CID)**: o envio não depende da pasta da Área de Trabalho nem de
hospedagem pública. Como o botão desenhado no banner é só imagem, o e-mail também tem um botão HTML
clicável (com versão VML para o Outlook no Windows), um link de texto e texto alternativo descritivo
nas imagens, para continuar compreensível com imagens bloqueadas.

## Variáveis de ambiente

| Variável | Obrigatória | Exemplo / observação |
|---|---|---|
| `STOCK4EVENT_EMAIL_HABILITADO` | sim, para enviar | `true`. Padrão `false`: nada é enviado |
| `STOCK4EVENT_EMAIL_REMETENTE` | sim | `Stock4event <nao-responda@seudominio.com.br>` |
| `STOCK4EVENT_APP_URL` | sim | URL real de acesso ao app (http/https), usada no botão |
| `SPRING_MAIL_HOST` | sim | servidor SMTP do provedor |
| `SPRING_MAIL_PORT` | sim | normalmente `587` (STARTTLS) |
| `SPRING_MAIL_USERNAME` | conforme o provedor | usuário SMTP |
| `SPRING_MAIL_PASSWORD` | conforme o provedor | senha ou chave SMTP. **Só em variável de ambiente** |
| `SPRING_MAIL_SMTP_AUTH` | não | padrão `true`; `false` para o Mailpit |
| `SPRING_MAIL_SMTP_STARTTLS` | não | padrão `true`; `false` para o Mailpit |

O modelo está em [`email.env.example`](../email.env.example). Com `STOCK4EVENT_EMAIL_HABILITADO=true`, o
backend **não sobe** se faltar alguma variável obrigatória, e a mensagem de erro diz qual é. Senhas e
chaves nunca vão para o log, para o frontend ou para o repositório (`email.env` e `.env` estão no
`.gitignore`). No log aparecem só o tipo do erro e o e-mail mascarado (`j***@exemplo.com`).

Ajustes opcionais (em `application.properties` ou variável de ambiente equivalente):
`stock4event.email.max-tentativas` (5), `stock4event.email.espera-inicial-segundos` (60),
`stock4event.email.intervalo-reprocessamento-ms` (60000), `stock4event.email.reprocessamento-automatico` (true).

### Como carregar as variáveis

PowerShell (na pasta do backend, com o `email.env` preenchido):

```powershell
Get-Content email.env | Where-Object { $_ -match '^\s*[^#].*=' } | ForEach-Object {
  $nome, $valor = $_ -split '=', 2
  Set-Item -Path "Env:$($nome.Trim())" -Value $valor.Trim()
}
.\mvnw spring-boot:run
```

IntelliJ: em *Run → Edit Configurations → Environment variables*, informe as mesmas variáveis.
Em servidores/nuvem, use o painel de variáveis de ambiente ou segredos do serviço de hospedagem.

## Configurar o provedor e o remetente

Qualquer provedor com SMTP funciona (Brevo, SendGrid, Amazon SES, Mailgun, Gmail/Google Workspace…):

1. Crie a conta no provedor e gere uma **credencial SMTP** (usuário e senha/chave). No Gmail, é preciso
   ativar a verificação em duas etapas e gerar uma *senha de app*; a senha normal não funciona.
2. **Verifique o remetente**: confirme o endereço de `STOCK4EVENT_EMAIL_REMETENTE` ou, de preferência,
   autentique o domínio inteiro.
3. Para um domínio próprio, adicione no DNS os registros que o provedor indicar: **SPF** (TXT),
   **DKIM** (TXT/CNAME) e uma política **DMARC** (`_dmarc`, TXT). Sem isso, a mensagem tende a cair no spam.
4. Use o host e a porta SMTP informados pelo provedor em `SPRING_MAIL_HOST`/`SPRING_MAIL_PORT`.

## Teste local, sem enviar para ninguém (Mailpit)

O [Mailpit](https://mailpit.axllent.org/) é um servidor SMTP de testes que captura as mensagens e as
mostra num navegador, sem entregá-las de verdade.

```powershell
docker run -d --name mailpit -p 1025:1025 -p 8025:8025 axllent/mailpit
$env:STOCK4EVENT_EMAIL_HABILITADO = "true"
$env:STOCK4EVENT_EMAIL_REMETENTE  = "Stock4event <nao-responda@stock4event.local>"
$env:STOCK4EVENT_APP_URL          = "http://localhost:8081"   # só para o teste local
$env:SPRING_MAIL_HOST             = "localhost"
$env:SPRING_MAIL_PORT             = "1025"
$env:SPRING_MAIL_SMTP_AUTH        = "false"
$env:SPRING_MAIL_SMTP_STARTTLS    = "false"
.\mvnw spring-boot:run
```

Depois cadastre uma conta **Cliente** e outra **Fornecedor** pelo app (ou pelo Swagger, em
`POST /api/cadastro`) e abra `http://localhost:8025` para ver as duas mensagens, inclusive a versão em
texto simples. Para acompanhar no banco:

```sql
SELECT usuario_id, destinatario, tipo, status, tentativas, ultimo_erro, enviado_em
  FROM email_boas_vindas ORDER BY id DESC;
```

## Habilitar em produção

1. Defina as variáveis da tabela acima no ambiente de produção, com o SMTP real e a URL publicada do app.
2. Confirme no provedor que o remetente ou o domínio está verificado (SPF/DKIM/DMARC).
3. Suba o backend: o log deve mostrar `E-mail de boas-vindas habilitado`. Faça um cadastro de teste com
   um endereço seu e confira a chegada e o painel do provedor.
4. Monitore `email_boas_vindas`: registros `FALHOU` mostram o motivo em `ultimo_erro`. Para reenviar um
   deles depois de corrigir a causa: `UPDATE email_boas_vindas SET status = 'PENDENTE', tentativas = 0,
   proxima_tentativa_em = now() WHERE id = …;`. Antes de reenviar um `INDETERMINADO`, confira no painel
   do provedor se a mensagem já saiu, para não duplicá-la.

## Testes automatizados

Nenhum teste envia e-mail real: o `JavaMailSender` é substituído por um mock.

- `unit/BoasVindasEmailTest`: textos e assuntos de cada versão, URL do botão e imagens por CID, escape
  do nome, regras de configuração, perfil → versão e classificação de falhas.
- `integracao/BoasVindasEmailIT`: fluxo real do `POST /api/cadastro` contra o banco de testes:
  destinatário e versão corretos para cliente, fornecedor e cliente-fornecedor; nome escapado; nada
  enviado em cadastro inválido, duplicado ou desfeito; falha de envio sem desfazer a conta; novas
  tentativas simultâneas sem duplicidade; limite de tentativas; falha definitiva.
- `unit/ValidacaoDtoTest`: formatos de e-mail aceitos e recusados no cadastro.
