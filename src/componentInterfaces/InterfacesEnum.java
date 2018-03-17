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

package componentInterfaces;

public class InterfacesEnum {
	
	/*
	 * enums, sadly, cannot have type parameters, and the following won't work:	
	public static enum InterfacesEnum {
		
		GenericComponentClass(componentInterfaces.GenericComponent.class), 
		ActorClass(componentInterfaces.Actor.class);		
		
		private Class<? extends GenericComponent> myClass;
		
		InterfacesEnum(Class<? extends GenericComponent> myClass) {
			this.myClass = myClass;
		}
		
		public Class<? extends GenericComponent> getMyClass() {
			return myClass;
		}
	}
 	*
    */
	
	/*
	 * Enum types can implement interfaces.
	 * Can enum types have variable arguments?
	 * */

    // names of interfaces
	public static final Class<GenericComponent> GenericComponentInterface = componentInterfaces.GenericComponent.class;
	public static final Class<Actor> ActorInterface = componentInterfaces.Actor.class;
	public static final Class<Collectable> CollectableInterface = componentInterfaces.Collectable.class;
	public static final Class<Description> DescriptionInterface = componentInterfaces.Description.class;
	public static final Class<Entity> EntityInterface = componentInterfaces.Entity.class;
	public static final Class<Inventory> InventoryInterface = componentInterfaces.Inventory.class;
	public static final Class<Player> PlayerInterface = componentInterfaces.Player.class;
	public static final Class<PuzzleLogic> PuzzleLogicInterface = componentInterfaces.PuzzleLogic.class;
	public static final Class<Room> RoomInterface = componentInterfaces.Room.class;
	
	// names of component types	
	public static enum ComponentTypes {
		Actor,
		Collectable,
		Description,
		Entity,
		Inventory,
		Player,
		PuzzleLogic,
		Room,
		Invalid;
	    
		@Override
		public String toString() {
			switch(this) {
				case Actor:
					return "Actor";
				case Collectable:
					return "Collectable";
				case Description:
					return "Description";
				case Entity:
					return "Entity";
				case Inventory:
					return "Inventory";
				case Player:
					return "Player";
				case PuzzleLogic:
					return "PuzzleLogic";
				case Room:
					return "Room";
				default:
					return "Invalid";
			}
		}
	}
	
	public static ComponentTypes getComponentTypeFromString(String componentTypeName) {
		
		if (componentTypeName.equalsIgnoreCase("entity")) {
			return ComponentTypes.Entity;
		} else if (componentTypeName.equalsIgnoreCase("description")) {
			return ComponentTypes.Description;
		} else if (componentTypeName.equalsIgnoreCase("collectable")) {
			return ComponentTypes.Collectable;
		} else if (componentTypeName.equalsIgnoreCase("inventory")) {
			return ComponentTypes.Inventory;
		} else if (componentTypeName.equalsIgnoreCase("room")) {
			return ComponentTypes.Room;
		} else if (componentTypeName.equalsIgnoreCase("actor")) {
			return ComponentTypes.Actor;
		} else if (componentTypeName.equalsIgnoreCase("puzzlelogic")) {
			return ComponentTypes.PuzzleLogic;
		} else if (componentTypeName.equalsIgnoreCase("player")) {
			return ComponentTypes.Player;
		}
		
		return null;
	}
}
