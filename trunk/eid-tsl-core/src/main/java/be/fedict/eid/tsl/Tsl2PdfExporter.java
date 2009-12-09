/*
 * eID TSL Project.
 * Copyright (C) 2009 FedICT.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */

package be.fedict.eid.tsl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.openssl.PEMWriter;
import org.etsi.uri._02231.v2_.PostalAddressType;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class Tsl2PdfExporter {

	public Tsl2PdfExporter() {
	}

	private static final int BORDER = 0;
	protected final Font title0Font = FontFactory.getFont(
			FontFactory.TIMES_BOLD, 30, Font.BOLD);
	protected final Font title1Font = FontFactory.getFont(
			FontFactory.TIMES_BOLD, 18, Font.BOLD);
	protected final Font title2Font = FontFactory.getFont(
			FontFactory.TIMES_BOLDITALIC, 16, Font.BOLD | Font.ITALIC);
	protected final Font title3Font = FontFactory.getFont(
			FontFactory.TIMES_ITALIC, 16, Font.ITALIC);
	protected final Font labelFont = FontFactory.getFont(
			FontFactory.TIMES_ITALIC, 11, Font.ITALIC);
	protected final Font valueFont = FontFactory.getFont(FontFactory.TIMES, 11,
			Font.NORMAL);
	protected final Font monoFont = FontFactory.getFont(FontFactory.COURIER, 5,
			Font.NORMAL);
	protected final Font headerFooterFont = FontFactory.getFont(
			FontFactory.TIMES, 10, Font.NORMAL);

	/**
	 * Produce a human readable export of the given tsl to the given file.
	 * 
	 * @param tsl
	 *            the TrustServiceList to export
	 * @param pdfFile
	 *            the file to generate
	 * @return
	 * @throws IOException
	 */
	public void humanReadableExport(final TrustServiceList tsl,
			final File pdfFile) {
		com.lowagie.text.Document document = new com.lowagie.text.Document();
		OutputStream outputStream;
		try {
			outputStream = new FileOutputStream(pdfFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("file not found: "
					+ pdfFile.getAbsolutePath(), e);
		}
		try {
			final PdfWriter pdfWriter = PdfWriter.getInstance(document,
					outputStream);
			//pdfWriter.setPDFXConformance(PdfWriter.PDFA1B);

			// title
			final EUCountry country = EUCountry.valueOf(tsl
					.getSchemeTerritory());
			final String title = country.getShortSrcLangName() + " ("
					+ country.getShortEnglishName() + "): Trusted List";

			Phrase footerPhrase = new Phrase("PDF document generated on "
					+ new Date().toString() + ", page ", headerFooterFont);
			HeaderFooter footer = new HeaderFooter(footerPhrase, true);
			document.setFooter(footer);

			Phrase headerPhrase = new Phrase(title, headerFooterFont);
			HeaderFooter header = new HeaderFooter(headerPhrase, false);
			document.setHeader(header);

			document.open();
			addTitle(title, title0Font, Paragraph.ALIGN_CENTER, 0, 20, document);

			addLongItem("Scheme name", tsl.getSchemeName(), document);
			addLongItem("Legal Notice", tsl.getLegalNotice(), document);

			// information table
			PdfPTable informationTable = createInfoTable();
			addItemRow("Scheme territory", tsl.getSchemeTerritory(),
					informationTable);
			addItemRow("Scheme status determination approach", substringAfter(
					tsl.getStatusDeterminationApproach(), "StatusDetn/"),
					informationTable);

			final List<String> schemeTypes = new ArrayList<String>();
			for (final String schemeType : tsl.getSchemeTypes()) {
				schemeTypes.add(schemeType.substring(schemeType
						.indexOf("schemerules/")
						+ "schemerules/".length()));
			}
			addItemRow("Scheme type community rules", schemeTypes,
					informationTable);

			addItemRow("Issue date", tsl.getListIssueDateTime().toString(),
					informationTable);
			addItemRow("Next update", tsl.getNextUpdate().toString(),
					informationTable);
			addItemRow("Historical information period", tsl
					.getHistoricalInformationPeriod().toString()
					+ " days", informationTable);
			addItemRow("Sequence number", tsl.getSequenceNumber().toString(),
					informationTable);
			addItemRow("Scheme information URIs", tsl
					.getSchemeInformationUris(), informationTable);

			document.add(informationTable);

			addTitle("Scheme Operator", title1Font, Paragraph.ALIGN_CENTER, 0,
					10, document);

			informationTable = createInfoTable();
			addItemRow("Scheme operator name", tsl.getSchemeOperatorName(),
					informationTable);
			PostalAddressType schemeOperatorPostalAddress = tsl
					.getSchemeOperatorPostalAddress(Locale.ENGLISH);
			addItemRow("Scheme operator street address",
					schemeOperatorPostalAddress.getStreetAddress(),
					informationTable);
			addItemRow("Scheme operator postal code",
					schemeOperatorPostalAddress.getPostalCode(),
					informationTable);
			addItemRow("Scheme operator locality", schemeOperatorPostalAddress
					.getLocality(), informationTable);
			addItemRow("Scheme operator state", schemeOperatorPostalAddress
					.getStateOrProvince(), informationTable);
			addItemRow("Scheme operator country", schemeOperatorPostalAddress
					.getCountryName(), informationTable);

			List<String> schemeOperatorElectronicAddressess = tsl
					.getSchemeOperatorElectronicAddresses();
			addItemRow("Scheme operator contact",
					schemeOperatorElectronicAddressess, informationTable);
			document.add(informationTable);

			addTitle("Trust Service Providers", title1Font,
					Paragraph.ALIGN_CENTER, 10, 2, document);

			List<TrustServiceProvider> trustServiceProviders = tsl
					.getTrustServiceProviders();
			for (TrustServiceProvider trustServiceProvider : trustServiceProviders) {
				addTitle(trustServiceProvider.getName(), title1Font,
						Paragraph.ALIGN_LEFT, 10, 2, document);

				PdfPTable providerTable = createInfoTable();
				addItemRow("Service provider trade name", trustServiceProvider
						.getTradeName(), providerTable);
				addItemRow("Information URI", trustServiceProvider
						.getInformationUri(), providerTable);
				PostalAddressType postalAddress = trustServiceProvider
						.getPostalAddress();
				addItemRow("Service provider street address", postalAddress
						.getStreetAddress(), providerTable);
				addItemRow("Service provider postal code", postalAddress
						.getPostalCode(), providerTable);
				addItemRow("Service provider locality", postalAddress
						.getLocality(), providerTable);
				addItemRow("Service provider state", postalAddress
						.getStateOrProvince(), providerTable);
				addItemRow("Service provider country", postalAddress
						.getCountryName(), providerTable);
				document.add(providerTable);

				List<TrustService> trustServices = trustServiceProvider
						.getTrustServices();
				for (TrustService trustService : trustServices) {
					addTitle(trustService.getName(), title2Font,
							Paragraph.ALIGN_LEFT, 10, 2, document);
					PdfPTable serviceTable = createInfoTable();
					addItemRow("Type", substringAfter(trustService.getType(),
							"Svctype/"), serviceTable);
					addItemRow("Status", substringAfter(trustService
							.getStatus(), "Svcstatus/"), serviceTable);
					addItemRow("Status starting time", trustService
							.getStatusStartingTime().toString(), serviceTable);
					document.add(serviceTable);

					addTitle("Service digital identity (X509)", title3Font,
							Paragraph.ALIGN_LEFT, 2, 0, document);
					final X509Certificate certificate = trustService
							.getServiceDigitalIdentity();
					final PdfPTable serviceIdentityTable = createInfoTable();
					addItemRow("Version", Integer.toString(certificate
							.getVersion()), serviceIdentityTable);
					addItemRow("Serial number", certificate.getSerialNumber()
							.toString(), serviceIdentityTable);
					addItemRow("Signature algorithm", certificate
							.getSigAlgName(), serviceIdentityTable);
					addItemRow("Issuer", certificate.getIssuerX500Principal()
							.toString(), serviceIdentityTable);
					addItemRow("Valid from", certificate.getNotBefore()
							.toString(), serviceIdentityTable);
					addItemRow("Valid to",
							certificate.getNotAfter().toString(),
							serviceIdentityTable);
					addItemRow("Subject", certificate.getSubjectX500Principal()
							.toString(), serviceIdentityTable);
					addItemRow("Public key", certificate.getPublicKey()
							.toString(), serviceIdentityTable);
					// TODO certificate policies
					addItemRow("Subject key identifier",
							toHex(getSKId(certificate)), serviceIdentityTable);
					addItemRow("CRL distribution points",
							getCrlDistributionPoints(certificate),
							serviceIdentityTable);
					addItemRow("Authority key identifier",
							toHex(getAKId(certificate)), serviceIdentityTable);
					addItemRow("Key usage", getKeyUsage(certificate),
							serviceIdentityTable);
					addItemRow("Basic constraints",
							getBasicConstraints(certificate),
							serviceIdentityTable);

					byte[] encodedCertificate;
					try {
						encodedCertificate = certificate.getEncoded();
					} catch (CertificateEncodingException e) {
						throw new RuntimeException("cert: " + e.getMessage(), e);
					}
					addItemRow("SHA1 Thumbprint", DigestUtils
							.shaHex(encodedCertificate), serviceIdentityTable);
					addItemRow("SHA256 Thumbprint", DigestUtils
							.sha256Hex(encodedCertificate),
							serviceIdentityTable);
					document.add(serviceIdentityTable);

					addLongMonoItem("The decoded certificate:", certificate
							.toString(), document);
					addLongMonoItem("The certificate in PEM format:",
							toPem(certificate), document);
				}
			}

			X509Certificate signerCertificate = tsl.verifySignature();
			if (null != signerCertificate) {
				Paragraph tslSignerTitle = new Paragraph("TSL Signer",
						new Font(Font.HELVETICA, 18, Font.BOLDITALIC));
				tslSignerTitle.setAlignment(Paragraph.ALIGN_CENTER);
				document.add(tslSignerTitle);

				PdfPTable signerTable = new PdfPTable(2);
				signerTable.getDefaultCell().setBorder(BORDER);
				signerTable.addCell("Subject");
				signerTable.addCell(signerCertificate.getSubjectX500Principal()
						.toString());
				signerTable.addCell("Issuer");
				signerTable.addCell(signerCertificate.getIssuerX500Principal()
						.toString());
				signerTable.addCell("Not before");
				signerTable
						.addCell(signerCertificate.getNotBefore().toString());
				signerTable.addCell("Not after");
				signerTable.addCell(signerCertificate.getNotAfter().toString());
				signerTable.addCell("Serial number");
				signerTable.addCell(signerCertificate.getSerialNumber()
						.toString());
				signerTable.addCell("Version");
				signerTable.addCell(Integer.toString(signerCertificate
						.getVersion()));
				byte[] encodedPublicKey = signerCertificate.getPublicKey()
						.getEncoded();
				signerTable.addCell("Public key SHA1 Thumbprint");
				String thumbprint = DigestUtils.shaHex(encodedPublicKey);
				signerTable.addCell(thumbprint);
				signerTable.addCell("Public key SHA256 Thumbprint");
				String sha256thumbprint = DigestUtils
						.sha256Hex(encodedPublicKey);
				signerTable.addCell(sha256thumbprint);
				document.add(signerTable);

				document.add(new Paragraph("The decoded certificate:"));
				Paragraph certParagraph = new Paragraph(signerCertificate
						.toString(), new Font(Font.COURIER, 8, Font.NORMAL));
				// certParagraph.setAlignment(Paragraph.ALIGN_CENTER);
				document.add(certParagraph);

				document.add(new Paragraph("The certificate in PEM format:"));
				Paragraph pemParagraph = new Paragraph(
						toPem(signerCertificate), new Font(Font.COURIER, 8,
								Font.NORMAL));
				pemParagraph.setAlignment(Paragraph.ALIGN_CENTER);
				document.add(pemParagraph);

				document.add(new Paragraph("The public key in PEM format:"));
				Paragraph publicKeyPemParagraph = new Paragraph(
						toPem(signerCertificate.getPublicKey()), new Font(
								Font.COURIER, 8, Font.NORMAL));
				publicKeyPemParagraph.setAlignment(Paragraph.ALIGN_CENTER);
				document.add(publicKeyPemParagraph);
			}

			document.close();
		} catch (DocumentException e) {
			throw new RuntimeException("PDF document error: " + e.getMessage(),
					e);
		} catch (Exception e) {
			throw new RuntimeException("Exception: " + e.getMessage(), e);
		}
	}

	private String toPem(Object object) {
		StringWriter buffer = new StringWriter();
		try {
			PEMWriter writer = new PEMWriter(buffer);
			writer.writeObject(object);
			writer.close();
			return buffer.toString();
		} catch (Exception e) {
			throw new RuntimeException(
					"Cannot convert public key to PEM format: "
							+ e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(buffer);
		}
	}

	private byte[] getSKId(final X509Certificate cert) throws IOException {
		final byte[] extValue = cert
				.getExtensionValue(X509Extensions.SubjectKeyIdentifier.getId());
		if (extValue != null) {
			final ASN1OctetString str = ASN1OctetString
					.getInstance(new ASN1InputStream(new ByteArrayInputStream(
							extValue)).readObject());
			final SubjectKeyIdentifier keyId = SubjectKeyIdentifier
					.getInstance(new ASN1InputStream(new ByteArrayInputStream(
							str.getOctets())).readObject());
			return keyId.getKeyIdentifier();
		} else {
			return null;
		}
	}

	private byte[] getAKId(final X509Certificate cert) throws IOException {
		final byte[] extValue = cert
				.getExtensionValue(X509Extensions.AuthorityKeyIdentifier
						.getId());
		if (extValue != null) {
			final DEROctetString oct = (DEROctetString) (new ASN1InputStream(
					new ByteArrayInputStream(extValue)).readObject());
			final AuthorityKeyIdentifier keyId = new AuthorityKeyIdentifier(
					(ASN1Sequence) new ASN1InputStream(
							new ByteArrayInputStream(oct.getOctets()))
							.readObject());
			return keyId.getKeyIdentifier();
		} else {
			return null;
		}
	}

	private static String getBasicConstraints(final X509Certificate cert) {
		final int x = cert.getBasicConstraints();
		return (x < 0) ? "CA=false"
				: ("CA=true; PathLen=" + ((x == Integer.MAX_VALUE) ? "unlimited"
						: String.valueOf(x)));
	}

	private static List<String> getCrlDistributionPoints(
			final X509Certificate cert) throws IOException {
		final byte[] extValue = cert
				.getExtensionValue(X509Extensions.CRLDistributionPoints.getId());
		if (extValue != null) {
			final ASN1InputStream oAsnInStream = new ASN1InputStream(
					new ByteArrayInputStream(extValue));
			final DERObject derObj = oAsnInStream.readObject();
			final DEROctetString dos = (DEROctetString) derObj;
			final byte[] val2 = dos.getOctets();
			final ASN1InputStream oAsnInStream2 = new ASN1InputStream(
					new ByteArrayInputStream(val2));
			final DERObject derObj2 = oAsnInStream2.readObject();
			return getDERValue(derObj2);
		} else {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	private static List<String> getDERValue(final DERObject derObj) {
		if (derObj instanceof DERSequence) {
			final List<String> ret = new LinkedList<String>();
			final DERSequence seq = (DERSequence) derObj;
			final Enumeration<DERObject> enum1 = seq.getObjects();
			while (enum1.hasMoreElements()) {
				final DERObject nestedObj = (DERObject) enum1.nextElement();
				final List<String> appo = getDERValue(nestedObj);
				if (appo != null) {
					ret.addAll(appo);
				}
			}
			return ret;
		}

		if (derObj instanceof DERTaggedObject) {
			final DERTaggedObject derTag = (DERTaggedObject) derObj;
			if (derTag.isExplicit() && !derTag.isEmpty()) {
				final DERObject nestedObj = derTag.getObject();
				return getDERValue(nestedObj);
			} else {
				final DEROctetString derOct = (DEROctetString) derTag
						.getObject();
				final String val = new String(derOct.getOctets());
				return Collections.singletonList(val);
			}
		}

		return null;
	}

	private static final String[] keyUsageLabels = new String[] {
			"digitalSignature", "nonRepudiation", "keyEncipherment",
			"dataEncipherment", "keyAgreement", "keyCertSign", "cRLSign",
			"encipherOnly", "decipherOnly" };

	private static List<String> getKeyUsage(final X509Certificate cert) {
		final boolean[] keyUsage = cert.getKeyUsage();
		if (keyUsage != null) {
			final List<String> ret = new LinkedList<String>();
			for (int i = 0; i < keyUsage.length; ++i) {
				if (keyUsage[i]) {
					if (i < keyUsageLabels.length) {
						ret.add(keyUsageLabels[i]);
					} else {
						ret.add(String.valueOf(i));
					}
				}
			}
			return ret;
		} else {
			return null;
		}
	}

	protected PdfPTable createInfoTable() {
		final float alpha = 0.22f;
		final PdfPTable t = new PdfPTable(new float[] { alpha, 1.0f - alpha });
		t.getDefaultCell().setBorder(BORDER);
		t.setWidthPercentage(101f);
		return t;
	}

	protected void addItemRow(final String label, final String value,
			final PdfPTable table) {
		if (value != null) {
			table.addCell(new Phrase(label, labelFont));
			table.addCell(new Phrase(value, valueFont));
		}
	}

	protected void addItemRow(final String label,
			final Iterable<String> values, final PdfPTable table) {
		if (values != null) {
			boolean nonEmpty = false;
			final PdfPCell valueCell = new PdfPCell();
			valueCell.setBorder(0);
			for (String s : values) {
				valueCell.addElement(new Paragraph(s, valueFont));
				nonEmpty = true;
			}
			if (nonEmpty) {
				table.addCell(new Phrase(label, labelFont));
				table.addCell(valueCell);
			}
		}
	}

	protected void addLongItem(final String label, final String value,
			final Document doc) throws DocumentException {
		doc.add(new Paragraph(label, labelFont));
		doc.add(new Paragraph(value, valueFont));
	}

	protected void addLongMonoItem(final String label, final String value,
			final Document doc) throws DocumentException {
		doc.add(new Paragraph(label, labelFont));
		doc.add(new Paragraph(value, monoFont));
	}

	protected void addTitle(final String titleText, final Font titleFont,
			final int align, final float spacingBefore,
			final float spacingAfter, final Document doc)
			throws DocumentException {
		final Paragraph titlePara = new Paragraph(titleText, titleFont);
		titlePara.setAlignment(align);
		titlePara.setSpacingBefore(spacingBefore);
		titlePara.setSpacingAfter(spacingAfter);
		doc.add(titlePara);
	}

	protected static String substringAfter(final String mainString,
			final String substring) {
		return mainString.substring(mainString.indexOf(substring)
				+ substring.length());
	}

	protected static String toHex(final byte[] value) {
		return (value != null) ? Hex.encodeHexString(value) : null;
	}
}
