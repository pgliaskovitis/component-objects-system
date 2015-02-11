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
import messages.MessagesEnum;
import messages.MessagesEnum.ExamineInfo;

import componentInterfaces.Description;
import componentInterfaces.GenericComponent;
import componentInterfaces.Hash;
import componentInterfaces.InterfacesEnum;

public final class DescriptionImpl extends GenericComponentImpl implements Description {

	private Hash mShortDescr;
	private Hash mLongDescr;
	
    // the static methods must be implemented by every component for component type initialization purposes 
    // or else, perhaps, Java should support static methods in interfaces
    
    public static void registerInterfaces() {
		globalsManager.getComponentManager().registerComponentInterface(InterfacesEnum.DescriptionInterface); 
    }
            
    public static void registerImplementationClass() {
		globalsManager.getComponentManager().registerComponentImplInfo(InterfacesEnum.ComponentTypes.Description, core.DescriptionImpl.class);
    }
    
    public static void subscribeToMessageTypes() {
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.DescriptionInterface, MessagesEnum.MessageTypes.MT_EXAMINE);
    }
    // end of static initializations
    
    public DescriptionImpl(Element generatorElement) {
        super();
        
        NodeList children = generatorElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equalsIgnoreCase("string")) {
                Node currentChild = children.item(i);
                NamedNodeMap attributes = currentChild.getAttributes();
                Node hashName = attributes.getNamedItem("name");
                if (hashName.getTextContent().equalsIgnoreCase("ShortDesc")) {
                	//read in short description
                	mShortDescr = CompHash.getHashForName(currentChild.getFirstChild().getNodeValue());
                } else if (hashName.getTextContent().startsWith("LongDesc")) {
                	//read in long description
                	mLongDescr = CompHash.getHashForName(currentChild.getFirstChild().getNodeValue());
                }
            } 
        }
        
    }
    
    @Override
    public Set<Class<? extends GenericComponent>> getInterfaces() {
        
        Set<Class<? extends GenericComponent>> output = new HashSet<Class<? extends GenericComponent>>();
        output.add(InterfacesEnum.DescriptionInterface);
        return output;
    }
    
    @Override
	public void init(Hash entityId, Node generatorNode) {
        super.init(entityId, generatorNode);
	}
    
	@Override
	public MessagesEnum.MessageResults handleMessage(final GenericMessage messageWrapper) {
		
		switch (messageWrapper.getType()) {
		
			case MT_EXAMINE: {
			
				ExamineInfo examineInfo = (ExamineInfo)messageWrapper.getData();
				if (getEntityComponent().isInteractionName(examineInfo.getExamineObjectInteractionName())) {
					// The user is looking at me. Check if this object (the one containing the description component) can be seen by the user
					if (getEntityComponent().canThisObjectBeSeenBy(examineInfo.getExaminerId(), true)) {
						Description pDescr = (Description)globalsManager.getComponentManager().queryEntityForInterface(getEntityId(), InterfacesEnum.DescriptionInterface);
						if (pDescr != null) {
							globalsManager.print(pDescr.getLongDescr().getHashValue());
						}
					}
				}
				return MessagesEnum.MessageResults.MR_TRUE;
			}
		}

		return MessagesEnum.MessageResults.MR_IGNORED;
    }

	@Override
	public Hash getShortDescr() {
		return mShortDescr;
	}

	@Override
	public void setShortDescr(Hash mShortDescr) {
		this.mShortDescr = mShortDescr;
	}

	@Override
	public Hash getLongDescr() {
		return mLongDescr;
	}

	@Override
	public void setLongDescr(Hash mLongDescr) {
		this.mLongDescr = mLongDescr;
	}
	    
}
