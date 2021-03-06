package br.com.master.resource;

import br.com.common.access.property.ValidarToken;
import br.com.common.resource.CommonResource;

import br.com.master.business.IArquivo;
import br.com.master.configuration.MasterException;
import br.com.master.domain.ArquivoTO;
import br.com.master.wrappers.SearchTab;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * <b>Description:</b> FIXME: Document this type <br>
 * <b>Project:</b> virtual-master <br>
 *
 * @author macelai
 * @date: 18 de nov de 2018
 * @version $
 */
@RestController()
@Api(tags = { "Arquivos" })
public class ArquivoResource extends CommonResource {

    @Autowired
    IArquivo arquivoBusiness;

    public ArquivoResource(final Environment env) {
        super(env);
    }

    @GetMapping(value = "/arquivos")
    @ApiOperation(value = "Consulta os metadados do arquivo", //
            nickname = "consulta", //
            notes = "<p>A consulta é usada para recuperar os <strong>metadados</strong> dos <strong>arquivos</strong> através do <strong>nome</strong>, caso seja localizado um ou mais registros será retornado os <strong>metadados</strong> no corpo da mensagem da resposta no formato <strong>json</strong> caso contrário, o status <strong>404</strong> indicando que o arquivo não existe ou não foi localizado.</p>", //
            response = List.class, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Nenhum arquivo foi localizado!")
    })
    public ResponseEntity<Page<ArquivoTO>> consulta(
            @ApiParam(name = "nome", //
                    value = "Nome é a identificação do arquivo digital.", //
                    required = true) //
            @RequestParam("nome") final String nome,
            @ApiParam(name = "search_tab", //
                    value = "Search Tab é o grupo do arquivo digital.", //
                    required = false) //
            @RequestParam(name = "search_tab", required = false) final String searchTab,
            @ApiParam(name = "page", //
                    value = "Page é a pagina para retorno com um limite de 10 itens", //
                    required = true) //
            @RequestParam(name = "page", required = true) final Integer page) {
        final Page<ArquivoTO> arquivos = arquivoBusiness.carregarPor(nome, SearchTab.of(searchTab), page);
        if (arquivos.isEmpty()) {
            throw new MasterException("slave.obj.nao.localizado").status(Status.NOT_FOUND);
        }
        return ResponseEntity.ok(arquivos);
    }

    @GetMapping(value = "/arquivos/{id:\\d+}")
    @ApiOperation(value = "Carrega o arquivo do volume", //
            nickname = "leitura", //
            notes = "<p>A leitura é usada para fazer o <strong>download</strong> do <strong>arquivo</strong> no servidor, através do <strong>id</strong>. O arquivo será reconstruído como os <strong>blocos</strong> que estão espalhados entre os servidores <strong>slave</strong> registrados no <strong>service Discovery</strong> se a operação for realizada com sucesso, retorna o <strong>binário</strong> no corpo da mensagem caso contrário, a mensagem de erro.</p>", //
            response = Resource.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Nenhum registro foi encontrado para a consulta.")
    })
    public ResponseEntity<Resource> leitura(
            @ApiParam(name = "id", //
                    value = "Id é um número utilizado para a identificação do arquivo", //
                    example = "1234", //
                    required = true) //
            @PathVariable("id") final long id) {

        final ArquivoTO arquivo = arquivoBusiness.ache(id);
        if (arquivo == null) {
            throw new MasterException("slave.obj.nao.localizado").status(Status.NOT_FOUND);
        }

        final InputStreamResource stream = arquivoBusiness.ler(arquivo);

        return ResponseEntity.ok()
                .contentLength(arquivo.getTamanho())
                .contentType(MediaType.parseMediaType(arquivo.getMimeType()))
                .body(stream);
    }

    @PostMapping("/arquivos")
    @ApiOperation(value = "Envia um arquivo para o volume", //
            nickname = "gravacao", notes = "<p>A gravação é usada para fazer o <strong>upload</strong> do <strong>arquivo</strong> para o volume, que será divido em <strong>blocos</strong> de tamanho fixo pré-configurado e enviados aos servidores <strong>slave</strong> para o armazenamento, caso esteja configurado também serão replicados em outros servidores registrados no <strong>service discovery</strong>. Se a operação for realizada com sucesso, retorna os <strong>metadados</strong> do arquivo caso contrário, a mensagem de erro</p>", //
            response = ArquivoTO.class, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "<p>Regras de Negócio:</p> <ul> <li>Não existe serviço registrado no service discovery</li> <li>A localização (C:/.../.../m1) do volume é invalida.</li> </ul>") })
    public ResponseEntity<ArquivoTO> gravacao(
            @ApiParam(name = "file", //
                    value = "<p>File é o binário que será enviado ao servidor pode ser um arquivo de texto, planilha, livro, vídeo, música e etc..</p>", //
                    required = true) @RequestParam("file") //
            final MultipartFile file,
            @ApiParam(name = "Authorization", //
                    value = "<p>O Authorization é uma string criptografada, gerada pelo servidor <strong>virtual-access</strong> que deverá ser enviada no cabeçalho</p>", //
                    example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOjIsImlhdCI6MTU2NTYxNzYyOSwiZXhwIjoxNTY1NjE5NDI5fQ.X_exWWVCKIptCipsYXMVuIcBHosgQgAWrN50IO9Ss68", //
                    required = false) //
            @RequestHeader(value = "Authorization", required = false) final String authorization,
            @ApiParam(name = "token", //
                    value = "<p>O token é uma string criptografada, gerada pelo servidor <strong>virtual-access</strong> que deve ser enviada como um <strong>queryParam</strong></p>", //
                    required = false) //
            @RequestParam(value = "token", required = false) final String token) {

        final ValidarToken access = validarTokenAccess(authorization, token);
        final ArquivoTO arquivo = arquivoBusiness.gravar(access, file);

        return ResponseEntity.ok(arquivo);
    }

    @DeleteMapping(value = "/arquivos/{id:\\d+}")
    @ApiOperation(value = "Remove o bloco do volume", //
            nickname = "exclusao", //
            notes = "<p>A exclusão é usada para <strong>remover</strong> o <strong>arquivo</strong> do volume, através do <strong>id</strong>. Todos os <strong>blocos</strong> do arquivo serão removidos do volume em seguida os <strong>metadados</strong> se a operação for realizada com sucesso, retorna o status <strong>204</strong> caso contrário, a mensagem de erro.</p>", //
            response = ArquivoTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Nenhum arquivo foi localizado!")
    })
    public ResponseEntity<Response> exclusao(
            @ApiParam(name = "id", //
                    value = "Id é um número utilizado para a identificação do arquivo que será removido", //
                    example = "1234", //
                    required = true) //
            @PathVariable("id") final long id,
            @ApiParam(name = "Authorization", //
                    value = "<p>O Authorization é uma string criptografada, gerada pelo servidor <strong>virtual-access</strong> que deverá ser enviada no cabeçalho</p>", //
                    example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOjIsImlhdCI6MTU2NTYxNzYyOSwiZXhwIjoxNTY1NjE5NDI5fQ.X_exWWVCKIptCipsYXMVuIcBHosgQgAWrN50IO9Ss68", //
                    required = false) //
            @RequestHeader(value = "Authorization", required = false) final String authorization,
            @ApiParam(name = "token", //
                    value = "<p>O token é uma string criptografada, gerada pelo servidor <strong>virtual-access</strong> que deve ser enviada como um <strong>queryParam</strong></p>", //
                    required = false) //
            @RequestParam(value = "token", required = false) final String token) {

        final ValidarToken access = validarTokenAccess(authorization, token);

        arquivoBusiness.excluir(access, id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/arquivos/usuario")
    @ApiOperation(value = "Carrega os arquivos do usuario", //
            nickname = "usuario", //
            notes = "<p>Este serviço é usado para recuperar os <strong>metadados</strong> dos <strong>arquivos</strong> através do <strong>usuario</strong>, caso seja localizado um ou mais registros será retornado os <strong>metadados</strong> no corpo da mensagem da resposta no formato <strong>json</strong> caso contrário, o status <strong>404</strong> indicando que o arquivo não existe ou não foi localizado.</p>", //
            response = Resource.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Nenhum registro foi encontrado para a consulta.")
    })
    public ResponseEntity<Page<ArquivoTO>> usuario(
            @RequestHeader(value = "Authorization", required = false) final String authorization,
            @ApiParam(name = "token", //
                    value = "<p>O token é uma string criptografada, gerada pelo servidor <strong>virtual-access</strong> que deve ser enviada como um <strong>queryParam</strong></p>", //
                    required = false) //
            @RequestParam(value = "token", required = false) final String token,
            @ApiParam(name = "page", //
                    value = "Page é a pagina para retorno com um limite de 10 itens", //
                    required = true) //
            @RequestParam(value = "page", required = true) final Integer page) {

        final ValidarToken access = validarTokenAccess(authorization, token);

        final Page<ArquivoTO> arquivos = arquivoBusiness.carregarPor(access, page);
        return ResponseEntity.ok(arquivos);
    }
}
