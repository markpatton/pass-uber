/*
 *
 * Copyright 2019 Johns Hopkins University
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.eclipse.pass.file.service;

import org.springframework.http.HttpStatus;

public class FileServiceException extends Exception {

    private HttpStatus code = null;
    private String responseBody = null;

    public FileServiceException(String message) {
        super(message);
    }

    public FileServiceException(HttpStatus code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Get the HTTP status code.
     *
     * @return HTTP status code
     */
    public HttpStatus getCode() {
        return code;
    }

    /**
     * Get the HTTP response body.
     *
     * @return Response body in the form of string
     */
    public String getResponseBody() {
        return responseBody;
    }

}
