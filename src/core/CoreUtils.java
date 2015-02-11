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
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with component-objects-system. If not, see <http://www.gnu.org/licenses/>.
*/

package core;

import java.util.HashSet;
import java.util.Set;

public final class CoreUtils {
	
	public static <T> Set<T> union(Set<? extends T> s1, Set<? extends T> s2) {
		
		Set<T> output = new HashSet<T>();
		output.addAll(s1);
		output.addAll(s2);		
		
		return output;
	}
		
}
