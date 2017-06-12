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

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Killy
 */
public class StateEngage extends State{

    /**
     *
     * @param b
     */
    public StateEngage(LepsisBot b) {
        super(b);
    }
    
    
        public void act(){
        if(bot.getHealth() < bot.healthLevel) {
            bot.changeState(bot.medkit);
            return;
        }
        if(bot.isNavigating()){
            bot.stopNavigation();
        }
        boolean shooting = false;
        double distance = Double.MAX_VALUE;
        if(bot.enemy == null){
        Map<UnrealId,Player> playerList = bot.getVisibleEnemies();
            if(playerList.size()>= 1)
            {
                bot.enemy = playerList.get(playerList.keySet().iterator().next());
            }
            else
            {
                bot.stopShooting();
                bot.changeState(bot.itemst);
                return;
            }
        }
            else if(!bot.enemy.isVisible()){
                bot.stopShooting();
                bot.navigate(bot.enemy);
                bot.runningToPlayer = true;
            }
            
            if(bot.isNavigating())bot.stopNavigation();
	 distance = bot.getLocation().getDistance(bot.enemy.getLocation());
         if(new Random().nextInt(10)< 7){
         bot.strafe();
         if(!bot.searching_ammo){
	 if (bot.shoot(bot.enemy) != null) {
	            shooting = true;
	 }
         }else{
             bot.shootSecondary(bot.enemy);
         }
          }
        int decentDistance = 500;
        bot.majWeapon();
        if (!bot.enemy.isVisible() || !shooting || decentDistance < distance) {
            if (!bot.runningToPlayer) {
                bot.stopShooting();
                bot.runTo(bot.enemy.getLocation());
                //navigation.navigate(enemy);
                bot.runningToPlayer = true;
            }
        } else {
            bot.runningToPlayer = false;
            bot.stopNavigation();
            bot.getBack();
            
        }
        
        bot.item = null;
        }
    
}
