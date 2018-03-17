/*
* Copyright 2012 Periklis G. Liaskovitis
*
* This file is part of component-objects-system.
*
* component-objects-system is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* component-objects-system is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY;  without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with component-objects-system. If not, see <http://www.gnu.org/licenses/>.
*/

package core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import componentInterfaces.Hash;

public final class CompHash implements Hash {

	private static final String INVALID_HASH = "InvalidHash";

	// caching the hashes because they are going to be the same most of the time
	private static final Map<String, Hash> hashCache = new ConcurrentHashMap<String, Hash>();

	private final String hashValue;

	// private constructor
	private CompHash(String hash) {
		if ((hash != null) && (!hash.equals("")) && (!hash.equals("null"))) {
			this.hashValue = hash;
		} else {
			this.hashValue = INVALID_HASH;
		}
	}

	// static factory method based on the private constructor
	public static Hash getHashForName(String name) {

		Hash result = null;

		if ((name != null) && (!name.equals("")) && (!name.equalsIgnoreCase("null"))) {
			result = hashCache.get(name);
		} else {
			result = hashCache.get("null");
			System.err.println("Null hash retrieved");
		}

		if (result == null) {
			result = new CompHash(name);
			if ((name != null) && (!name.equals("")) && (!name.equalsIgnoreCase("null"))) {
				hashCache.put(name, result);
			} else {
				hashCache.put("null", result);
				System.err.println("Null hash stored");
			}
		}

		return result;
	}

	@Override
	public String getHashValue() {
		return hashValue;
	}

	@Override
	public boolean isValid() {
		return (!hashValue.equals(INVALID_HASH));
	}

	@Override
	public int hashCode() {
		// must be consistent with lower case names
		int result = ((hashValue == null) ? 0 : hashValue.toLowerCase().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object targetHash) {
		//must be consistent with lower case names
		return equalsIgnoreCase(targetHash);
	}

	@Override
	public boolean equalsIgnoreCase(Object targetHash) {
		if (this == targetHash)
			return true;
		if (targetHash == null)
			return false;
		if (getClass() != targetHash.getClass())
			return false;
		CompHash other = (CompHash) targetHash;
		if (hashValue == null) {
			if (other.hashValue != null)
				return false;
		} else if (!hashValue.equalsIgnoreCase(other.hashValue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		// must be consistent with lower case names
		return hashValue.toLowerCase();
	}
}
