package br.com.master.business.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.common.business.DBusiness;
import br.com.common.utils.Utils;
import br.com.master.business.IArquivo;
import br.com.master.business.IBloco;
import br.com.master.business.IConfiguracao;
import br.com.master.configuration.MasterBalance;
import br.com.master.configuration.MasterException;
import br.com.master.domain.ArquivoTO;
import br.com.master.domain.BlocoTO;
import br.com.master.domain.ConfiguracaoTO;
import br.com.master.persistence.ArquivoDAO;
import okhttp3.Response;

/**
 * <b>Description:</b> <br>
 * <b>Project:</b> virtual-master <br>
 *
 * @author macelai
 * @date: 18 de nov de 2018
 */
@Service
public class ArquivoBusiness extends DBusiness<ArquivoTO> implements IArquivo {

    @Autowired
    ArquivoDAO    persistence;

    @Autowired
    IConfiguracao configuracaoBusiness;

    @Autowired
    IBloco        blocoBusiness;

    @Autowired
    MasterBalance masterBalance;

    String        pathTmpDirectory = null;

    @Autowired
    public ArquivoBusiness(ServletContext servletContext) {
        File servletTempDir = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        if (servletTempDir != null) {
            pathTmpDirectory = servletTempDir.getAbsolutePath();
        } else {
            pathTmpDirectory = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ArquivoTO> carregarPor(String nome) throws MasterException {
        return persistence.findAllByNome(nome);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void excluir(ArquivoTO arquivo) throws MasterException {
        try {
            List<BlocoTO> blocos = blocoBusiness.carregarTodosPor(arquivo);
            if (blocos.isEmpty()) {
                throw new MasterException("slave.obj.nao.localizado").status(Status.NOT_FOUND);
            }

            Iterator<BlocoTO> blocoIterator = blocos.iterator();
            while (blocoIterator.hasNext()) {
                BlocoTO bloco = blocoIterator.next();
                Response response = Utils.httpDelete(masterBalance.volumeUrlSlave(bloco.getInstanceId()), "/exclusao/", bloco.getUuid());
                if (Status.OK.getStatusCode() != response.code()) {
                    throw new MasterException(response.body().string()).status(Status.BAD_REQUEST);
                }
            }

            super.excluir(arquivo);
        } catch (IOException e) {
            throw new MasterException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArquivoTO upload(MultipartFile multipartFile) throws MasterException {
        ConfiguracaoTO configuracao = configuracaoBusiness.buscar();

        ArquivoTO arquivo = new ArquivoTO();
        arquivo.setNome(multipartFile.getOriginalFilename());
        arquivo.setTamanho((int) multipartFile.getSize());
        arquivo.setMimeType(multipartFile.getContentType());

        int tamanhoBloco = configuracao.getTamanhoBloco() * 1024;
        int miniBloco = arquivo.getTamanho() % tamanhoBloco;
        int qtdeBloco = arquivo.getTamanho() / tamanhoBloco;
        int qtdePecas = 0;
        try (FileChannel channel = ((FileInputStream) multipartFile.getInputStream()).getChannel()) {
            while (qtdePecas < qtdeBloco) {
                BlocoTO bloco = createBlocoOffline(channel, ((qtdePecas++) * tamanhoBloco), tamanhoBloco, qtdePecas);
                bloco.setArquivo(arquivo);
                arquivo.getBlocos().add(bloco);
            }
            if (miniBloco > 0) {
                BlocoTO bloco = createBlocoOffline(channel, (qtdePecas * tamanhoBloco), miniBloco, ++qtdePecas);
                bloco.setArquivo(arquivo);
                arquivo.getBlocos().add(bloco);
            }
            arquivo.setPecas(qtdePecas);

            TransactionStatus status = txManager.getTransaction(getTransactionDefinition());
            try {
                super.incluir(arquivo);
                txManager.commit(status);
            } catch (Throwable e) {
                if (!status.isCompleted()) {
                    txManager.rollback(status);
                }
                e.printStackTrace();
            }

            return arquivo;
        } catch (Exception e) {
            throw new MasterException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public InputStreamResource download(ArquivoTO arquivo) throws MasterException {
        try {
            List<BlocoTO> blocos = blocoBusiness.carregarTodosPor(arquivo);
            if (blocos.isEmpty()) {
                throw new MasterException("slave.obj.nao.localizado").status(Status.NOT_FOUND);
            }

            Vector<InputStream> vector = new Vector<>();
            Iterator<BlocoTO> blocoIterator = blocos.iterator();
            Set<Integer> pecas = new HashSet<>();
            while (blocoIterator.hasNext()) {
                BlocoTO bloco = blocoIterator.next();
                if (pecas.add(bloco.getNumero())) {
                    try {
                        vector.add(new FileInputStream(new File(bloco.getDiretorioOffLine())));
                    } catch (Exception e) {
                        pecas.remove(bloco.getNumero());
                    }
                }
            }
            if (!arquivo.getPecas().equals(pecas.size())) {
                Iterator<InputStream> vectorIterator = vector.iterator();
                while (vectorIterator.hasNext()) {
                    InputStream inputStream = vectorIterator.next();
                    inputStream.close();
                }
                throw new MasterException("master.montar.blocos.qtde").args(arquivo.getPecas().toString(), String.valueOf(pecas.size()))
                        .status(Status.BAD_REQUEST);
            }

            return new InputStreamResource(new SequenceInputStream(vector.elements()));
        } catch (Exception e) {
            throw new MasterException(e.getMessage(), e);
        }
    }

    private BlocoTO createBlocoOffline(FileChannel channel, long position, long byteSize, int numero) {
        String uuid = Utils.gerarIdentificador();
        Path path = Paths.get(pathTmpDirectory);
        path = path.resolve(uuid + Utils.BLOCO_EXTENSION);
        int tamanho = Utils.fileEscrever(path, Utils.fileParticionar(channel, position, byteSize));

        BlocoTO bloco = new BlocoTO();
        bloco.setNumero(numero);
        bloco.setUuid(uuid);
        bloco.setTamanho(tamanho);
        bloco.setReplica(false);
        bloco.setDiretorioOffLine(path.toFile().getAbsolutePath());

        return bloco;
    }
}
