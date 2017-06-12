/*
 * Copyright (C) 2017 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mycompany.ut2004afia;

/**
 *
 * @author Killy
 */
public abstract class State {
   /**
    * This is a reference to the bot that we might use to call bot methods
    */
    LepsisBot bot;
    
    /**
     * Base constructor
     * @param b the botfor which this state is created
     */
    public State(LepsisBot b){
        bot = b;
    }

    /**
     * Declaration of an abstract method "act". It will be the main method of 
     * the different states.
     */
    abstract void act();

}
