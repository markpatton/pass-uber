/*
 * Copyright 2023 Johns Hopkins University
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
package org.eclipse.pass.deposit.transport.sftp;

import org.eclipse.pass.deposit.transport.TransportResponse;

/**
 * @author Russ Poetker (rpoetke1@jh.edu)
 */
class SftpTransportResponse implements TransportResponse {
    private final boolean success;
    private final Throwable throwable;

    SftpTransportResponse(boolean success) {
        this(success, null);
    }

    SftpTransportResponse(boolean success, Throwable throwable) {
        this.success = success;
        this.throwable = throwable;
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public Throwable error() {
        return throwable;
    }
}
