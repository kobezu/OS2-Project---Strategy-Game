import scala.collection.mutable.Buffer

class GameLevel(val tileGrid: Vector[Vector[(Tile)]]):

  val troops = Buffer[Troop]()

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

  //counts movement cost for each tile at given range from given tile
  def tileMovementCosts(startTile: (Tile, Int), range: Int, troopType: TroopType, tiles: Buffer[(Tile, Int)]): Buffer[(Tile, Int)] =
    val tilesWithCosts = tiles
    if range > 0 then
      for adjacent <- adjacentTiles(startTile._1) do
        val cost = startTile._2 + startTile._1.movementCost(troopType)
        if !tilesWithCosts.exists(_._1 == adjacent) then
          tilesWithCosts += ((adjacent, cost))
          tilesWithCosts ++ tileMovementCosts((adjacent, cost), range-1, troopType, tilesWithCosts)
        else
          tilesWithCosts.find(_ == adjacent && _ > cost) match
            case Some(a) =>
              tilesWithCosts.update(tilesWithCosts.indexOf(a), (adjacent, cost))
              tilesWithCosts ++ tileMovementCosts((adjacent, cost), range-1, troopType, tilesWithCosts)
            case None =>
    tilesWithCosts

  //returns each tile that troop can reach with given movement range
  def tilesAtMovementRange(startTile: Tile) =
    val troop = startTile.troop.get
    tileMovementCosts((startTile, 0), troop.movement, troop.troopType, Buffer[(Tile, Int)]()).filterNot(_._2 > troop.movement)

  //returns the Tile-object at given grid-coordinates
  def tileAt(coords: (Int, Int)): Tile = tileGrid(coords._2)(coords._1)

  def adjacentTiles(tile: Tile) =
    val x = tile.coords._1
    val y = tile.coords._2
    Vector((x-1, y), (x+1, y), (x, y-1), (x, y+1))
      .filterNot(c => c._1 < 0 | c._1 >= gridWidth | c._2 < 0 | c._2 >= gridHeight)
      .map(tileAt(_))
end GameLevel
