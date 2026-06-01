import java.util.Objects;

/**
 * Registro imutável correspondente a uma única operação do log append-only.
 *
 * <p>Esta classe é a fronteira entre o modelo em memória e a representação
 * textual gravada em disco. Ela sabe traduzir uma operação para uma linha de
 * texto ({@link #codificar()}) e reconstruir a operação a partir da linha
 * ({@link #decodificar(String)}).</p>
 *
 * <p><strong>Formato de uma linha</strong> (campos separados pelo caractere de
 * tabulação {@code '\t'}):</p>
 * <pre>
 *   PUT&lt;TAB&gt;chave&lt;TAB&gt;valor
 *   DEL&lt;TAB&gt;chave
 * </pre>
 *
 * <p>O caractere de tabulação foi escolhido como separador por raramente
 * ocorrer em chaves ou valores do domínio (timestamps e leituras numéricas),
 * evitando colisões que um separador como {@code ';'} ou {@code ','} poderia
 * causar.</p>
 *
 * @author Grupo 07 — Telemetria IoT
 */
public final class RegistroLog {

    /** Separador de campos dentro de uma linha do log. */
    private static final char SEPARADOR = '\t';

    private final TipoOperacao tipo;
    private final String chave;
    /** Valor associado; é sempre {@code null} quando {@code tipo == DEL}. */
    private final String valor;

    /**
     * Cria um registro de gravação ({@code PUT}).
     *
     * @param chave chave gravada; não pode ser {@code null}
     * @param valor valor gravado; não pode ser {@code null}
     * @return um registro {@code PUT} imutável
     */
    public static RegistroLog put(String chave, String valor) {
        return new RegistroLog(TipoOperacao.PUT,
                Objects.requireNonNull(chave, "chave"),
                Objects.requireNonNull(valor, "valor"));
    }

    /**
     * Cria um registro de remoção ({@code DEL}).
     *
     * @param chave chave removida; não pode ser {@code null}
     * @return um registro {@code DEL} imutável
     */
    public static RegistroLog del(String chave) {
        return new RegistroLog(TipoOperacao.DEL,
                Objects.requireNonNull(chave, "chave"), null);
    }

    private RegistroLog(TipoOperacao tipo, String chave, String valor) {
        this.tipo = tipo;
        this.chave = chave;
        this.valor = valor;
    }

    public TipoOperacao getTipo() {
        return tipo;
    }

    public String getChave() {
        return chave;
    }

    /** @return o valor gravado, ou {@code null} para registros {@code DEL}. */
    public String getValor() {
        return valor;
    }

    /**
     * Serializa este registro em uma linha de texto pronta para ser anexada ao
     * log (sem o terminador de linha).
     *
     * @return a representação textual do registro
     */
    public String codificar() {
        if (tipo == TipoOperacao.DEL) {
            return TipoOperacao.DEL.name() + SEPARADOR + chave;
        }
        return TipoOperacao.PUT.name() + SEPARADOR + chave + SEPARADOR + valor;
    }

    /**
     * Reconstrói um registro a partir de uma linha lida do log.
     *
     * <p>Linhas vazias ou malformadas (tipo desconhecido, campos faltando) são
     * descartadas devolvendo {@code null}, tornando a releitura tolerante a um
     * eventual registro truncado ao final do arquivo.</p>
     *
     * @param linha linha de texto lida do log (sem o terminador de linha)
     * @return o registro correspondente, ou {@code null} se a linha for inválida
     */
    public static RegistroLog decodificar(String linha) {
        if (linha == null || linha.isEmpty()) {
            return null;
        }
        // limit = -1 preserva campos vazios à direita (ex.: valor "").
        String[] campos = linha.split(String.valueOf(SEPARADOR), -1);
        if (campos.length < 2) {
            return null;
        }
        TipoOperacao tipo;
        try {
            tipo = TipoOperacao.valueOf(campos[0]);
        } catch (IllegalArgumentException e) {
            return null; // primeiro campo não é PUT nem DEL
        }
        String chave = campos[1];
        if (tipo == TipoOperacao.DEL) {
            return del(chave);
        }
        if (campos.length < 3) {
            return null; // PUT exige o campo de valor
        }
        return put(chave, campos[2]);
    }

    @Override
    public String toString() {
        return codificar();
    }
}
