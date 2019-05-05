/*
 * MIT License
 *
 * Copyright (c) Bao Ho (hotribao@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.codeityourself.springshowcases.batch.jpa;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Bao Ho (hotribao@gmail.com)
 * @since 03.05.2019
 */
// using modular to avoid worrying about bean definition name clashes
@EnableBatchProcessing(modular = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAutoConfiguration
@Import({ BatchPersistentConfiguration.class })
@ComponentScan(basePackageClasses = JpaBatchConfiguration.class)
@Configuration
@PropertySource("classpath:application.properties") // CommandLineJobRunner doesn't load it automatically
public class JpaBatchConfiguration {

    @Bean
    public JpaSpringBatchConfigurer batchConfigurer(DataSource dataSource,
                                                    PlatformTransactionManager transactionManager) {
        return new JpaSpringBatchConfigurer(dataSource, transactionManager);
    }

    // this is to have spring-batch use the same transaction manager with application code.
    @Bean
    public PlatformTransactionManager transactionManager(
        @Autowired(required = false) UserTransaction userTransaction,
        @Autowired(required = false) TransactionManager transactionManager) {
        // ConditionOnClass/ConditionOnMissingClass seems not work when having 2 @Bean method with the same name
        if (userTransaction == null) {
            return new JpaTransactionManager();
        }
        return new JpaTransactionManager();
    }
}
