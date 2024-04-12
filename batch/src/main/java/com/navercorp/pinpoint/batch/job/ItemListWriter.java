/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.job;

import jakarta.annotation.Nonnull;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;


/**
 * @author youngjin.kim2
 */
public class ItemListWriter<T> implements ItemWriter<List<T>> {

    private final ItemWriter<T> delegate;

    public ItemListWriter(ItemWriter<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(@Nonnull Chunk<? extends List<T>> items) throws Exception {
        if (this.delegate != null) {
            this.delegate.write(flatten(items));
        }
    }

    private Chunk<T> flatten(Chunk<? extends List<T>> items) {
        Chunk<T> lst = new Chunk<>();
        for (List<T> sub: items) {
            lst.addAll(sub);
        }
        return lst;
    }

}
