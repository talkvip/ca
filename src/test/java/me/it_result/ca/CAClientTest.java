/**
 * Copyright 2010 Roman Kisilenko
 *
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.it_result.ca;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.X509Certificate;

import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.testng.annotations.Test;

/**
 * @author roman
 *
 */
public abstract class CAClientTest {

	protected static final String SUBJECT_NAME = "CN=test,UID=test@test";

	@Test
	public void testDestroy() throws CAException {
		// Given an initialized CAClient
		client().initialize(ca().getCACertificate());
		// When destroy is invoked
		client().destroy();
		// Then CA certificate, client certificates and CRLs are destroyed
		assertFalse(client().isInitialized());
		// TODO: crl support?
	}
	
	@Test
	public void testInitialize() throws Exception {
		// Given an uninitialized CAClient
		assertFalse(client().isInitialized());
		// When client is initialized
		client().initialize(ca().getCACertificate());
		// Then client becomes initialized
		assertTrue(client().isInitialized());
		// And CA certificate is stored in the client keystore
		assertEquals(ca().getCACertificate(), client().getCaCertificate());
		// TODO: CRL impl
	}
	
	@Test 
	public void testInitializeInitialized() throws Exception {
		// Given an uninitialized client 
		assertFalse(client().isInitialized());
		// When client is initialized
		client().initialize(ca().getCACertificate());
		// Then attempt to initialize it once more time should fail
		try {
			client().initialize(ca().getCACertificate());
			fail("AlreadyInitializedException expected");
		} catch (AlreadyInitializedException e) {}
	}
	
	@Test
	public void testGetCaCertificateUninitialized() throws Exception {
		// Given an unitialized client
		assertFalse(client().isInitialized());
		// When attempt is made to retrieve CA certificate
		// Then CANotInitializedException should be thrown
		try {
			client().getCaCertificate();
			fail("CANotInitializedException expected");
		} catch (CANotInitializedException e) {}
	}

	@Test
	public void testGenerateCSRForExistingKeypair() throws DuplicateSubjectException, CAException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException {
		// Given CSR is generated for 'CN=test,UID=test@test' subject name
		byte[] csr = client().generateCSR(SUBJECT_NAME);
		KeyPair keypair = client().getKeypair(SUBJECT_NAME);
		X509Certificate certificate = client().getCertificate(SUBJECT_NAME);
		// When generateCSR('CN=test,UID=test@test') is invoked
		byte[] newCsr = client().generateCSR(SUBJECT_NAME);
		// Then a new CSR is generated for the subject name using the keypair generated earlier
		PKCS10CertificationRequest parsedCsr = new PKCS10CertificationRequest(csr);
		PKCS10CertificationRequest newParsedCsr = new PKCS10CertificationRequest(newCsr);
		assertEquals(parsedCsr.getCertificationRequestInfo().getSubject(), newParsedCsr.getCertificationRequestInfo().getSubject());
		assertEquals(keypair.getPublic(), parsedCsr.getPublicKey());
		// And keypair is not modified
		KeyPair newKeypair = client().getKeypair(SUBJECT_NAME);
		assertEquals(keypair.getPrivate(), newKeypair.getPrivate());
		assertEquals(keypair.getPublic(), newKeypair.getPublic());
		// And a self-signed certificate is not modified
		X509Certificate newCertificate = client().getCertificate(SUBJECT_NAME);
		assertEquals(certificate, newCertificate);
	}
	
	@Test
	public void testGenerateCSRForSignedCertificate() throws DuplicateSubjectException, CAException {
		// Given certificate is signed for 'CN=test,UID=test@test' subject name
		byte[] csr = client().generateCSR(SUBJECT_NAME);
		X509Certificate cert = ca().signCertificate(csr, false);
		client().initialize(ca().getCACertificate());
		client().storeCertificate(cert);
		// When generateCSR('CN=test,UID=test@test') is invoked
		try {
			client().generateCSR(SUBJECT_NAME);
			fail("DuplicateSubjectException expected");
		} catch (DuplicateSubjectException e) {
			// Then DuplicateSubjectException is thrown
		}
	}
	
	@Test
	public void testStorecertificate() throws DuplicateSubjectException, CAException {
		// Given an initialized client
		client().initialize(ca().getCACertificate());
		// When certificate signed
		byte[] csr = client().generateCSR(SUBJECT_NAME);
		X509Certificate certificate = ca().signCertificate(csr, false);
		// Then it should be possible to store the certificate
		client().storeCertificate(certificate);
	}
	
	@Test
	public void testStoreCertificateCANotInitialized() throws CANotInitializedException, DuplicateSubjectException, CAException {
		// Given an uninitialized client
		assertFalse(client().isInitialized());
		// When a signed certificate stored
		byte[] csr = client().generateCSR(SUBJECT_NAME);
		X509Certificate cert = ca().signCertificate(csr, false);
		try {
			client().storeCertificate(cert);
			fail("CANotInitializedException expected");
		} catch (CANotInitializedException e) {
			// Then CANotInitializedException is thrown
		}
	}
	
	@Test
	public void testStoreCertificateInvalidCertificateKey() throws DuplicateSubjectException, CAException {
		byte[] wrongCsr = client().generateCSR(SUBJECT_NAME);
		client().destroy();
		// Given a CSR is generated for 'CN=test,UID=test@test' subject name 
		client().generateCSR(SUBJECT_NAME);
		// And client is initialized
		client().initialize(ca().getCACertificate());
		// When a signed certificate for the same subject name, but with different public key is stored
		X509Certificate wrongCert = ca().signCertificate(wrongCsr, false);
		try {
			client().storeCertificate(wrongCert);
			fail("InvalidCertificateKeyException expected");
		} catch (InvalidCertificateKeyException e) {
			// Then InvalidCertificateKeyException is thrown			
		}
	}
	
	@Test
	public void testStoreCertificateInvalidCA() throws AlreadyInitializedException, CANotInitializedException, CAException {
		// Given a CSR is generated for 'CN=test,UID=test@test' subject name
		byte[] csr = client().generateCSR(SUBJECT_NAME);
		// And client is initialized with CA certificate
		client().initialize(ca().getCACertificate());
		// When a certificate, signed by a different CA is stored
		ca().destroy();
		ca().initialize();
		X509Certificate wrongCertificate = ca().signCertificate(csr, false);
		try {
			client().storeCertificate(wrongCertificate);
			fail("InvalidCAException expected");
		} catch (InvalidCAException e) {
			// Then InvalidCAException is thrown
		}
	}
	
	protected abstract CAClient client();
	protected abstract CA ca();
			
}