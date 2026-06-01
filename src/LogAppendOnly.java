import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Log append-only com persistencia em disco.
 *
 * Cada operacao (put/delete) e registrada como uma linha ao final do
 * arquivo, que nunca e reescrito (somente cresce). Apos cada gravacao
 * faz-se flush + force(true)/fsync para garantir a durabilidade.
 *
 * Na inicializacao, o metodo lerTudo() relê todo o arquivo, permitindo
 * reconstruir o estado do banco em memoria.
 */
public class LogAppendOnly {

    private final String caminho;
    private final FileOutputStream fileOut;
    private final Writer writer;

    public LogAppendOnly(String caminho) {
        this.caminho = caminho;
        try {
            // append = true: nao apaga o conteudo existente.
            this.fileOut = new FileOutputStream(caminho, true);
            this.writer = new OutputStreamWriter(fileOut, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao abrir o log: " + caminho, e);
        }
    }

    /** Le todos os registros ja gravados, na ordem de gravacao. */
    public List<RegistroLog> lerTudo() {
        List<RegistroLog> registros = new ArrayList<>();
        java.io.File arquivo = new java.io.File(caminho);
        if (!arquivo.exists()) {
            return registros;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo, StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                RegistroLog reg = RegistroLog.decodificar(linha);
                if (reg != null) {
                    registros.add(reg);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o log: " + caminho, e);
        }
        return registros;
    }

    /** Acrescenta um registro ao final do log e garante a gravacao em disco. */
    public void anexar(RegistroLog registro) {
        try {
            writer.write(registro.codificar());
            writer.write(System.lineSeparator());
            writer.flush();                 // empurra o buffer da JVM para o SO
            fileOut.getFD().sync();         // fsync: forca o SO a gravar no disco
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar no log: " + caminho, e);
        }
    }

    /** Fecha os recursos do log. */
    public void fechar() {
        try {
            writer.close(); // fecha tambem o FileOutputStream subjacente
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao fechar o log: " + caminho, e);
        }
    }
}
