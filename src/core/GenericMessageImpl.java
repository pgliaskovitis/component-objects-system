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

import componentInterfaces.Hash;

import messages.GenericMessage;
import messages.MessagesEnum;

public final class GenericMessageImpl implements GenericMessage {

	private final MessagesEnum.MessageTypes mType; 
	private final Object mData; // this is a single affected entityId or an info object depending on the message type
		
	public GenericMessageImpl(MessagesEnum.MessageTypes type, Object data) {
		mType = type;
		mData = data;
	}
	
	public MessagesEnum.MessageTypes getType() {
		return mType;
	}

	@Override
	public Object getData() {
		return mData;
	}
	
}
