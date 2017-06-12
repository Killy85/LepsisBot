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

import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;

/**
 *
 * @author Killy
 */
public class StateMedkit extends State {

    /**
     *
     * @param b
     */
    public StateMedkit(LepsisBot b) {
        super(b);
    }
        public void act(){
         Item item = bot.getPathNearestSpawnedItem(ItemType.Category.HEALTH);
        Item item2 = bot.getPathNearestSpawnedItem(ItemType.Category.SHIELD);

        if(bot.getHealth() > 80){
         bot.changeState(bot.itemst);
         return;
        }
        if(bot.canSeePlayers()){
            bot.changeState(bot.engage);
            return;
        }
        if (item == null) {
            if(item2 == null){
                bot.changeState(bot.itemst);
            }else{
                bot.navigate(item2);
                bot.item = item2;
            }	
        } else {


        	bot.navigate(item);
        	bot.item = item;
        }
    }
    
}
