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

package componentSystem;

import java.io.InputStream;
import java.util.Set;

import org.w3c.dom.Node;

import messages.GenericMessage;
import messages.MessagesEnum;

import componentInterfaces.Entity;
import componentInterfaces.GenericComponent;
import componentInterfaces.Hash;
import componentInterfaces.InterfacesEnum;

public interface ObjectManagerDB {

    // component-entity related methods
	public <T> void registerComponentInterface(Class<T> componentInterface);
	//public <T> void addComponentForInterface(Class<T> componentInterface, GenericComponent componentInstance);
	public <T> void addComponentForEntity(Class<T> componentInterface, Hash entityId, GenericComponent componentInstance);
	
    public void createAllEntitiesFromStream(InputStream input); //called by the engine
        
    public <T> Set<T> getComponentsForInterface(Class<T> componentInterface);
    public Set<GenericComponent> getComponentsOfEntity(Hash entityId);
    public <T> GenericComponent queryEntityForInterface(Hash entityId, Class<T> componentInterface);
    
    public <T> boolean deleteAllComponentsForInterface(Class<T> componentInterface);
    public boolean deleteEntity(Hash entityId);
    public <T> GenericComponent deleteComponentOfEntity(Hash entityId, Class<T> componentInterface);
    	
	// message related methods
    public void registerAllMessageTypes(); // called by the engine
	public <T> void subscribeInterfaceToMessageType(Class<T> componentInterface, MessagesEnum.MessageTypes messageType);
	
    public Set<GenericComponent> getSubscribedComponentsForMessageType(MessagesEnum.MessageTypes messageType);
    public void postMessage(Hash entityId, GenericMessage message);
    public void broadcastMessage(GenericMessage message);
	
	// component implementation related methods
    public void registerAllComponentTypes(); // called by the engine
	public <T> void registerComponentImplInfo(InterfacesEnum.ComponentTypes componentTypeName, Class<T> componentClass);
	
}
