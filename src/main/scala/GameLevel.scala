import scala.collection.mutable.Buffer

class GameLevel(val tileGrid: Vector[Vector[(Tile)]]):

  val troops = Buffer[Troop]()
    /*(Solider(RedPlayer, (3, 4)),Solider(RedPlayer, (6, 5)),Solider(BluePlayer, (7, 5)))*/

  val areas =
    Vector[Area]()
    /*
    (Base(Vector((3,11), (4,11), (3,10), (4, 10)).map(tileAt(_)), RedPlayer),
    Base(Vector((21,11), (20,11), (21,10), (20, 10)).map(tileAt(_)), BluePlayer),
    Settlement(Vector((3,4), (4,4), (3,5), (4, 5)).map(tileAt(_))),
    Settlement(Vector((8,4), (9,4), (8,5), (9, 5)).map(tileAt(_))))
    */
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
  def tileMovementCosts(startTile: (Tile, Int), range: Int, tiles: Buffer[(Tile, Int)]): Buffer[(Tile, Int)] =
    val tilesWithCosts = tiles
    if range > 0 then
      for adjacent <- adjacentTiles(startTile._1) do
        val cost = startTile._2 + startTile._1.movementCost
        if !tilesWithCosts.exists(_._1 == adjacent) then
          tilesWithCosts += ((adjacent, cost))
          tilesWithCosts ++ tileMovementCosts((adjacent, cost), range-1, tilesWithCosts)
        else
          tilesWithCosts.find(_ == adjacent && _ > cost) match
            case Some(a) =>
              tilesWithCosts.update(tilesWithCosts.indexOf(a), (adjacent, cost))
              tilesWithCosts ++ tileMovementCosts((adjacent, cost), range-1, tilesWithCosts)
            case None =>
    tilesWithCosts

  //returns each tile that troop can reach with given movement range
  def tilesAtMovementRange(startTile: Tile, movRange: Int) =
    tileMovementCosts((startTile, 0), movRange, Buffer[(Tile, Int)]()).filterNot(_._2 > movRange)

  //returns the Tile-object at given grid-coordinates
  def tileAt(coords: (Int, Int)): Tile = tileGrid(coords._2)(coords._1)

  def adjacentTiles(tile: Tile) =
    val x = tile.coords._1
    val y = tile.coords._2
    Vector((x-1, y), (x+1, y), (x, y-1), (x, y+1))
      .filterNot(c => c._1 < 0 | c._1 >= gridWidth | c._2 < 0 | c._2 >= gridHeight)
      .map(tileAt(_))
end GameLevel
