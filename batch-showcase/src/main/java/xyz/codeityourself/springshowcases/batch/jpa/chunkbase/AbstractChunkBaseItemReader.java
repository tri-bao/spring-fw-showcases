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

import static java.lang.String.format;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.persistence.EntityManager;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.OnProcessError;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.util.Assert;

/**
 * @param <I> type of the read item
 * @author Bao Ho (hotribao@gmail.com)
 * @since 04.05.2019
 */
public abstract class AbstractChunkBaseItemReader<I> extends AbstractPagingItemReader<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractChunkBaseItemReader.class);

    private int chunkRemainingItemCount = 0;

    public AbstractChunkBaseItemReader() {
        // here we want to start-over when rerun the job instead of starting from the last-read item
        setSaveState(false);
    }

    @Override
    public I read() throws Exception {
        initChunkIfNeeded();

        I item = super.read();
        LOGGER.info("  [READ   ] item: {}", item);
        return item;
    }

    private void initChunkIfNeeded() {
        if (chunkRemainingItemCount > 0) {
            return;
        }
        LOGGER.info("  Start new chunk. Size: {}", getChunkSize());

        chunkRemainingItemCount = getChunkSize();

        // Avoid holding the whole dataset in memory
        // - Can't make this upon BeforeChunk as it is called at every retrying chunk!
        // - Can't make this upon AfterChunk because if all items in the chunk have error, AfterChunk will not be called
        //getEntityManager().clear();
        Session session = getEntityManager().unwrap(Session.class);
        Assert.isTrue(session.getStatistics().getEntityCount() == 0,
                      format("Expect empty persistent context at the chunk start. But found %d entities",
                             session.getStatistics().getEntityCount()));
        Assert.isTrue(session.getStatistics().getCollectionCount() == 0,
                      format("Expect empty persistent context at the chunk start. But found %d collection",
                             session.getStatistics().getCollectionCount()));

        LOGGER.info("  [CLEAR  ] Entity manager");

        beforeFirstChunkItemRead();
    }

    protected abstract int getChunkSize();

    protected abstract EntityManager getEntityManager();

    /**
     * This method, as its name say, will be called at the beginning of every chunk, just before the first item is
     * read, in a an empty entity manager.
     */
    protected void beforeFirstChunkItemRead() {
        // override to perform needed initialization
    }

    @OnProcessError
    public final void onItemProcessError(I item, Exception e) {
        // spring-batch will try the chunk without this erroneous item.
        --chunkRemainingItemCount;
    }

    /**
     * called when the chunk is processed successfully (even retried chunk).
     */
    @AfterChunk
    public final void onChunkSuccessfullyFinished(ChunkContext context) {
        chunkRemainingItemCount = 0;
    }

    @Override
    protected void doReadPage() {
        if (results == null) {
            results = new CopyOnWriteArrayList<>();
        } else {
            results.clear();
        }

        results.addAll(readItemsOfOnePage());
    }

    protected abstract List<I> readItemsOfOnePage();

    @Override
    protected void doJumpToPage(int itemIndex) {
        throw new UnsupportedOperationException("restart-able is not supported in this showcase");
    }
}
