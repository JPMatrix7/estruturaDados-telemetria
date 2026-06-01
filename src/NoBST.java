/**
 * No de uma arvore binaria de busca (BST) simples, sem balanceamento,
 * indexada por timestamp.
 *
 * Cada no guarda um par (timestamp, valor). O timestamp e a chave de
 * ordenacao: subarvore esquerda contem timestamps menores e a direita,
 * maiores. Isso permite responder consultas de soma em faixa percorrendo
 * a arvore em ordem.
 */
public class NoBST {

    int timestamp;
    double valor;
    NoBST esquerda;
    NoBST direita;

    public NoBST(int timestamp, double valor) {
        this.timestamp = timestamp;
        this.valor = valor;
    }

    /**
     * Insere um par (timestamp, valor) na BST cuja raiz e {@code raiz}.
     * Se o timestamp ja existir, o valor e atualizado (necessario para
     * refletir leituras atualizadas). Devolve a raiz (possivelmente nova).
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
            // Mesmo timestamp: atualiza o valor no proprio no.
            raiz.valor = valor;
        }
        return raiz;
    }

    /**
     * Soma os valores cujos timestamps estao no intervalo [inicio, fim]
     * (inclusive em ambas as pontas), percorrendo a BST a partir de
     * {@code raiz}.
     *
     * Faz poda de ramos: so desce a esquerda se houver chance de existir
     * timestamp >= inicio, e a direita se houver chance de timestamp <= fim.
     * A poda e desejavel (nao exigida) e mantem o percurso em ordem.
     */
    public static double somarFaixa(NoBST raiz, int inicio, int fim) {
        if (raiz == null) {
            return 0.0;
        }
        double soma = 0.0;

        // So vale descer a esquerda se a raiz ainda nao for menor que inicio.
        if (raiz.timestamp > inicio) {
            soma += somarFaixa(raiz.esquerda, inicio, fim);
        }
        // O proprio no entra na soma se estiver dentro da faixa.
        if (raiz.timestamp >= inicio && raiz.timestamp <= fim) {
            soma += raiz.valor;
        }
        // So vale descer a direita se a raiz ainda nao for maior que fim.
        if (raiz.timestamp < fim) {
            soma += somarFaixa(raiz.direita, inicio, fim);
        }
        return soma;
    }
}
