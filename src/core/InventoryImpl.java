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
import org.w3c.dom.Node;

import messages.GenericMessage;
import messages.GenericMessageImpl;
import messages.MessagesEnum;

import componentInterfaces.Description;
import componentInterfaces.Entity;
import componentInterfaces.GenericComponent;
import componentInterfaces.Hash;
import componentInterfaces.InterfacesEnum;
import componentInterfaces.Inventory;

public final class InventoryImpl extends GenericComponentImpl implements Inventory {

	private Set<Hash> mInventorySet = new HashSet<Hash>();

	// the static methods must be implemented by every component for component type initialization purposes
	// or else, perhaps, Java should support static methods in interfaces

	public static void registerInterfaces() {
		globalsManager.getComponentManager().registerComponentInterface(InterfacesEnum.InventoryInterface);
	}

	public static void registerImplementationClass() {
		globalsManager.getComponentManager().registerComponentImplInfo(InterfacesEnum.ComponentTypes.Inventory, core.InventoryImpl.class);
	}

	public static void subscribeToMessageTypes() {
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.InventoryInterface, MessagesEnum.MessageTypes.MT_PICK_UP);
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.InventoryInterface, MessagesEnum.MessageTypes.MT_EXAMINE_INVENTORY);
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.InventoryInterface, MessagesEnum.MessageTypes.MT_SET_INVENTORY_ITEM_POS);
	}
	// end of static initializations

	public InventoryImpl(Element generatorElement) {
		super();
	}

	@Override
	public Set<Class<? extends GenericComponent>> getInterfaces() {

		Set<Class<? extends GenericComponent>> output = new HashSet<Class<? extends GenericComponent>>();
		output.add(InterfacesEnum.InventoryInterface);
		return output;
	}

	@Override
	public void init(Hash entityId, Node generatorNode) {
		super.init(entityId, generatorNode);
	}

	@Override
	public void deInit() {
		mInventorySet.clear();
		mInventorySet = null;
	}

	@Override
	public MessagesEnum.MessageResults handleMessage(final GenericMessage messageWrapper) {

		switch (messageWrapper.getType()) {

			case MT_PICK_UP: {
			// An object is telling us it wants to be picked up
				Hash pickupObject = (Hash)(messageWrapper.getData());
				if (insertItem(pickupObject)) {
				// Successfully picked up the object
					Description pPickupDescr = (Description)globalsManager.getComponentManager().queryEntityForInterface(pickupObject, InterfacesEnum.DescriptionInterface);
					if (pPickupDescr != null) {
					   globalsManager.print("You picked up the " + pPickupDescr.getShortDescr() + ".");
					} else {
						globalsManager.print("You picked up the object.");
					}

					// Tell the object it has been picked up. Use a message so that any component in the picked up object gets a chance
					// to perform an action when this happens.
					globalsManager.getComponentManager().postMessage(pickupObject, GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_PICK_UP_SUCCESSFUL, getEntityId()));
					return MessagesEnum.MessageResults.MR_TRUE;
				}

				return MessagesEnum.MessageResults.MR_FALSE;
			}

			case MT_EXAMINE_INVENTORY: {
			// Print our inventory to the screen.
				examineInventory();
				return MessagesEnum.MessageResults.MR_TRUE;
			}

			case MT_SET_INVENTORY_ITEM_POS: {
			// Set the positions of all the items in the inventory (to make them be at the same place as this object)
				Hash newPos = (Hash)(messageWrapper.getData());
				if (newPos.isValid()) {
					setItemPositions(newPos);
				}
				return MessagesEnum.MessageResults.MR_TRUE;
			}

		}

		return MessagesEnum.MessageResults.MR_IGNORED;

	}

	@Override
	public int getNumItems() {
		return mInventorySet.size();
	}

	@Override
	public Hash getItem(int index) {

		Hash itemId = null;

		// Is index valid?
		if ((index >= 0) && (index < getNumItems())) {
			int i = 0;
			for (Hash itemHash: mInventorySet) {
				i++;
				if (i == index) {
					itemId = itemHash;
					break;
				}
			}
		}

		return itemId;
	}

	@Override
	public boolean isItemInInventory(Hash itemId) {
		return mInventorySet.contains(itemId);
	}

	@Override
	public boolean insertItem(Hash itemId) {
		mInventorySet.add(itemId);
		return true;
	}

	@Override
	public void removeItem(Hash itemId) {
		mInventorySet.remove(itemId);
	}

	@Override
	public void examineInventory() {

		if (mInventorySet.size() == 0) {
			globalsManager.print("The inventory is empty");
			return;
		}

		for (Hash itemId: mInventorySet) {
			Description pItemDescr = (Description)globalsManager.getComponentManager().queryEntityForInterface(itemId, InterfacesEnum.DescriptionInterface);
			if (pItemDescr != null) {
				globalsManager.print(pItemDescr.getShortDescr().getHashValue());
			}
		}

	}

	@Override
	public void setItemPositions(Hash newPos) {

		for (Hash itemId: mInventorySet) {
			Entity pItemEntity = (Entity)globalsManager.getComponentManager().queryEntityForInterface(itemId, InterfacesEnum.EntityInterface);
			pItemEntity.setPosition(newPos);
		}
	}
}
