import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Log de operações <em>append-only</em> com persistência durável em disco.
 *
 * <p>Esta é a camada de persistência do banco. Cada operação ({@code PUT} ou
 * {@code DEL}) é registrada como uma linha acrescentada ao final do arquivo,
 * que <strong>nunca é reescrito</strong> — apenas cresce. Essa estratégia
 * (também chamada de <em>write-ahead log</em>) torna a gravação sequencial e
 * simples de raciocinar: o estado do banco em qualquer instante é o resultado
 * de reaplicar, em ordem, todos os registros do log.</p>
 *
 * <p><strong>Durabilidade.</strong> Após escrever cada linha são executados
 * {@code flush()} (esvazia o buffer da JVM em direção ao sistema operacional) e
 * {@code FileDescriptor.sync()} (fsync — força o SO a persistir os blocos no
 * dispositivo físico). Assim, uma vez que {@link #anexar(RegistroLog)} retorna,
 * o registro sobrevive a uma queda do programa ou do sistema.</p>
 *
 * <p><strong>Complexidade.</strong> {@link #anexar(RegistroLog)} é O(1) em
 * trabalho de CPU (mais o custo de E/S do fsync); {@link #lerTudo()} é O(n) no
 * número de registros já gravados.</p>
 *
 * @author Grupo 07 — Telemetria IoT
 * @see RegistroLog
 */
public final class LogAppendOnly {

    private final File arquivo;
    private final FileOutputStream saidaArquivo;
    private final Writer escritor;

    /**
     * Abre (ou cria) o arquivo de log no caminho informado, em modo de anexação.
     * O conteúdo já existente é preservado, permitindo a reconstrução posterior.
     *
     * @param caminho caminho do arquivo de log; não pode ser {@code null}
     * @throws UncheckedIOException se o arquivo não puder ser aberto
     */
    public LogAppendOnly(String caminho) {
        this.arquivo = new File(Objects.requireNonNull(caminho, "caminho"));
        try {
            // append = true: não trunca o conteúdo existente.
            this.saidaArquivo = new FileOutputStream(arquivo, true);
            this.escritor = new OutputStreamWriter(saidaArquivo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao abrir o log: " + caminho, e);
        }
    }

    /**
     * Lê todos os registros já gravados, na ordem cronológica de gravação.
     *
     * <p>Registros malformados são silenciosamente ignorados (ver
     * {@link RegistroLog#decodificar(String)}), o que torna a releitura
     * resiliente a um eventual último registro truncado.</p>
     *
     * @return lista imutável dos registros lidos (vazia se o log ainda não existe)
     * @throws UncheckedIOException em caso de erro de leitura
     */
    public List<RegistroLog> lerTudo() {
        List<RegistroLog> registros = new ArrayList<>();
        if (!arquivo.exists()) {
            return Collections.emptyList();
        }
        try (BufferedReader leitor =
                     new BufferedReader(new FileReader(arquivo, StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                RegistroLog registro = RegistroLog.decodificar(linha);
                if (registro != null) {
                    registros.add(registro);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o log: " + arquivo, e);
        }
        return Collections.unmodifiableList(registros);
    }

    /**
     * Acrescenta um registro ao final do log e garante sua gravação efetiva no
     * disco antes de retornar.
     *
     * @param registro operação a ser persistida; não pode ser {@code null}
     * @throws UncheckedIOException em caso de erro de gravação
     */
    public void anexar(RegistroLog registro) {
        Objects.requireNonNull(registro, "registro");
        try {
            escritor.write(registro.codificar());
            escritor.write(System.lineSeparator());
            escritor.flush();              // buffer da JVM -> sistema operacional
            saidaArquivo.getFD().sync();   // fsync: sistema operacional -> disco físico
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar no log: " + arquivo, e);
        }
    }

    /**
     * Fecha os recursos do log. Idempotência não é garantida: deve ser chamado
     * uma única vez, ao encerrar o banco.
     *
     * @throws UncheckedIOException em caso de erro ao fechar
     */
    public void fechar() {
        try {
            escritor.close(); // fecha também o FileOutputStream subjacente
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao fechar o log: " + arquivo, e);
        }
    }
}
