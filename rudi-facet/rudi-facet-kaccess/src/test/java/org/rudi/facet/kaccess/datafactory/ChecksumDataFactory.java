package org.rudi.facet.kaccess.datafactory;

import org.rudi.facet.kaccess.bean.HashAlgorithm;
import org.rudi.facet.kaccess.bean.MediaFileAllOfChecksum;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer des objets Checksum.
 * Un Checksum représente l'intégrité d'un fichier (algorithm + hash).
 */
@Component
@RequiredArgsConstructor
public class ChecksumDataFactory {

	/**
	 * Crée un checksum MD5.
	 */
	public MediaFileAllOfChecksum createChecksumMd5() {
		return createChecksum(HashAlgorithm.MD5, "AZZ");
	}

	/**
	 * Crée un checksum SHA-256.
	 */
	public MediaFileAllOfChecksum createChecksumSha256() {
		return createChecksum(HashAlgorithm.SHA_256, "RAI");
	}

	private MediaFileAllOfChecksum createChecksum(HashAlgorithm algo, String hash) {
		MediaFileAllOfChecksum checksum = new MediaFileAllOfChecksum();
		checksum.setAlgo(algo);
		checksum.setHash(hash);
		return checksum;
	}

}
