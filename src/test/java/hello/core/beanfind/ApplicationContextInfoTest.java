package hello.core.beanfind;

import hello.core.AppConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;

public class ApplicationContextInfoTest {

    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

    @Test
    @DisplayName("모든 빈 출력하기")
    void findAllBean() {
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        Arrays.stream(beanDefinitionNames).forEach(s -> {
            Object bean = ac.getBean(s);
            System.out.println("name = " + s + " object = " + bean );
        });
    }

    @Test
    @DisplayName("모든 빈 출력하기")
    void findApplicationBean() {
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        Arrays.stream(beanDefinitionNames).forEach(s -> {
            BeanDefinition beanDefinition = ac.getBeanDefinition(s);
            if (beanDefinition.getRole() == BeanDefinition.ROLE_APPLICATION) {
                Object bean = ac.getBean(s);
                System.out.println("name = " + s + " object = " + bean );
            }
        });
    }
}
