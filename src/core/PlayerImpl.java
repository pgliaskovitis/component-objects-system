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
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import messages.GenericMessage;
import messages.GenericMessageImpl;
import messages.MessagesEnum;
import messages.MessagesEnum.TellRoomInfo;

import componentInterfaces.GenericComponent;
import componentInterfaces.Hash;
import componentInterfaces.InterfacesEnum;
import componentInterfaces.Player;
import componentInterfaces.PuzzleLogic;
import componentInterfaces.Room;

public final class PlayerImpl extends GenericComponentImpl implements Player {
	
    // the static methods must be implemented by every component for component type initialization purposes 
    // or else, perhaps, Java should support static methods in interfaces
    
    public static void registerInterfaces() {
		globalsManager.getComponentManager().registerComponentInterface(InterfacesEnum.PlayerInterface); 
    }
            
    public static void registerImplementationClass() {
		globalsManager.getComponentManager().registerComponentImplInfo(InterfacesEnum.ComponentTypes.Player, core.PlayerImpl.class);
    }
    
    public static void subscribeToMessageTypes() {
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.PlayerInterface, MessagesEnum.MessageTypes.MT_OBJECT_CREATED);
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.PlayerInterface, MessagesEnum.MessageTypes.MT_ALL_OBJECTS_CREATED);
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.PlayerInterface, MessagesEnum.MessageTypes.MT_COMMAND);
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.PlayerInterface, MessagesEnum.MessageTypes.MT_EVENT);
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.PlayerInterface, MessagesEnum.MessageTypes.MT_TELL_ROOM);
    }
    // end of static initializations
    
    public PlayerImpl(Element generatorElement) {
        super();
    }
    
    @Override
    public Set<Class<? extends GenericComponent>> getInterfaces() {
        
        Set<Class<? extends GenericComponent>> output = new HashSet<Class<? extends GenericComponent>>();
        output.add(InterfacesEnum.PlayerInterface);
        return output;
    }
    
    @Override
	public void init(Hash entityId, Node generatorNode) {
    	super.init(entityId, generatorNode);
	}
    
    @Override
    public MessagesEnum.MessageResults handleMessage(final GenericMessage messageWrapper) {
    	
    	switch (messageWrapper.getType()) {
    	
    		case MT_OBJECT_CREATED: {
    		   			
    			return MessagesEnum.MessageResults.MR_TRUE;
    		}
    		
    		case MT_ALL_OBJECTS_CREATED: {
    			
    			globalsManager.getComponentManager().postMessage(getEntityComponent().getPosition(), GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_LOOK, null));
    			return MessagesEnum.MessageResults.MR_TRUE;
    		}
    		
    		case MT_COMMAND:
    		{
    			if (handleCommand((String)messageWrapper.getData())) {
    				return MessagesEnum.MessageResults.MR_TRUE;
    			}
    			return MessagesEnum.MessageResults.MR_FALSE;
    		}
    		
    		case MT_EVENT: { 
    			// In the real world, this part would live in a script. That seems a bit overkill for this example though.
    			MessagesEnum.EventInfo pEventInfo = (MessagesEnum.EventInfo)messageWrapper.getData();
    			if (pEventInfo.getmEventName().equals(CompHash.getHashForName("StateChange")) && pEventInfo.getmTargetId().equals(CompHash.getHashForName("Laxative"))) {
    				globalsManager.print("Oh no! The laxative has an explosive effect on your digestive system, rendering you hostage to your own bowels. You'll be lucky to see the outside of a lavatory for the next two weeks. Alas, there's no way you'll be able to make the GDC now. Your journey ends here.");
    				globalsManager.getTextAdventureEngine().endGame();
    			}
       			if (pEventInfo.getmEventName().equals(CompHash.getHashForName("StateChange")) && pEventInfo.getmTargetId().equals(CompHash.getHashForName("WaterCooler"))) {
    				PuzzleLogic pCoolerPuzzle = (PuzzleLogic)globalsManager.getComponentManager().queryEntityForInterface(pEventInfo.getmTargetId(), InterfacesEnum.PuzzleLogicInterface);
    				if (pCoolerPuzzle != null) { 
    					// if there is laxative in the water, we're out of luck
    					if (pCoolerPuzzle.getState().equals(CompHash.getHashForName("LaxativeInWaterIngested"))) {
    		   				globalsManager.print("Oh no! The laxative-enriched water has an explosive effect on your digestive system, rendering you hostage to your own bowels. You'll be lucky to see the outside of a lavatory for the next two weeks. Alas, there's no way you'll be able to make the GDC now. Your journey ends here.");
    	    				globalsManager.getTextAdventureEngine().endGame();
    					} 
    				}
 
    			}
    			return MessagesEnum.MessageResults.MR_TRUE;
    		}
    		
    		case MT_TELL_ROOM: {
    		
    			TellRoomInfo trInfo = (TellRoomInfo)messageWrapper.getData();
    			if (getEntityComponent().getPosition().equals(trInfo.getRoom())) {
    				//globalsManager.getTextAdventureEngine().clearInputStringDisplay();
    				globalsManager.print(trInfo.getpMessage());
    				//globalsManager.getTextAdventureEngine().displayInputString();
      			}
    			return MessagesEnum.MessageResults.MR_TRUE;
    		}
    	}

    	return MessagesEnum.MessageResults.MR_IGNORED;
    }

	@Override
	public boolean handleCommand(final String commandString) {
		
        if (commandString != null) {
            String[] commandTokens = commandString.split(" ", 2);
            List<String> commandList = Arrays.asList(commandTokens);
            Hash command = CompHash.getHashForName(commandList.get(0));
            System.err.println("Player: command is: " + command.getHashValue());

            if (command.equalsIgnoreCase(CompHash.getHashForName("Enter"))) {

                Room pCurrentRoomInterface = (Room) globalsManager.getComponentManager().queryEntityForInterface(
                        getEntityComponent().getPosition(), InterfacesEnum.RoomInterface);

                if (pCurrentRoomInterface != null) {
                    Hash destRoom = pCurrentRoomInterface.getConnectedRoom(CompHash.getHashForName(commandList.get(1)));

                    if (destRoom.isValid()) {

                        getEntityComponent().setPosition(destRoom);

                        MessagesEnum.EventInfo evInfo = new MessagesEnum.EventInfo(
                                CompHash.getHashForName("EnterRoom"), destRoom, getEntityId());
                        globalsManager.getComponentManager().broadcastMessage(
                                GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_EVENT, evInfo));

                        globalsManager.getComponentManager().postMessage(
                                getEntityId(),
                                GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_SET_INVENTORY_ITEM_POS,
                                        destRoom));
                        
                        globalsManager.getComponentManager().postMessage(destRoom,
                                GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_LOOK, null));
                    } else {
                        globalsManager.print("I don't know how to get to " + commandList.get(1) + " from here.");
                    }
                }
                return true;

            } else if (command.equalsIgnoreCase(CompHash.getHashForName("Help"))) {
                
                globalsManager.print("Command list: Enter <room>, Look, Examine <item>, Get <item>, Use <item> [with <item>], Inventory");
                return true;
                
            } else if (command.equalsIgnoreCase(CompHash.getHashForName("Look"))) {

                Room pCurrentRoomInterface = (Room) globalsManager.getComponentManager().queryEntityForInterface(
                        getEntityComponent().getPosition(), InterfacesEnum.RoomInterface);
                
                globalsManager.getComponentManager().postMessage(pCurrentRoomInterface.getEntityId(),
                        GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_LOOK, null));
                
                return true;

            } else if (command.equalsIgnoreCase(CompHash.getHashForName("Examine"))) {
                
                MessagesEnum.ExamineInfo exInfo = new MessagesEnum.ExamineInfo(CompHash.getHashForName(commandList
                        .get(1)), getEntityId());
                globalsManager.getComponentManager().broadcastMessage(
                        GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_EXAMINE, exInfo));
                return true;
                
            }  else if (command.equalsIgnoreCase(CompHash.getHashForName("Take"))
                    || command.equalsIgnoreCase(CompHash.getHashForName("Get"))) {

                MessagesEnum.PickupInfo pickupInfo = new MessagesEnum.PickupInfo(CompHash.getHashForName(commandList
                        .get(1)), getEntityId());

                globalsManager.getComponentManager().broadcastMessage(
                        GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_BE_PICKED_UP, pickupInfo));

                return true;

            } else if (command.equalsIgnoreCase(CompHash.getHashForName("Inventory"))) {

                globalsManager.print("Inventory: ");
                globalsManager.getComponentManager().postMessage(getEntityId(),
                        GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_EXAMINE_INVENTORY, null));
                return true;

            } else if (command.equalsIgnoreCase(CompHash.getHashForName("Use"))) {

                MessagesEnum.UseInfo useInfo;

                String[] useObjectsTokens = commandList.get(1).split(" with ", 2);
                List<String> useObjectsList = Arrays.asList(useObjectsTokens);

                if (useObjectsList.size() == 1) {
                    // Use A
                    useInfo = new MessagesEnum.UseInfo(CompHash.getHashForName(useObjectsList.get(0)),
                            CompHash.getHashForName(null), getEntityId());
                    globalsManager.getComponentManager().broadcastMessage(
                            GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_USE, useInfo));
                } else if (useObjectsList.size() == 2) {
                    // Use A with B
                    // Be careful! UseInfo must have the "recipient" object first  
                    useInfo = new MessagesEnum.UseInfo(CompHash.getHashForName(useObjectsList.get(1)),
                            CompHash.getHashForName(useObjectsList.get(0)), getEntityId());
                    globalsManager.getComponentManager().broadcastMessage(
                            GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_USE, useInfo));
                } else {
                    globalsManager.print("I don't know how to use " + commandList.get(1));
                }

                return true;

            } else if (command.equalsIgnoreCase(CompHash.getHashForName("Quit"))) {

                globalsManager.getTextAdventureEngine().endGame();
                return true;

            }

            globalsManager.print("I don't know how to: " + commandString);
            return false;
        }
        
        return false;
	}
    
}
