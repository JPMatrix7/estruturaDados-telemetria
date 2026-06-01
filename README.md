# Trabalho A2 — Grupo 07: Telemetria IoT

SGBD didático do tipo chave-valor, escrito em **Java puro**, com persistência
em disco (log append-only) e uma **BST (árvore binária de busca)** usada como
índice ordenado por timestamp para consultas de soma em faixa.

- **Disciplina:** Estrutura de Dados — UNITINS — 2026.1
- **Domínio:** leituras numéricas de um sensor IoT
  - **Chave:** timestamp da leitura (String que representa um inteiro, ex.: `"100"`)
  - **Valor:** leitura do sensor gravada como String (ex.: `"23.5"`)

## Como compilar e executar

Requer um JDK (testado com Java 21). A partir da pasta raiz do projeto:

```sh
# Compilar (gera os .class na pasta out/)
javac -d out src/*.java

# Executar a demonstração
java -cp out DemoGrupo07
```

A execução cria o arquivo `dados.log` na pasta atual. A demonstração apaga esse
arquivo no início para que a saída seja reproduzível; em seguida ela própria
demonstra a persistência fechando e reabrindo o banco.

## Saída esperada da demonstração

```
=== Telemetria IoT - Grupo 07 ===
12 leituras inseridas (timestamps fora de ordem).

Soma [0, 999] = 209,80
Soma [100, 300] = 129,10

Apos atualizar t=175 para 30.0:
Soma [100, 300] = 133,30

get("250") = 18.0
apos delete("250"), get("250") = null

--- Reabrindo o banco (reconstrucao a partir do log) ---
get("100") apos reabrir = 23.5
get("250") apos reabrir = null (removido, deve ser null)
Soma [100, 300] = 133,30  (reflete a atualizacao de t=175 persistida)
```

## Arquitetura

| Arquivo | Papel |
|---|---|
| `SGBD.java` | Interface (TAD) do banco: `put`, `get`, `delete`, `fechar`. |
| `TipoOperacao.java` | Enum com `PUT` e `DEL`. |
| `RegistroLog.java` | Codifica/decodifica uma linha do log. |
| `LogAppendOnly.java` | Log append-only com `flush` + `fsync`; relê tudo na reconstrução. |
| `NoBST.java` | Nó da BST por timestamp, com `inserir` e `somarFaixa`. |
| `Telemetria.java` | Implementação concreta: `HashMap` + BST + log. |
| `DemoGrupo07.java` | Programa de demonstração executável. |

### Estruturas em memória

- **`HashMap<String,String>`** — acesso direto por timestamp (`get` em O(1) médio).
- **BST indexada por timestamp** — índice ordenado por tempo. O método
  `somarFaixa(inicio, fim)` percorre a árvore em ordem e acumula apenas os nós
  cujo timestamp está em `[inicio, fim]` (inclusive nas duas pontas). Foi
  incluída **poda de ramos** que não interceptam a faixa — desejável, mas não
  exigida pelo enunciado.

### Persistência (log append-only)

Cada `put` e `delete` é registrado como uma linha ao final de `dados.log`
(o arquivo nunca é reescrito, só cresce). Após cada gravação faz-se
`flush()` seguido de `FileDescriptor.sync()` (fsync), garantindo a
durabilidade. Ao iniciar, `Telemetria` relê o log e reaplica os registros,
reconstruindo o HashMap e a BST.

Formato de cada linha (separador TAB):

```
PUT<TAB>100<TAB>23.5
DEL<TAB>250
```

### Observações sobre o comportamento

- **Atualização de leitura:** um novo `put` com timestamp já existente
  sobrescreve o valor no HashMap e atualiza o nó correspondente na BST — por
  isso a soma reflete a leitura mais recente.
- **Remoção:** conforme a seção 4.2 do enunciado, a remoção física na BST não é
  exigida. `delete` remove apenas do HashMap; o nó permanece na BST. Por isso a
  soma em faixa pode continuar contabilizando timestamps removidos via `delete`.

## Funcionalidade específica do grupo

```java
public double consultarSoma(int inicio, int fim);
```

Devolve a soma das leituras cujos timestamps estão em `[inicio, fim]`,
utilizando a BST.
