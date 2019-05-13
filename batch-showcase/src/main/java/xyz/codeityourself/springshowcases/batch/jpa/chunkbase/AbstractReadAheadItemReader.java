/*
 * Project: EGDD
 *
 * Copyright 2018-2019 by Canton de Vaud
 * All rights reserved.
 *
 *
 * This software is the confidential and proprietary information
 * of Canton de Vaud. ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with Canton de Vaud.
 */

package xyz.codeityourself.springshowcases.batch.jpa.chunkbase;

import static java.lang.String.format;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.OnProcessError;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.util.Assert;

/**
 * <p>
 * An ItemReader that emits every item at a time but internally, allows querying for items in pagination fashion.
 * This ensure maximum <code>pageSize</code> items will be read ahead. Normally, use this ItemReader template to
 * optimize the read by trying to fetch many items to be processed at once.
 * </P>
 *
 * <p>
 * This is similar to <code>AbstractChunkBaseItemReader</code> but to be used when the pagination is not
 * possible. The state of processed items (either success or failed) is kept in the reader or in database.
 * Then the reader just queries and return the first page.
 * </P>
 *
 * @param <I> type of the read item. When there is error on the processor, the transaction will be rolled backed.
 *            A new started will be started with an empty entity manger. Then the processor will be called again
 *            will items that are not error (e.g. the reader will not be called). Thus, unless the reader ensure
 *            that everything needed for the processor is loaded, to avoid lazy loading error, an item ID should be
 *            returned by the reader instead of the entity. In the processor, the item entity will be loaded.
 *            If no error in the chunk, the load on processor cost nothing as the entity is already in the hibernate
 *            session.
 * @author tbh
 * @since 07.05.2019
 */
public abstract class AbstractReadAheadItemReader<I> implements ItemReader<I> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractReadAheadItemReader.class);

    private int chunkRemainingItemCount;

    private int lastReadIndex = -1;
    private List<I> readAheadItems = new ArrayList<>();

    private int pageSize = 500;

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public I read() {
        initChunkIfNeeded();

        if (readAheadItems.isEmpty() || (lastReadIndex == readAheadItems.size() - 1)) {
            readAheadItems = readItemsOfOnePage(pageSize);
            lastReadIndex = -1;
        }

        lastReadIndex++;
        if (lastReadIndex < readAheadItems.size()) {
            I item = readAheadItems.get(lastReadIndex);
            LOGGER.info("  [READ   ] item: {}", item);
            return item;
        }

        return null;
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

    protected abstract List<I> readItemsOfOnePage(int pageSize);

}
