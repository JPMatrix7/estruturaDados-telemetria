/**
 * Contrato publico do SGBD didatico chave-valor.
 *
 * Trabalho A2 - Grupo 07: Telemetria IoT.
 * A chave e o timestamp da leitura (String representando um inteiro)
 * e o valor e a leitura do sensor gravada como String.
 */
public interface SGBD {

    /** Insere ou atualiza o valor associado a chave. */
    void put(String chave, String valor);

    /** Devolve o valor associado a chave, ou null se nao existir. */
    String get(String chave);

    /** Remove a chave do banco. */
    void delete(String chave);

    /** Encerra o banco, liberando os recursos do log. */
    void fechar();
}
