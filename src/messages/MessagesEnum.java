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

package messages;

import componentInterfaces.Hash;

public class MessagesEnum {

	public static enum MessageTypes {
		MT_UPDATE,
		MT_OBJECT_CREATED,
		MT_ALL_OBJECTS_CREATED,
		MT_COMMAND,
		MT_LOOK,
		MT_DESCRIBE_OBJECT,
		MT_DESCRIBE_CHARACTER,
		MT_USE,
		MT_EXAMINE,
		MT_EXAMINE_INVENTORY,
		MT_SET_INVENTORY_ITEM_POS,
		MT_BE_PICKED_UP,
		MT_PICK_UP,
		MT_PICK_UP_SUCCESSFUL,
		MT_HIDE,
		MT_SHOW,
		MT_EVENT,
		MT_TELL_ROOM,
		NUM_MESSAGE_TYPES;
	}
	
	public static enum MessageResults {
		MR_FALSE,
		MR_TRUE,
		MR_IGNORED,
		MR_ERROR
	}
	
	public static abstract class MessageInfo {
				    
	}
	
	public static final class EventInfo extends MessageInfo {

		private final Hash mEventName;		
		private final Hash mTargetId;
		private final Hash mActorId;

		public EventInfo(Hash mEventName, Hash mTargetId, Hash mActorId) {
			super();
			this.mEventName = mEventName;
			this.mTargetId = mTargetId;
			this.mActorId = mActorId;
		}

		public Hash getmEventName() {
			return mEventName;
		}

		public Hash getmTargetId() {
			return mTargetId;
		}

		public Hash getmActorId() {
			return mActorId;
		}
	}
	
	public static final class ExamineInfo extends MessageInfo {
		
		private final Hash mExamineObjectInteractionName; 
		private final Hash mExaminerId;

		public ExamineInfo(Hash examineObjectInteractionName, Hash examinerId) {
			super();
			this.mExamineObjectInteractionName = examineObjectInteractionName;
			this.mExaminerId = examinerId;
		}

		public Hash getExamineObjectInteractionName() {
			return mExamineObjectInteractionName;
		}

		public Hash getExaminerId() {
			return mExaminerId;
		}
	}
	
	public static final class PickupInfo extends MessageInfo {
        
		private final Hash mCollectedObjectInteractionName;
		private final Hash mCollectorId;

		public PickupInfo(Hash collectedObjectInteractionName, Hash collectorId) {
			super();
			this.mCollectedObjectInteractionName = collectedObjectInteractionName;
			this.mCollectorId = collectorId;
		}

		public Hash getCollectorId() {
			return mCollectorId;
		}

		public Hash getCollectedObjectInteractionName() {
			return mCollectedObjectInteractionName;
		}
    }

	public static final class UseInfo extends MessageInfo {
	    
		private final Hash mUseObjectInteractionName;
		private final Hash mUseWithInteractionName;
		private final Hash mUserId;

		public UseInfo(Hash useObjectInteractionName, Hash useWithInteractionName, Hash userId) {
			super();
			this.mUseObjectInteractionName = useObjectInteractionName;
			this.mUseWithInteractionName = useWithInteractionName;
			this.mUserId = userId;
		}

		public Hash getUseObjectInteractionName() {
			return mUseObjectInteractionName;
		}

		public Hash getUseWithInteractionName() {
			return mUseWithInteractionName;
		}

		public Hash getUserId() {
			return mUserId;
		}
	}

    public static final class TellRoomInfo extends MessageInfo {
    	
		private final Hash mRoom;
		private final String pMessage;

		public TellRoomInfo(Hash room, String pMessage) {
			super();
			this.mRoom = room;
			this.pMessage = pMessage;
		}

		public Hash getRoom() {
			return mRoom;
		}

		public String getpMessage() {
			return pMessage;
		}
	}
}
