package br.com.master.persistence;

import br.com.master.configuration.MasterException;
import br.com.master.domain.ArquivoTO;
import br.com.master.domain.BlocoTO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * <b>Description:</b> FIXME: Document this type <br>
 * <b>Project:</b> virtual-master <br>
 *
 * @author macelai
 * @date: 16 de nov de 2018
 * @version $
 */
public interface BlocoDAO extends JpaRepository<BlocoTO, Long> {

    /**
     * Carrega todos os blocos que pertence ao arquivo
     *
     * @param arquivo
     * @return List<BlocoTO>
     * @throws MasterException
     */
    List<BlocoTO> findByArquivoOrderByNumeroAsc(ArquivoTO arquivo) throws MasterException;

    /**
     * Carrega todos os blocos que estão apenas no diretório local {@link BlocoTO#getDiretorioOffLine()} e não possuem
     * {@link BlocoTO#getInstanceId()}
     *
     * @return List<BlocoTO>
     * @throws MasterException List<BlocoTO>
     */
    @Modifying(clearAutomatically = true)
    @Query("" //
            + " SELECT id, " // ------------------------------ 0
            + "        uuid," // ----------------------------- 1
            + "        diretorioOffLine " // ----------------- 2
            + " FROM BlocoTO " //
            + " WHERE instanceId IS NULL ")
    List<Object[]> carregarParaGracacao() throws MasterException;

    /**
     * Carrega os blocos que deverão ser replicados em outras instâncias slaves
     *
     * @return List<Object[]>
     * @throws MasterException
     */
    @Query(value = " " + //
            " SELECT MIN(B.id)          AS ID, " + // ----------------------- 0
            "        B.numero           AS NUMERO, " + // ------------------- 1
            "        B.uuid             AS UUID, " + // --------------------- 2
            "        B.tamanho          AS TAMANHO, " + // ------------------ 3
            "        B.dir_off_line     AS DIR_OFF_LINE, " + // ------------- 4
            "        MAX(B.instance_id) AS INSTANCE_ID, " + // -------------- 5
            "        B.arquivo_id       AS ARQUIVO_ID, " + // --------------- 6
            "        C.qtde_replicacao  AS QTDE_REPLICACAO, " + // ---------- 7
            "        COUNT(B.id)        AS QTDE_BL " + // ------------------- 8
            " FROM RV_BLOCO B " + //
            " LEFT JOIN RV_CONFIGURACAO C ON C.id = C.id " + //
            " WHERE B.instance_id IS NOT NULL " + //
            " GROUP BY B.numero, " + //
            "      B.uuid, " + //
            "      B.tamanho, " + //
            "      B.dir_off_line, " + //
            "      B.arquivo_id, " + //
            "      C.qtde_replicacao " + //
            " HAVING COUNT(B.id) <= C.qtde_replicacao ", //
            nativeQuery = true) //
    List<Object[]> carregarParaReplicacao() throws MasterException;

    /**
     * Carrega os blocos que deverão ser excluídos das instâncias slaves
     *
     * @return List<Object[]>
     * @throws MasterException
     */
    @Query(value = " " + //
            " SELECT B.uuid             AS UUID, " + // --------------------- 0
            "        MAX(B.instance_id) AS INSTANCE_ID, " + // -------------- 1
            "        C.qtde_replicacao  AS QTDE_REPLICACAO, " + // ---------- 2
            "        COUNT(B.id)        AS QTDE_BL" + // -------------------- 3
            " FROM RV_BLOCO B " + //
            " LEFT JOIN RV_CONFIGURACAO C ON C.id = C.id " + //
            " WHERE B.instance_id IS NOT NULL " + //
            " AND   B.replica = :paramIsReplica " + //
            " GROUP BY B.uuid, " + //
            "      C.qtde_replicacao " + //
            " HAVING COUNT(B.id) > C.qtde_replicacao ", //
            nativeQuery = true) //
    List<Object[]> carregarParaExclusao(@Param("paramIsReplica") boolean replica) throws MasterException;

    /**
     * Atualiza algumas informações do bloco
     *
     * @param blocoInstanceId
     * @param blocoId
     */
    @Modifying(clearAutomatically = true)
    @Query(" " + //
            " UPDATE BlocoTO " + //
            " SET instanceId =:blocoInstanceId " + //
            " WHERE id =:blocoId ")
    void update(@Param("blocoInstanceId") String blocoInstanceId, @Param("blocoId") long blocoId)
            throws MasterException;

    /**
     * Verifica se já existe um bloco na instância
     *
     * @param uuid
     * @param instanceId
     * @return boolean
     */
    boolean existsByUuidAndInstanceId(String uuid, String instanceId) throws MasterException;

    /**
     * Exclui o bloco que tem o uuid e instanceId informado
     *
     * @param uuid
     * @param instanceId void
     */
    void deleteByUuidAndInstanceId(String uuid, String instanceId);

}
