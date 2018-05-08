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

package org.springframework.security.saml2.metadata;

import javax.annotation.Nonnull;

public enum Binding {

    POST("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"),
    REDIRECT("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"),
    URI("urn:oasis:names:tc:SAML:2.0:bindings:URI"),
    ARTIFACT("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Artifact"),
    POST_SIMPLE_SIGN("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST-SimpleSign"),
    PAOS("urn:oasis:names:tc:SAML:2.0:bindings:PAOS"),
    SOAP("urn:oasis:names:tc:SAML:2.0:bindings:SOAP"),
    DISCOVERY("urn:oasis:names:tc:SAML:profiles:SSO:idp-discovery-protocol"),
    REQUEST_INITIATOR("urn:oasis:names:tc:SAML:profiles:SSO:request-init");


    private final String urn;

    Binding(@Nonnull String urn) {
        this.urn = urn;
    }


    @Override
    public String toString() {
        return this.urn;
    }

    public static Binding fromUrn(String other) {
        for (Binding binding : values()) {
            if (binding.toString().equals(other)) {
                return binding;
            }
        }
        throw new IllegalArgumentException("No enum for:"+other);
    }
}
