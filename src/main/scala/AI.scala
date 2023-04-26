import scala.util.Random
import scala.collection.mutable.Buffer
import scala.collection.mutable.Map

class AI(game: Game, val player: Player):
  private val gameState = game.gameState
  private val gameLevel = game.gameLevel
  private val enemy = if player == RedPlayer then BluePlayer else RedPlayer
  //AIs base
  private val base = player match
    case RedPlayer => gameLevel.areas(0)
    case BluePlayer => gameLevel.areas(1)
  //each area mapped to one tile in the area
  private val destinations = gameLevel.areas.map(a => (a, a.tiles(a.tiles.length/2))).toMap
  //routes for each areas with each TroopType
  private val destinationsWithRoutes = Map[TroopType, Map[Area, Vector[Vector[Int]]]](initializeRoutes(TroopType.Human),
    initializeRoutes(TroopType.Vehicle), initializeRoutes(TroopType.Flying))
  //each area with player weights
  private val areaWeights: Map[Area, (Double, Double)] = gameLevel.areas.map(a => (a, weightArea(a))).to(Map)
  //troop that AI will build next
  private var buildFocus = "Solider"
  //areas with troops AI has commited there
  private var commitsToAreas: Map[Area, Buffer[Troop]] = gameLevel.areas.map(a => (a, Buffer[Troop]())).to(Map)
  //troops that are not commited to any area
  private val uncommitedTroops = ownTroops.toBuffer
  //maps troop to the area it's commited
  private val troopsWithCommits: Map[Troop, Option[Area]] = ownTroops.map(a => (a, None)).to(Map)

  def ownTroops = gameLevel.playerTroops(player)
  def enemyTroops = gameLevel.playerTroops(enemy)

  //method used for initializing routes to areas
  def initializeRoutes(troopType: TroopType) =
    val routes = Map[Area, Vector[Vector[Int]]]()
    for item <- destinations do
      val destination = item._2
      val costs = Array.fill[Int](gameLevel.gridHeight, gameLevel.gridWidth)(250)
      costs(destination.coords._2).update(destination.coords._1, destination.movementCost(troopType))
      val numbers: Array[Array[Int]] = gameLevel.tileMoveFromCosts(destination, troopType, costs)
      val route = numbers.map(_.toVector).toVector
      //add newly made route to routes
      routes += (item._1 -> route)
    troopType -> routes

  //get movement cost to given area for given troop
  def getCost(troop: Troop, area: Area) =
    destinationsWithRoutes(troop.troopType)(area)(troop.gridCoords._2)(troop.gridCoords._1)

  //returns troop value
  def troopValue(troop: Troop) = troop.cost + 3

  //count each troops cost and distance to area and sum them to get player weight-score
  def weightArea(area: Area) =
    var enemyWeight = 0.0
    var ownWeight = 0.0
    def countWeight(troop: Troop) =
      val distance = getCost(troop, area)
      val distanceValue = if distance < 10 then 1 else distance / 10.0
      if troop.controller == player then
        ownWeight += (1.0 / distanceValue) * troopValue(troop)
      else enemyWeight += (1.0 / distanceValue) * troopValue(troop)
    for troop <- gameLevel.troops do
       countWeight(troop)
    (ownWeight, enemyWeight)

  //update each area's weight-values
  def updateWeights() =
    for area <- gameLevel.areas.drop(2) do
      areaWeights(area) = weightArea(area)

  def tryCommitToArea(): Boolean =
    val commitedAreas = commitsToAreas.filter(_._2.nonEmpty).keySet
    //get uncommited area with lowest enemy weight and highest own weight
    val areaWeight = areaWeights.filterNot(a => commitedAreas.contains(a._1)).maxByOption(a => a._2._1 - a._2._2)
      areaWeight match
        case Some(weight) =>
          val commitedArea = weight._1
          val enemyWeight = weight._2._2
          val neededTroopValue = (enemyWeight/2 + 10).toInt
          //order uncommited troops by cost to move to the commited area
          val orderedByMoveCost = uncommitedTroops.map(a => (a, getCost(a, commitedArea))).filter(_._2 < 250).sortBy(_._2).map(_._1)
          //count if uncommited troops have enough troop value to commit to the area
          var i = 0
          var troopValueSum = 0
          while  troopValueSum < neededTroopValue && i < orderedByMoveCost.length do
            troopValueSum += troopValue(orderedByMoveCost(i))
            i += 1
          //if troops have enough value commit troops
          if troopValueSum >= neededTroopValue then
            val commitedTroops = orderedByMoveCost.take(i+1)
            val troopsCommited = commitsToAreas(commitedArea)
            for troop <- commitedTroops do
              troopsCommited += troop
              troopsWithCommits(troop) = Some(commitedArea)
            uncommitedTroops --= commitedTroops
            true
          else
            false
        case None => false

  //commits to areas until commit fails
  def commitToAreas() =
    var commitSuccess = tryCommitToArea()
    while commitSuccess do
      commitSuccess = tryCommitToArea()


  //get tile where troop will move with given destination wrapped in Option
  def getMoveTile(troop: Troop, destination: Area): Option[Tile] =
    val costMap = destinationsWithRoutes(troop.troopType)(destination)
    val tilesAtMoveRange = gameLevel.tilesAtMovementRange(troop).filter(_.isPassable).map(a => (a, (costMap(a.coords._2)(a.coords._1))))
    tilesAtMoveRange.minByOption(_._2).map(_._1)

  //move with given troop towards given destination
  def move(troop: Troop) =
    troopsWithCommits(troop) match
      case Some(area) =>
        //check if troop is already in destination
        if !area.tiles.contains(troop.currentTile) then
          val endTile = getMoveTile(troop, area)
          endTile match
            case Some(tile) => troop.move(tile)
            case None =>
      case None =>

  //return target that troop will attack wrapped in Option
  def attack(troop: Troop): Option[Troop] =
    val coordsAtRange = gameLevel.coordsAtRange(troop.gridCoords, troop.range)
    val potentialTargets = enemyTroops.filter(a => coordsAtRange.contains(a.gridCoords))

    def attackValues(target: Troop) =
      val damage = troop.attackDamage(target)
      (target, damage, target.dies(damage))

    val targetsWithValues = potentialTargets.map(a => attackValues(a))
    val dyingTargets = targetsWithValues.filter(_._3)
    val targetsByDamage = targetsWithValues.sortBy(_._2)

    if dyingTargets.nonEmpty then
      Some(dyingTargets.minBy(a => troop.wastedAttackDamage(a._1))._1)
    else if targetsByDamage.nonEmpty then
      Some(targetsByDamage.last._1)
    else None

  def updateTroops() =
    //remove dead troops from commits
    val deadTroops = troopsWithCommits.keySet.filter(_.isDead)
    for troop <- deadTroops do
      troopsWithCommits.remove(troop) match
        case Some(area) =>
          area.foreach(a => commitsToAreas(a) -= troop)
        case None =>
    uncommitedTroops.foreach(a => if a.isDead then uncommitedTroops -= a)


  //build troops until resources are not enough for build focus
  def buildTroops(): Buffer[Troop] =
    val troops = Vector("Solider", "Sniper", "Tank", "Artillery", "Apache")
    val builtTroops = Buffer[Troop]()

    def buildTroop() =
      base.tiles.find(_.isPassable) match
        case Some(tile) =>
          val troopCount = gameLevel.troopCount
          val troop =
            buildFocus match
              case "Solider" => Solider(player, tile, troopCount)
              case "Sniper" => Sniper(player, tile, troopCount)
              case "Tank" => Tank(player, tile, troopCount)
              case "Artillery" => Artillery(player, tile, troopCount)
              case "Apache" => Apache(player, tile, troopCount)
          val endResources = player.resources - troop.cost
          if endResources >= 0 then
            player.resources = endResources
            buildFocus = troops(Random.nextInt(troops.length))
            Some(troop)
          else
            None
        case None => None

    var builtTroop = buildTroop()
    while builtTroop.nonEmpty do
      val troop = builtTroop.get
      troop.initializeStats()
      game.gameLevel.tileAt(troop.gridCoords).moveTo(troop)
      gameLevel.addTroop(troop)
      builtTroops += troop
      troopsWithCommits += troop -> None
      builtTroop = buildTroop()
    uncommitedTroops ++= builtTroops

end AI

