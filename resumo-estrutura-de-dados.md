# Estrutura de Dados — Resumo extraído

## 1. Algoritmos de ordenação — in-place

**Pergunta:**  
O que significa dizer que um algoritmo de ordenação é **in-place**? Cite dois algoritmos vistos em aula que são in-place.

**Resposta:**  
Um algoritmo de ordenação **in-place** é aquele que reorganiza os elementos **na própria estrutura original**, usando **pouca memória extra**, geralmente apenas variáveis auxiliares.

**Exemplos de algoritmos in-place:**
- Selection Sort
- Insertion Sort

> Observação: Bubble Sort também é normalmente classificado como in-place.

---

## 3. Recursividade

**Pergunta:**  
Quais são os **dois elementos obrigatórios** de um método recursivo? O que acontece se um deles estiver ausente?

**Resposta:**  
Os dois elementos obrigatórios são:

1. **Caso base**
2. **Passo recursivo**

**Se o caso base estiver ausente:**  
A recursão pode continuar indefinidamente até causar erro, como `StackOverflowError`.

**Se o passo recursivo estiver ausente:**  
O problema não é reduzido e o método não avança corretamente em direção ao caso base.

---

## 6. Pilha — rastreamento de operações com `ArrayDeque`

**Código analisado:**

```java
Deque<Integer> s = new ArrayDeque<>();
s.push(10);
s.push(20);
s.push(10);
s.push(30);
System.out.println(s.pop());
s.push(40);
System.out.println(s.peek());
System.out.println(s.pop());
System.out.println(s.size());
```

**Saída completa, na ordem:**

```text
30
40
40
3
```

**Forma resumida:**

```text
30 / 40 / 40 / 3
```

**Rastreamento da pilha:**
- `push(10)` → `[10]`
- `push(20)` → `[20, 10]`
- `push(10)` → `[10, 20, 10]`
- `push(30)` → `[30, 10, 20, 10]`
- `pop()` → imprime **30**
- `push(40)` → `[40, 10, 20, 10]`
- `peek()` → imprime **40**
- `pop()` → imprime **40**
- `size()` → imprime **3**

---

## 7. Notações O, Omega e Theta

**Caso apresentado:**  
Na busca linear em um array, o elemento pode estar na primeira posição (**melhor caso**) ou exigir a varredura de todo o array (**pior caso**).

**Afirmações analisadas:**
1. **Big-O** descreve o pior caso (limite superior).
2. **Omega (Ω)** descreve o melhor caso (limite inferior).
3. **Theta (Θ)** descreve o caso exato, quando melhor e pior caso crescem na mesma taxa.
4. Na prática, usa-se muito Big-O porque ele fornece uma garantia para o cenário mais desfavorável.

**Resposta correta:**  
**I, II, III e IV.**

---

## 8. Compilação e execução de programas Java

**Caso apresentado:**  
Arquivo `Ola.java` com uma classe pública `Ola`.

**Afirmações analisadas:**
1. O comando correto para compilar é: `javac Ola.java`
2. Após compilar com sucesso, é gerado um arquivo chamado `Ola.class`
3. Para executar o programa, o comando correto é: `javaw Ola.class`
4. O nome da classe pública pode ser diferente do nome do arquivo `.java`

**Resposta correta:**  
**Apenas I e II estão corretas.**

**Justificativa:**
- I está correta: `javac Ola.java`
- II está correta: a compilação gera `Ola.class`
- III está incorreta: o correto é `java Ola`
- IV está incorreta: se a classe é pública, o nome da classe deve coincidir com o nome do arquivo `.java`

---

## Resumo objetivo

- **In-place:** ordena na própria estrutura, com pouca memória extra.
- **Recursão:** precisa de caso base e passo recursivo.
- **Pilha com `ArrayDeque`:** a saída do código é `30 / 40 / 40 / 3`.
- **Complexidade:** Big-O, Omega e Theta estavam todas corretamente descritas no enunciado.
- **Java:** compila com `javac NomeArquivo.java` e executa com `java NomeClasse`.
