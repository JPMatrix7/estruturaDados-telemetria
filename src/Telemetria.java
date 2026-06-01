import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementacao concreta do SGBD para o Grupo 07 (Telemetria IoT).
 *
 * Mantem duas estruturas em memoria:
 *   - HashMap<String,String>: acesso direto por timestamp (chave -> valor);
 *   - BST (NoBST) indexada por timestamp: indice ordenado por tempo, usado
 *     pela consulta de soma em faixa.
 *
 * A durabilidade vem do log append-only: cada put/delete e registrado em
 * disco e, ao iniciar, o estado e reconstruido relendo o log.
 */
public class Telemetria implements SGBD {

    private final Map<String, String> mapa;
    private NoBST raizBST;
    private final LogAppendOnly log;

    public Telemetria(String caminhoLog) {
        this.mapa = new HashMap<>();
        this.raizBST = null;
        this.log = new LogAppendOnly(caminhoLog);
        reconstruir();
    }

    /** Reaplica os registros do log para reconstruir o estado em memoria. */
    private void reconstruir() {
        List<RegistroLog> registros = log.lerTudo();
        for (RegistroLog reg : registros) {
            if (reg.getTipo() == TipoOperacao.PUT) {
                mapa.put(reg.getChave(), reg.getValor());
                indexarNaBST(reg.getChave(), reg.getValor());
            } else { // DEL
                mapa.remove(reg.getChave());
                // A BST nao exige remocao fisica; o no pode ser mantido.
            }
        }
    }

    /**
     * Insere o par na BST quando chave e valor sao numericos (timestamp/leitura).
     * Entradas nao numericas ficam apenas no HashMap.
     */
    private void indexarNaBST(String chave, String valor) {
        try {
            int timestamp = Integer.parseInt(chave.trim());
            double leitura = Double.parseDouble(valor.trim());
            raizBST = NoBST.inserir(raizBST, timestamp, leitura);
        } catch (NumberFormatException e) {
            // Chave/valor fora do dominio numerico: ignora o indice ordenado.
        }
    }

    @Override
    public void put(String chave, String valor) {
        log.anexar(new RegistroLog(TipoOperacao.PUT, chave, valor));
        mapa.put(chave, valor);
        indexarNaBST(chave, valor);
    }

    @Override
    public String get(String chave) {
        return mapa.get(chave);
    }

    @Override
    public void delete(String chave) {
        log.anexar(new RegistroLog(TipoOperacao.DEL, chave, null));
        mapa.remove(chave);
        // Remocao fisica na BST nao e exigida (ver enunciado, secao 4.2).
    }

    @Override
    public void fechar() {
        log.fechar();
    }

    /**
     * Metodo auxiliar do dominio: grava uma leitura numerica convertendo
     * para o contrato publico put(String, String).
     */
    public void gravarLeitura(int timestamp, double valor) {
        put(String.valueOf(timestamp), String.valueOf(valor));
    }

    /**
     * Funcionalidade especifica do grupo: soma das leituras cujos timestamps
     * estao em [inicio, fim], usando a BST (indice ordenado por tempo).
     */
    public double consultarSoma(int inicio, int fim) {
        return NoBST.somarFaixa(raizBST, inicio, fim);
    }
}
