import java.io.File;
import java.util.Locale;

/**
 * Programa de demonstracao do Trabalho A2 - Grupo 07 (Telemetria IoT).
 *
 * Demonstra:
 *   1. insercao de leituras com timestamps fora de ordem;
 *   2. consultarSoma na faixa completa;
 *   3. consultarSoma em uma faixa parcial;
 *   4. atualizacao de uma leitura e a soma refletindo a mudanca;
 *   5. operacoes basicas get/delete;
 *   6. persistencia: fechar, reabrir e reconstruir o estado a partir do log.
 */
public class DemoGrupo07 {

    private static final String CAMINHO_LOG = "dados.log";

    public static void main(String[] args) {
        // Comeca de um log limpo para que a demonstracao seja reproduzivel.
        new File(CAMINHO_LOG).delete();

        Telemetria banco = new Telemetria(CAMINHO_LOG);

        // 1. Insere 12 leituras com timestamps deliberadamente fora de ordem.
        int[] timestamps = {100, 250, 50, 400, 175, 330, 205, 90, 300, 150, 500, 220};
        double[] valores  = {23.5, 18.0, 30.2, 12.0, 25.8, 9.5, 14.2, 21.0, 11.1, 19.9, 8.0, 16.6};
        for (int i = 0; i < timestamps.length; i++) {
            banco.gravarLeitura(timestamps[i], valores[i]);
        }

        System.out.println("=== Telemetria IoT - Grupo 07 ===");
        System.out.println(timestamps.length + " leituras inseridas (timestamps fora de ordem).");
        System.out.println();

        // 2. Soma da faixa completa.
        System.out.println(linhaSoma(0, 999, banco.consultarSoma(0, 999)));

        // 3. Soma de uma faixa parcial.
        System.out.println(linhaSoma(100, 300, banco.consultarSoma(100, 300)));

        // 4. Atualiza uma leitura existente e mostra a soma refletindo a mudanca.
        System.out.println();
        System.out.println("Apos atualizar t=175 para 30.0:");
        banco.gravarLeitura(175, 30.0);
        System.out.println(linhaSoma(100, 300, banco.consultarSoma(100, 300)));

        // 5. Operacoes basicas get/delete.
        System.out.println();
        System.out.println("get(\"250\") = " + banco.get("250"));
        banco.delete("250");
        System.out.println("apos delete(\"250\"), get(\"250\") = " + banco.get("250"));

        banco.fechar();

        // 6. Persistencia: reabre o banco e reconstroi o estado a partir do log.
        System.out.println();
        System.out.println("--- Reabrindo o banco (reconstrucao a partir do log) ---");
        Telemetria banco2 = new Telemetria(CAMINHO_LOG);
        System.out.println("get(\"100\") apos reabrir = " + banco2.get("100"));
        System.out.println("get(\"250\") apos reabrir = " + banco2.get("250") + " (removido, deve ser null)");
        System.out.println(linhaSoma(100, 300, banco2.consultarSoma(100, 300))
                + "  (reflete a atualizacao de t=175 persistida)");
        banco2.fechar();
    }

    /** Formata a linha de soma no padrao "Soma [a, b] = valor" com virgula decimal. */
    private static String linhaSoma(int inicio, int fim, double soma) {
        return String.format(Locale.forLanguageTag("pt-BR"),
                "Soma [%d, %d] = %.2f", inicio, fim, soma);
    }
}
