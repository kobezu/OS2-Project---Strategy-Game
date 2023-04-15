import Stat.Hp

import scala.collection.mutable.Buffer

class GameLevel(val tileGrid: Vector[Vector[(Tile)]]):
  var troopCount = 0
  val troops = Buffer[Troop]()
  var redTroops = Vector[Troop]()
  var blueTroops = Vector[Troop]()
  val areas = Buffer[Area]()

  def gridWidth: Int = tileGrid(0).size
  def gridHeight: Int = tileGrid.size

  //returns all coordinates that are within range of the given coordinate
  def coordsAtRange(gridCoords: (Int, Int), range: Int): Vector[(Int, Int)] =
    val coordsBuffer = Buffer[(Int, Int)]()
    for y <- -range to range do
      val yCoord = gridCoords._2 + y
      if yCoord >= 0 && yCoord < gridHeight then
        for x <- (-range + y.abs) to (range - y.abs) do
          val xCoord = gridCoords._1 + x
          if xCoord >= 0 && xCoord < gridWidth && !(xCoord == gridCoords._1 && yCoord == gridCoords._2) then
            coordsBuffer += ((xCoord, yCoord))
    coordsBuffer.toVector

  //counts movement cost for moving to each tile at given range from given tile
  def tileMoveToCosts(startTile: Tile, range: Int, troopType: TroopType, costs: Array[Array[Int]]): Array[Array[Int]] =
    val costAtCoords = costs(startTile.coords._2)(startTile.coords._1)
    if range > 0 then
      for adjacent <- startTile.adjacentTiles do
        val x = adjacent.coords._1
        val y = adjacent.coords._2
        val newCost = costAtCoords + startTile.movementCost(troopType)
        val oldCost = costs(y)(x)
        if newCost < oldCost then
          costs(y)update(x, newCost)
          tileMoveToCosts(adjacent, range-1, troopType, costs)
    costs

  //returns each tile that troop can reach with given movement range
  def tilesAtMovementRange(troop: Troop) =
    val costs = Array.fill[Int](gridHeight, gridWidth)(troop.stats(Stat.Mov) + 1)
    costs(troop.gridCoords._2).update(troop.gridCoords._1, 0)
    val numbers = tileMoveToCosts(troop.currentTile, troop.stats(Stat.Mov), troop.troopType, costs)
    val route = numbers.zipWithIndex
      .flatMap(row => (row._1.zipWithIndex
      .map(cell => (tileAt((cell._2, row._2)), cell._1)))).toVector
    route.filterNot(_._2 > troop.stats(Stat.Mov)).map(_._1)

  //returns the Tile-object at given grid-coordinates
  def tileAt(coords: (Int, Int)): Tile = tileGrid(coords._2)(coords._1)

  def addTroop(troop: Troop) =
    troop.controller match
      case RedPlayer => redTroops = redTroops ++ Vector(troop)
      case BluePlayer => blueTroops = blueTroops ++ Vector(troop)
    troops += troop
    troopCount += 1

  def removeTroop(troop: Troop) =
    troop.controller match
      case RedPlayer => redTroops = redTroops.filterNot(_ == troop)
      case BluePlayer => blueTroops = blueTroops.filterNot(_ == troop)
    troops -= troop
    troop.currentTile.removeTroop()

  def playerTroops(player: Player) =
    player match
      case RedPlayer => redTroops
      case BluePlayer => blueTroops

  //only used by AI
  //counts movement cost for moving from each tile from given tile
  def tileMoveFromCosts(startTile: Tile, troopType: TroopType, costs: Array[Array[Int]]): Array[Array[Int]] =
    val costAtCoords = costs(startTile.coords._2)(startTile.coords._1)
    for adjacent <- startTile.adjacentTiles do
      val x = adjacent.coords._1
      val y = adjacent.coords._2
      val newCost = costAtCoords + adjacent.movementCost(troopType)
      val oldCost = costs(y)(x)
      if newCost < oldCost then
        costs(y).update(x, newCost)
        tileMoveFromCosts(adjacent, troopType, costs)
    costs
end GameLevel
