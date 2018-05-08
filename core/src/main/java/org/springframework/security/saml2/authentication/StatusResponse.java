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

package org.springframework.security.saml2.authentication;

import javax.xml.crypto.dsig.XMLSignature;

import org.joda.time.DateTime;
import org.springframework.security.saml2.Saml2Object;

public class StatusResponse<T extends StatusResponse> implements Saml2Object {

    private String id;
    private String inResponseTo;
    private String version;
    private DateTime issueInstant;
    private String destination;
    private String consent;
    private Issuer issuer;
    private XMLSignature signature;
    private Status status;

    @SuppressWarnings("checked")
    protected T _this() {
        return (T)this;
    }

    public String getId() {
        return id;
    }

    public T setId(String id) {
        this.id = id;
        return _this();
    }

    public String getInResponseTo() {
        return inResponseTo;
    }

    public T setInResponseTo(String inResponseTo) {
        this.inResponseTo = inResponseTo;
        return _this();
    }

    public String getVersion() {
        return version;
    }

    public T setVersion(String version) {
        this.version = version;
        return _this();
    }

    public DateTime getIssueInstant() {
        return issueInstant;
    }

    public T setIssueInstant(DateTime issueInstant) {
        this.issueInstant = issueInstant;
        return _this();
    }

    public String getDestination() {
        return destination;
    }

    public T setDestination(String destination) {
        this.destination = destination;
        return _this();
    }

    public String getConsent() {
        return consent;
    }

    public T setConsent(String consent) {
        this.consent = consent;
        return _this();
    }

    public Issuer getIssuer() {
        return issuer;
    }

    public T setIssuer(Issuer issuer) {
        this.issuer = issuer;
        return _this();
    }

    public XMLSignature getSignature() {
        return signature;
    }

    public T setSignature(XMLSignature signature) {
        this.signature = signature;
        return _this();
    }

    public Status getStatus() {
        return status;
    }

    public T setStatus(Status status) {
        this.status = status;
        return _this();
    }
}
