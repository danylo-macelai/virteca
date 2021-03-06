package br.com.slave.configuration;

import br.com.common.wrappers.CommonException;

import javax.persistence.NoResultException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * <b>Description:</b> FIXME: Document this type <br>
 * <b>Project:</b> virtual-slave <br>
 *
 * @author macelai
 * @date: 29 de out de 2018
 * @version $
 */
@Component
public class SlaveExceptionMapper implements ExceptionMapper<Exception> {

    @Autowired
    MessageSource messageSource;

    @Override
    public Response toResponse(Exception exception) {
        String code = exception.getMessage();
        Object[] args = null;
        Status status = Response.Status.BAD_REQUEST;
        if (exception instanceof CommonException) {
            args = ((CommonException) exception).args();
            status = ((CommonException) exception).status();
        } else if (exception instanceof NoResultException) {
            code = "slave.obj.nao.localizado";
        }
        final String message = messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        return Response.status(status).entity(message).type(MediaType.TEXT_HTML).build();
    }
}
