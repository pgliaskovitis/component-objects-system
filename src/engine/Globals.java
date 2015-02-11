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

import componentSystem.ObjectManagerDB;

public class Globals {

	private ObjectManagerDB componentManager;
    private TextAdventureEngine textAdventureEngine;
	private Timer timer;
	private GameConsole console;

    public ObjectManagerDB getComponentManager() {
		return componentManager;
	}
    
	public void setComponentManager(ObjectManagerDB componentManager) {
        this.componentManager = componentManager;
    }

	public TextAdventureEngine getTextAdventureEngine() {
		return textAdventureEngine;
	}
	
	public void setTextAdventureEngine(TextAdventureEngine textAdventureEngine) {
	    this.textAdventureEngine = textAdventureEngine;
	}

    public Timer getTimer() {
        return timer;
    }

    public GameConsole getConsole() {
        return console;
    }

    public void setConsole(GameConsole console) {
        this.console = console;
    }   
    
    public void setTimer(Timer timer) {
        this.timer = timer;
    }
	
	public void print(String pStr) {
		System.err.println("Sending game input " + pStr + " to piped output");
		console.printGame(pStr);
	}
	
}
