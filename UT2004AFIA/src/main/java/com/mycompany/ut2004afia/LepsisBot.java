package com.mycompany.ut2004afia;

import javax.vecmath.Vector3d;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.communication.connection.exception.ConnectionException;
import cz.cuni.amis.pogamut.base.communication.worldview.event.IWorldEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.component.bus.event.BusAwareCountDownLatch.BusStoppedInterruptedException;
import cz.cuni.amis.pogamut.base.component.exception.ComponentCantStartException;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPref;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathAutoFixer;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometryModule;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Configuration;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.RemoveRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Rotate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.StopShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AutoTraceRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.HearNoise;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import cz.cuni.amis.utils.collections.MyCollections;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import static sun.audio.AudioPlayer.player;

/**
 * AFIA Competition Bot 
 * 
 * Création d'un bot pour UnrealToornament 2004 (UT2004) dont le but est de se rapprocher le plus possible d'un
 * comportement humain dans son éxécution et ses interactions avec les autres
 * joueurs d'une partie
 * 
 * @author Nicolas Goureau
 */

@AgentScoped
@Deprecated
public class LepsisBot extends UT2004BotModuleController {

    // Constants for rays' ids. It is allways better to store such values
    // in constants instead of using directly strings on multiple places of your
    // source code

    /**
     *
     */
    
    protected static final String LEFT45 = "left45Ray";
    /**
     *
     */
        protected static final String FRONT = "frontRay";

      /**
     *
     */

  protected static final String LEFT90 = "left90Ray";

    /**
     *
     */
    protected static final String RIGHT45 = "right45Ray";

    /**
     *
     */
    protected static final String RIGHT90 = "right90Ray";
    
    /**
     * State system:
     * Current describe the state in which the bot is currently
     * Other states describe the differents states that the bot can have
     */
    State current;
    StateEngage engage = new StateEngage(this);
    StateItem itemst = new StateItem(this);
    StateMedkit medkit = new StateMedkit(this);
    /**
     * Whether the bot is running for ammo or not, in case he didn't have any 
     * ammo for any  weapons he got loaded at the moment.
     */
    public boolean searching_ammo = false;
    
    private AutoTraceRay left,left2,right,right2;
    
    /**
     * Flag indicating that the bot has been just executed.
     */
    private boolean first = true;
    private boolean raysInitialized = false;
    /**
     * Whether the left45 sensor signalizes the collision. (Computed in the
     * doLogic()) <p><p> Using {@link RaycastingBot#LEFT45} as the key for the
     * ray.
     */
    @JProp
    private boolean sensorLeft45 = false;
    /**
     * Whether the right45 sensor signalizes the collision. (Computed in the
     * doLogic()) <p><p> Using {@link RaycastingBot#RIGHT45} as the key for the
     * ray.
     */
    @JProp
    private boolean sensorRight45 = false;
    /**
     * Whether the front sensor signalizes the collision. (Computed in the
     * doLogic()) <p><p> Using {@link RaycastingBot#FRONT} as the key for the
     * ray.
     */
    @JProp
    private boolean sensorFront = false;
    
    private boolean sensorGround = true;
    
    /**
     * Whether the bot is moving. (Computed in the doLogic())
     */
    @JProp
    private boolean moving = false;
    /**
     * Whether any of the sensor signalize the collision. (Computed in the
     * doLogic())
     */
    @JProp
    private boolean sensor = false;
    /**
     * How much time should we wait for the rotation to finish (milliseconds).
     */
    @JProp
    private int turnSleep = 250;
    /**
     * How fast should we move? Interval <0, 1>.
     */
    private float moveSpeed = 0.6f;
    /**
     * Small rotation (degrees).
     */
    @JProp
    private int smallTurn = 30;
    /**
     * Big rotation (degrees).
     */
    @JProp
    private int bigTurn = 90;
    WeaponPref pref;
    boolean hasPref;

    /**
     * The bot is initialized in the environment - a physical representation of
     * the bot is present in the game.
     *
     * @param info
     * @param currentConfig information about configuration
     * @param init information about configuration
     */
    @Override
    public void botInitialized(GameInfo info, ConfigChange currentConfig, InitedMessage init) {
     // initialize rays for raycasting
        final int rayLength = (int) (UnrealUtils.CHARACTER_COLLISION_RADIUS * 15);
        // settings for the rays
        boolean fastTrace = false;        // perform only fast trace == we just need true/false information
        boolean floorCorrection = false; // provide floor-angle correction for the ray (when the bot is running on the skewed floor, the ray gets rotated to match the skew)
        boolean traceActor = false;      // whether the ray should collid with other actors == bots/players as well

        // 1. remove all previous rays, each bot starts by default with three
        // rays, for educational purposes we will set them manually
        getAct().act(new RemoveRay("All"));

        // 2. create new rays
        raycasting.createRay(FRONT,   new Vector3d(1, 0, 0), rayLength, fastTrace, floorCorrection, traceActor);
        // note that we will use only three of them, so feel free to experiment with LEFT90 and RIGHT90 for yourself
        raycasting.createRay(LEFT90,  new Vector3d(0, -1, 0), rayLength, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(RIGHT90, new Vector3d(0, 1, 0), rayLength, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(RIGHT45, new Vector3d(1, 1, 0), rayLength, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(LEFT45,  new Vector3d(1, -1, 0), rayLength, fastTrace, floorCorrection, traceActor);


        // register listener called when all rays are set up in the UT engine
        raycasting.getAllRaysInitialized().addListener(new FlagListener<Boolean>() {

            public void flagChanged(Boolean changedValue) {
                // once all rays were initialized store the AutoTraceRay objects
                // that will come in response in local variables, it is just
                // for convenience
                left = raycasting.getRay(LEFT90);
                right = raycasting.getRay(RIGHT90);
                right2 = raycasting.getRay(RIGHT45);
                left2 = raycasting.getRay(LEFT45);

            }
        });
        // have you noticed the FlagListener interface? The Pogamut is often using {@link Flag} objects that
        // wraps some iteresting values that user might respond to, i.e., whenever the flag value is changed,
        // all its listeners are informed

        // 3. declare that we are not going to setup any other rays, so the 'raycasting' object may know what "all" is        
        raycasting.endRayInitSequence();

        // change bot's default speed
        config.setSpeedMultiplier(moveSpeed);

         getAct().act(new Configuration().setDrawTraceLines(false).setAutoTrace(true));

    }
    
      /**
     * boolean switch to activate engage behavior
     */
    @JProp
    public boolean shouldEngage = true;
    /**
     * boolean switch to activate pursue behavior
     */
    @JProp
    public boolean shouldPursue = true;
    /**
     * boolean switch to activate rearm behavior
     */
    @JProp
    public boolean shouldRearm = true;
    /**
     * boolean switch to activate collect health behavior
     */
    @JProp
    public boolean shouldCollectHealth = true;
    /**
     * how low the health level should be to start collecting health items
     */
    @JProp
    public int healthLevel = 35;
    /**
     * how many bot the hunter killed other bots (i.e., bot has fragged them /
     * got point for killing somebody)
     */
    @JProp
    public int frags = 0;
    /**
     * how many times the hunter died
     */
    @JProp
    public int deaths = 0;

     /**
      * Listener for the playerkilled action. Act when a player has been killed
      * If the current bot is the killer, we add 1 to our score
      * If our enemy have been killed, set it to null, we would not run to a 
      * dead man.
      * @param event Define an event desribing circumstances of the death of the
      * player that has been detected
      */
    @EventListener(eventClass = PlayerKilled.class)
    public void playerKilled(PlayerKilled event) {
        if (event.getKiller().equals(info.getId())) {
            ++frags;
        }
        if (enemy == null) {
            return;
        }
        if (enemy.getId().equals(event.getId())) {
            enemy = null;
        }
    }
    /**
     * Used internally to maintain the information about the bot we're currently
     * hunting, i.e., should be firing at.
     */
    protected Player enemy = null;
    /**
     * Item we're running for. 
     */
    protected Item item = null;
    /**
     * Taboo list of items that are forbidden for some time.
     */
    protected TabooSet<Item> tabooItems = null;
    
    private UT2004PathAutoFixer autoFixer;
    
	private static int instanceCount = 0;

    /**
     * Bot's preparation - called before the bot is connected to GB2004 and
     * launched into UT2004.
     * @param bot
     */
    @Override
    public void prepareBot(UT2004Bot bot) {
        tabooItems = new TabooSet<Item>(bot);

        autoFixer = new UT2004PathAutoFixer(bot, navigation.getPathExecutor(), fwMap, aStar, navBuilder); // auto-removes wrong navigation links between navpoints

        // listeners        
        navigation.getState().addListener(new FlagListener<NavigationState>() {

            @Override
            public void flagChanged(NavigationState changedValue) {
                switch (changedValue) {
                    case PATH_COMPUTATION_FAILED:
                    case STUCK:
                       reset();
                        enemy = players.getNearestEnemy(2000);
                        if(enemy != null)
                        runTo(enemy.getLocation());
                        break;

                    case TARGET_REACHED:
                        reset();
                        break;
                }
            }
        });

        // DEFINE WEAPON PREFERENCES
        weaponPrefs.addGeneralPref(UT2004ItemType.ROCKET_LAUNCHER, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.MINIGUN, false);   
        weaponPrefs.addGeneralPref(UT2004ItemType.LINK_GUN, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.SHOCK_RIFLE, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.LIGHTNING_GUN, true); 
        
        pref = weaponPrefs.getPreferredWeapons().iterator().next();
        hasPref = false;
        current = itemst;
    }

    /**
     *
     * @return
     */
    @Override
    public Initialize getInitializeCommand() {
        // just set the name of the bot and his skill level, 1 is the lowest, 7 is the highest
    	// skill level affects how well will the bot aim
        return new Initialize().setName("LepsisBot FTW" + (++instanceCount)).setDesiredSkill(7);
    }

    /**
     * Resets the state of the Hunter.
     */
    protected void reset() {
    	item = null;
        enemy = null;
        navigation.stopNavigation();
        itemsToRunAround = null;
    }
  /**
     * Used by the different states to change the current state of the bot, acc-
     * ording to intern conditions
     * @param etat
     */
    public void changeState(State etat){
        this.current = etat;
    }

    
     /**
     */
    @Override
    public void logic() {
        if(info.getHealth()>=100){
            tabooItems.addAll(items.getAllItems(ItemType.Category.HEALTH).values());
        }
        else{
            tabooItems.clear();
        }
        current.act();
    }

    /**
     *
     */
    protected boolean runningToPlayer = false;

    /**
     *
     * @param enemy
     */
    protected void runTo(Location enemy){
        move.moveTo(enemy.getLocation());
        move.strafeLeft(Math.sqrt(Math.pow(bot.getVelocity().y/2,2) +Math.pow(bot.getVelocity().x/2,2)));
        move.strafeLeft(Math.sqrt(Math.pow(bot.getVelocity().y/2,2) +Math.pow(bot.getVelocity().x/2,2)));
        move.strafeRight(Math.sqrt(Math.pow(bot.getVelocity().y,2) +Math.pow(bot.getVelocity().x,2)));
    }

    /**
     *
     */
    protected List<Item> itemsToRunAround = null;

    ////////////////
    // BOT KILLED //
    ////////////////
 /**
     *
     * @param event
     */
    @Override
    public void botKilled(BotKilled event) {
    	reset();
    }
    /**Main
     * 
     * @param args if present they will define custom adress and port of the 
     * server where we want the bot to connect
     * @throws PogamutException 
     */
    public static void main(String[] args) throws PogamutException {
        
        String host = "localhost";
        int port = 3000;

        if (args.length > 0)
        {
            host = args[0];
        }
        if (args.length > 1)
        {
            String customPort = args[1];
            try
            {
                port = Integer.parseInt(customPort);
            }
            catch (Exception e)
            {
                System.out.println("Invalid port. Expecting numeric. Resuming with default port: "+port);
            }
        }


        while (true)
        {
            try
            {
                UT2004BotRunner runner = new UT2004BotRunner(LepsisBot.class, "LEPSIS - COSYS ", host, port);
                runner.setMain(true);
                runner.setLogLevel(Level.SEVERE);
                runner.startAgent();
                Thread.sleep(1500);
            }
            catch (ComponentCantStartException e)
            {
                Throwable cause = e.getCause();
                if (cause instanceof ConnectionException)
                {
                    System.out.println("Connection to server failed... retrying");
                    e.printStackTrace();
                }
                else if (cause instanceof BusStoppedInterruptedException)
                {
                    e.printStackTrace();
                    System.out.println("Aborting...");
                    break;
                }
                else
                {
                    e.printStackTrace();
                    System.out.println("Some other cause for ComponentCantStartException... retrying");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.println("Some other exception... retrying");
            }
        }
    }

    void getBack() {
        move.turnTo(enemy);
       move.dodgeBack(info.getLocation().add(enemy.getLocation()), moving);
       move.strafeLeft(Math.sqrt(Math.pow(bot.getVelocity().y/2,2) +Math.pow(bot.getVelocity().x/2,2)));
       move.strafeLeft(Math.sqrt(Math.pow(bot.getVelocity().y/2,2) +Math.pow(bot.getVelocity().x/2,2)));
       move.strafeRight(Math.sqrt(Math.pow(bot.getVelocity().y,2) +Math.pow(bot.getVelocity().x,2)));
    }

    /* The next methods are used in the states
     * to get informations about the bot that aren't accessible 
     * in other class
     */
    
    
    int getHealth() {
        return info.getHealth();
    }

    Map<UnrealId, Player> getVisibleEnemies() {
        return players.getVisibleEnemies();
    }

    Location getLocation() {
        return info.getLocation();
    }

    Object shoot(Player enemy) {
        move.turnTo(enemy);
        return shoot.shoot(weaponPrefs,enemy);
    }

    void stopNavigation() {
        navigation.stopNavigation();
    }

    boolean isNavigatingToItem() {
        return navigation.isNavigatingToItem();
    }

    boolean hasLoadedWeapon(ItemType itemType) {
        return weaponry.hasLoadedWeapon();
    }

    Map<UnrealId, Item> getSpawnedItems(ItemType itemType) {
        return items.getSpawnedItems();
    }

    boolean canSeePlayers() {
        return players.canSeePlayers();
    }

    Map<UnrealId, Player> getEnemies() {
        return players.getEnemies();
    }

    void navigate(Player get) {
        navigation.navigate(get);
    }
    
    void navigate(Item i) {
        navigation.navigate(i);
    }

    Item getPathNearestSpawnedItem(ItemType.Category category) {
        return items.getNearestSpawnedItem(category);
    }

    Item getNearestPrefWeapon() {
       return items.getNearestSpawnedItem(pref.getWeapon());
    }

   Item getNearestSpawnItem() {
       return items.getNearestSpawnedItem();
    }

    void log(String message) {
        log.info(message);
    }

    boolean getPrefWeapon() {
        return weaponry.hasWeapon(pref.getWeapon());
    }

    void stopShooting() {
        shoot.stopShooting();
    }

    Player getNearestEnemies() {
        return players.getNearestEnemy(2000);
    }

    void Randomnavigate() {
        navigation.navigate(navPoints.getRandomNavPoint());
    }

    boolean isNavigating() {
        return navigation.isNavigatingToItem();
    }

    void shootSecondary(Player enemy) {
        move.turnTo(enemy);
        shoot.shootSecondary(enemy);
    }

    void strafe() {
        if(new Random().nextInt(20)>13){
        if(left.isResult() || left2.isResult()){
        move.dodgeRight(enemy.getLocation(), true);
        move.dodgeRight(enemy.getLocation(), false);
        move.dodgeRight(enemy.getLocation(), false);
        move.dodgeLeft(enemy.getLocation(), true);
        }else if(right.isResult() || right2.isResult()){
        move.dodgeLeft(enemy.getLocation(), true);   
        move.dodgeLeft(enemy.getLocation(), false);
        move.dodgeLeft(enemy.getLocation(), false);
        move.dodgeRight(enemy.getLocation(), true);
        }
        else if(new Random().nextInt(20)>13){
        move.dodgeLeft(enemy.getLocation(), false);
        move.dodgeLeft(enemy.getLocation(), false);
        move.dodgeRight(enemy.getLocation(), true);
        }else{
        move.dodgeRight(enemy.getLocation(), false);
        move.dodgeRight(enemy.getLocation(), false);
        move.dodgeLeft(enemy.getLocation(), true);
        }
        }
        else
        {
         if(left.isResult() || left2.isResult()){
        move.strafeRight(4);
        move.strafeRight(2);
        move.strafeRight(2);
        move.strafeLeft(4);
        }else if(right.isResult() || right2.isResult()){
        move.strafeLeft(4);   
        move.strafeLeft(2);
        move.strafeLeft(2);
        move.strafeRight(4);
        }
        else if(new Random().nextInt(20)>13){
        move.strafeRight(2);
        move.strafeRight(2);
        move.strafeLeft(4);
        }else{
        move.strafeLeft(2);
        move.strafeLeft(2);
        move.strafeRight(4);
        }   
        }
    }
    
    
        boolean isHit() {
        return senses.isBeingDamaged();
    }

    void searchEnemy() {
        getAct().act(new Rotate().setAmount(32000));
    }

    void majWeapon() {
      Map<ItemType,Weapon> map =  weaponry.getLoadedRangedWeapons();
      Collection<ItemType> weapons = map.keySet();
      for(ItemType w : weapons){
          if(weaponry.hasAmmoForWeapon(w)){
              weaponry.changeWeapon(w);
              searching_ammo = false;
              return;
          }
      }
      searching_ammo = true;
      navigation.navigate(items.getNearestSpawnedItem(Category.AMMO));
    }
    
    
}
