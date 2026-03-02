# SysSentinel Client (Agent)

O **SysSentinel Client** é um agente Java responsável por coletar informações do sistema operacional utilizando **OSHI** e enviá-las periodicamente para o **SysSentinel Server** via HTTP.

Após o provisionamento inicial, o agente opera de forma autônoma.


---

## Índice

* [Tecnologias Utilizadas](#tecnologias-utilizadas)
* [Execução](#execução)
* [Arquivos de Configuração (Gerados Automaticamente)](#arquivos-de-configuração-gerados-automaticamente)

  * [sysSentinel.config](#syssentinelconfig)
  * [Security/RegisterToken.config](#securityregistertokenconfig)
  * [Security/jwtToken.config](#securityjwttokenconfig)
* [Dados Coletados](#dados-coletados)

  * [Dados Fixos (Inventário)](#dados-fixos-inventário)
  * [Dados Voláteis](#dados-voláteis)

    * [Informações básicas](#informações-básicas)
    * [Rede](#rede)
    * [Processos](#processos)
* [Fluxo de Comunicação com o Servidor](#fluxo-de-comunicação-com-o-servidor)

  * [1. Provisionamento Inicial](#1-provisionamento-inicial)
  * [2. Atualização de Inventário](#2-atualização-de-inventário)
  * [3. Renovação de Token](#3-renovação-de-token)
  * [4. Envio Periódico de Métricas](#4-envio-periódico-de-métricas)
* [Ciclo de Execução](#ciclo-de-execução)
* [Reset do Agente](#reset-do-agente)

---

## Tecnologias Utilizadas

* Java
* Maven
* OSHI (coleta de métricas do sistema)
* Jackson (serialização JSON)
* OkHttp (cliente HTTP)

---

## Execução

Dentro da pasta do projeto:

```bash
mvn package
java -cp target/classes com.bolota.SysSentinelClient.Client
```

---

## Arquivos de Configuração (Gerados Automaticamente)

O agente gera arquivos locais na primeira execução.
Esses arquivos estão listados no `.gitignore` e **não devem ser versionados**.

### sysSentinel.config

Armazena informações locais do agente.

Formato:

```
url=<...>
uuid=<...>
```

* `uuid` é definido após o primeiro provisionamento bem-sucedido.
* O arquivo é atualizado automaticamente pelo agente.

---

### Security/RegisterToken.config

Armazena a chave de registro usada no provisionamento inicial e na renovação de token.

Formato:

```
RegisterKey=<...>
```

Caso o arquivo não exista, o agente pode solicitá-lo durante a execução.

> [!WARNING]
> A `RegisterKey` definida no Cliente deve ser idêntica à que for definida no Servidor, caso contrário, o cliente não conseguirá fazer uma requisição para receber um JWT próprio. .

---

### Security/jwtToken.config

Armazena o JWT do agente.

Formato:

```
token=<...>
```

O token é:

* Gerado pelo servidor
* Atualizado automaticamente quando necessário

---

## Dados Coletados

A coleta é feita utilizando OSHI.

### Dados Fixos (Inventário)

Enviados como `SystemDTO`:

* Hostname
* Sistema operacional
* Modelo da máquina
* CPU
* Lista de GPUs
* Memória total (GB)

Esses dados representam o perfil estático do sistema.

---

### Dados Voláteis

Enviados como `SystemVolatileEntity`.

Incluem:

#### Informações básicas

* Temperatura da CPU
* Uptime formatado
* Total de processos ativos
* Memória RAM utilizada

#### Rede

* Interfaces de rede (adapter → IPv4)
* Download e Upload (calculados por diferença de bytes em intervalo de medição)

#### Processos

Lista contendo:

* Nome
* PID
* Memória residente (MB)
* Memória virtual (GB)
* CPU load normalizado por número de núcleos lógicos

---

## Fluxo de Comunicação com o Servidor

Base dos endpoints utilizados:

```
/api/systems/
```

---

### 1. Provisionamento Inicial

`POST /api/systems/sysinfo`

Headers:

```
JwtToken: "null"
RegisterToken: <RegisterKey>
```

Body:

```
SystemDTO (JSON)
```

Resposta esperada:

```json
{
  "UUID": "...",
  "token": "..."
}
```

O cliente então:

* Salva o UUID
* Salva o JWT

---

### 2. Atualização de Inventário

`POST /api/systems/sysinfo`

Headers:

```
JwtToken: <token>
RegisterToken: "null"
```

Body:

```
SystemDTO (JSON)
```

Se receber `401`, o cliente tenta renovar o token.

---

### 3. Renovação de Token

`GET /api/systems/updateAuth`

Headers:

```
JwtToken: "null"
RegisterToken: <RegisterKey>
sysUUID: <uuid>
```

Resposta:

```json
{
  "UUID": "...",
  "token": "..."
}
```

---

### 4. Envio Periódico de Métricas

`POST /api/systems/sysinfovolatile`

Headers:

```
Authorization: Bearer <token>
```

Body:

```
SystemVolatileEntity (JSON)
```

Tratamento de resposta:

| Status | Comportamento             |
| ------ | ------------------------- |
| 200    | OK                        |
| 401    | Tenta renovar token       |
| 404    | Tenta reenviar inventário |

---

## Ciclo de Execução

Após iniciar:

1. Envia inventário fixo
2. Entra em loop:

    * Atualiza dados voláteis
    * Envia ao servidor
    * Aguarda intervalo
    * Repete

---

## Reset do Agente

Para reiniciar completamente o provisionamento, basta remover:

* `sysSentinel.config`
* `Security/jwtToken.config`
* `Security/RegisterToken.config` (se desejar redefinir a chave)

O agente recriará os arquivos automaticamente na próxima execução.
