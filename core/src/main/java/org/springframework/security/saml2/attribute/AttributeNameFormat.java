/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.springframework.security.saml2.attribute;

public enum AttributeNameFormat  {

    UNSPECIFIED("urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified"),
    URI("urn:oasis:names:tc:SAML:2.0:attrname-format:uri"),
    BASIC("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");


    private final String urn;

    AttributeNameFormat(String urn) {
        this.urn = urn;
    }

    public static AttributeNameFormat fromUrn(String name) {
        for (AttributeNameFormat t : values()) {
            if (t.urn.equals(name)) {
                return t;
            }
        }
        return UNSPECIFIED;
    }

    @Override
    public String toString() {
        return urn;
    }
}
