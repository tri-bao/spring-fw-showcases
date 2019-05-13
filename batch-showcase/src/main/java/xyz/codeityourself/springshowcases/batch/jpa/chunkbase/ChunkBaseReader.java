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

import static org.springframework.data.domain.Sort.Direction.ASC;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import lombok.Getter;
import xyz.codeityourself.springshowcases.batch.jpa.entity.CustomerTmp;
import xyz.codeityourself.springshowcases.batch.jpa.repository.CustomerTmpRepository;
import xyz.codeityourself.springshowcases.batch.jpa.support.ChunkBaseBatchSimulation;

/**
 * @author Bao Ho (hotribao@gmail.com)
 * @since 03.05.2019
 */
public class ChunkBaseReader
    extends
    // below is 2 types of reader can try
    AbstractReadAheadItemReader<CustomerTmp> {
    // AbstractChunkBaseItemReader<CustomerTmp> {

    private int pageId = 0;

    @Autowired
    private CustomerTmpRepository customerTmpRepository;

    @PersistenceContext
    @Getter
    private EntityManager entityManager;

    @PostConstruct
    public void postConstruct() {
        setPageSize(ChunkBaseBatchSimulation.PAGE_SIZE);
    }

    @Override
    protected int getChunkSize() {
        return ChunkBaseBatchSimulation.CHUNK_SIZE;
    }

    @Override
    public CustomerTmp read() {
        CustomerTmp c = super.read();
        if (c != null) {
            ChunkBaseBatchSimulation.triggerErrorOnReading(c.getId());
        }
        return c;
    }

    @Override
    protected List<CustomerTmp> readItemsOfOnePage(int pageSize) {
        PageRequest pageRequest = new PageRequest(pageId++, pageSize, new Sort(ASC, "id"));
        Page<CustomerTmp> page = customerTmpRepository.findAll(pageRequest);

        return page.getContent();
    }
}
