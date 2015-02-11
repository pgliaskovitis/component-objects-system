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
import messages.MessagesEnum.PickupInfo;

import componentInterfaces.Collectable;
import componentInterfaces.Description;
import componentInterfaces.GenericComponent;
import componentInterfaces.Hash;
import componentInterfaces.InterfacesEnum;

public final class CollectableImpl extends GenericComponentImpl implements Collectable {

	private Hash mHolder;
	
    // the static methods must be implemented by every component for component type initialization purposes 
    // or else, perhaps, Java should support static methods in interfaces
    public static void registerInterfaces() {
    	globalsManager.getComponentManager().registerComponentInterface(InterfacesEnum.CollectableInterface); 
    }
            
    public static void registerImplementationClass() {
    	globalsManager.getComponentManager().registerComponentImplInfo(InterfacesEnum.ComponentTypes.Collectable, core.CollectableImpl.class);
    }
    
    public static void subscribeToMessageTypes() {
    	globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.CollectableInterface, MessagesEnum.MessageTypes.MT_BE_PICKED_UP);
    	globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.CollectableInterface, MessagesEnum.MessageTypes.MT_PICK_UP_SUCCESSFUL);
    }
    // end of static initializations
    
    public CollectableImpl(Element generatorElement) {
        super();
    }
    
    @Override
    public Set<Class<? extends GenericComponent>> getInterfaces() {
        
        Set<Class<? extends GenericComponent>> output = new HashSet<Class<? extends GenericComponent>>();
        output.add(InterfacesEnum.CollectableInterface);
        return output;
    }
    
    @Override
	public void init(Hash entityId, Node generatorNode) {
		super.init(entityId, generatorNode);
	}
    
	@Override
	public MessagesEnum.MessageResults handleMessage(final GenericMessage messageWrapper) {
		
		switch (messageWrapper.getType()) {
			case MT_BE_PICKED_UP: {
				
				PickupInfo	pPickupInfo = (PickupInfo)messageWrapper.getData();
				if (getEntityComponent().isInteractionName(pPickupInfo.getCollectedObjectInteractionName())) {
				// You talkin' to me? You talkin' to me? Yes you are...
				// Can this object be picked up?
					Hash collectorId = pPickupInfo.getCollectorId();
					Description pDescr = (Description)globalsManager.getComponentManager().queryEntityForInterface(getEntityId(), InterfacesEnum.DescriptionInterface);
					if (isCollected()) {
					// It's already collected, so we won't be picking it up.
						if (getHolder().equals(collectorId)) {
							if (pDescr == null) {
								globalsManager.print("You've already got that object in your inventory.\n");
							} else {
								globalsManager.print("You've already got the " + pDescr.getShortDescr().getHashValue() + " in your inventory.\n");
							}
							return MessagesEnum.MessageResults.MR_TRUE;
						} else {
							if (pDescr == null) {
								globalsManager.print("You can't pick up that object.\n");
							} else {
								globalsManager.print("You can't pick up the " + pDescr.getShortDescr().getHashValue() + ".\n");
							}
							return MessagesEnum.MessageResults.MR_FALSE;
						}
					}
					// The object isn't collected. Can it be seen by the collector?
					if (getEntityComponent().canThisObjectBeSeenBy(collectorId, true)) {
					// Go and be picked up. There's nothing stopping us now.
						globalsManager.getComponentManager().postMessage(collectorId, GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_PICK_UP, getEntityId()));
						globalsManager.getComponentManager().postMessage(getEntityId(), GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_HIDE, getEntityId()));
						return MessagesEnum.MessageResults.MR_TRUE;
					}
					else {
						if (pDescr == null) {
							globalsManager.print("You can't see that object.\n");
						} else {
							globalsManager.print("You can't see the " + pDescr.getShortDescr().getHashValue() + "from here.\n");
						}
						return MessagesEnum.MessageResults.MR_FALSE;
					}
				}
				return MessagesEnum.MessageResults.MR_FALSE; // If we succeeded, we should have returned already
			}
			
			case MT_PICK_UP_SUCCESSFUL: { 
			// Make sure we know that we're being held (and who's doing the holding)
				mHolder = (Hash)messageWrapper.getData();
				return MessagesEnum.MessageResults.MR_TRUE;
			}
		}
		return MessagesEnum.MessageResults.MR_IGNORED;
	}
	
    @Override
    public boolean isCollected() {
    	return (mHolder != null);
	}
    
    @Override
    public Hash getHolder() {
    	return mHolder;
    }
    
}
