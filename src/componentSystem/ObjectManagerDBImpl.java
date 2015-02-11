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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import scripting.ScriptingUtils;

import messages.GenericMessage;
import messages.MessagesEnum;

import componentInterfaces.Actor;
import componentInterfaces.Collectable;
import componentInterfaces.Description;
import componentInterfaces.Entity;
import componentInterfaces.GenericComponent;
import componentInterfaces.Hash;
import componentInterfaces.InterfacesEnum;
import componentInterfaces.Inventory;
import componentInterfaces.Player;
import componentInterfaces.PuzzleLogic;
import componentInterfaces.Room;

import core.CompHash;
import core.CoreUtils;
import core.GenericComponentImpl;
import engine.Globals;

public class ObjectManagerDBImpl implements ObjectManagerDB {

	/*
     * Interfaces are Class<? extends GenericComponent> or componentInterfaces.*.class, i.e., the class objects of the interfaces
     * ComponentTypes are Class<? extends GenericComponentImpl>, i.e., the class objects of the actual implementation classes
     * But, componentTypes are indexed in the hashMaps indirectly, through their implemented interfaces
     * Essentially, there is a many-to-one relationship between Interfaces and ComponentTypes   
     * Entities are virtual, they exist only within the EntitiesContainer as aggregations of components
     * There is redundancy in this initial implementation, since component instances get indexed both by the ComponentsContainer and the EntitiesContainer
     * The key is that the actual component objects are created once, but are then referenced by two separate data structures
     */
	
	private Globals globalsManager;
	
    private static ComponentsContainer allComponents = new ComponentsContainer();

    private static EntitiesContainer allEntities = new EntitiesContainer();

    private static MessageTypesContainer allComponentMessages = new MessageTypesContainer();

    private Map<InterfacesEnum.ComponentTypes, ComponentImplInfo> allComponentTypesInfo = new ConcurrentHashMap<InterfacesEnum.ComponentTypes, ComponentImplInfo>();
    
    public ObjectManagerDBImpl(Globals globalsManager) {
		super();
		this.globalsManager = globalsManager;
	}

	// this is used by every concrete component to register the interfaces it implements
    @Override
    public <T> void registerComponentInterface(Class<T> componentInterface) {

        if (componentInterface == null) {
            System.err.println("componentInterface is null");
            return;
        }

        try {
            allComponents.registerInterface(componentInterface);
            allEntities.registerInterface(componentInterface);
        } catch (Exception e) {
            System.err.println("Error while registering componentInterface " + componentInterface.getName()
                    + e.getMessage());
        }
    }

    /*
    @Override
    public <T> void addComponentForInterface(Class<T> componentInterface, GenericComponent componentInstance) {

        if (componentInterface == null) {
            System.err.println("componentInterface is null");
            return;
        }

        if (componentInstance == null) {
            System.err.println("componentInstance is null");
            return;
        }

        try {
            allComponents.addComponentForInterface(componentInterface, componentInstance);
        } catch (Exception e) {
            System.err.println("Error while registering component " + componentInstance.toString()
                    + " for componentInterface " + componentInterface.getName() + e.getMessage());
        }
    }
    */
    
    @Override
    public <T> void addComponentForEntity(Class<T> componentInterface, Hash entityId, GenericComponent componentInstance) {

        if (componentInterface == null) {
            System.err.println("componentInterface is null");
            return;
        }

        if (entityId == null) {
            System.err.println("entity is null");
            return;
        }

        if (componentInstance == null) {
            System.err.println("componentInstance is null");
            return;
        }

        try {
            allComponents.addComponentForInterface(componentInterface, componentInstance);
            allEntities.addComponentToEntity(componentInterface, entityId, componentInstance);
        } catch (Exception e) {
            System.err.println("Error while registering component " + componentInstance.toString()
                    + " for componentInterface " + componentInterface.getName() + e.getMessage());
        }
    }

    @Override
    public void createAllEntitiesFromStream(InputStream input) {
        
        try {
            Document doc = ScriptingUtils.readXml(input);
            
            NodeList allEntities = doc.getElementsByTagName("entity");
            
            for (int i = 0; i < allEntities.getLength(); i++) {
                Element currentEntity = (Element)allEntities.item(i);
                createNewEntity(currentEntity);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to initialize entities from XML file: " + e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
        
    }
    
    public Hash createNewEntity(Element generatorElement) {

        // construction of a specific entity
        Hash currentEntityId = null;
        Entity currentConcreteEntity = null;
        GenericComponent currentConcreteComponent = null;

        // find entityId
        String currentEntityName = generatorElement.getAttribute("name");
        currentEntityId = CompHash.getHashForName(currentEntityName);
        
        StringBuilder strb = new StringBuilder();
        strb.append("Handling entity " + currentEntityName + " -> ");
        
        NodeList allEntityComponents = generatorElement.getElementsByTagName("component");
        
        // first find and construct entityComponent for this entity before all other components
        for (int j = 0; j < allEntityComponents.getLength(); j++) {
        	Element currentXMLComponent = (Element) allEntityComponents.item(j);
        	String currentComponentName = currentXMLComponent.getAttribute("name");
        	
            if (currentComponentName.equalsIgnoreCase("Entity")) {
                // found entity component for this entity
              	strb.append(currentComponentName + ", ");
                currentConcreteEntity = (Entity) createNewComponentForTypeName(currentXMLComponent, currentComponentName, currentEntityId, null);
                addComponentForEntity(InterfacesEnum.EntityInterface, currentEntityId, currentConcreteEntity);
                break;
            }
        }

        // then construct all other components for this entity
        for (int j = 0; j < allEntityComponents.getLength(); j++) {
        	Element currentXMLComponent = (Element) allEntityComponents.item(j);
        	String currentComponentName = currentXMLComponent.getAttribute("name");
        	
            if (!currentComponentName.equalsIgnoreCase("Entity")) {
                // found entity component for this entity
              	strb.append(currentComponentName + ", ");
                currentConcreteComponent = createNewComponentForTypeName(currentXMLComponent, currentComponentName, currentEntityId, currentConcreteEntity);

        		if (currentComponentName.equalsIgnoreCase("description")) {
        			addComponentForEntity(InterfacesEnum.DescriptionInterface, currentEntityId, (Description)currentConcreteComponent);
        		} else if (currentComponentName.equalsIgnoreCase("collectable")) {
        			addComponentForEntity(InterfacesEnum.CollectableInterface, currentEntityId, (Collectable)currentConcreteComponent);
        		} else if (currentComponentName.equalsIgnoreCase("inventory")) {
        			addComponentForEntity(InterfacesEnum.InventoryInterface, currentEntityId, (Inventory)currentConcreteComponent);
        		} else if (currentComponentName.equalsIgnoreCase("room")) {
        			addComponentForEntity(InterfacesEnum.RoomInterface, currentEntityId, (Room)currentConcreteComponent);
        		} else if (currentComponentName.equalsIgnoreCase("actor")) {
        			addComponentForEntity(InterfacesEnum.ActorInterface, currentEntityId, (Actor)currentConcreteComponent);
        		} else if (currentComponentName.equalsIgnoreCase("puzzlelogic")) {
        			addComponentForEntity(InterfacesEnum.PuzzleLogicInterface, currentEntityId, (PuzzleLogic)currentConcreteComponent);
        		} else if (currentComponentName.equalsIgnoreCase("player")) {
        			addComponentForEntity(InterfacesEnum.PlayerInterface, currentEntityId, (Player)currentConcreteComponent);
        		}
            }
        }

        System.err.println(strb.toString());
        
        return currentEntityId;
    }
    
    private GenericComponent createNewComponentForTypeName(Element generatorElement, String componentTypeName, Hash entityId, Entity entityComponent) {

        ComponentImplInfo componentInfo = allComponentTypesInfo.get(InterfacesEnum.getComponentTypeFromString(componentTypeName));
        GenericComponent output = componentInfo.newInstance(generatorElement); // the actual constructor is called through reflection

        // do not forget to set a global id for this component (though it is never used so far)
        output.setCompId(generateNewComponentHash(componentTypeName.toString(), componentInfo.incrementAndGetCounter()));

        // do not forget to set the entityId in every component
        output.setEntityId(entityId);

        // do not forget to set the entityComponent in every component
        if (entityComponent != null) {
        	output.setEntityComponent(entityComponent);
        } else {
        	// this is already an entity component
        	output.setEntityComponent((Entity)output); //self reference?
        }
        
        return output;
    }
    
    private Hash generateNewComponentHash(String componentTypeName, int sequenceNumber) {
    	
    	return CompHash.getHashForName(componentTypeName + "_" + sequenceNumber);
    }
    
    @Override
    public <T> Set<T> getComponentsForInterface(Class<T> componentInterface) {

        if (componentInterface == null) {
            System.err.println("componentInterface is null");
            return null;
        }

        Set<T> output = null;

        try {
            output = allComponents.getComponentsForInterface(componentInterface);
        } catch (ClassCastException e) {
            System.err.println("Error while registering componentInterface " + componentInterface.getName());
        }

        return output;
    }
    
    @Override
    public Set<GenericComponent> getComponentsOfEntity(Hash entityId) {

        if (entityId == null) {
            System.err.println("entityId is null");
            return null;
        }

        return allEntities.getAllComponentsOfEntity(entityId);
    }

    @Override
    public <T> GenericComponent queryEntityForInterface(Hash entityId, Class<T> componentInterface) {
    	
    	return allEntities.getSpecificComponentOfEntity(entityId, componentInterface);
    }
    
    @Override
    public <T> boolean deleteAllComponentsForInterface(Class<T> componentInterface) {
        
        Set<T> deletedComponents = allComponents.getComponentsForInterface(componentInterface);
        
        if (deletedComponents.isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean deleteEntity(Hash entityId) {
        
        return true;
    }
    
    @Override
    public <T> GenericComponent deleteComponentOfEntity(Hash entityId, Class<T> componentInterface) {
        
        return null;
    }
    
    @Override
    public void registerAllMessageTypes() {
        
    	allComponentMessages.registerAllMessageTypes();
    }
    
    // Here, we have opted for interfaces to be subscribing to message types, 
    // instead of components themselves subscribing to message types
    // This is being tried out for greater decoupling of theoretical concepts
    @Override
    public <T> void subscribeInterfaceToMessageType(Class<T> componentInterface, MessagesEnum.MessageTypes messageType) {
    	
        if (componentInterface == null) {
            System.err.println("componentInterface is null");
            return;
        }

        allComponentMessages.subscribeInterfaceToMessageType(messageType, componentInterface);
    }
    
    @Override
    public Set<GenericComponent> getSubscribedComponentsForMessageType(MessagesEnum.MessageTypes messageType) {

        if (messageType == null) {
            System.err.println("messageType is null");
            return null;
        }

        Set<GenericComponent> output = new HashSet<GenericComponent>();

        Set<Class<? extends GenericComponent>> setOfInterfaces = allComponentMessages.getSubscribedInterfacesForMessageType(messageType);

        for (Class<? extends GenericComponent> currentInterface : setOfInterfaces) {
            Set<? extends GenericComponent> currentComponents = allComponents.getComponentsForInterface(currentInterface);
            output = CoreUtils.<GenericComponent> union(output, currentComponents);
        }

        return output;
    }

    @Override
    public void postMessage(Hash entityId, GenericMessage message) {
    	
    	// FUTURE: what if we found which components of the entity are actually subscribed to this MessageType and send the message only to them?
    	Set<GenericComponent> affectedComponents = allEntities.getAllComponentsOfEntity(entityId);
    	
    	for (GenericComponent comp: affectedComponents) {
    		comp.handleMessage(message);
    	}
    }
    
    @Override
    public void broadcastMessage(GenericMessage message) {
    	
    	Set<GenericComponent> affectedComponents = getSubscribedComponentsForMessageType(message.getType());
    	
    	for (GenericComponent comp: affectedComponents) {
    		comp.handleMessage(message);
    	}
    }
    
    @Override
    public void registerAllComponentTypes() {
        initializeComponentType(core.ActorImpl.class);
        initializeComponentType(core.CollectableImpl.class);
        initializeComponentType(core.DescriptionImpl.class);
        initializeComponentType(core.EntityImpl.class);
        initializeComponentType(core.InventoryImpl.class);
        initializeComponentType(core.PlayerImpl.class);
        initializeComponentType(core.PuzzleLogicImpl.class);
        initializeComponentType(core.RoomImpl.class);        
    }

    @Override
    public <T> void registerComponentImplInfo(InterfacesEnum.ComponentTypes componentTypeName, Class<T> componentClass) {

        if (componentClass == null) {
            System.err.println("componentClass is null");
            return;
        }

        allComponentTypesInfo.put(componentTypeName, new ComponentImplInfo(this, componentClass));
    }
    
    private void initializeComponentType(Class<? extends GenericComponentImpl> componentClass) {
    	
    	Method m;
    	
        try {
            m = componentClass.getMethod("injectGlobals", Globals.class);
            m.invoke(null, this.globalsManager);
        } catch (Exception e) {
            System.err.println("Registration of component type " + componentClass + " failed at step 1");
        } 
        
        try {
            m = componentClass.getMethod("registerInterfaces", (Class<?>[]) null);
            m.invoke(null, (Object[]) null);
        } catch (Exception e) {
            System.err.println("Registration of component type " + componentClass + " failed at step 2");
        }
        
        try {
            m = componentClass.getMethod("registerImplementationClass", (Class<?>[]) null);
            m.invoke(null, (Object[]) null);
        } catch (Exception e) {
            System.err.println("Registration of component type " + componentClass + " failed at step 3");        	
        }
        
        try{
            m = componentClass.getMethod("subscribeToMessageTypes", (Class<?>[]) null);
            m.invoke(null, (Object[]) null);
        } catch (Exception e) {
            System.err.println("Registration of component type " + componentClass + " failed at step 4");
        }
        
        System.err.println("Registration of component type " + componentClass + " successful");
    }

    private static class ComponentsContainer {

        // A map of sets. Each key represents a particular interface and the corresponding value a set of concrete
        // components that implement it
        // The map has as many keys as there are distinct interfaces
        private Map<Class<? extends GenericComponent>, Set<GenericComponent>> mapComponentTypesToComponents = new ConcurrentHashMap<Class<? extends GenericComponent>, Set<GenericComponent>>();

        public <T> void registerInterface(Class<T> componentInterface) {
        	
            if (componentInterface == null) {
                throw new NullPointerException("componentInterface is null");
            }

            // an interface could be attempted to be registered multiple times (by different components)
            // add interface to the map only if no other component has already done so
            if (!mapComponentTypesToComponents.containsKey(componentInterface.asSubclass(InterfacesEnum.GenericComponentInterface))) {
                mapComponentTypesToComponents.put(componentInterface.asSubclass(InterfacesEnum.GenericComponentInterface), new HashSet<GenericComponent>());
            }
            
        }

        public <T> void addComponentForInterface(Class<T> componentInterface, GenericComponent componentInstance) {
        	
            if (componentInterface == null) {
                throw new NullPointerException("componentInterface is null");
            }
            
            mapComponentTypesToComponents.get(componentInterface).add(componentInstance);
        }

        public <T> Set<T> getComponentsForInterface(Class<T> componentInterface) {
        	
            if (componentInterface == null) {
                throw new NullPointerException("componentInterface is null");
            }

            Set<GenericComponent> rawComponents = mapComponentTypesToComponents.get(componentInterface);
            Set<T> typeCastComponents = new HashSet<T>(); // can we avoid this memory allocation?

            for (Object obj : rawComponents) {
                // here we cast explicitly, since we know that the Set of every entry in the map by construction
                // contains only components of the type of its key
                typeCastComponents.add(componentInterface.cast(obj));
            }

            return typeCastComponents;
        }
        
    }

    private static class EntitiesContainer {

        // to construct the entity from its constituent components one has to
        // traverse all componentTypes (interfaces) and get the concrete component
        // corresponding to this entity (if any) from the second map
        // this design permits at most one component per interface for a particular entity
        private Map<Class<? extends GenericComponent>, Map<Hash, GenericComponent>> mapEntitiesToComponents = new ConcurrentHashMap<Class<? extends GenericComponent>, Map<Hash, GenericComponent>>();

        public <T> void registerInterface(Class<T> componentInterface) {
            
            if (componentInterface == null) {
                throw new NullPointerException("componentInterface is null");
            }

            // an interface could be attempted to be registered multiple times (by different components)
            // add interface to the map only if no other component has already done so
            if (!mapEntitiesToComponents.containsKey(componentInterface.asSubclass(InterfacesEnum.GenericComponentInterface))) {
                mapEntitiesToComponents.put(componentInterface.asSubclass(InterfacesEnum.GenericComponentInterface), new HashMap<Hash, GenericComponent>());
            }
            
        }
        
        public <T> void addComponentToEntity(Class<T> componentInterface, Hash entityId, GenericComponent component) {
        	
            if (entityId == null) {
                throw new NullPointerException("entity is null");
            }
            
            mapEntitiesToComponents.get(componentInterface).put(entityId, component);
        }
        
        public <T> GenericComponent getSpecificComponentOfEntity(Hash entityId, Class<T> componentInterface) {

            if (entityId == null) {
                throw new NullPointerException("entity is null");
            }

            if (componentInterface == null) {
                throw new NullPointerException("componentInterface is null");
            }

            return mapEntitiesToComponents.get(componentInterface.asSubclass(InterfacesEnum.GenericComponentInterface)).get(entityId);
        }

        public Set<GenericComponent> getAllComponentsOfEntity(Hash entityId) {

            if (entityId == null) {
                throw new NullPointerException("entity is null");
            }

            Set<GenericComponent> output = new HashSet<GenericComponent>();

            for (Class<? extends GenericComponent> currentInterface : mapEntitiesToComponents.keySet()) {
                GenericComponent specificComponent = getSpecificComponentOfEntity(entityId, currentInterface);
                if (specificComponent != null) {
                    output.add(getSpecificComponentOfEntity(entityId, currentInterface));
                }
            }

            return output;
        }
        
    }

    private static class MessageTypesContainer {

        private Map<MessagesEnum.MessageTypes, Set<Class<? extends GenericComponent>>> mapMessageTypesToComponentTypes = new ConcurrentHashMap<MessagesEnum.MessageTypes, Set<Class<? extends GenericComponent>>>();

        public void registerAllMessageTypes() {
        	for (MessagesEnum.MessageTypes messageType: MessagesEnum.MessageTypes.values()) {
        		mapMessageTypesToComponentTypes.put(messageType, new HashSet<Class<? extends GenericComponent>>());
        	}
        }
        
        public <T> void subscribeInterfaceToMessageType(MessagesEnum.MessageTypes messageType, Class<T> componentInterface) {
        	
            if (messageType == null) {
                throw new NullPointerException("MessageType to be subscribed to is null");
            }

            mapMessageTypesToComponentTypes.get(messageType).add(
                    componentInterface.asSubclass(InterfacesEnum.GenericComponentInterface));
        }

        // for internal use only, so we can use wild-cards in the return type
        public Set<Class<? extends GenericComponent>> getSubscribedInterfacesForMessageType(MessagesEnum.MessageTypes messageType) {

            if (messageType == null) {
                throw new NullPointerException("messageType is null");
            }

            return mapMessageTypesToComponentTypes.get(messageType);
        }
        
    }

    // this is used for dynamic component creation
    private static class ComponentImplInfo {

        private final Class<? extends GenericComponentImpl> componentClass;
        
        private int componentCounter;

        public ComponentImplInfo(ObjectManagerDB manager, Class<?> componentClass) {
            this.componentClass = componentClass.asSubclass(GenericComponentImpl.class);
            this.componentCounter = 0;
        }

        // create a new instance of the component with reflection
        public GenericComponent newInstance(Element generatorElement) {

            GenericComponent output = null;

            try {
                Constructor<? extends GenericComponentImpl> componentConstructor = componentClass.getConstructor(new Class<?>[] {org.w3c.dom.Element.class});
                Object result = componentConstructor.newInstance(new Object[] {generatorElement});
                return componentClass.cast(result);
            } catch (Exception e) {
                System.err.println("New instance creation for type " + componentClass + " failed " + e.getMessage());
            }

            return output;
        }
        
        public int incrementAndGetCounter() {
        	return componentCounter++;
        }

    }

}
