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
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import xyz.codeityourself.springshowcases.batch.jpa.entity.Customer;
import xyz.codeityourself.springshowcases.batch.jpa.entity.CustomerTmp;
import xyz.codeityourself.springshowcases.batch.jpa.repository.CustomerRepository;
import xyz.codeityourself.springshowcases.batch.jpa.repository.CustomerTmpRepository;
import xyz.codeityourself.springshowcases.batch.jpa.support.ChunkBaseBatchSimulation;

/**
 * @author Bao Ho (hotribao@gmail.com)
 * @since 05.05.2019
 */
public class VerifyCopiedDataTasklet implements Tasklet {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyCopiedDataTasklet.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerTmpRepository customerTmpRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<Integer> sourceIds = customerTmpRepository.findAll()
            .stream()
            .map(CustomerTmp::getId)
            .collect(Collectors.toList());

        List<Integer> targetIds = customerRepository.findAll()
            .stream()
            .map(Customer::getId)
            .sorted()
            .collect(Collectors.toList());

        List<Integer> expectedTargetIds = sourceIds.stream()
            .filter(id -> !ChunkBaseBatchSimulation.isErrorItemId(id))
            .sorted()
            .collect(Collectors.toList());

        Assert.isTrue(expectedTargetIds.equals(targetIds), String.format(
            "Unexpected data were copied. Expected: %s. Actual: %s", expectedTargetIds, targetIds));

        LOGGER.info("Data copied as expected: {}", expectedTargetIds);

        return RepeatStatus.FINISHED;
    }
}
