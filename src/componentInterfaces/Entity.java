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

public interface Entity extends GenericComponent {

	public Hash getPosition();
	public void setPosition(Hash room);

	public boolean getVisible();
	public void setVisible(boolean visible);

	public void addInteractionName(Hash name);
	public boolean isInteractionName(Hash name);

	public boolean canThisObjectBeSeenBy(Hash viewer, boolean includeInventoryObject);
}
