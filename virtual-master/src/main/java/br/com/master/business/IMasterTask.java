package br.com.master.business;

import br.com.master.configuration.MasterException;

/**
 * <b>Description:</b> FIXME: Document this type <br>
 * <b>Project:</b> virtual-master <br>
 *
 * @author macelai
 * @date: 15 de abr de 2019
 * @version $
 */
public interface IMasterTask {

    /**
     * Executa a tarefa que obtêm as instâncias do virtual slave
     *
     * @throws MasterException
     */
    void instance() throws MasterException;

    /**
     * Envia os blocos que estão no diretório para uma instância de slave
     *
     * @throws MasterException
     */
    void gravacao() throws MasterException;

    /**
     * Replica os blocos que estão armazenados
     *
     * @throws MasterException
     */
    void replicacao() throws MasterException;

    /**
     * Limpa o diretório temporário dos arquivos da aplicação.
     *
     * @throws MasterException
     */
    void limpar() throws MasterException;

    /**
     * Remove os últimos blocos que foram replicados dos arquivos
     *
     * @throws MasterException
     */
    void exclusao() throws MasterException;

    /**
     * Realiza a migração de blocos entre as instâncias
     *
     * @throws MasterException
     */
    void migracao() throws MasterException;

}
