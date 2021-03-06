package br.com.slave;

import br.com.slave.configuration.CorsInterceptor;
import br.com.slave.configuration.SlaveConfiguration;
import br.com.slave.resource.SwaggerUIResource;
import br.com.slave.resource.VolumeResource;

import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.wso2.msf4j.spring.MSF4JSpringApplication;
import org.wso2.msf4j.spring.SpringMicroservicesRunner;

/**
 * <b>Description:</b> FIXME: Document this type <br>
 * <b>Project:</b> virtual-slave <br>
 *
 * @author macelai
 * @date: 23 de out de 2018
 * @version $
 */
public class Application {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = null;
        try {
            new MSF4JSpringApplication(Application.class);
            context = BeanUtils
                    .instantiateClass(AnnotationConfigApplicationContext.class);
            context.register(SlaveConfiguration.class);
            context.refresh();

            final ClassPathBeanDefinitionScanner classPathBeanDefinitionScanner = new ClassPathBeanDefinitionScanner(
                    context);
            classPathBeanDefinitionScanner.scan(VolumeResource.class.getPackage().getName());

            final SpringMicroservicesRunner runner = context.getBean(SpringMicroservicesRunner.class);
            runner.setApplicationContext(context);
            runner.addGlobalRequestInterceptor(context.getBean(CorsInterceptor.class));
            runner.deploy(VolumeResource.RESOURCE_ROOT_URL, context.getBean(VolumeResource.class));
            runner.deploy(SwaggerUIResource.RESOURCE_ROOT_URL, context.getBean(SwaggerUIResource.class));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

}
