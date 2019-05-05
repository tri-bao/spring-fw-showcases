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
package xyz.codeityourself.springshowcases.batch.jpa.support;

import static java.lang.String.format;
import java.util.Arrays;
import java.util.List;
import org.springframework.util.CollectionUtils;
import xyz.codeityourself.springshowcases.batch.support.BatchShowCaseSimulationErrorException;

/**
 * @author Bao Ho (hotribao@gmail.com)
 * @since 05.05.2019
 */
public class ChunkBaseBatchSimulation {
    // items to process: 1..20
    // chunk size: 3
    // page size: 2

    // Chunk 1
    //  - items: 1..3
    //  - all OK

    // Chunk 2
    //  - items: 4..6
    //  - Error on processing item #5

    // Chunk 3
    //  - items: 7..9
    //  - Error on processing all items

    // Chunk 4
    //  - items: 10-12
    //  - Error on writing

    // Chunk 5
    //  - items: 13-15
    //  - error on reading item #15

    // remaining chunks: OK

    public static final int CHUNK_SIZE = 3;
    public static final int PAGE_SIZE = 2;

    private static final List<Integer> PROCESS_ERROR_ITEMS = Arrays.asList(5, 7, 8, 9);
    private static final List<Integer> WRITE_ERROR_ITEMS = Arrays.asList(10, 11, 12);
    private static final List<Integer> READ_ERROR_ITEMS = Arrays.asList(15);

    public static void triggerErrorOnProcessing(int itemId) {
        if (PROCESS_ERROR_ITEMS.contains(itemId)) {
            throw new BatchShowCaseSimulationErrorException(format("simulate PROCESS error on item: %s", itemId));
        }
    }

    public static void triggerErrorOnWriting(List<Integer> itemIds) {
        if (CollectionUtils.containsAny(WRITE_ERROR_ITEMS, itemIds)) {
            throw new BatchShowCaseSimulationErrorException(format("simulate WRITE error on item: %s", itemIds));
        }
    }

    public static void triggerErrorOnReading(int itemId) {
        if (READ_ERROR_ITEMS.contains(itemId)) {
            throw new BatchShowCaseSimulationErrorException(format("simulate READ error on item: %s", itemId));
        }
    }

    public static boolean isErrorItemId(int itemId) {
        return PROCESS_ERROR_ITEMS.contains(itemId)
            || WRITE_ERROR_ITEMS.contains(itemId)
            || READ_ERROR_ITEMS.contains(itemId);
    }
}
