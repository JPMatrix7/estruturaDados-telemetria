/**
 * Tipo Abstrato de Dados (TAD) de um Sistema Gerenciador de Banco de Dados
 * (SGBD) didático do tipo chave-valor.
 *
 * <p>O contrato define as quatro operações fundamentais de um banco
 * chave-valor com persistência:</p>
 * <ul>
 *   <li>{@link #put(String, String) put} — grava ou atualiza um par;</li>
 *   <li>{@link #get(String) get} — recupera um valor pela chave;</li>
 *   <li>{@link #delete(String) delete} — remove uma chave;</li>
 *   <li>{@link #fechar() fechar} — encerra o banco e libera os recursos.</li>
 * </ul>
 *
 * <p>No domínio do Grupo 07 (Telemetria IoT), a chave é o <em>timestamp</em> da
 * leitura — uma {@code String} que representa um inteiro (ex.: {@code "100"}) —
 * e o valor é a leitura do sensor, gravada como {@code String} (ex.:
 * {@code "23.5"}). Implementações concretas podem oferecer métodos auxiliares
 * de conveniência específicos do domínio, desde que o contrato público acima
 * seja preservado.</p>
 *
 * @author Grupo 07 — Telemetria IoT
 * @see Telemetria
 */
public interface SGBD {

    /**
     * Insere um novo par chave-valor ou atualiza o valor de uma chave já
     * existente. A operação é durável: deve ser persistida antes de retornar.
     *
     * @param chave identificador do registro; não pode ser {@code null}
     * @param valor conteúdo a ser associado à chave; não pode ser {@code null}
     */
    void put(String chave, String valor);

    /**
     * Recupera o valor associado a uma chave.
     *
     * @param chave chave procurada
     * @return o valor associado, ou {@code null} se a chave não existir
     */
    String get(String chave);

    /**
     * Remove a chave do banco, caso exista. A remoção é durável.
     *
     * @param chave chave a ser removida
     */
    void delete(String chave);

    /**
     * Encerra o banco de dados, liberando os recursos de persistência (arquivo
     * de log). Após esta chamada o banco não deve mais ser utilizado.
     */
    void fechar();
}
