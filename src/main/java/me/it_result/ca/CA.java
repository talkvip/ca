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

import java.security.cert.X509Certificate;
import java.util.Set;

/**
 * Represents a CA. CA should be initialized before the first use.
 * 
 * @author roman
 */
public interface CA {
	
	/**
	 * Cleans up the keystores and CA keypair/certificate
	 */
	public void destroy();
	
	/**
	 * Initialized CA has a CA keypair/certificate generated.
	 * 
	 * @return initialization status
	 */
	public boolean isInitialized();
	
	/**
	 * Generates a CA keypairs and certificates on an uninitialized CA.
	 * 
	 * @throws AlreadyInitializedException in case the CA is initialized already
	 * @throws CAException
	 */
	public void initialize() throws AlreadyInitializedException, CAException;
	
	/**
	 * Lists certificates signed by this CA
	 * 
	 * @return a Set of certificates signed by this CA
	 * 
	 * @throws CAException
	 */
	public Set<X509Certificate> listCertificates() throws CAException;
	
	/**
	 * Sings a certificate request.
	 * 
	 * @param csrBytes DER-encoded PKCS#10 CSR
	 * @param server generate a server-side certificate. A server-side 
	 * certificate is distinguished via Extended Key Usage extension. 
	 * 
	 * @return a signed certificate
	 * 
	 * @throws CANotInitializedException in case CA is not initialized yet
	 * @throws DuplicateSubjectException in case the certificate with the same
	 * subject name is signed already
	 * @throws CAException
	 */
	public X509Certificate signCertificate(byte[] csrBytes, boolean server) throws CANotInitializedException, DuplicateSubjectException, CAException;

	/**
	 * Provides an access to the CA certificate
	 * 
	 * @return the CA certificate
	 * 
	 * @throws CANotInitializedException in case CA is not initialized yet
	 * @throws CAException
	 */
	public X509Certificate getCACertificate() throws CANotInitializedException, CAException;

}