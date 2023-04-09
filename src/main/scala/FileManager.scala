import scala.collection.mutable.Buffer
import java.io.FileInputStream
import java.io.FileReader
import scala.io.Source
import scala.xml

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

  //load given save file
  def loadSaveData(saveName: String) =
    val saveFile = xml.XML.loadFile(("data\\text\\" + saveName))
    val gameState = saveFile \\ "game_state"

    def playerColor(player: xml.NodeSeq) =
      (player \@ "color") match
        case "Red" => RedPlayer
        case "Blue" => BluePlayer

    def setUpPlayers(playerData: xml.NodeSeq) =
      var player = playerColor(playerData)
      player.resources = (playerData \\ "resources").text.toInt

    //set up each player
    (gameState \\ "player").foreach(setUpPlayers(_))

    def locationDataToCoords(locData: String) =
      val coordsInArray = locData.split(",").map(_.toInt)
      (coordsInArray(0), coordsInArray(1))

    //set acting player
    (gameState \\ "acting_player").text match
      case "Red" => game.gameState.actingPlayer = RedPlayer
      case "Blue" => game.gameState.actingPlayer = BluePlayer

    //load troops
    for player <- (saveFile \\ "troops" \\ "player") do
      val color = playerColor(player)
      for troopData <- player \\ "troop" do
        val coords = locationDataToCoords((troopData \\ "location").text)
        //make correct troop
        val troop = (troopData \\ "type").text match
          case "solider" => Solider(color, coords)
        //set troop hp and status
        (troopData \\ "hp").text match
          case "full" =>
          case notFull => troop.hp = notFull.toInt
        (troopData \\ "status").text match
          case "ready" =>
          case "moved" => troop.hasMoved = true
          case "exhausted" => troop.exhausted = true
        //add troop to GameLevel
        game.gameLevel.troops += troop

    //get list of tiles according to tile data
    def getTiles(tileData: xml.NodeSeq) =
      tileData.map(a => game.gameLevel.tileAt(locationDataToCoords(a.text))).toVector

    //load areas
    for baseData <- (saveFile \\ "areas" \\ "base") do
      val base = Base(getTiles(baseData \\ "tiles" \\ "tile"), playerColor(baseData))
      //set base strength
      base.strength = (baseData \\ "strength").text.toInt
      game.gameLevel.areas += base
    for settlementData <- (saveFile \\ "areas" \\ "settlement") do
      val settlement = Settlement(getTiles(settlementData \\ "tiles" \\ "tile"))
      //set settlement controller
      (settlementData \\ "controller").text match
        case "none" =>
        case "red" => settlement.updateScore(Some(RedPlayer))
        case "blue" => settlement.updateScore(Some(BluePlayer))
      //set settlement strength
      settlement.strength = (settlementData \\ "strength").text.toInt
      game.gameLevel.areas += settlement

end FileManager