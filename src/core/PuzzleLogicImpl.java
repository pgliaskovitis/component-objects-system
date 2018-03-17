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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import componentInterfaces.PuzzleLogic;

public final class PuzzleLogicImpl extends GenericComponentImpl implements PuzzleLogic {

	private Map<Hash, StateTransitionInfo> mStateTransitionMap = new HashMap<Hash, StateTransitionInfo>();
	private Map<Hash, StateInfo> mStateInfoMap = new HashMap<Hash, StateInfo>();
	private Hash mCurrentState;

	// the static methods must be implemented by every component for component type initialization purposes 
	// or else, perhaps, Java should support static methods in interfaces

	public static void registerInterfaces() {
		globalsManager.getComponentManager().registerComponentInterface(InterfacesEnum.PuzzleLogicInterface); 
	}
			
	public static void registerImplementationClass() {
		globalsManager.getComponentManager().registerComponentImplInfo(InterfacesEnum.ComponentTypes.PuzzleLogic, core.PuzzleLogicImpl.class);
	}

	public static void subscribeToMessageTypes() {
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.PuzzleLogicInterface, MessagesEnum.MessageTypes.MT_OBJECT_CREATED);
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.PuzzleLogicInterface, MessagesEnum.MessageTypes.MT_DESCRIBE_OBJECT);
		globalsManager.getComponentManager().subscribeInterfaceToMessageType(InterfacesEnum.PuzzleLogicInterface, MessagesEnum.MessageTypes.MT_USE);
	}
	// end of static initializations

	public PuzzleLogicImpl(Element generatorElement) {
		super();

		Hash bufferStateTransition = null;
		StateTransitionInfo bufferStateTransitionInfo = null;
		
		Hash bufferState = null;
		StateInfo bufferStateInfo = null;

		NodeList children = generatorElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeName().equalsIgnoreCase("hash")) {
				Node currentChild = children.item(i);
				NamedNodeMap attributes = currentChild.getAttributes();
				Node hashName = attributes.getNamedItem("name");
				if (hashName.getTextContent().startsWith("InitialState")) {
					mCurrentState = CompHash.getHashForName(currentChild.getFirstChild().getNodeValue());
				} else if (hashName.getTextContent().startsWith("FromState")) {
					bufferStateTransition = CompHash.getHashForName(currentChild.getFirstChild().getNodeValue());
					bufferStateTransitionInfo = new StateTransitionInfo();
					mStateTransitionMap.put(bufferStateTransition, bufferStateTransitionInfo);
				} else if (hashName.getTextContent().startsWith("ToState")) {
					mStateTransitionMap.get(bufferStateTransition).setEndState(CompHash.getHashForName(currentChild.getFirstChild().getNodeValue()));
				} else if (hashName.getTextContent().startsWith("RequiredEntity")) {
					if (currentChild.getFirstChild() != null) {
						mStateTransitionMap.get(bufferStateTransition).setNeededObject(CompHash.getHashForName(currentChild.getFirstChild().getNodeValue()));
					} else {
						mStateTransitionMap.get(bufferStateTransition).setNeededObject(CompHash.getHashForName(null));
					}
				} else if (hashName.getTextContent().startsWith("State")) {
					bufferState = CompHash.getHashForName(currentChild.getFirstChild().getNodeValue());
					bufferStateInfo = new StateInfo();
					mStateInfoMap.put(bufferState, bufferStateInfo);
				}
			} else if (children.item(i).getNodeName().equalsIgnoreCase("string")) {
				Node currentChild = children.item(i);
				NamedNodeMap attributes = currentChild.getAttributes();
				Node hashName = attributes.getNamedItem("name");
				if (hashName.getTextContent().startsWith("ShortDescState")) {
					mStateInfoMap.get(bufferState).setShortDescr(currentChild.getFirstChild().getNodeValue());
				} else if (hashName.getTextContent().startsWith("LongDescState")) {
					mStateInfoMap.get(bufferState).setLongDescr(currentChild.getFirstChild().getNodeValue());
				} else if (hashName.getTextContent().startsWith("ChangeToState")) {
					if (currentChild.getFirstChild() != null) {
						mStateInfoMap.get(bufferState).setChangeToStateString(currentChild.getFirstChild().getNodeValue());
					}
				}
			}
		}
		
	}

	@Override
	public Set<Class<? extends GenericComponent>> getInterfaces() {

		Set<Class<? extends GenericComponent>> output = new HashSet<Class<? extends GenericComponent>>();
		output.add(InterfacesEnum.PuzzleLogicInterface);
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
				
				setDescriptionForState(mCurrentState);
				return MessagesEnum.MessageResults.MR_TRUE;
			}

			case MT_DESCRIBE_OBJECT: {
				
				Hash containingObject = (Hash)messageWrapper.getData();
				describeObject(containingObject);
				return MessagesEnum.MessageResults.MR_TRUE;
			}

			case MT_USE: {
				
				MessagesEnum.UseInfo pUseInfo = (MessagesEnum.UseInfo)messageWrapper.getData();
				if (pUseInfo == null) {
					return MessagesEnum.MessageResults.MR_ERROR;
				}
				if (getEntityComponent().isInteractionName(pUseInfo.getUseObjectInteractionName())) { 
					// The user is talking to me. Check if this object can be used by the user
					if (getEntityComponent().canThisObjectBeSeenBy(pUseInfo.getUserId(), true)) {
						if (pUseInfo.getUseWithInteractionName().isValid()) {
							useWith(pUseInfo.getUserId(), pUseInfo.getUseWithInteractionName());
						} else {
							use();
						}
					} else {
						Description pDescr = (Description)globalsManager.getComponentManager().queryEntityForInterface(getEntityId(), InterfacesEnum.DescriptionInterface);
						if (pDescr != null) {
							globalsManager.print("You can not see the " + pDescr.getShortDescr().getHashValue() + " from here");
						} else {
							globalsManager.print("You can not see that object from here.");
						}
					}
				}
				return MessagesEnum.MessageResults.MR_TRUE;    			
			}

		}
		
		return MessagesEnum.MessageResults.MR_IGNORED;
	}

	@Override
	public Hash getState() {
		
		return mCurrentState;
	}

	@Override
	public boolean setState(Hash targetState, boolean silent) {
		
		if (!targetState.isValid()) {
			return false;
		}

		if (!setDescriptionForState(targetState)) {
			return false;
		}

		if (!silent) {
			StateInfo pInfo = getStateInfo(targetState);
			if (pInfo == null) {
				return false;
			}
			globalsManager.print(pInfo.changeToStateString);
		}

		mCurrentState = targetState;
		MessagesEnum.EventInfo evInfo = new MessagesEnum.EventInfo(CompHash.getHashForName("StateChange"), getEntityId(), null);
		globalsManager.getComponentManager().broadcastMessage(GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_EVENT, evInfo));

		return true;
	}

	@Override
	public boolean use() {

		StateTransitionInfo transitionInfo = mStateTransitionMap.get(mCurrentState); 
		
		if (transitionInfo == null) {
			// There is no transition out of this state
			return false;
		}

		Hash targetState = transitionInfo.getEndState();

		if (transitionInfo.getNeededObject().isValid()) {
			// We need an (additional) object to get to the next state
			globalsManager.print("Nothing excruciatingly interesting happens.");
			return false;
		} 
		
		return setState(targetState, false);
	}


	@Override
	public boolean useWith(Hash userId, Hash object) {
		
		StateTransitionInfo transitionInfo = mStateTransitionMap.get(mCurrentState);

		if (transitionInfo == null) {
			return false; // There is no transition out of this state
		}

		Entity pNeededObjectEntity = (Entity)globalsManager.getComponentManager().queryEntityForInterface(transitionInfo.getNeededObject(), InterfacesEnum.EntityInterface);

		if (pNeededObjectEntity.isInteractionName(object) && pNeededObjectEntity.canThisObjectBeSeenBy(userId, true)) {
			return setState(transitionInfo.getEndState(), false);
		}

		return false;
	}

	private void describeObject(Hash viewer) {

		Description pDescr = (Description)globalsManager.getComponentManager().queryEntityForInterface(getEntityId(), InterfacesEnum.DescriptionInterface);

		if (pDescr == null) {
			return;
		}

		if (getEntityComponent().canThisObjectBeSeenBy(viewer, false)) {
			globalsManager.print(pDescr.getShortDescr().getHashValue());
		}

	}

	private void addStateTransition(Hash fromState, Hash toState, Hash neededObject) {

		// here we have single exits from states, because the state is the key in the HashMap
		StateTransitionInfo transitionInfo = new StateTransitionInfo();
		transitionInfo.setNeededObject(neededObject);
		transitionInfo.setEndState(toState);
		mStateTransitionMap.put(fromState, transitionInfo);	    
	}

	private void addStateInfo(Hash state, StateInfo stateInfo) {

		mStateInfoMap.put(state, stateInfo);
	}

	private boolean setDescriptionForState(Hash stateName) {

		Description pDescr = (Description)globalsManager.getComponentManager().queryEntityForInterface(getEntityId(), InterfacesEnum.DescriptionInterface);

		if (pDescr == null) {
			return false;
		}

		StateInfo pInfo = getStateInfo(stateName);
		if (pInfo == null) {
			return false;
		}

		pDescr.setShortDescr(CompHash.getHashForName(pInfo.getShortDescr()));
		pDescr.setLongDescr(CompHash.getHashForName(pInfo.getLongDescr()));

		return true;
	}

	private StateInfo getStateInfo(Hash stateName) {

		return mStateInfoMap.get(stateName);
	}

	private static class StateInfo {

		private String mShortDescr;
		private String mLongDescr;
		private String changeToStateString;

		public String getShortDescr() {
			return mShortDescr;
		}

		public void setShortDescr(String mShortDescr) {
			this.mShortDescr = mShortDescr;
		}

		public String getLongDescr() {
			return mLongDescr;
		}

		public void setLongDescr(String mLongDescr) {
			this.mLongDescr = mLongDescr;
		}

		public String getChangeToStateString() {
			return changeToStateString;
		}

		public void setChangeToStateString(String changeToStateString) {
			this.changeToStateString = changeToStateString;
		}

	}

	private static class StateTransitionInfo {

		private Hash mNeededObject;
		private Hash mEndState;

		public Hash getNeededObject() {
			return mNeededObject;
		}

		public void setNeededObject(Hash mNeededObject) {
			this.mNeededObject = mNeededObject;
		}

		public Hash getEndState() {
			return mEndState;
		}

		public void setEndState(Hash mEndState) {
			this.mEndState = mEndState;
		}

	}
}
