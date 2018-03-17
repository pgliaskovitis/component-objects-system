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

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import messages.GenericMessage;
import messages.GenericMessageImpl;
import messages.MessagesEnum;

import componentInterfaces.Description;
import componentInterfaces.Entity;
import componentInterfaces.GenericComponent;
import componentInterfaces.Hash;
import componentInterfaces.InterfacesEnum;
import componentInterfaces.Room;

public final class RoomImpl extends GenericComponentImpl implements Room {

	private Set<Hash> mConnectedRoomsSet = new HashSet<Hash>();

	// the static methods must be implemented by every component for component type initialization purposes 
	// or else, perhaps, Java should support static methods in interfaces

	public static void registerInterfaces() {
		globalsManager.getComponentManager().registerComponentInterface(InterfacesEnum.RoomInterface); 
	}

	public static void registerImplementationClass() {
		globalsManager.getComponentManager().registerComponentImplInfo(InterfacesEnum.ComponentTypes.Room, core.RoomImpl.class);
	}

	public static void subscribeToMessageTypes() {
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.RoomInterface, MessagesEnum.MessageTypes.MT_LOOK);
	}
	// end of static initializations

	public RoomImpl(Element generatorElement) {
		super();

		NodeList children = generatorElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeName().equalsIgnoreCase("hash")) {
				Node currentChild = children.item(i);
				NamedNodeMap attributes = currentChild.getAttributes();
				Node hashName = attributes.getNamedItem("name");
				if (hashName.getTextContent().startsWith("ConnectedRoom")) {
					//read in connected room
					mConnectedRoomsSet.add(CompHash.getHashForName(currentChild.getFirstChild().getNodeValue()));
				}
			}
		}
	}

	@Override
	public Set<Class<? extends GenericComponent>> getInterfaces() {

		Set<Class<? extends GenericComponent>> output = new HashSet<Class<? extends GenericComponent>>();
		output.add(InterfacesEnum.RoomInterface);
		return output;
	}

	@Override
	public void init(Hash entityId, Node generatorNode) {
		super.init(entityId, generatorNode);
	}

	@Override
	public void deInit() {
		mConnectedRoomsSet.clear();
		mConnectedRoomsSet = null;
	}

	@Override
	public MessagesEnum.MessageResults handleMessage(final GenericMessage messageWrapper) {

		switch(messageWrapper.getType()) {
			case MT_LOOK: {
				printLookDescription();
				break;
			}
		}

		return MessagesEnum.MessageResults.MR_IGNORED;

	}

	@Override
	public boolean addConnectedRoom(Hash room) {
		mConnectedRoomsSet.add(room);
		return true;
	}

	@Override
	public Hash getConnectedRoom(Hash interactionName) {

		for (Hash connectedRoom: mConnectedRoomsSet) {
			Entity pConnectedRoomEntity = (Entity)globalsManager.getComponentManager().queryEntityForInterface(connectedRoom, InterfacesEnum.EntityInterface);
			if ((pConnectedRoomEntity != null) && (pConnectedRoomEntity.isInteractionName(interactionName))) {
				return pConnectedRoomEntity.getEntityId();
			}
		}

		return CompHash.getHashForName(null);
	}

	@Override
	public boolean iRoomConnected(Hash room) {
		return mConnectedRoomsSet.contains(room);
	}

	@Override
	public void printLookDescription() {

		// Description
		Description pDescr = (Description)globalsManager.getComponentManager().queryEntityForInterface(getEntityId(), InterfacesEnum.DescriptionInterface);
		if (pDescr != null) {
			
			globalsManager.print(pDescr.getShortDescr().getHashValue());
			globalsManager.print(pDescr.getLongDescr().getHashValue());
		}

		// Characters
		globalsManager.print("Characters:");
		GenericMessage describeCharacterMsg = GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_DESCRIBE_CHARACTER, getEntityId());
		globalsManager.getComponentManager().broadcastMessage(describeCharacterMsg);

		// Items
		globalsManager.print("Items:");
		GenericMessage describeObjectMsg = GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_DESCRIBE_OBJECT, getEntityId());
		globalsManager.getComponentManager().broadcastMessage(describeObjectMsg);
		
		// Exits
		if (mConnectedRoomsSet.isEmpty()) {
			globalsManager.print("There are no exits from this room.");
		} else {
			globalsManager.print("Exits:");
			for (Hash connectedRoom: mConnectedRoomsSet) {
				Description pConnectedRoomDescr = (Description)(globalsManager.getComponentManager().queryEntityForInterface(connectedRoom, InterfacesEnum.DescriptionInterface));
				if (pConnectedRoomDescr != null) {
					globalsManager.print(pConnectedRoomDescr.getShortDescr().getHashValue());
				}
			}
		}

	}
}
