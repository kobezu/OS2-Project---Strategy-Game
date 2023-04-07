import scala.collection.mutable.Buffer

class GameLevel(numGrid: Vector[Vector[(Int)]]):

  val troops = Buffer[Troop](Solider(RedPlayer, (3, 4)),Solider(RedPlayer, (6, 5)),Solider(BluePlayer, (7, 5)))

  def numberToTile(number: Int, gridCoords: (Int, Int)): Tile =
    number match
      case 0 => Plain(gridCoords)
      case 1 => Forest(gridCoords)
      case 2 => Water(gridCoords)

  //grid of Tile-objects based on grid of numbers
  val tileGrid =
    numGrid.zipWithIndex
      .map(row => (row._1.zipWithIndex
        .map(cell => numberToTile(cell._1, (cell._2, row._2)))))

  val areas =
    Vector[Area](Base(Vector((3,11), (4,11), (3,10), (4, 10)).map(tileAt(_)), RedPlayer), 
    Base(Vector((21,11), (20,11), (21,10), (20, 10)).map(tileAt(_)), BluePlayer), 
    Settlement(Vector((3,4), (4,4), (3,5), (4, 5)).map(tileAt(_))),
    Settlement(Vector((8,4), (9,4), (8,5), (9, 5)).map(tileAt(_))))

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

  //returns the Tile-object at given grid-coordinates
  def tileAt(coords: (Int, Int)): Tile = tileGrid(coords._2)(coords._1)
  
end GameLevel
