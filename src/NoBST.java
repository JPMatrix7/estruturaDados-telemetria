/**
 * Nó de uma Árvore Binária de Busca (BST) simples — sem balanceamento —
 * indexada por <em>timestamp</em>.
 *
 * <p>Cada nó armazena um par {@code (timestamp, valor)}. O {@code timestamp}
 * é a chave de ordenação e mantém a <strong>propriedade da BST</strong>: para
 * todo nó {@code x}, todos os timestamps da subárvore esquerda são menores que
 * {@code x.timestamp} e todos os da subárvore direita são maiores. Essa
 * invariante permite percorrer a árvore <em>em ordem</em> (esquerda, raiz,
 * direita) e visitar os timestamps em ordem crescente, o que torna natural a
 * consulta de soma em faixa.</p>
 *
 * <p>Os métodos estáticos operam sobre a raiz recebida e devolvem a (eventual
 * nova) raiz, estilo idiomático para BSTs imutáveis na estrutura de chamada.</p>
 *
 * <p><strong>Complexidade.</strong> Tanto {@link #inserir} quanto
 * {@link #somarFaixa} custam O(h), onde {@code h} é a altura da árvore. Em uma
 * BST balanceada {@code h = O(log n)}; no pior caso (inserções já ordenadas) a
 * árvore degenera e {@code h = O(n)}. O balanceamento não é exigido pelo
 * enunciado.</p>
 *
 * @author Grupo 07 — Telemetria IoT
 */
public class NoBST {

    /** Chave de ordenação: instante da leitura. */
    int timestamp;
    /** Leitura do sensor associada ao timestamp. */
    double valor;
    /** Subárvore com timestamps menores que {@link #timestamp}. */
    NoBST esquerda;
    /** Subárvore com timestamps maiores que {@link #timestamp}. */
    NoBST direita;

    /**
     * Cria um nó folha.
     *
     * @param timestamp instante da leitura (chave)
     * @param valor leitura do sensor
     */
    public NoBST(int timestamp, double valor) {
        this.timestamp = timestamp;
        this.valor = valor;
    }

    /**
     * Insere um par {@code (timestamp, valor)} na BST enraizada em {@code raiz},
     * preservando a propriedade da BST.
     *
     * <p>Se o {@code timestamp} já existir, o nó correspondente tem seu valor
     * <strong>atualizado</strong> (semântica de chave única). Isso permite que
     * uma leitura corrigida substitua a anterior e que a soma em faixa reflita
     * o valor mais recente.</p>
     *
     * @param raiz raiz da (sub)árvore; {@code null} representa árvore vazia
     * @param timestamp chave a inserir ou atualizar
     * @param valor valor a associar
     * @return a raiz da árvore após a inserção
     */
    public static NoBST inserir(NoBST raiz, int timestamp, double valor) {
        if (raiz == null) {
            return new NoBST(timestamp, valor);
        }
        if (timestamp < raiz.timestamp) {
            raiz.esquerda = inserir(raiz.esquerda, timestamp, valor);
        } else if (timestamp > raiz.timestamp) {
            raiz.direita = inserir(raiz.direita, timestamp, valor);
        } else {
            raiz.valor = valor; // timestamp já existe: atualiza no próprio nó
        }
        return raiz;
    }

    /**
     * Soma os valores cujos timestamps pertencem ao intervalo fechado
     * {@code [inicio, fim]}, percorrendo a árvore em ordem.
     *
     * <p>É aplicada <em>poda de ramos</em> (desejável, não exigida pelo
     * enunciado): a subárvore esquerda só é visitada quando ainda pode conter
     * timestamps {@code >= inicio} (isto é, quando {@code raiz.timestamp >
     * inicio}); a subárvore direita só é visitada quando ainda pode conter
     * timestamps {@code <= fim} (isto é, quando {@code raiz.timestamp < fim}).
     * A poda preserva a corretude do percurso em ordem e evita descer por ramos
     * que comprovadamente não interceptam a faixa.</p>
     *
     * @param raiz raiz da (sub)árvore; {@code null} representa árvore vazia
     * @param inicio limite inferior da faixa (inclusive)
     * @param fim limite superior da faixa (inclusive)
     * @return a soma dos valores na faixa; {@code 0.0} se nenhum timestamp casar
     */
    public static double somarFaixa(NoBST raiz, int inicio, int fim) {
        if (raiz == null) {
            return 0.0;
        }
        double soma = 0.0;

        // Desce à esquerda apenas se ainda houver chance de timestamp >= inicio.
        if (raiz.timestamp > inicio) {
            soma += somarFaixa(raiz.esquerda, inicio, fim);
        }
        // Contabiliza o próprio nó se estiver dentro da faixa.
        if (raiz.timestamp >= inicio && raiz.timestamp <= fim) {
            soma += raiz.valor;
        }
        // Desce à direita apenas se ainda houver chance de timestamp <= fim.
        if (raiz.timestamp < fim) {
            soma += somarFaixa(raiz.direita, inicio, fim);
        }
        return soma;
    }
}
