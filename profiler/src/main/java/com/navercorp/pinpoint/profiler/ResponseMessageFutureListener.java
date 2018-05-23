/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.rpc.DefaultFuture;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;

public class ResponseMessageFutureListener implements FutureListener<ResponseMessage> {

    private final DefaultFuture<ResponseMessage> future;

    public ResponseMessageFutureListener(DefaultFuture<ResponseMessage> future) {
        this.future = future;
    }

    @Override
    public void onComplete(Future<ResponseMessage> future) {
        if (future == null) {
            this.future.setFailure(new IllegalStateException("ResponseMessage future is null"));
            return;
        }
        if (!future.isReady()) {
            this.future.setFailure(new IllegalStateException("ResponseMessage future is not complete"));
            return;
        }

        if (future.isSuccess()) {
            ResponseMessage responseMessage = future.getResult();
            this.future.setResult(responseMessage);
        } else {
            Throwable cause = future.getCause();
            this.future.setFailure(cause);
        }
    }
}
