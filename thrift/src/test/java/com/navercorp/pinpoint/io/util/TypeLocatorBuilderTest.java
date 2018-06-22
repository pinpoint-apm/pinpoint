/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.io.util;

import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.thrift.TBase;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TypeLocatorBuilderTest {

    @Test
    public void addBodyFactory() {
        TypeLocatorBuilder<TBase<?, ?>> typeLocatorBuilder = new TypeLocatorBuilder<TBase<?, ?>>();

        typeLocatorBuilder.addBodyFactory((short) 1, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TSpan();
            }
        });

        typeLocatorBuilder.addBodyFactory((short) 3, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TSpanEvent();
            }
        });

        TypeLocator<TBase<?, ?>> build = typeLocatorBuilder.build();
        Assert.assertNotNull(build.bodyLookup((short) 1));
        Assert.assertNull(build.bodyLookup((short) 5));

        Assert.assertEquals(build.headerLookup((short) 1).getType(), 1);
        Assert.assertEquals(build.headerLookup((short) 3).getType(), 3);
    }


    @Test(expected = IllegalStateException.class)
    public void addBodyFactory_duplicated_type_code() {
        TypeLocatorBuilder<TBase<?, ?>> typeLocatorBuilder = new TypeLocatorBuilder<TBase<?, ?>>();

        typeLocatorBuilder.addBodyFactory((short) 1, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TSpan();
            }
        });

        typeLocatorBuilder.addBodyFactory((short) 1, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TSpanEvent();
            }
        });

    }

    @Test(expected = IllegalStateException.class)
    public void addBodyFactory_duplicated_body_class() {
        TypeLocatorBuilder<TBase<?, ?>> typeLocatorBuilder = new TypeLocatorBuilder<TBase<?, ?>>();

        typeLocatorBuilder.addBodyFactory((short) 1, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TSpan();
            }
        });

        typeLocatorBuilder.addBodyFactory((short) 3, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TSpan();
            }
        });

    }
}