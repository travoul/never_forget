/*      Copyright 2016 Marcello de Paula Ferreira Costa

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License. */
package mchs.neverforget.util;

public enum ReturnValues {
    LOGIN_SUCCESSFUL(0),
    LOGIN_FAILURE(1),
    RETRIEVE_PAGE_FAILURE(2);

    private final int returnCode;

    ReturnValues(int returnCode) {
        this.returnCode = returnCode;
    }

    public int getLevelCode() {
        return this.returnCode;
    }
}
