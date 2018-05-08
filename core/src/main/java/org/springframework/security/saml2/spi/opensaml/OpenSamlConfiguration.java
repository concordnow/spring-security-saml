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

package org.springframework.security.saml2.spi.opensaml;

import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.joda.time.DateTime;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSBase64Binary;
import org.opensaml.core.xml.schema.XSBoolean;
import org.opensaml.core.xml.schema.XSBooleanValue;
import org.opensaml.core.xml.schema.XSDateTime;
import org.opensaml.core.xml.schema.XSInteger;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.core.xml.schema.impl.XSBooleanBuilder;
import org.opensaml.core.xml.schema.impl.XSDateTimeBuilder;
import org.opensaml.core.xml.schema.impl.XSIntegerBuilder;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.core.xml.schema.impl.XSURIBuilder;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.ext.idpdisco.DiscoveryResponse;
import org.opensaml.saml.ext.idpdisco.impl.DiscoveryResponseBuilder;
import org.opensaml.saml.ext.saml2mdreqinit.RequestInitiator;
import org.opensaml.saml.ext.saml2mdreqinit.impl.RequestInitiatorBuilder;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Condition;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.StatusMessage;
import org.opensaml.saml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.IndexedEndpoint;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.saml2.metadata.impl.ExtensionsBuilder;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.credential.impl.KeyStoreCredentialResolver;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.NamedKeyInfoGeneratorManager;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureSupport;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.opensaml.xmlsec.signature.support.Signer;
import org.springframework.security.saml2.Saml2Object;
import org.springframework.security.saml2.attribute.Attribute;
import org.springframework.security.saml2.attribute.AttributeNameFormat;
import org.springframework.security.saml2.authentication.Assertion;
import org.springframework.security.saml2.authentication.AssertionCondition;
import org.springframework.security.saml2.authentication.AudienceRestriction;
import org.springframework.security.saml2.authentication.AuthenticationContext;
import org.springframework.security.saml2.authentication.AuthenticationContextClassReference;
import org.springframework.security.saml2.authentication.AuthenticationRequest;
import org.springframework.security.saml2.authentication.AuthenticationStatement;
import org.springframework.security.saml2.authentication.Conditions;
import org.springframework.security.saml2.authentication.Issuer;
import org.springframework.security.saml2.authentication.NameIdPrincipal;
import org.springframework.security.saml2.authentication.OneTimeUse;
import org.springframework.security.saml2.authentication.RequestedAuthenticationContext;
import org.springframework.security.saml2.authentication.Response;
import org.springframework.security.saml2.authentication.Status;
import org.springframework.security.saml2.authentication.StatusCode;
import org.springframework.security.saml2.authentication.Subject;
import org.springframework.security.saml2.authentication.SubjectConfirmation;
import org.springframework.security.saml2.authentication.SubjectConfirmationData;
import org.springframework.security.saml2.authentication.SubjectConfirmationMethod;
import org.springframework.security.saml2.authentication.SubjectPrincipal;
import org.springframework.security.saml2.init.SpringSecuritySaml;
import org.springframework.security.saml2.metadata.Binding;
import org.springframework.security.saml2.metadata.Endpoint;
import org.springframework.security.saml2.metadata.IdentityProvider;
import org.springframework.security.saml2.metadata.IdentityProviderMetadata;
import org.springframework.security.saml2.metadata.Metadata;
import org.springframework.security.saml2.metadata.NameId;
import org.springframework.security.saml2.metadata.Provider;
import org.springframework.security.saml2.metadata.ServiceProvider;
import org.springframework.security.saml2.metadata.ServiceProviderMetadata;
import org.springframework.security.saml2.metadata.SsoProvider;
import org.springframework.security.saml2.signature.AlgorithmMethod;
import org.springframework.security.saml2.signature.Canonicalization;
import org.springframework.security.saml2.signature.DigestMethod;
import org.springframework.security.saml2.util.InMemoryKeyStore;
import org.springframework.security.saml2.xml.KeyType;
import org.springframework.security.saml2.xml.SimpleKey;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration.EXACT;
import static org.springframework.security.saml2.Namespace.NS_PROTOCOL;
import static org.springframework.util.StringUtils.hasText;

public class OpenSamlConfiguration extends SpringSecuritySaml<OpenSamlConfiguration> {

    private BasicParserPool parserPool;

    public OpenSamlConfiguration() {
        this.parserPool = new BasicParserPool();
    }

    public BasicParserPool getParserPool() {
        return parserPool;
    }

    protected void bootstrap() {
        //configure default values
        //maxPoolSize = 5;
        parserPool.setMaxPoolSize(50);
        //coalescing = true;
        parserPool.setCoalescing(true);
        //expandEntityReferences = false;
        parserPool.setExpandEntityReferences(false);
        //ignoreComments = true;
        parserPool.setIgnoreComments(true);
        //ignoreElementContentWhitespace = true;
        parserPool.setIgnoreElementContentWhitespace(true);
        //namespaceAware = true;
        parserPool.setNamespaceAware(true);
        //schema = null;
        parserPool.setSchema(null);
        //dtdValidating = false;
        parserPool.setDTDValidating(false);
        //xincludeAware = false;
        parserPool.setXincludeAware(false);

        Map<String, Object> builderAttributes = new HashMap<>();
        parserPool.setBuilderAttributes(builderAttributes);

        Map<String, Boolean> parserBuilderFeatures = new HashMap<>();
        parserBuilderFeatures.put("http://apache.org/xml/features/disallow-doctype-decl", TRUE);
        parserBuilderFeatures.put("http://javax.xml.XMLConstants/feature/secure-processing", TRUE);
        parserBuilderFeatures.put("http://xml.org/sax/features/external-general-entities", FALSE);
        parserBuilderFeatures.put("http://apache.org/xml/features/validation/schema/normalized-value", FALSE);
        parserBuilderFeatures.put("http://xml.org/sax/features/external-parameter-entities", FALSE);
        parserPool.setBuilderFeatures(parserBuilderFeatures);

        try {
            parserPool.initialize();
        } catch (ComponentInitializationException x) {
            throw new RuntimeException("Unable to initialize OpenSaml v3 ParserPool", x);
        }


        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new RuntimeException("Unable to initialize OpenSaml v3", e);
        }

        XMLObjectProviderRegistry registry;
        synchronized (ConfigurationService.class) {
            registry = ConfigurationService.get(XMLObjectProviderRegistry.class);
            if (registry == null) {
                registry = new XMLObjectProviderRegistry();
                ConfigurationService.register(XMLObjectProviderRegistry.class, registry);
            }
        }

        registry.setParserPool(parserPool);
    }


    public XMLObjectBuilderFactory getBuilderFactory() {
        return XMLObjectProviderRegistrySupport.getBuilderFactory();
    }

    public MarshallerFactory getMarshallerFactory() {
        return XMLObjectProviderRegistrySupport.getMarshallerFactory();
    }

    public UnmarshallerFactory getUnmarshallerFactory() {
        return XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
    }

    public EntityDescriptor getEntityDescriptor() {
        XMLObjectBuilderFactory builderFactory = getBuilderFactory();
        SAMLObjectBuilder<EntityDescriptor> builder =
            (SAMLObjectBuilder<EntityDescriptor>) builderFactory.getBuilder(EntityDescriptor.DEFAULT_ELEMENT_NAME);
        return builder.buildObject();
    }

    public SPSSODescriptor getSPSSODescriptor() {
        SAMLObjectBuilder<SPSSODescriptor> builder =
            (SAMLObjectBuilder<SPSSODescriptor>) getBuilderFactory().getBuilder(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        return builder.buildObject();
    }

    public IDPSSODescriptor getIDPSSODescriptor() {
        SAMLObjectBuilder<IDPSSODescriptor> builder =
            (SAMLObjectBuilder<IDPSSODescriptor>) getBuilderFactory().getBuilder(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        return builder.buildObject();
    }

    public Extensions getMetadataExtensions() {
        SAMLObjectBuilder<Extensions> builder =
            (SAMLObjectBuilder<Extensions>) getBuilderFactory().getBuilder(Extensions.DEFAULT_ELEMENT_NAME);
        return builder.buildObject();
    }

    public NameIDFormat getNameIDFormat(NameId nameId) {
        SAMLObjectBuilder<NameIDFormat> builder =
            (SAMLObjectBuilder<NameIDFormat>) getBuilderFactory().getBuilder(NameIDFormat.DEFAULT_ELEMENT_NAME);
        NameIDFormat format = builder.buildObject();
        format.setFormat(nameId.toString());
        return format;
    }

    public SingleSignOnService getSingleSignOnService(Endpoint endpoint, int index) {
        SAMLObjectBuilder<SingleSignOnService> builder =
            (SAMLObjectBuilder<SingleSignOnService>) getBuilderFactory().getBuilder(SingleSignOnService.DEFAULT_ELEMENT_NAME);
        SingleSignOnService sso = builder.buildObject();
        sso.setLocation(endpoint.getLocation());
        sso.setBinding(endpoint.getBinding().toString());
        return sso;
    }

    public AssertionConsumerService getAssertionConsumerService(Endpoint endpoint, int index) {
        SAMLObjectBuilder<AssertionConsumerService> builder =
            (SAMLObjectBuilder<AssertionConsumerService>) getBuilderFactory().getBuilder(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
        AssertionConsumerService consumer = builder.buildObject();
        consumer.setLocation(endpoint.getLocation());
        consumer.setBinding(endpoint.getBinding().toString());
        consumer.setIsDefault(endpoint.isDefault());
        consumer.setIndex(index);
        return consumer;
    }

    public SingleLogoutService getSingleLogoutService(Endpoint endpoint) {
        SAMLObjectBuilder<SingleLogoutService> builder =
            (SAMLObjectBuilder<SingleLogoutService>) getBuilderFactory().getBuilder(SingleLogoutService.DEFAULT_ELEMENT_NAME);
        SingleLogoutService service = builder.buildObject();
        service.setBinding(endpoint.getBinding().toString());
        service.setLocation(endpoint.getLocation());
        return service;
    }

    public KeyDescriptor getKeyDescriptor(SimpleKey key) {
        SAMLObjectBuilder<KeyDescriptor> builder =
            (SAMLObjectBuilder<KeyDescriptor>) getBuilderFactory().getBuilder(KeyDescriptor.DEFAULT_ELEMENT_NAME);
        KeyDescriptor descriptor = builder.buildObject();

        KeyStoreCredentialResolver resolver = getCredentialsResolver(key);
        Credential credential = getCredential(key, resolver);
        try {
            KeyInfo info = getKeyInfoGenerator(credential).generate(credential);
            descriptor.setKeyInfo(info);
            if (key.getType() != null) {
                descriptor.setUse(UsageType.valueOf(key.getType().toString()));
            }
            return descriptor;
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public Credential getCredential(SimpleKey key, KeyStoreCredentialResolver resolver) {
        try {
            CriteriaSet cs = new CriteriaSet();
            EntityIdCriterion criteria = new EntityIdCriterion(key.getAlias());
            cs.add(criteria);
            return resolver.resolveSingle(cs);
        } catch (ResolverException e) {
            throw new RuntimeException("Can't obtain SP private key", e);
        }
    }

    public KeyStoreCredentialResolver getCredentialsResolver(SimpleKey key) {
        InMemoryKeyStore ks = InMemoryKeyStore.fromKey(key);
        Map<String, String> passwords = hasText(key.getPrivateKey()) ?
            Collections.singletonMap(key.getAlias(), key.getPassphrase()) :
            Collections.emptyMap();
        KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(
            ks.getKeyStore(),
            passwords
        );
        return resolver;
    }

    public KeyInfoGenerator getKeyInfoGenerator(Credential credential) {
        NamedKeyInfoGeneratorManager manager = DefaultSecurityConfigurationBootstrap.buildBasicKeyInfoGeneratorManager();
        return manager.getDefaultManager().getFactory(credential).newInstance();
    }

    public void signObject(SignableSAMLObject signable,
                           SimpleKey key,
                           AlgorithmMethod algorithm,
                           DigestMethod digest) {

        KeyStoreCredentialResolver resolver = getCredentialsResolver(key);
        Credential credential = getCredential(key, resolver);

        XMLObjectBuilder<Signature> signatureBuilder =
            (XMLObjectBuilder<Signature>) getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME);
        Signature signature = signatureBuilder.buildObject(Signature.DEFAULT_ELEMENT_NAME);

        signable.setSignature(signature);


        SignatureSigningParameters parameters = new SignatureSigningParameters();
        parameters.setSigningCredential(credential);
        parameters.setKeyInfoGenerator(getKeyInfoGenerator(credential));
        parameters.setSignatureAlgorithm(algorithm.toString());
        parameters.setSignatureReferenceDigestMethod(digest.toString());
        parameters.setSignatureCanonicalizationAlgorithm(Canonicalization.ALGO_ID_C14N_EXCL_OMIT_COMMENTS.toString());

        try {
            SignatureSupport.prepareSignatureParams(signature, parameters);
            Marshaller marshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(signable);
            marshaller.marshall(signable);
            Signer.signObject(signature);
        } catch (SecurityException | MarshallingException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public void validateSignature(SignableSAMLObject object, List<SimpleKey> keys) {
        if (object.getSignature() != null && keys != null && !keys.isEmpty()) {
            SignatureException last = null;
            for (SimpleKey key : keys) {
                try {
                    KeyStoreCredentialResolver resolver = getCredentialsResolver(key);
                    Credential credential = getCredential(key, resolver);
                    SignatureValidator.validate(object.getSignature(), credential);
                    last = null;
                    break;
                } catch (SignatureException e) {
                    last = e;
                }
            }
            if (last != null) {
                throw new org.springframework.security.saml2.signature.SignatureException(
                    "Signature validation against a " + object.getClass().getName() +
                        " object failed using " + keys.size() + (keys.size() == 1 ? " key." : " keys."),
                    last);
            }
        }
    }

    protected XMLObject parse(String xml) {
        return parse(xml.getBytes(StandardCharsets.UTF_8));
    }

    protected XMLObject parse(byte[] xml) {
        try {
            Document document = getParserPool().parse(new ByteArrayInputStream(xml));
            Element element = document.getDocumentElement();
            return getUnmarshallerFactory().getUnmarshaller(element).unmarshall(element);
        } catch (UnmarshallingException | XMLParserException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<? extends Provider> getSsoProviders(EntityDescriptor descriptor) {
        final List<SsoProvider> providers = new LinkedList<>();
        for (RoleDescriptor roleDescriptor : descriptor.getRoleDescriptors()) {
            providers.add(getSsoProvider(roleDescriptor));
        }
        return providers;
    }

    protected SsoProvider getSsoProvider(RoleDescriptor descriptor) {
        if (descriptor instanceof SPSSODescriptor) {
            SPSSODescriptor desc = (SPSSODescriptor) descriptor;
            ServiceProvider provider = new ServiceProvider();
            provider.setId(desc.getID());
            provider.setValidUntil(desc.getValidUntil());
            provider.setProtocolSupportEnumeration(desc.getSupportedProtocols());
            provider.setNameIds(getNameIDs(desc.getNameIDFormats()));
            provider.setArtifactResolutionService(getEndpoints(desc.getArtifactResolutionServices()));
            provider.setSingleLogoutService(getEndpoints(desc.getSingleLogoutServices()));
            provider.setManageNameIDService(getEndpoints(desc.getManageNameIDServices()));
            provider.setAuthnRequestsSigned(desc.isAuthnRequestsSigned());
            provider.setWantAssertionsSigned(desc.getWantAssertionsSigned());
            provider.setAssertionConsumerService(getEndpoints(desc.getAssertionConsumerServices()));
            provider.setRequestedAttributes(getRequestAttributes(desc));
            provider.setKeys(getProviderKeys(descriptor));
            provider.setDiscovery(getDiscovery(desc));
            provider.setRequestInitiation(getRequestInitiation(desc));
            //TODO
            //provider.setAttributeConsumingService(getEndpoints(desc.getAttributeConsumingServices()));
            return provider;
        } else if (descriptor instanceof IDPSSODescriptor) {
            IDPSSODescriptor desc = (IDPSSODescriptor) descriptor;
            IdentityProvider provider = new IdentityProvider();
            provider.setId(desc.getID());
            provider.setValidUntil(desc.getValidUntil());
            provider.setProtocolSupportEnumeration(desc.getSupportedProtocols());
            provider.setNameIds(getNameIDs(desc.getNameIDFormats()));
            provider.setArtifactResolutionService(getEndpoints(desc.getArtifactResolutionServices()));
            provider.setSingleLogoutService(getEndpoints(desc.getSingleLogoutServices()));
            provider.setManageNameIDService(getEndpoints(desc.getManageNameIDServices()));
            provider.setWantAuthnRequestsSigned(desc.getWantAuthnRequestsSigned());
            provider.setSingleSignOnService(getEndpoints(desc.getSingleSignOnServices()));
            provider.setKeys(getProviderKeys(descriptor));
            provider.setDiscovery(getDiscovery(desc));
            provider.setRequestInitiation(getRequestInitiation(desc));
            return provider;
        } else {

        }
        throw new UnsupportedOperationException();
    }

    protected List<Attribute> getRequestAttributes(SPSSODescriptor desc) {
        List<Attribute> result = new LinkedList<>();
        if (desc.getDefaultAttributeConsumingService() != null) {
            result.addAll(getRequestedAttributes(desc.getDefaultAttributeConsumingService().getRequestAttributes()));
        } else {
            for (AttributeConsumingService s : ofNullable(desc.getAttributeConsumingServices()).orElse(emptyList())) {
                if (s != null) {
                    //take the first one
                    result.addAll(getRequestedAttributes(s.getRequestAttributes()));
                    break;
                }
            }
        }
        return result;
    }

    protected Endpoint getRequestInitiation(RoleDescriptor desc) {
        if (desc.getExtensions() == null) {
            return null;
        }
        Endpoint result = null;
        for (XMLObject obj : desc.getExtensions().getUnknownXMLObjects()) {
            if (obj instanceof RequestInitiator) {
                RequestInitiator req = (RequestInitiator) obj;
                result = new Endpoint()
                    .setIndex(0)
                    .setDefault(false)
                    .setBinding(Binding.fromUrn(req.getBinding()))
                    .setLocation(req.getLocation())
                    .setResponseLocation(req.getResponseLocation());
            }
        }
        return result;
    }

    protected Endpoint getDiscovery(RoleDescriptor desc) {
        if (desc.getExtensions() == null) {
            return null;
        }
        Endpoint result = null;
        for (XMLObject obj : desc.getExtensions().getUnknownXMLObjects()) {
            if (obj instanceof DiscoveryResponse) {
                DiscoveryResponse resp = (DiscoveryResponse) obj;
                result = new Endpoint()
                    .setDefault(resp.isDefault())
                    .setIndex(resp.getIndex())
                    .setBinding(Binding.fromUrn(resp.getBinding()))
                    .setLocation(resp.getLocation())
                    .setResponseLocation(resp.getResponseLocation());
            }
        }
        return result;
    }

    protected List<SimpleKey> getProviderKeys(RoleDescriptor descriptor) {
        List<SimpleKey> result = new LinkedList<>();
        for (KeyDescriptor desc : ofNullable(descriptor.getKeyDescriptors()).orElse(emptyList())) {
            if (desc != null) {
                result.addAll(getKeyFromDescriptor(desc));
            }
        }
        return result;
    }

    protected List<SimpleKey> getKeyFromDescriptor(KeyDescriptor desc) {
        List<SimpleKey> result = new LinkedList<>();
        if (desc.getKeyInfo() == null) {
            return null;
        }
        KeyType type = desc.getUse() != null ? KeyType.valueOf(desc.getUse().name()) : KeyType.UNSPECIFIED;
        int index = 0;
        for (X509Data x509 : ofNullable(desc.getKeyInfo().getX509Datas()).orElse(emptyList())) {
            for (X509Certificate cert : ofNullable(x509.getX509Certificates()).orElse(emptyList())) {
                result.add(new SimpleKey(type.getTypeName() + "-" + (index++), null, cert.getValue(), null, type));
            }
        }

        return result;
    }

    protected List<Endpoint> getEndpoints(List<? extends org.opensaml.saml.saml2.metadata.Endpoint> services) {
        List<Endpoint> result = new LinkedList<>();
        if (services != null) {
            services
                .stream()
                .forEach(s -> {
                             Endpoint endpoint = new Endpoint()
                                 .setBinding(Binding.fromUrn(s.getBinding()))
                                 .setLocation(s.getLocation())
                                 .setResponseLocation(s.getResponseLocation());
                             result.add(endpoint);
                             if (s instanceof IndexedEndpoint) {
                                 IndexedEndpoint idxEndpoint = (IndexedEndpoint) s;
                                 endpoint
                                     .setIndex(idxEndpoint.getIndex())
                                     .setDefault(idxEndpoint.isDefault());
                             }
                         }
                );


        }


        return result;
    }

    protected List<NameId> getNameIDs(List<NameIDFormat> nameIDFormats) {
        List<NameId> result = new LinkedList<>();
        if (nameIDFormats != null) {
            nameIDFormats.stream()
                .forEach(n -> result.add(NameId.fromUrn(n.getFormat())));
        }
        return result;
    }

    @Override
    public long toMillis(Duration duration) {
        if (isNull(duration)) {
            return -1;
        } else {
            return DOMTypeSupport.durationToLong(duration);
        }
    }

    @Override
    public Duration toDuration(long millis) {
        if (millis < 0) {
            return null;
        } else {
            return DOMTypeSupport.getDataTypeFactory().newDuration(millis);
        }
    }

    @Override
    public String toXml(Saml2Object saml2Object) {
        XMLObject result = null;
        if (saml2Object instanceof AuthenticationRequest) {
            result = internalToXml((AuthenticationRequest) saml2Object);
        } else if (saml2Object instanceof Assertion) {
            result = internalToXml((Assertion) saml2Object);
        } else if (saml2Object instanceof Metadata) {
            result = internalToXml((Metadata) saml2Object);
        } else if (saml2Object instanceof Response) {
            result = internalToXml((Response) saml2Object);
        }
        if (result != null) {
            return marshallToXml(result);
        }
        throw new UnsupportedOperationException();
    }

    protected org.opensaml.saml.saml2.core.Response internalToXml(Response response) {
        org.opensaml.saml.saml2.core.Response result = buildSAMLObject(org.opensaml.saml.saml2.core.Response.class);
        result.setConsent(response.getConsent());
        result.setID(ofNullable(response.getId()).orElse("a" + UUID.randomUUID().toString()));
        result.setInResponseTo(response.getInResponseTo());
        result.setVersion(SAMLVersion.VERSION_20);
        result.setIssueInstant(response.getIssueInstant());
        result.setDestination(response.getDestination());
        result.setIssuer(toIssuer(response.getIssuer()));

        if (response.getStatus() == null || response.getStatus().getCode() == null) {
            throw new IllegalArgumentException("Status cannot be null on a response");
        }
        org.opensaml.saml.saml2.core.Status status = buildSAMLObject(org.opensaml.saml.saml2.core.Status.class);
        org.opensaml.saml.saml2.core.StatusCode code = buildSAMLObject(org.opensaml.saml.saml2.core.StatusCode.class);
        code.setValue(response.getStatus().getCode().toString());
        status.setStatusCode(code);

        if (hasText(response.getStatus().getMessage())) {
            StatusMessage message = buildSAMLObject(StatusMessage.class);
            message.setMessage(response.getStatus().getMessage());
            status.setStatusMessage(message);
        }

        result.setStatus(status);

        for (Assertion a : ofNullable(response.getAssertions()).orElse(emptyList())) {
            result.getAssertions().add(internalToXml(a));
        }
        if (response.getSigningKey() != null) {
            signObject(result, response.getSigningKey(), response.getAlgorithm(), response.getDigest());
        }
        return result;
    }

    protected EntityDescriptor internalToXml(Metadata<? extends Metadata> metadata) {
        EntityDescriptor desc = getEntityDescriptor();
        desc.setEntityID(metadata.getEntityId());
        if (hasText(metadata.getId())) {
            desc.setID(metadata.getId());
        } else {
            desc.setID(UUID.randomUUID().toString());
        }
        List<RoleDescriptor> descriptors = getRoleDescriptors(metadata);
        desc.getRoleDescriptors().addAll(descriptors);
        if (metadata.getSigningKey() != null) {
            signObject(desc, metadata.getSigningKey(), metadata.getAlgorithm(), metadata.getDigest());
        }
        return desc;
    }

    protected List<RoleDescriptor> getRoleDescriptors(Metadata<? extends Metadata> metadata) {
        List<RoleDescriptor> result = new LinkedList<>();
        for (SsoProvider<? extends SsoProvider> p : metadata.getSsoProviders()) {
            RoleDescriptor roleDescriptor = null;
            if (p instanceof ServiceProvider) {
                ServiceProvider sp = (ServiceProvider) p;
                SPSSODescriptor descriptor = getSPSSODescriptor();
                roleDescriptor = descriptor;
                descriptor.setAuthnRequestsSigned(sp.isAuthnRequestsSigned());
                descriptor.setWantAssertionsSigned(sp.isWantAssertionsSigned());

                for (NameId id : p.getNameIds()) {
                    descriptor.getNameIDFormats().add(getNameIDFormat(id));
                }

                for (int i = 0; i < sp.getAssertionConsumerService().size(); i++) {
                    Endpoint ep = sp.getAssertionConsumerService().get(i);
                    descriptor.getAssertionConsumerServices().add(getAssertionConsumerService(ep, i));
                }
                for (int i = 0; i < sp.getArtifactResolutionService().size(); i++) {
                    Endpoint ep = sp.getArtifactResolutionService().get(i);
                    descriptor.getArtifactResolutionServices().add(getArtifactResolutionService(ep, i));
                }
                for (int i = 0; i < sp.getSingleLogoutService().size(); i++) {
                    Endpoint ep = sp.getSingleLogoutService().get(i);
                    descriptor.getSingleLogoutServices().add(getSingleLogoutService(ep));
                }
                descriptor.getAttributeConsumingServices().add(
                    getAttributeConsumingService(sp.getRequestedAttributes())
                );

            } else if (p instanceof IdentityProvider) {
                IdentityProvider idp = (IdentityProvider) p;
                IDPSSODescriptor descriptor = getIDPSSODescriptor();
                roleDescriptor = descriptor;
                descriptor.setWantAuthnRequestsSigned(idp.getWantAuthnRequestsSigned());
                for (NameId id : p.getNameIds()) {
                    descriptor.getNameIDFormats().add(getNameIDFormat(id));
                }
                for (int i = 0; i < idp.getSingleSignOnService().size(); i++) {
                    Endpoint ep = idp.getSingleSignOnService().get(i);
                    descriptor.getSingleSignOnServices().add(getSingleSignOnService(ep, i));
                }
                for (int i = 0; i < p.getSingleLogoutService().size(); i++) {
                    Endpoint ep = p.getSingleLogoutService().get(i);
                    descriptor.getSingleLogoutServices().add(getSingleLogoutService(ep));
                }
                for (int i = 0; i < p.getArtifactResolutionService().size(); i++) {
                    Endpoint ep = p.getArtifactResolutionService().get(i);
                    descriptor.getArtifactResolutionServices().add(getArtifactResolutionService(ep, i));
                }
            }
            long now = System.currentTimeMillis();
            if (p.getCacheDuration() != null) {
                roleDescriptor.setCacheDuration(p.getCacheDuration().getTimeInMillis(new Date(now)));
            }
            roleDescriptor.setValidUntil(p.getValidUntil());
            roleDescriptor.addSupportedProtocol(NS_PROTOCOL);
            roleDescriptor.setID(ofNullable(p.getId()).orElse(UUID.randomUUID().toString()));

            for (SimpleKey key : p.getKeys()) {
                roleDescriptor.getKeyDescriptors().add(getKeyDescriptor(key));
            }

            ExtensionsBuilder extensionsBuilder = (ExtensionsBuilder) getBuilderFactory().getBuilder(Extensions.DEFAULT_ELEMENT_NAME);
            roleDescriptor.setExtensions(extensionsBuilder.buildObject());

            Endpoint requestInitiation = p.getRequestInitiation();
            if (requestInitiation != null) {
                RequestInitiatorBuilder builder = (RequestInitiatorBuilder) getBuilderFactory().getBuilder(RequestInitiator.DEFAULT_ELEMENT_NAME);
                RequestInitiator init = builder.buildObject();
                init.setBinding(requestInitiation.getBinding().toString());
                init.setLocation(requestInitiation.getLocation());
                init.setResponseLocation(requestInitiation.getResponseLocation());
                roleDescriptor.getExtensions().getUnknownXMLObjects().add(init);
            }
            Endpoint discovery = p.getDiscovery();
            if (discovery != null) {
                DiscoveryResponseBuilder builder = (DiscoveryResponseBuilder) getBuilderFactory().getBuilder(DiscoveryResponse.DEFAULT_ELEMENT_NAME);
                DiscoveryResponse response = builder.buildObject(DiscoveryResponse.DEFAULT_ELEMENT_NAME);
                response.setBinding(discovery.getBinding().toString());
                response.setLocation(discovery.getLocation());
                response.setResponseLocation(discovery.getResponseLocation());
                response.setIsDefault(discovery.isDefault());
                response.setIndex(discovery.getIndex());
                roleDescriptor.getExtensions().getUnknownXMLObjects().add(response);
            }
            result.add(roleDescriptor);
        }
        return result;
    }

    protected AttributeConsumingService getAttributeConsumingService(List<Attribute> attributes) {

        AttributeConsumingService service = buildSAMLObject(AttributeConsumingService.class);
        service.setIsDefault(true);
        service.setIndex(0);
        List<RequestedAttribute> attrs = new LinkedList<>();
        for (Attribute a : attributes) {
            RequestedAttribute ra = buildSAMLObject(RequestedAttribute.class);
            ra.setIsRequired(a.isRequired());
            ra.setFriendlyName(a.getFriendlyName());
            ra.setName(a.getName());
            ra.setNameFormat(a.getNameFormat().toString());
            attrs.add(ra);
        }
        service.getRequestAttributes().addAll(attrs);
        return service;
    }

    protected ArtifactResolutionService getArtifactResolutionService(Endpoint ep, int i) {
        ArtifactResolutionService service = buildSAMLObject(ArtifactResolutionService.class);
        service.setLocation(ep.getLocation());
        service.setBinding(ep.getBinding().toString());
        service.setIndex(i);
        service.setIsDefault(ep.isDefault());
        service.setResponseLocation(ep.getResponseLocation());
        return service;
    }

    protected org.opensaml.saml.saml2.core.Assertion internalToXml(Assertion request) {
        org.opensaml.saml.saml2.core.Assertion a = buildSAMLObject(org.opensaml.saml.saml2.core.Assertion.class);
        a.setVersion(SAMLVersion.VERSION_20);
        a.setIssueInstant(request.getIssueInstant());
        a.setID(request.getId());
        org.opensaml.saml.saml2.core.Issuer issuer = buildSAMLObject(org.opensaml.saml.saml2.core.Issuer.class);
        issuer.setValue(request.getIssuer().getValue());
        a.setIssuer(issuer);

        NameIdPrincipal principal = (NameIdPrincipal) request.getSubject().getPrincipal();

        org.opensaml.saml.saml2.core.NameID nid = buildSAMLObject(org.opensaml.saml.saml2.core.NameID.class);
        nid.setValue(request.getSubject().getPrincipal().getValue());
        nid.setFormat(principal.getFormat().toString());
        nid.setSPNameQualifier(principal.getSpNameQualifier());

        org.opensaml.saml.saml2.core.SubjectConfirmationData confData = buildSAMLObject(org.opensaml.saml.saml2.core.SubjectConfirmationData.class);
        confData.setInResponseTo(request.getSubject().getConfirmations().get(0).getConfirmationData().getInResponseTo());
        confData.setNotBefore(request.getSubject().getConfirmations().get(0).getConfirmationData().getNotBefore());
        confData.setNotOnOrAfter(request.getSubject().getConfirmations().get(0).getConfirmationData().getNotOnOrAfter());

        org.opensaml.saml.saml2.core.SubjectConfirmation confirmation = buildSAMLObject(org.opensaml.saml.saml2.core.SubjectConfirmation.class);
        confirmation.setMethod(request.getSubject().getConfirmations().get(0).getMethod().toString());
        confirmation.setSubjectConfirmationData(confData);

        org.opensaml.saml.saml2.core.Subject subject = buildSAMLObject(org.opensaml.saml.saml2.core.Subject.class);
        a.setSubject(subject);
        subject.setNameID(nid);
        subject.getSubjectConfirmations().add(confirmation);

        org.opensaml.saml.saml2.core.Conditions conditions = buildSAMLObject(org.opensaml.saml.saml2.core.Conditions.class);
        conditions.setNotBefore(request.getConditions().getNotBefore());
        conditions.setNotOnOrAfter(request.getConditions().getNotOnOrAfter());
        a.setConditions(conditions);

        request.getConditions().getCriteria().forEach(c -> addCondition(conditions, c));


        for (AuthenticationStatement stmt : request.getAuthenticationStatements()) {
            org.opensaml.saml.saml2.core.AuthnStatement authnStatement = buildSAMLObject(org.opensaml.saml.saml2.core.AuthnStatement.class);
            org.opensaml.saml.saml2.core.AuthnContext actx = buildSAMLObject(org.opensaml.saml.saml2.core.AuthnContext.class);
            org.opensaml.saml.saml2.core.AuthnContextClassRef aref = buildSAMLObject(org.opensaml.saml.saml2.core.AuthnContextClassRef.class);
            aref.setAuthnContextClassRef(stmt.getAuthenticationContext().getClassReference().toString());
            actx.setAuthnContextClassRef(aref);
            authnStatement.setAuthnContext(actx);
            a.getAuthnStatements().add(authnStatement);
            authnStatement.setSessionIndex(stmt.getSessionIndex());
            authnStatement.setSessionNotOnOrAfter(stmt.getSessionNotOnOrAfter());
            authnStatement.setAuthnInstant(stmt.getAuthInstant());
        }

        org.opensaml.saml.saml2.core.AttributeStatement astmt = buildSAMLObject(org.opensaml.saml.saml2.core.AttributeStatement.class);
        for (Attribute attr : request.getAttributes()) {
            org.opensaml.saml.saml2.core.Attribute attribute = buildSAMLObject(org.opensaml.saml.saml2.core.Attribute.class);
            attribute.setName(attr.getName());
            attribute.setFriendlyName(attr.getFriendlyName());
            attribute.setNameFormat(attr.getNameFormat().toString());
            attr.getValues().stream().forEach(
                av -> attribute.getAttributeValues().add(objectToXmlObject(av))
            );
            astmt.getAttributes().add(attribute);
        }
        a.getAttributeStatements().add(astmt);

        if (request.getSigningKey() != null) {
            signObject(a, request.getSigningKey(), request.getAlgorithm(), request.getDigest());
        }

        return a;
    }

    protected void addCondition(org.opensaml.saml.saml2.core.Conditions conditions, AssertionCondition c) {
        if (c instanceof AudienceRestriction) {
            org.opensaml.saml.saml2.core.AudienceRestriction ar = buildSAMLObject(org.opensaml.saml.saml2.core.AudienceRestriction.class);
            for (String audience : ((AudienceRestriction) c).getAudiences()) {
                Audience aud = buildSAMLObject(Audience.class);
                aud.setAudienceURI(audience);
                ar.getAudiences().add(aud);
            }
            conditions.getAudienceRestrictions().add(ar);
        } else if (c instanceof OneTimeUse) {
            org.opensaml.saml.saml2.core.OneTimeUse otu = buildSAMLObject(org.opensaml.saml.saml2.core.OneTimeUse.class);
            conditions.getConditions().add(otu);
        }
    }

    protected AuthnRequest internalToXml(AuthenticationRequest request) {
        AuthnRequest auth = buildSAMLObject(AuthnRequest.class);
        auth.setID(request.getId());
        auth.setVersion(SAMLVersion.VERSION_20);
        auth.setIssueInstant(request.getIssueInstant());
        auth.setForceAuthn(request.isForceAuth());
        auth.setIsPassive(request.isPassive());
        auth.setProtocolBinding(request.getBinding().toString());
        auth.setAssertionConsumerServiceIndex(request.getAssertionConsumerService().getIndex());
        auth.setAssertionConsumerServiceURL(request.getAssertionConsumerService().getLocation());
        auth.setDestination(request.getDestination().getLocation());
        auth.setNameIDPolicy(getNameIDPolicy(request.getNameIDPolicy()));
        auth.setRequestedAuthnContext(getRequestedAuthenticationContext(request));
        if (request.getSigningKey() != null) {
            this.signObject(auth, request.getSigningKey(), request.getAlgorithm(), request.getDigest());
        }

        return auth;
    }

    protected String marshallToXml(XMLObject auth) {
        try {
            Element element = getMarshallerFactory()
                .getMarshaller(auth)
                .marshall(auth);
            return SerializeSupport.nodeToString(element);
        } catch (MarshallingException e) {
            throw new RuntimeException(e);
        }
    }

    protected RequestedAuthnContext getRequestedAuthenticationContext(AuthenticationRequest request) {
        RequestedAuthnContext result = null;
        if (request.getRequestedAuthenticationContext() != null) {
            result = buildSAMLObject(RequestedAuthnContext.class);
            switch (request.getRequestedAuthenticationContext()) {
                case exact:
                    result.setComparison(EXACT);
                    break;
                case better:
                    result.setComparison(AuthnContextComparisonTypeEnumeration.BETTER);
                    break;
                case maximum:
                    result.setComparison(AuthnContextComparisonTypeEnumeration.MAXIMUM);
                    break;
                case minimum:
                    result.setComparison(AuthnContextComparisonTypeEnumeration.MAXIMUM);
                    break;
                default:
                    result.setComparison(EXACT);
                    break;
            }
        }
        return result;
    }

    protected NameIDPolicy getNameIDPolicy(org.springframework.security.saml2.authentication.NameIDPolicy nameIDPolicy) {
        NameIDPolicy result = null;
        if (nameIDPolicy != null) {
            result = buildSAMLObject(NameIDPolicy.class);
            result.setAllowCreate(nameIDPolicy.getAllowCreate());
            result.setFormat(nameIDPolicy.getFormat().toString());
            result.setSPNameQualifier(nameIDPolicy.getSpNameQualifier());
        }
        return result;
    }

    protected org.springframework.security.saml2.authentication.NameIDPolicy fromNameIDPolicy(NameIDPolicy nameIDPolicy) {
        org.springframework.security.saml2.authentication.NameIDPolicy result = null;
        if (nameIDPolicy != null) {
            result = new org.springframework.security.saml2.authentication.NameIDPolicy()
                .setAllowCreate(nameIDPolicy.getAllowCreate())
                .setFormat(NameId.fromUrn(nameIDPolicy.getFormat()))
                .setSpNameQualifier(nameIDPolicy.getSPNameQualifier());
        }
        return result;
    }

    public <T> T buildSAMLObject(final Class<T> clazz) {
        T object = null;
        try {
            QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
            object = (T) getBuilderFactory().getBuilder(defaultElementName).buildObject(defaultElementName);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not create SAML object");
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Could not create SAML object");
        }

        return object;
    }

    @Override
    public Saml2Object resolve(String xml, List<SimpleKey> trustedKeys) {
        return resolve(xml.getBytes(StandardCharsets.UTF_8), trustedKeys);
    }

    public Saml2Object resolve(byte[] xml, List<SimpleKey> trustedKeys) {
        XMLObject parsed = parse(xml);
        validateSignature((SignableSAMLObject) parsed, trustedKeys);
        if (parsed instanceof EntityDescriptor) {
            return resolveMetadata((EntityDescriptor) parsed);
        }
        if (parsed instanceof AuthnRequest) {
            return resolveAuthenticationRequest((AuthnRequest) parsed);
        }
        if (parsed instanceof org.opensaml.saml.saml2.core.Assertion) {
            return resolveAssertion((org.opensaml.saml.saml2.core.Assertion) parsed, trustedKeys);
        }
        if (parsed instanceof org.opensaml.saml.saml2.core.Response) {
            return resolveResponse((org.opensaml.saml.saml2.core.Response) parsed, trustedKeys);
        }
        throw new IllegalArgumentException("not yet implemented class parsing:" + parsed.getClass());
    }

    protected Response resolveResponse(org.opensaml.saml.saml2.core.Response parsed, List<SimpleKey> trustedKeys) {
        Response result = new Response()
            .setConsent(parsed.getConsent())
            .setDestination(parsed.getDestination())
            .setId(parsed.getID())
            .setInResponseTo(parsed.getInResponseTo())
            .setIssueInstant(parsed.getIssueInstant())
            .setIssuer(getIssuer(parsed.getIssuer()))
            .setVersion(parsed.getVersion().toString())
            .setStatus(getStatus(parsed.getStatus()))
            .setAssertions(
                parsed.getAssertions().stream().map(a -> resolveAssertion(a, trustedKeys)).collect(Collectors.toList())
            );

        return result;

    }

    protected Status getStatus(org.opensaml.saml.saml2.core.Status status) {
        return new Status()
            .setCode(StatusCode.fromUrn(status.getStatusCode().getValue()))
            .setMessage(status.getStatusMessage().getMessage());


    }

    protected Assertion resolveAssertion(org.opensaml.saml.saml2.core.Assertion parsed, List<SimpleKey> trustedKeys) {
        validateSignature(parsed, trustedKeys);
        return new Assertion()
            .setId(parsed.getID())
            .setIssueInstant(parsed.getIssueInstant())
            .setVersion(parsed.getVersion().toString())
            .setIssuer(getIssuer(parsed.getIssuer()))
            .setSubject(getSubject(parsed.getSubject()))
            .setConditions(getConditions(parsed.getConditions()))
            .setAuthenticationStatements(getAuthenticationStatements(parsed.getAuthnStatements()))
            .setAttributes(getAttributes(parsed.getAttributeStatements()))
            ;
    }

    protected List<Attribute> getRequestedAttributes(List<RequestedAttribute> attributes) {
        List<Attribute> result = new LinkedList<>();
        for (RequestedAttribute a : ofNullable(attributes).orElse(emptyList())) {
            result.add(
                new Attribute()
                    .setFriendlyName(a.getFriendlyName())
                    .setName(a.getName())
                    .setNameFormat(AttributeNameFormat.fromUrn(a.getNameFormat()))
                    .setValues(getJavaValues(a.getAttributeValues()))
                    .setRequired(a.isRequired())
            );
        }
        return result;
    }

    protected List<Attribute> getAttributes(List<AttributeStatement> attributeStatements) {
        List<Attribute> result = new LinkedList<>();
        for (AttributeStatement stmt : ofNullable(attributeStatements).orElse(emptyList())) {
            for (org.opensaml.saml.saml2.core.Attribute a : ofNullable(stmt.getAttributes()).orElse(emptyList())) {
                result.add(
                    new Attribute()
                        .setFriendlyName(a.getFriendlyName())
                        .setName(a.getName())
                        .setNameFormat(AttributeNameFormat.fromUrn(a.getNameFormat()))
                        .setValues(getJavaValues(a.getAttributeValues()))
                );
            }
        }
        return result;
    }

    protected List<Object> getJavaValues(List<XMLObject> attributeValues) {
        List<Object> result = new LinkedList<>();
        for (XMLObject o : ofNullable(attributeValues).orElse(emptyList())) {
            if (o == null) {

            } else if (o instanceof XSString) {
                result.add(((XSString) o).getValue());
            } else if (o instanceof XSURI) {
                try {
                    result.add(new URI(((XSURI) o).getValue()));
                } catch (URISyntaxException e) {
                    result.add(((XSURI) o).getValue());
                }
            } else if (o instanceof XSBoolean) {
                result.add(((XSBoolean) o).getValue().getValue());
            } else if (o instanceof XSDateTime) {
                result.add(((XSDateTime) o).getValue());
            } else if (o instanceof XSInteger) {
                result.add(((XSInteger) o).getValue());
            } else if (o instanceof XSAny) {
                result.add(((XSAny) o).getTextContent());
            } else {
                //we don't know the type.
                result.add(o);
            }
        }

        return result;
    }

    protected List<AuthenticationStatement> getAuthenticationStatements(List<AuthnStatement> authnStatements) {
        List<AuthenticationStatement> result = new LinkedList<>();

        for (AuthnStatement s : ofNullable(authnStatements).orElse(emptyList())) {
            AuthnContext authnContext = s.getAuthnContext();
            AuthnContextClassRef authnContextClassRef = authnContext.getAuthnContextClassRef();
            String ref = null;
            if (authnContextClassRef.getAuthnContextClassRef() != null) {
                ref = authnContextClassRef.getAuthnContextClassRef();
            }

            result.add(
                new AuthenticationStatement()
                    .setSessionIndex(s.getSessionIndex())
                    .setAuthInstant(s.getAuthnInstant())
                    .setSessionNotOnOrAfter(s.getSessionNotOnOrAfter())
                    .setAuthenticationContext(
                        authnContext != null ?
                            new AuthenticationContext()
                                .setClassReference(AuthenticationContextClassReference.fromUrn(ref))
                            : null
                    )
            );

        }
        return result;
    }

    protected Conditions getConditions(org.opensaml.saml.saml2.core.Conditions conditions) {
        return new Conditions()
            .setNotBefore(conditions.getNotBefore())
            .setNotOnOrAfter(conditions.getNotOnOrAfter())
            .setCriteria(getCriteria(conditions.getConditions()));
    }

    protected List<AssertionCondition> getCriteria(List<org.opensaml.saml.saml2.core.Condition> conditions) {
        List<AssertionCondition> result = new LinkedList<>();
        for (Condition c : conditions) {
            if (c instanceof org.opensaml.saml.saml2.core.AudienceRestriction) {
                org.opensaml.saml.saml2.core.AudienceRestriction aud = (org.opensaml.saml.saml2.core.AudienceRestriction) c;
                if (aud.getAudiences() != null) {
                    result.add(
                        new AudienceRestriction()
                            .setAudiences(
                                aud.getAudiences().stream().map(
                                    a -> a.getAudienceURI()
                                ).collect(Collectors.toList())
                            )
                    );
                }
            } else if (c instanceof org.opensaml.saml.saml2.core.OneTimeUse) {
                result.add(new OneTimeUse());
            }
        }
        return result;
    }

    protected Subject getSubject(org.opensaml.saml.saml2.core.Subject subject) {

        return new Subject()
            .setPrincipal(getPrincipal(subject))
            .setConfirmations(getConfirmations(subject.getSubjectConfirmations()))
            ;
    }

    protected List<SubjectConfirmation> getConfirmations(List<org.opensaml.saml.saml2.core.SubjectConfirmation> subjectConfirmations) {
        List<SubjectConfirmation> result = new LinkedList<>();
        for (org.opensaml.saml.saml2.core.SubjectConfirmation s : subjectConfirmations) {
            result.add(
                new SubjectConfirmation()
                    .setMethod(SubjectConfirmationMethod.fromUrn(s.getMethod()))
                    .setConfirmationData(
                        new SubjectConfirmationData()
                            .setRecipient(s.getSubjectConfirmationData().getRecipient())
                            .setNotOnOrAfter(s.getSubjectConfirmationData().getNotOnOrAfter())
                            .setNotBefore(s.getSubjectConfirmationData().getNotBefore())
                            .setInResponseTo(s.getSubjectConfirmationData().getInResponseTo())
                    )
            );
        }
        return result;
    }

    protected SubjectPrincipal getPrincipal(org.opensaml.saml.saml2.core.Subject subject) {
        org.opensaml.saml.saml2.core.NameID p = subject.getNameID();
        if (p != null) {
            return new NameIdPrincipal()
                .setSpNameQualifier(p.getSPNameQualifier())
                .setNameQualifier(p.getNameQualifier())
                .setFormat(NameId.fromUrn(p.getFormat()))
                .setSpProvidedId(p.getSPProvidedID())
                .setValue(p.getValue());
        } else {
            throw new UnsupportedOperationException("Currently only supporting NameID subject principals");
        }
    }

    protected org.opensaml.saml.saml2.core.Issuer toIssuer(Issuer issuer) {
        org.opensaml.saml.saml2.core.Issuer result = buildSAMLObject(org.opensaml.saml.saml2.core.Issuer.class);
        result.setValue(issuer.getValue());
        if (issuer.getFormat() != null) {
            result.setFormat(issuer.getFormat().toString());
        }
        result.setSPNameQualifier(issuer.getSpNameQualifier());
        result.setNameQualifier(issuer.getNameQualifier());
        return result;
    }

    protected Issuer getIssuer(org.opensaml.saml.saml2.core.Issuer issuer) {
        return new Issuer()
            .setValue(issuer.getValue())
            .setFormat(NameId.fromUrn(issuer.getFormat()))
            .setSpNameQualifier(issuer.getSPNameQualifier())
            .setNameQualifier(issuer.getNameQualifier());
    }

    protected Saml2Object resolveAuthenticationRequest(AuthnRequest parsed) {
        AuthnRequest request = parsed;
        AuthenticationRequest result = new AuthenticationRequest()
            .setBinding(Binding.fromUrn(request.getProtocolBinding()))
            .setAssertionConsumerService(
                getEndpoint(request.getAssertionConsumerServiceURL(),
                            Binding.fromUrn(request.getProtocolBinding()),
                            request.getAssertionConsumerServiceIndex(),
                            false)
            )
            .setDestination(
                getEndpoint(
                    request.getDestination(),
                    Binding.fromUrn(request.getProtocolBinding()),
                    -1,
                    false
                )
            )
            .setForceAuth(request.isForceAuthn())
            .setPassive(request.isPassive())
            .setId(request.getID())
            .setIssueInstant(request.getIssueInstant())
            .setVersion(request.getVersion().toString())
            .setRequestedAuthenticationContext(getRequestedAuthenticationContext(request))
            .setNameIDPolicy(fromNameIDPolicy(request.getNameIDPolicy()));

        return result;
    }

    protected RequestedAuthenticationContext getRequestedAuthenticationContext(AuthnRequest request) {
        RequestedAuthenticationContext result = null;

        if (request.getRequestedAuthnContext() != null) {
            AuthnContextComparisonTypeEnumeration comparison = request.getRequestedAuthnContext().getComparison();
            if (null != comparison) {
                result = RequestedAuthenticationContext.valueOf(comparison.toString());
            }
        }
        return result;
    }

    protected Saml2Object resolveMetadata(EntityDescriptor parsed) {
        EntityDescriptor descriptor = parsed;
        List<? extends Provider> ssoProviders = getSsoProviders(descriptor);
        Metadata desc = getMetadata(ssoProviders);
        desc.setCacheDurationMillis(descriptor.getCacheDuration() != null ? descriptor.getCacheDuration() : -1);
        desc.setEntityId(descriptor.getEntityID());
        desc.setEntityAlias(descriptor.getEntityID());
        desc.setId(descriptor.getID());
        desc.setValidUntil(descriptor.getValidUntil());
        return desc;
    }

    protected Metadata getMetadata(List<? extends Provider> ssoProviders) {
        Metadata result = new Metadata();
        if (ssoProviders.size() == 1) {
            if (ssoProviders.get(0) instanceof ServiceProvider) {
                result = new ServiceProviderMetadata();
            } else if (ssoProviders.get(0) instanceof IdentityProvider) {
                result = new IdentityProviderMetadata();
            }
        }
        result.setProviders(ssoProviders);
        return result;
    }

    protected XMLObject objectToXmlObject(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            XSStringBuilder builder = (XSStringBuilder) getBuilderFactory().getBuilder(XSString.TYPE_NAME);
            XSString s = builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
            s.setValue((String) o);
            return s;
        } else if (o instanceof URI || o instanceof URL) {
            XSURIBuilder builder = (XSURIBuilder) getBuilderFactory().getBuilder(XSURI.TYPE_NAME);
            XSURI uri = builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSURI.TYPE_NAME);
            uri.setValue(o.toString());
            return uri;
        } else if (o instanceof Boolean) {
            XSBooleanBuilder builder = (XSBooleanBuilder) getBuilderFactory().getBuilder(XSBoolean.TYPE_NAME);
            XSBoolean b = builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSBoolean.TYPE_NAME);
            XSBooleanValue v = XSBooleanValue.valueOf(o.toString());
            b.setValue(v);
            return b;
        } else if (o instanceof DateTime) {
            XSDateTimeBuilder builder = (XSDateTimeBuilder) getBuilderFactory().getBuilder(XSDateTime.TYPE_NAME);
            XSDateTime dt = builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSDateTime.TYPE_NAME);
            dt.setValue((DateTime) o);
            return dt;
        } else if (o instanceof Integer) {
            XSIntegerBuilder builder = (XSIntegerBuilder) getBuilderFactory().getBuilder(XSInteger.TYPE_NAME);
            XSInteger i = builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSInteger.TYPE_NAME);
            i.setValue(((Integer) o).intValue());
            return i;
        } else {
            XSAnyBuilder builder = (XSAnyBuilder) getBuilderFactory().getBuilder(XSAny.TYPE_NAME);
            XSAny any = builder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSAny.TYPE_NAME);
            any.setTextContent(o.toString());
            return any;
        }
    }

    protected String xmlObjectToString(XMLObject o) {
        String toMatch = null;
        if (o instanceof XSString) {
            toMatch = ((XSString) o).getValue();
        } else if (o instanceof XSURI) {
            toMatch = ((XSURI) o).getValue();
        } else if (o instanceof XSBoolean) {
            toMatch = ((XSBoolean) o).getValue().getValue() ? "1" : "0";
        } else if (o instanceof XSInteger) {
            toMatch = ((XSInteger) o).getValue().toString();
        } else if (o instanceof XSDateTime) {
            final DateTime dt = ((XSDateTime) o).getValue();
            if (dt != null) {
                toMatch = ((XSDateTime) o).getDateTimeFormatter().print(dt);
            }
        } else if (o instanceof XSBase64Binary) {
            toMatch = ((XSBase64Binary) o).getValue();
        } else if (o instanceof XSAny) {
            final XSAny wc = (XSAny) o;
            if (wc.getUnknownAttributes().isEmpty() && wc.getUnknownXMLObjects().isEmpty()) {
                toMatch = wc.getTextContent();
            }
        }
        if (toMatch != null) {
            return toMatch;
        }
        return null;
    }
}
