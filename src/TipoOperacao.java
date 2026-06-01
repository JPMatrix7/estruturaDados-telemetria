/**
 * Tipo de operação registrada no log append-only.
 *
 * <p>Cada linha do log começa por um destes identificadores, permitindo que a
 * reconstrução do estado saiba se deve reaplicar uma gravação ou uma remoção.</p>
 *
 * @author Grupo 07 — Telemetria IoT
 * @see RegistroLog
 * @see LogAppendOnly
 */
public enum TipoOperacao {

    /** Gravação ou atualização de um par chave-valor. */
    PUT,

    /** Remoção de uma chave. */
    DEL
}
