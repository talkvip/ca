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
package me.it_result.ca.bouncycastle;

import java.security.KeyPair;

import me.it_result.ca.CertificateParameters;

import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.x509.X509V3CertificateGenerator;

/**
 * @author roman
 *
 */
public interface Profile {
	
	public PKCS10CertificationRequest generateCsr(KeyPair keyPair, CertificateParameters certificateParameters, String signatureAlgorithm) throws Exception;
	public void generateCertificateExtensions(ASN1Set csrAttributes, X509V3CertificateGenerator certificateGenerator);
	public boolean isCompatible(ASN1Set csrAttributes);
	public boolean isCompatible(CertificateParameters certificateParameters);

}
