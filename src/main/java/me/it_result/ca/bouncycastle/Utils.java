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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.util.encoders.Hex;

/**
 * @author roman
 *
 */
public class Utils {

	/**
	 * TODO: Choose an appropriate attribute OID for profile ID
	 */
	private static final DERObjectIdentifier PROFILE_ID_ATTR = PKCSObjectIdentifiers.pkcs_9_at_contentType;

	private Utils() {}
	
	public static String generateAlias(X509Name name) {
		return name.toString(false, X509Name.DefaultSymbols);
	}
	
	public static String generateAlias(X500Principal name) {
		return new X509Name(name.getName()).toString(true, X509Name.DefaultSymbols);
	}
	
	public static String generateAlias(String name) {
		return generateAlias(new X509Name(name));
	}
	
	public static String extractProfileId(ASN1Set csrAttributes, String defaultProfileId) {
		String profileId = null;
		try {
			Enumeration<?> attrEnum = csrAttributes.getObjects();
			while (attrEnum.hasMoreElements()) {
				DERSequence attr = (DERSequence) attrEnum.nextElement();
				if (attr.getObjectAt(0).equals(PROFILE_ID_ATTR)) {
					ASN1Set profileIdSet = (ASN1Set) attr.getObjectAt(1);
					DERPrintableString profileIdValue = (DERPrintableString) profileIdSet.getObjectAt(0);
					profileId = profileIdValue.getString();
					break;
				}
			}
		} catch (Exception e) {}
		if (profileId == null)
			profileId = defaultProfileId;
		return profileId;
	}

	public static Attribute generateProfileIdAttribute(String profileId) {
		return new Attribute(PROFILE_ID_ATTR, new DERSet(new ASN1Encodable[] {new DERPrintableString(profileId)}));
	}
	
	public static Attribute generateChallengePasswordAttribute(String challengePassword) {
		ASN1EncodableVector passwordVector = new ASN1EncodableVector();
		passwordVector.add(new DERPrintableString(challengePassword));
		Attribute passwordAttribute = new Attribute(PKCSObjectIdentifiers.pkcs_9_at_challengePassword, new DERSet(passwordVector));
		return passwordAttribute;
	}

	public static String extractChallengePassword(ASN1Set csrAttributes) {
		String challengePassword = null;
		try {
			Enumeration<?> attrEnum = csrAttributes.getObjects();
			while (attrEnum.hasMoreElements()) {
				DERSequence attr = (DERSequence) attrEnum.nextElement();
				if (attr.getObjectAt(0).equals(PKCSObjectIdentifiers.pkcs_9_at_challengePassword)) {
					ASN1Set passwordSet = (ASN1Set) attr.getObjectAt(1);
					DERPrintableString passwordValue = (DERPrintableString) passwordSet.getObjectAt(0);
					challengePassword = passwordValue.getString();
					break;
				}
			}
		} catch (Exception e) {}
		return challengePassword;
	}

	public static String sha1(byte[] data) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA1");
		byte[] sha1 = digest.digest(data);
		byte[] encodedSha1 = Hex.encode(sha1);
		return new String(encodedSha1);
	}
	
}
