import scala.collection.mutable.Buffer

class GameLevel(numGrid: Vector[Vector[Int]]):

  val troops = Buffer[Troop](Solider(Red, (3, 4)),Solider(Red, (6, 5)),Solider(Blue, (7, 5)))

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
