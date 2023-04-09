import scala.collection.mutable.Buffer
import java.io.FileInputStream
import java.io.FileReader
import scala.io.Source

class FileManager(game: Game):

  def gameMapReader(mapName: String) =
    val mapDataSource = Source.fromFile("data\\text\\" + mapName)
    val lines = Buffer[String]()
    var source = mapDataSource
    for line <- source.getLines() do
      lines += line
    mapDataSource.close()

    val mapDataGrid = lines.map(_.split("\t"))
    val numGrid  = mapDataGrid.map(a => a.map(b => (b.toInt)))

    def numberToTile(number: Int, gridCoords: (Int, Int)): Tile =
      number match
        case 1 => Plain(gridCoords)
        case 2 => Forest(gridCoords)
        case 3 => Water(gridCoords)

    val map =
      numGrid.toVector.zipWithIndex
        .map(row => (row._1.zipWithIndex
        .map(cell => numberToTile(cell._1, (cell._2, row._2)))).toVector)
    map

end FileManager