package br.com.master.persistence;

import br.com.master.domain.ArquivoTO;
import br.com.master.wrappers.SearchTab;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <b>Description:</b> FIXME: Document this type <br>
 * <b>Project:</b> virtual-master <br>
 *
 * @author macelai
 * @date: 18 de nov de 2018
 * @version $
 */
public interface ArquivoDAO extends JpaRepository<ArquivoTO, Long> {

    /**
     * Consulta o arquivo por nome
     *
     * @param nome
     * @return List<ArquivoTO>
     */
    List<ArquivoTO> findAllByNomeIgnoreCaseContaining(String nome);

    /**
     * Consulta o arquivo por nome
     *
     * @param nome - Nome do arquivo
     * @param searchTab - Grupo do arquivo
     * @return List<ArquivoTO>
     */
    List<ArquivoTO> findAllBySearchTabAndNomeIgnoreCaseContaining(SearchTab tab, String nome);

}
