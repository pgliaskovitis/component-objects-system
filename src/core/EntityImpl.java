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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import messages.GenericMessage;
import messages.MessagesEnum;

import componentInterfaces.Collectable;
import componentInterfaces.Description;
import componentInterfaces.Entity;
import componentInterfaces.GenericComponent;
import componentInterfaces.Hash;
import componentInterfaces.InterfacesEnum;

public final class EntityImpl extends GenericComponentImpl implements Entity {

	private Hash mRoom;
	private boolean mVisible;
	private Set<Hash> mInteractionNameSet = new HashSet<Hash>();
	
    // the static methods must be implemented by every component for component type initialization purposes 
    // or else, perhaps, Java should support static methods in interfaces
    
    public static void registerInterfaces() {
		globalsManager.getComponentManager().registerComponentInterface(InterfacesEnum.EntityInterface); 
    }
            
    public static void registerImplementationClass() {
		globalsManager.getComponentManager().registerComponentImplInfo(InterfacesEnum.ComponentTypes.Entity, core.EntityImpl.class);
    }
    
    public static void subscribeToMessageTypes() {
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.EntityInterface, MessagesEnum.MessageTypes.MT_OBJECT_CREATED);
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.EntityInterface, MessagesEnum.MessageTypes.MT_HIDE);
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.EntityInterface, MessagesEnum.MessageTypes.MT_SHOW);
    }
    // end of static initializations
    
    public EntityImpl(Element generatorElement) {
        super();
        
        NodeList children = generatorElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equalsIgnoreCase("hash")) {
                Node currentChild = children.item(i);
                NamedNodeMap attributes = currentChild.getAttributes();
                Node hashName = attributes.getNamedItem("name");
                if (hashName.getTextContent().equalsIgnoreCase("Room")) {
                	//read in room
                	mRoom = CompHash.getHashForName(currentChild.getFirstChild().getNodeValue());
                } else if (hashName.getTextContent().startsWith("InteractionName")) {
                	//read in interaction name
                	mInteractionNameSet.add(CompHash.getHashForName(currentChild.getFirstChild().getNodeValue()));
                }
            } else if (children.item(i).getNodeName().equalsIgnoreCase("bool")) {
                Node currentChild = children.item(i);
                NamedNodeMap attributes = currentChild.getAttributes();
                Node hashName = attributes.getNamedItem("name");
                if (hashName.getTextContent().equalsIgnoreCase("Visible")) {
                    //read in visible
                    mVisible = Boolean.parseBoolean(currentChild.getFirstChild().getNodeValue());
                }
            }
        }
          
    }
    
    @Override
    public Set<Class<? extends GenericComponent>> getInterfaces() {
        
        Set<Class<? extends GenericComponent>> output = new HashSet<Class<? extends GenericComponent>>();
        output.add(InterfacesEnum.EntityInterface);
        return output;
    }
    
    @Override
	public void init(Hash entityId, Node generatorNode) {
		super.init(entityId, generatorNode);
	}
    
	@Override
	public void deInit() {
		mInteractionNameSet.clear();
		mInteractionNameSet = null;
	}
	
	@Override
	public MessagesEnum.MessageResults handleMessage(final GenericMessage messageWrapper) {
		
		switch(messageWrapper.getType()) {
		
			case MT_OBJECT_CREATED: {
				Description pDescr = (Description)globalsManager.getComponentManager().queryEntityForInterface(getEntityId(), InterfacesEnum.DescriptionInterface);
				if	((pDescr != null) && (!isInteractionName(pDescr.getShortDescr()))) {
					addInteractionName(pDescr.getShortDescr());
				}
			}
				
			case MT_HIDE: {
				setVisible(false);
				return MessagesEnum.MessageResults.MR_TRUE;
			}
				
			case MT_SHOW: {
				setVisible(true);
				return MessagesEnum.MessageResults.MR_TRUE;
			}
		}
		
		return MessagesEnum.MessageResults.MR_IGNORED;
	}
	
    @Override
	public Hash getPosition() {
		return mRoom;
	}

    @Override
	public void setPosition(Hash room) {
		this.mRoom = room;
	}

    @Override
	public boolean getVisible() {
		return mVisible;
	}

    @Override
	public void setVisible(boolean visible) {
		this.mVisible = visible;
	}
    
	@Override
	public void addInteractionName(Hash name) {
		if (name != null) {
			mInteractionNameSet.add(name);
		}
	}
	
	@Override
	public boolean isInteractionName(Hash name) {
		System.err.println(mInteractionNameSet.toString());
		if(mInteractionNameSet.contains(name)) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean canThisObjectBeSeenBy(Hash viewer, boolean includeInventoryObject) {
		
		boolean hasBeenCollected = true;
		
		Collectable pCollectable = (Collectable)globalsManager.getComponentManager().queryEntityForInterface(getEntityId(), InterfacesEnum.CollectableInterface);
		
		if (pCollectable == null) {
			hasBeenCollected = false;
		} else if (!pCollectable.isCollected()) {
			hasBeenCollected = false;
		}
		
		if (!hasBeenCollected) {
		// The object is not collectable or it is collectable but not collected. Check if it's in the same room as the viewer.
			if (!getVisible()) {
				return false; // Can't be seen by anyone
			}
			
			Entity pViewerEntity = (Entity)globalsManager.getComponentManager().queryEntityForInterface(viewer, InterfacesEnum.EntityInterface);
			if (pViewerEntity == null) {
				return false;
			} 
			if (getPosition().equals(pViewerEntity.getPosition())) {
				return true;
			}
		}
			
		if (pCollectable != null) {
			if (pCollectable.isCollected()) {
				if (pCollectable.getHolder().equals(viewer)) {
					// This object is collected by the viewer. We're fine
					return includeInventoryObject;
				}
			}
		}
		
		return false;
	}
	
}
