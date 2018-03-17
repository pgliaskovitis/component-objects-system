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

package engine;

import java.io.FileInputStream;
import java.util.Arrays;

import messages.GenericMessage;
import messages.GenericMessageImpl;
import messages.MessagesEnum;

public class TextAdventureEngineImpl implements TextAdventureEngine {

	private final Globals globalsManager;
	private volatile String mInputBuffer;
	private volatile boolean mbGameRunning;
	private long mLastUpdateTick;
		
	public TextAdventureEngineImpl(Globals globalsManager) {
		super();
		this.globalsManager = globalsManager;
	}

	@Override
	public void init() {
		try {
			globalsManager.print("GDC Quest!\n\nWith only 4 days to go until the next GDC takes place, you are in desperate need of a conference pass. After asking your boss repeatidly to stump up the cash, you are left with no alternative but to steal his personal pass. Hey, he won't need it anyway; it's been years since he touched a line of code. Unfortunately, he's in the building today and the pass will be inside his office somewhere.\n");
			globalsManager.getComponentManager().registerAllMessageTypes();
			globalsManager.getComponentManager().registerAllComponentTypes();
			globalsManager.getComponentManager().createAllEntitiesFromStream(new FileInputStream("resources/objects.xml"));
			globalsManager.getComponentManager().broadcastMessage(GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_ALL_OBJECTS_CREATED, null));
			globalsManager.getTimer().reset();
			mLastUpdateTick = globalsManager.getTimer().getSeconds();
			globalsManager.getComponentManager().broadcastMessage(GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_UPDATE, null));
		} catch (Exception e) {
			System.err.println("Text Adventure Engine failed at initialization");
			System.err.println(Arrays.toString(e.getStackTrace()));
		}
	}

	@Override
	public void deInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runGame() {
		
		mbGameRunning = true;
		while (mbGameRunning) {
			try {
				Thread.sleep(10);
				updateUserInput();
				processUserInput();
				if (globalsManager.getTimer().getSeconds() > mLastUpdateTick) {
					mLastUpdateTick = globalsManager.getTimer().getSeconds();
					globalsManager.getComponentManager().broadcastMessage(GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_UPDATE, null));
				}
			} catch (Exception e) {
				System.err.println("Exception in main loop");
				System.err.println(Arrays.toString(e.getStackTrace()));
			}
		}
		
	}

	@Override
	public void endGame() {
		mbGameRunning = false;
	}

	private void updateUserInput() {
		
		String userInput = globalsManager.getConsole().getNextUserInput();
		if (userInput != null) {
			mInputBuffer = userInput;	
			userInput = null; //memory management
			System.err.println("Main loop consumer: " + mInputBuffer);
		}
		
	}

	private void processUserInput() {
		
		// Send input buffer to objects as a command message
		GenericMessage msg = GenericMessageImpl.createMessage(MessagesEnum.MessageTypes.MT_COMMAND, mInputBuffer);
		globalsManager.getComponentManager().broadcastMessage(msg);
		mInputBuffer = null; //memory management
	}
}
