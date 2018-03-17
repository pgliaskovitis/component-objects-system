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

import java.util.List;

import org.w3c.dom.Node;

import messages.GenericMessage;
import messages.MessagesEnum;
import attributes.GenericCompAttribute;

import componentInterfaces.Entity;
import componentInterfaces.GenericComponent;
import componentInterfaces.Hash;
import engine.Globals;

//this class is STRICTLY for implementation sharing
// all concrete component classes should be final
public abstract class GenericComponentImpl implements GenericComponent {

	protected static Globals globalsManager;

	public static void injectGlobals(Globals globals) {
		globalsManager = globals;
	}

	protected Hash compId;

	// this is the Id of the entity containing this component, as stored in the object manager maps
	protected Hash entityId;

	// this is a component that every other component within this entity needs to be able to access
	protected Entity entityComponent;

	protected List<GenericCompAttribute> compAttributes;

	@Override
	public Class<? extends GenericComponentImpl> getComponentType() {
		return this.getClass();
	}

	@Override
	public Hash getCompId() {
		return compId;
	}

	@Override
	public void setCompId(Hash compId) {
		this.compId = compId;
	}

	@Override
	public Hash getEntityId() {
		return entityId;
	}

	@Override
	public void setEntityId(Hash entityId) {
		this.entityId = entityId;
	}

	@Override
	public Entity getEntityComponent() {
		return entityComponent;
	}

	@Override
	public void setEntityComponent(Entity entityComponent) {
		this.entityComponent = entityComponent;
	}

	@Override
	public void init(Hash entityId, Node generatorNode) {
		this.entityId = entityId;
	}

	@Override
	public void deInit() {

	}

	// default implementation of message handling
	@Override
	public MessagesEnum.MessageResults handleMessage(final GenericMessage messageWrapper) {
		return MessagesEnum.MessageResults.MR_IGNORED;
	}

	@Override
	public void update() {

	}
}
