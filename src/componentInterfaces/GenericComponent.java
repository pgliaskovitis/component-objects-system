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

import java.util.Set;
import org.w3c.dom.Node;
import core.GenericComponentImpl;
import messages.GenericMessage;
import messages.MessagesEnum;

public interface GenericComponent {

	// database related methods
	public Set<Class<? extends GenericComponent>> getInterfaces();
	public Class<? extends GenericComponentImpl> getComponentType();

	// component related methods
	public Hash getCompId();
	public void setCompId(Hash compId);

	// corresponding entity related methods
	public Hash getEntityId();
	public void setEntityId(Hash entityId);

	// associated entity component related methods	
	public Entity getEntityComponent();
	public void setEntityComponent(Entity entityComponent);

	//public void init(Hash entityId, GenericCompAttributeDTO initDTO);
	public void init(Hash entityId, Node generatorNode);
	public void deInit();

	public MessagesEnum.MessageResults handleMessage(final GenericMessage messageWrapper);

	public void update();
}
