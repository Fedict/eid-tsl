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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class TSLNamespacePrefixMapper extends NamespacePrefixMapper {

	private static final Log LOG = LogFactory
			.getLog(TSLNamespacePrefixMapper.class);

	private static final Map<String, String> prefixes = new HashMap<String, String>();

	static {
		prefixes.put("http://uri.etsi.org/02231/v2#", "tsl");
		prefixes.put("http://www.w3.org/2000/09/xmldsig#", "ds");
		prefixes
				.put(
						"http://uri.etsi.org/TrstSvc/SvcInfoExt/eSigDir-1999-93-EC-TrustedList/#",
						"ecc");
	}

	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion,
			boolean requirePrefix) {
		LOG.debug("get preferred prefix: " + namespaceUri);
		LOG.debug("suggestion: " + suggestion);
		String prefix = prefixes.get(namespaceUri);
		if (null != prefix) {
			return prefix;
		}
		return suggestion;
	}
}
