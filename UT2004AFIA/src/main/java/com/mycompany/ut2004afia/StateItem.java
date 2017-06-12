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

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.utils.collections.MyCollections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Killy
 */
public class StateItem extends State{

    /**
     *
     * @param b
     */
    public StateItem(LepsisBot b) {
        super(b);
    }
        public void act(){
            if (bot.canSeePlayers()) {
             bot.changeState(bot.engage);
             return;
            } 
            if(bot.isHit()){
                bot.searchEnemy();
            }
        if(bot.hasPref){
        List<Item> interesting = new ArrayList<Item>();
        
        // ADD WEAPONS
        for (ItemType itemType : ItemType.Category.WEAPON.getTypes()) {
        	if (!bot.hasLoadedWeapon(itemType)) interesting.addAll(bot.getSpawnedItems(itemType).values());
        }
       
        interesting.addAll(bot.getSpawnedItems(UT2004ItemType.U_DAMAGE_PACK).values());

        Item item = MyCollections.getRandom(interesting);
        if (item == null) {
        	if (bot.isNavigating()) return;
        	bot.Randomnavigate();
        } else {
            if (bot.isNavigating())return;
        	bot.item = item;
        	bot.navigate(item);        	
        }        
        }
        else{
            if (bot.canSeePlayers()) {

             bot.changeState(bot.engage);
             return;
            } 
            
            Item item ;
            item = bot.getNearestPrefWeapon();
            if(item !=null){
               if(!bot.isNavigatingToItem()){
                 bot.navigate(item);
               }
            }
            else{
                if(!bot.isNavigatingToItem())
                bot.navigate(bot.getNearestSpawnItem());
            }
            
            if(bot.getPrefWeapon()){
                bot.hasPref = true;
            }
        }

      
    }
    
    private void move(){
        
    }
}
