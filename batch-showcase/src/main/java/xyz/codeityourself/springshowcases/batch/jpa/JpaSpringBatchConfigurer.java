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

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Copy of {@link org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer} to have
 * spring-batch use JPA TX so that modification can be written to DB.
 *
 * @author Bao Ho (hotribao@gmail.com)
 * @since 03.05.2019
 */
public class JpaSpringBatchConfigurer implements BatchConfigurer {

    private DataSource dataSource;

    // When @EnableBatchProcessing, DefaultBatchConfigurer will be triggered which creates 
    // a DataSourceTransactionManager which make it impossible to write anything to database when hibernate is used
    // which requires JpaTransactionManager.
    // see https://github.com/spring-projects/spring-batch/blob/master/spring-batch-docs/asciidoc/job.adoc#javaConfig
    private PlatformTransactionManager transactionManager;

    private JobRepository jobRepository;
    private JobLauncher jobLauncher;
    private JobExplorer jobExplorer;

    /**
     * Usage:
     * <pre>
     *   &#064;Bean
     *   public BatchConfigurer batchConfigurer(DataSource dataSource, PlatformTransactionManager transactionManager) {
     *       return new SpringBatchConfigure(dataSource, transactionManager);
     *   }
     * </pre>
     */
    public JpaSpringBatchConfigurer(DataSource dataSource,
                                    PlatformTransactionManager transactionManager) {
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
    }

    @PostConstruct
    public void initialize() {
        try {
            this.jobRepository = createPersistentJobRepository();

            JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
            jobExplorerFactoryBean.setDataSource(this.dataSource);
            jobExplorerFactoryBean.afterPropertiesSet();
            this.jobExplorer = jobExplorerFactoryBean.getObject();
            this.jobLauncher = createJobLauncher();
        } catch (Exception e) {
            throw new BatchConfigurationException(e);
        }
    }

    private JobRepository createPersistentJobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    private JobRepository createInMemoryJobRepository() throws Exception {
        // https://docs.spring.io/spring-batch/3.0.x/reference/html/configureJob.html#inMemoryRepository
        //
        // Caution: the use of in-memory repository means the following restrictions:
        //  - not allow restart between JVM instances
        //  - cannot guarantee that two job instances with the same parameters are launched simultaneously
        //  - not suitable for use in a multi-threaded Job
        //  - not suitable for locally partitioned Steps

        // work in conjunction with flag spring.batch.initializer.enabled=false in application property file
        MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean();
        factory.setTransactionManager(transactionManager);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    private JobLauncher createJobLauncher() throws Exception {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);

        // by default, SyncTaskExecutor is used, which runs job one by one
        // To run jobs async, use ThreadPoolTaskExecutor. However, when triggering batch job via commandline runner
        // this may not be suitable
        // simpleJobLauncher.setTaskExecutor

        launcher.afterPropertiesSet();
        return launcher;
    }

    @Override
    public JobRepository getJobRepository() {
        return jobRepository;
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public JobLauncher getJobLauncher() {
        return jobLauncher;
    }

    @Override
    public JobExplorer getJobExplorer() {
        return jobExplorer;
    }
}
