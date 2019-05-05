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
import org.springframework.batch.item.ItemWriter;
import xyz.codeityourself.springshowcases.batch.jpa.entity.Customer;
import xyz.codeityourself.springshowcases.batch.jpa.support.ChunkBaseBatchSimulation;

/**
 * @author Bao Ho (hotribao@gmail.com)
 * @since 03.05.2019
 */
public class ChunkBaseWriter implements ItemWriter<Customer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkBaseWriter.class);

    @Override
    public void write(List<? extends Customer> items) {
        LOGGER.info("  [WRITE  ] items: {}", items);

        ChunkBaseBatchSimulation.triggerErrorOnWriting(items.stream()
                                                           .map(Customer::getId)
                                                           .collect(Collectors.toList()));
    }
}
