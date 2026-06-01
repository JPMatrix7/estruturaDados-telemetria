/**
 * Representa um registro (uma linha) do log append-only.
 *
 * Formato textual de uma linha:
 *   PUT\tchave\tvalor
 *   DEL\tchave
 *
 * Usa-se o caractere TAB ('\t') como separador para nao colidir com
 * pontos, virgulas ou outros caracteres que possam aparecer no valor.
 */
public class RegistroLog {

    private static final char SEP = '\t';

    private final TipoOperacao tipo;
    private final String chave;
    private final String valor; // null quando tipo == DEL

    public RegistroLog(TipoOperacao tipo, String chave, String valor) {
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

    public String getValor() {
        return valor;
    }

    /** Codifica este registro em uma linha de texto para gravar no log. */
    public String codificar() {
        if (tipo == TipoOperacao.DEL) {
            return tipo.name() + SEP + chave;
        }
        return tipo.name() + SEP + chave + SEP + valor;
    }

    /**
     * Decodifica uma linha do log de volta para um RegistroLog.
     * Devolve null se a linha estiver vazia ou malformada.
     */
    public static RegistroLog decodificar(String linha) {
        if (linha == null || linha.isEmpty()) {
            return null;
        }
        String[] partes = linha.split(String.valueOf(SEP), -1);
        if (partes.length < 2) {
            return null;
        }
        TipoOperacao tipo;
        try {
            tipo = TipoOperacao.valueOf(partes[0]);
        } catch (IllegalArgumentException e) {
            return null;
        }
        String chave = partes[1];
        if (tipo == TipoOperacao.DEL) {
            return new RegistroLog(TipoOperacao.DEL, chave, null);
        }
        // PUT precisa de um terceiro campo (valor).
        if (partes.length < 3) {
            return null;
        }
        return new RegistroLog(TipoOperacao.PUT, chave, partes[2]);
    }
}
