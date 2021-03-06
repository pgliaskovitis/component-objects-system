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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import attributes.GenericCompAttribute;

public class GenericCompAttributeImpl implements GenericCompAttribute {

	private List<Number> numberSet = new ArrayList<Number>();
	private List<String> stringSet = new ArrayList<String>();
	private List<GenericCompAttribute> wrappedSet = new ArrayList<GenericCompAttribute>();

	@Override
	public List<Number> getNumberList() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setNumberList(List<Number> numberList) {
		// TODO Auto-generated method stub

	}
	@Override
	public List<String> getStringList() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setStringList(List<String> stringList) {
		// TODO Auto-generated method stub

	}
	@Override
	public List<GenericCompAttribute> getWrappedList() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setWrappedList(List<GenericCompAttribute> attributeSet) {
		// TODO Auto-generated method stub

	}
}
