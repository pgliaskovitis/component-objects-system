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
import messages.MessagesEnum.EventInfo;
import messages.MessagesEnum.MessageInfo;

import componentInterfaces.Actor;
import componentInterfaces.Description;
import componentInterfaces.GenericComponent;
import componentInterfaces.Hash;
import componentInterfaces.InterfacesEnum;
import componentInterfaces.PuzzleLogic;

public final class ActorImpl extends GenericComponentImpl implements Actor {

	private enum EBossState	{
		IN_OFFICE,
		ON_WAY_TO_COOLER,
		DRINK,
		ON_WAY_TO_THERMOSTAT,
		SET_THERMOSTAT,
		ON_WAY_BACK_THROUGH_CORRIDOR,
		ON_WAY_TO_OFFICE
	};

	EBossState	mBossState;
	private long mNextStateTime;
	private boolean	mThermostatHot;

	// the static methods must be implemented by every component for component type initialization purposes
	// or else, perhaps, Java should support static methods in interfaces

	public static void registerInterfaces() {
		globalsManager.getComponentManager().registerComponentInterface(InterfacesEnum.ActorInterface);
	}

	public static void registerImplementationClass() {
		globalsManager.getComponentManager().registerComponentImplInfo(InterfacesEnum.ComponentTypes.Actor, core.ActorImpl.class);
	}

	public static void subscribeToMessageTypes() {
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.ActorInterface, MessagesEnum.MessageTypes.MT_DESCRIBE_CHARACTER);
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.ActorInterface, MessagesEnum.MessageTypes.MT_EVENT);
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.ActorInterface, MessagesEnum.MessageTypes.MT_UPDATE);
	}
	// end of static initializations

	//the componentId and entityId fields are set externally by the componentManager
	public ActorImpl(Element generatorElement) {
		super();
		mBossState = EBossState.IN_OFFICE;
		mThermostatHot = false;
	}

	@Override
	public Set<Class<? extends GenericComponent>> getInterfaces() {

		Set<Class<? extends GenericComponent>> output = new HashSet<Class<? extends GenericComponent>>();
		output.add(InterfacesEnum.ActorInterface);
		return output;
	}

	@Override
	public void init(Hash entityId, Node generatorNode) {
		super.init(entityId, generatorNode);
	}

	@Override
	public MessagesEnum.MessageResults handleMessage(final GenericMessage messageWrapper) {

		switch (messageWrapper.getType()) {
			case MT_EVENT: {
				MessageInfo msgInfo = (MessageInfo)messageWrapper.getData();
				handleEvent((EventInfo)msgInfo);
				return MessagesEnum.MessageResults.MR_TRUE;
			}
			case MT_UPDATE: {
				update();
				return MessagesEnum.MessageResults.MR_TRUE;
			}
			case MT_DESCRIBE_CHARACTER: {
				Hash containingObject = (Hash)messageWrapper.getData();
				describeCharacter(containingObject);
				return MessagesEnum.MessageResults.MR_TRUE;
			}
		}

		return MessagesEnum.MessageResults.MR_IGNORED;
	}

	@Override
	public void update() {

		long currentTime = globalsManager.getTimer().getSeconds();

		switch (mBossState)	{
			case IN_OFFICE: {
				// The thermostat being hot sets off the sequence of events
				if (mThermostatHot) {
					mBossState = EBossState.ON_WAY_TO_COOLER;
					mNextStateTime = currentTime + 5;
				}
				break;
			}
			case ON_WAY_TO_COOLER: {
				if (currentTime >= mNextStateTime) {
					gotoRoom(CompHash.getHashForName("Corridor"));
					// Next state
					mBossState = EBossState.DRINK;
					mNextStateTime = currentTime + 2;
				}
				break;
			}
			case DRINK: {
				if (currentTime >= mNextStateTime) {

					PuzzleLogic pWaterCoolerPuzzle = (PuzzleLogic)globalsManager.getComponentManager().queryEntityForInterface(CompHash.getHashForName("WaterCooler"), InterfacesEnum.PuzzleLogicInterface);

					if (pWaterCoolerPuzzle != null) {
						globalsManager.print("The boss takes a drink from the water cooler.");
						if (pWaterCoolerPuzzle.getState().equals(CompHash.getHashForName("normal"))) {
							mBossState = EBossState.ON_WAY_TO_THERMOSTAT;
							mNextStateTime = currentTime + 5;
						} else if (pWaterCoolerPuzzle.getState().equals(CompHash.getHashForName("LaxativeInWater"))) {
							globalsManager.print("With a walk like John Wayne, the boss hurriedly makes his way into the toilet. By the sounds of it, he'll be occupied for quite some time. You now have ample opportunity to make that GDC pass yours! Well done!");
							globalsManager.getTextAdventureEngine().endGame();
						}
					}
				}
				break;
			}
			case ON_WAY_TO_THERMOSTAT: {
				if (currentTime >= mNextStateTime) {
					gotoRoom(CompHash.getHashForName("ProgrammingRoom"));
					// Next state
					mBossState = EBossState.SET_THERMOSTAT;
					mNextStateTime = currentTime + 1;
					tellRoom("Boss: Who turned this thermostat up? It's way too hot in here.");
				}
				break;
			}
			case SET_THERMOSTAT: {
				if (currentTime >= mNextStateTime) {
					PuzzleLogic pThermoPuzzle = (PuzzleLogic)globalsManager.getComponentManager().queryEntityForInterface(CompHash.getHashForName("Thermostat"), InterfacesEnum.PuzzleLogicInterface);
					if (pThermoPuzzle != null) {
						pThermoPuzzle.setState(CompHash.getHashForName("normal"), true);
						globalsManager.print("The boss sets the thermostat back to normal.");
						mBossState = EBossState.ON_WAY_BACK_THROUGH_CORRIDOR;
						mNextStateTime = currentTime + 2;
					}
				}
				break;
			}
			case ON_WAY_BACK_THROUGH_CORRIDOR: {
				if (currentTime >= mNextStateTime) {
					gotoRoom(CompHash.getHashForName("Corridor"));
					mBossState = EBossState.ON_WAY_TO_OFFICE;
					mNextStateTime = currentTime + 2;
				}
				break;
			}
			case ON_WAY_TO_OFFICE: {
				if (currentTime >= mNextStateTime) {
					gotoRoom(CompHash.getHashForName("BossOffice"));
					mBossState = EBossState.IN_OFFICE;
				}
				break;
			}
		}
	}

	private void handleEvent(MessagesEnum.EventInfo eventInfo) {

		if (eventInfo.getmEventName().equals(CompHash.getHashForName("StateChange"))) {

			if (eventInfo.getmTargetId().equals(CompHash.getHashForName("Thermostat"))) {

				PuzzleLogic pThermoPuzzle = (PuzzleLogic)globalsManager.getComponentManager().queryEntityForInterface(eventInfo.getmTargetId(), InterfacesEnum.PuzzleLogicInterface);
				if (pThermoPuzzle != null) {
					// Go get a drink
					if (pThermoPuzzle.getState().equals(CompHash.getHashForName("hot"))) {
						mThermostatHot = true;
					} else if (pThermoPuzzle.getState().equals(CompHash.getHashForName("normal"))) {
						mThermostatHot = false;
					}
				}
			}
		}
	}

	@Override
	public void describeCharacter(Hash viewer) {

		Description pDescr = (Description)globalsManager.getComponentManager().queryEntityForInterface(getEntityId(), InterfacesEnum.DescriptionInterface);
		if (pDescr == null) {
			// We have nothing to describe
			return;
		}

		if (getEntityComponent().canThisObjectBeSeenBy(viewer, false)) {
			globalsManager.print(pDescr.getShortDescr().getHashValue());
		}
	}

	@Override
	public void tellRoom(final String pMsg) {

		MessagesEnum.TellRoomInfo trInfo = new MessagesEnum.TellRoomInfo(getEntityComponent().getPosition(), pMsg);
		globalsManager.getComponentManager().broadcastMessage(GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_TELL_ROOM, trInfo));
	}

	@Override
	public void gotoRoom(Hash room) {

		Description pEnteredRoomDescr = (Description)globalsManager.getComponentManager().queryEntityForInterface(room, InterfacesEnum.DescriptionInterface);

		if (pEnteredRoomDescr != null) {
			StringBuilder tmpStr = new StringBuilder();
			tmpStr.append("The boss goes to the ");
			tmpStr.append(pEnteredRoomDescr.getShortDescr().getHashValue());
			tellRoom(tmpStr.toString());
		}

		Description pLeftRoomDescr = (Description)globalsManager.getComponentManager().queryEntityForInterface(getEntityComponent().getPosition(), InterfacesEnum.DescriptionInterface);

		getEntityComponent().setPosition(room);
		if (pLeftRoomDescr != null) {
			StringBuilder tmpStr = new StringBuilder();
			tmpStr.append("The boss enters from the ");
			tmpStr.append(pLeftRoomDescr.getShortDescr().getHashValue());
			tellRoom(tmpStr.toString());
		}

	}
}
