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
package xyz.codeityourself.springshowcases.batch.jpa.chunkbase;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.item.DefaultItemFailureHandler;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.policy.NeverRetryPolicy;
import xyz.codeityourself.springshowcases.batch.jpa.entity.Customer;
import xyz.codeityourself.springshowcases.batch.jpa.entity.CustomerTmp;
import xyz.codeityourself.springshowcases.batch.jpa.support.ChunkBaseBatchSimulation;
import xyz.codeityourself.springshowcases.batch.support.BatchShowCaseSimulationErrorException;
import xyz.codeityourself.springshowcases.batch.support.TimestampJobParametersIncrementer;

/**
 * This job demonstrate:
 * <ul>
 * <li></li>
 * </ul>
 *
 * @author Bao Ho (hotribao@gmail.com)
 * @since 03.05.2019
 */
@Configuration
public class ChunkBaseBatchConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkBaseBatchConfiguration.class);

    // use this name to trigger the batch job
    public static final String JOB_NAME = "jobChunkBaseShowCase";

    @Bean(JOB_NAME)
    public Job jobChunkBaseShowCase(JobBuilderFactory jobFactory,
                                    StepBuilderFactory stepFactory) {
        return jobFactory.get(JOB_NAME)

            // allow re-run the job the same set of parameters
            // From CommandLineJobRunner, use parameter "-next"
            .incrementer(parametersIncrementer())

            .start(stepFactory.get("cleanUpCopiedCustomerData")
                       .tasklet(clean())
                       .build())

            .next(
                stepFactory.get("copyCustomer")

                    //  - allowStartIfComplete:
                    //      + At step level
                    //      + Scenario: a job, with restartable=true, has 3 steps. In one execution, step 3 failed.
                    //          When the job restarts:
                    //          * If all steps have allowStartIfComplete=false.
                    //              Step 3 will be rerun.
                    //              Step 1 and step 2 will be skipped as they were completed.
                    //          * If step 1 has allowStartIfComplete=true, step 2 and step 3 have allowStartIfComplete=false
                    //              Step 1 will be rerun
                    //              Step 2 will be skipped
                    //              Step 3 will be rerun
                    .allowStartIfComplete(true)

                    // reads ID of CustomerTmp
                    // writes (copies) Customer
                    .<CustomerTmp, Customer>chunk(ChunkBaseBatchSimulation.CHUNK_SIZE)

                    // define skip/retry policy
                    .faultTolerant()
                    .skipPolicy(new AlwaysSkipItemSkipPolicy()) // skip the item for whatever exception
                    .retryPolicy(new NeverRetryPolicy())

                    .listener(chunkListener())
                    .listener((ItemWriteListener<? super Customer>) itemFailureHandler())
                    .listener((ItemProcessListener<? super CustomerTmp, ? super Customer>) itemFailureHandler())
                    .listener((ItemWriteListener<? super Customer>) itemFailureHandler())

                    .reader(reader())
                    .processor(processor())
                    .writer(writer())

                    .build()
                 )

            .next(stepFactory.get("verifyCopiedCustomerData")
                      .tasklet(verify())
                      .build())

            .build();
    }

    @Bean
    DefaultItemFailureHandler itemFailureHandler() {
        return new DefaultItemFailureHandler() {
            @Override
            public void onReadError(Exception ex) {
                if (ex instanceof BatchShowCaseSimulationErrorException) {
                    LOGGER.error("  Error (simulation) encountered while reading");
                } else {
                    super.onReadError(ex);
                }
            }

            @Override
            public void onWriteError(Exception ex, List<?> item) {
                if (ex instanceof BatchShowCaseSimulationErrorException) {
                    LOGGER.error("  Error (simulation) encountered while writing");
                } else {
                    super.onWriteError(ex, item);
                }
            }

            @Override
            public void onProcessError(Object item, Exception e) {
                if (e instanceof BatchShowCaseSimulationErrorException) {
                    LOGGER.error("  Error (simulation) encountered while processing: {}", item);
                } else {
                    LOGGER.error("  Error encountered while processing: {}", item, e);
                }
            }
        };
    }

    @Bean
    ChunkListener chunkListener() {
        return new ChunkListener() {
            @Override
            public void beforeChunk(ChunkContext context) {
                LOGGER.info(" ");
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
                                + "Starting chunk (transaction started)");
                LOGGER.info("beforeChunk: {}", context);
            }

            @Override
            public void afterChunk(ChunkContext context) {
                LOGGER.info("afterChunk: {}", context);
                LOGGER.info("*************************************Chunk OK (transaction committed)");
            }

            @Override
            public void afterChunkError(ChunkContext context) {
                LOGGER.info("afterChunkError: {}", context);
                LOGGER.info("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxChunk FAILED (transaction rolled back)");
            }
        };
    }

    @Bean
    TimestampJobParametersIncrementer parametersIncrementer() {
        return new TimestampJobParametersIncrementer();
    }

    @Bean
    @StepScope
    ChunkBaseReader reader() {
        return new ChunkBaseReader();
    }

    @Bean
    @StepScope
    ChunkBaseProcessor processor() {
        return new ChunkBaseProcessor();
    }

    @Bean
    @StepScope
    ChunkBaseWriter writer() {
        return new ChunkBaseWriter();
    }

    @Bean
    @StepScope
    VerifyCopiedDataTasklet verify() {
        return new VerifyCopiedDataTasklet();
    }

    @Bean
    @StepScope
    CleanCopiedDataTasklet clean() {
        return new CleanCopiedDataTasklet();
    }
}
