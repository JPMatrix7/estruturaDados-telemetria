import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementação concreta do {@link SGBD} para o domínio do Grupo 07
 * (Telemetria IoT). Coordena as estruturas em memória com a camada de
 * persistência.
 *
 * <p><strong>Estruturas em memória.</strong></p>
 * <ul>
 *   <li>{@link HashMap}{@code <String,String>} — índice primário, oferece
 *       acesso direto por timestamp ({@code get} em O(1) médio);</li>
 *   <li>{@link NoBST} — índice secundário ordenado por timestamp, usado pela
 *       consulta de soma em faixa ({@link #consultarSoma(int, int)}).</li>
 * </ul>
 *
 * <p><strong>Durabilidade e reconstrução.</strong> Toda gravação e remoção é
 * primeiro registrada no {@link LogAppendOnly} e só então aplicada às
 * estruturas em memória. Ao ser construída, a instância relê o log e reaplica
 * os registros em ordem, reconstruindo fielmente o estado anterior ao
 * encerramento — garantindo que a memória reflita o conteúdo persistido.</p>
 *
 * <p><strong>Remoção.</strong> Conforme a seção 4.2 do enunciado, a remoção
 * física na BST não é exigida: {@link #delete(String)} remove apenas do
 * {@code HashMap}; o nó correspondente permanece na BST.</p>
 *
 * @author Grupo 07 — Telemetria IoT
 */
public class Telemetria implements SGBD {

    /** Índice primário: timestamp (String) -> leitura (String). */
    private final Map<String, String> indicePrimario;
    /** Índice secundário ordenado por timestamp; {@code null} se vazio. */
    private NoBST raizBST;
    /** Camada de persistência durável. */
    private final LogAppendOnly log;

    /**
     * Abre o banco no caminho de log informado e reconstrói o estado a partir
     * dos registros já persistidos.
     *
     * @param caminhoLog caminho do arquivo de log; não pode ser {@code null}
     */
    public Telemetria(String caminhoLog) {
        this.indicePrimario = new HashMap<>();
        this.raizBST = null;
        this.log = new LogAppendOnly(caminhoLog);
        reconstruir();
    }

    /**
     * Reaplica, em ordem cronológica, todos os registros do log, reconstruindo
     * os índices primário e secundário.
     */
    private void reconstruir() {
        List<RegistroLog> registros = log.lerTudo();
        for (RegistroLog registro : registros) {
            if (registro.getTipo() == TipoOperacao.PUT) {
                indicePrimario.put(registro.getChave(), registro.getValor());
                indexarNaBST(registro.getChave(), registro.getValor());
            } else { // DEL
                indicePrimario.remove(registro.getChave());
                // A BST não exige remoção física (enunciado, seção 4.2).
            }
        }
    }

    /**
     * Indexa um par no índice secundário (BST) quando chave e valor são
     * numéricos (timestamp inteiro e leitura real). Pares fora desse domínio
     * permanecem apenas no índice primário.
     *
     * @param chave chave a interpretar como timestamp
     * @param valor valor a interpretar como leitura
     */
    private void indexarNaBST(String chave, String valor) {
        try {
            int timestamp = Integer.parseInt(chave.trim());
            double leitura = Double.parseDouble(valor.trim());
            raizBST = NoBST.inserir(raizBST, timestamp, leitura);
        } catch (NumberFormatException e) {
            // Chave/valor não numéricos: não participam do índice ordenado.
        }
    }

    @Override
    public void put(String chave, String valor) {
        Objects.requireNonNull(chave, "chave");
        Objects.requireNonNull(valor, "valor");
        log.anexar(RegistroLog.put(chave, valor)); // persiste antes de aplicar
        indicePrimario.put(chave, valor);
        indexarNaBST(chave, valor);
    }

    @Override
    public String get(String chave) {
        return indicePrimario.get(chave);
    }

    @Override
    public void delete(String chave) {
        Objects.requireNonNull(chave, "chave");
        log.anexar(RegistroLog.del(chave)); // persiste antes de aplicar
        indicePrimario.remove(chave);
        // Remoção física na BST não é exigida (enunciado, seção 4.2).
    }

    @Override
    public void fechar() {
        log.fechar();
    }

    /**
     * Método auxiliar do domínio: grava uma leitura numérica convertendo para
     * o contrato público {@link #put(String, String)}.
     *
     * @param timestamp instante da leitura
     * @param valor leitura do sensor
     */
    public void gravarLeitura(int timestamp, double valor) {
        put(String.valueOf(timestamp), String.valueOf(valor));
    }

    /**
     * Funcionalidade específica do grupo: soma das leituras cujos timestamps
     * pertencem ao intervalo fechado {@code [inicio, fim]}, calculada sobre o
     * índice secundário (BST).
     *
     * @param inicio limite inferior da faixa (inclusive)
     * @param fim limite superior da faixa (inclusive)
     * @return a soma das leituras na faixa
     */
    public double consultarSoma(int inicio, int fim) {
        return NoBST.somarFaixa(raizBST, inicio, fim);
    }
}
