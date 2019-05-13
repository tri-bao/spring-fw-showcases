/*
 * MIT License
 *
 * Copyright (c) 2019 Bao Ho (hotribao@gmail.com)
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

import static xyz.codeityourself.springshowcases.batch.jpa.chunkbase.ChunkBaseBatchConfiguration.MAIN_STEP_NAME;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Bao Ho (hotribao@gmail.com)
 * @since 07.05.2019
 */
@JobScope
public class ExecutionSummaryTasklet implements Tasklet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionSummaryTasklet.class);

    @Value("#{jobExecution}")
    protected JobExecution jobExecution;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        if (LOGGER.isInfoEnabled()) {
            Optional<StepExecution> mainStep = jobExecution.getStepExecutions().stream()
                .filter(step -> step.getStepName().equals(MAIN_STEP_NAME))
                .findAny();
            if (mainStep.isPresent()) {
                StepExecution stepInfo = mainStep.get();

                // readCount=19, 
                // filterCount=0, 
                // writeCount=12 
                // readSkipCount=1, 
                // writeSkipCount=3, 
                // processSkipCount=4, 
                // commitCount=7, # of times committing
                // rollbackCount=8
                LOGGER.info("step {}: \n"
                                + "             - total item processed: {}\n"
                                + "             -             items OK: {}\n"
                                + "             -             items KO: {}\n",
                            stepInfo.getStepName(),
                            stepInfo.getReadCount() + stepInfo.getReadSkipCount(),
                            stepInfo.getWriteCount(),
                            stepInfo.getReadSkipCount()
                                + stepInfo.getProcessSkipCount()
                                + stepInfo.getWriteSkipCount()
                           );
            } else {
                LOGGER.warn("step {} was not executed!", MAIN_STEP_NAME);
            }
        }

        return RepeatStatus.FINISHED;
    }
}
