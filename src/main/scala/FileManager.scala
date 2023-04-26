import javax.xml.stream.events.Attribute
import scala.collection.mutable.Buffer
import scala.io.Source
import scala.xml
import scala.xml.{Elem, Node, UnprefixedAttribute}
import scala.xml.transform.RewriteRule

class FileManager():
  private var loadedMap = ""

  //create game map from map-file
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
        case 4 => Marsh(gridCoords)
        case 5 => Road(gridCoords)

    val map =
      numGrid.toVector.zipWithIndex
        .map(row => (row._1.zipWithIndex
        .map(cell => numberToTile(cell._1, (cell._2, row._2)))).toVector)

    //update loaded map
    loadedMap = mapName
    //return map
    map

  //set player controllers for given file
  def setPlayersControl(fileName: String, redCPU: Boolean, blueCPU: Boolean) =
    val file = xml.XML.loadFile(("data\\text\\" + fileName))
    val state = file \\ "game_state"

    val redControl = if redCPU then "CPU" else "Player"
    val blueControl = if blueCPU then "CPU" else "Player"

    def getPlayerControl(node: Node) =
      (node \@ "color") match
        case "Red" => redControl
        case "Blue" => blueControl

    val modifiedPlayers = (state \\ "player").map(player =>
      <player color={player \@ "color"}>
        <control>{getPlayerControl(player)}</control>
        {player \\ "resources"}
      </player>)

    val modifiedState =
      <game_state>
        {modifiedPlayers}
        {state \\ "acting_player"}
      </game_state>

    val modifiedFile =
      <save>
        {file \\ "game_map"}
        {modifiedState}
        {file \\ "areas"}
        {file \\ "troops"}
      </save>

    xml.XML.save("data\\text\\"+fileName, modifiedFile)


  //load given save
  def loadSave(saveName: String) =
    val saveFile = xml.XML.loadFile(("data\\text\\" + saveName))
    val game = Game(GameLevel(gameMapReader((saveFile \\ "game_map").text)))
    val gameState = saveFile \\ "game_state"

    //initialize adjancent tiles in tiles
    val gameLevel = game.gameLevel
    def adjacentTiles(tile: Tile) =
      val x = tile.coords._1
      val y = tile.coords._2
      Vector((x-1, y), (x+1, y), (x, y-1), (x, y+1))
        .filterNot(c => c._1 < 0 | c._1 >= gameLevel.gridWidth | c._2 < 0 | c._2 >= gameLevel.gridHeight)
        .map(gameLevel.tileAt(_))
    for row <- gameLevel.tileGrid do
      for tile <- row do
        tile.adjacentTiles = adjacentTiles(tile)

    def playerColor(player: xml.NodeSeq) =
      (player \@ "color") match
        case "Red" => RedPlayer
        case "Blue" => BluePlayer

    def setUpPlayers(playerData: xml.NodeSeq) =
      var player = playerColor(playerData)
      player.resources = (playerData \\ "resources").text.toInt
      player.cpu = (playerData \\ "control").text match
        case "CPU" => Some(AI(game, player))
        case "Player" => None

    def locationDataToCoords(locData: String) =
      val coordsInArray = locData.split(",").map(_.toInt)
      (coordsInArray(0), coordsInArray(1))

    //set acting player
    (gameState \\ "acting_player").text match
      case "Red" => game.gameState.actingPlayer = RedPlayer
      case "Blue" => game.gameState.actingPlayer = BluePlayer

    //load troops
    for player <- (saveFile \\ "troops") do
      val color = playerColor(player)
      val troops = gameLevel.playerTroops(color)
      for troopData <- player \\ "troop" do
        val troopCount = gameLevel.troopCount
        val tile = game.gameLevel.tileAt(locationDataToCoords((troopData \\ "location").text))
        //make correct troop
        val troop = (troopData \\ "type").text match
          case "solider" => Solider(color, tile, troopCount)
          case "tank" => Tank(color, tile, troopCount)
          case "artillery" => Artillery(color, tile, troopCount)
          case "apache" => Apache(color, tile, troopCount)
          case "sniper" => Sniper(color, tile, troopCount)
        //set troop hp and status
        (troopData \\ "hp").text match
          case "full" =>
          case notFull => troop.modifyStat(Stat.Hp, notFull.toInt - troop.hp)
        //add troop to GameLevel
        gameLevel.addTroop(troop)
        //set troop status
        (troopData \\ "status").text match
          case "ready" => troop.refresh()
          case "moved" => troop.exhausted = false
          case "exhausted" =>


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
        case "None" =>
        case "Red" => settlement.updateScore(Some(RedPlayer))
        case "Blue" => settlement.updateScore(Some(BluePlayer))
      //set settlement strength
      settlement.strength = (settlementData \\ "strength").text.toInt
      game.gameLevel.areas += settlement

    //set up each player
    (gameState \\ "player").foreach(setUpPlayers(_))

    //return loaded game
    game

  //save game to saveFile
  def saveGame(game: Game) =
    val gameState =
      <game_state>
        <player color="Red">
          <control>{if RedPlayer.isCPU then "CPU" else "Player"}</control>
          <resources>{RedPlayer.resources}</resources>
        </player>
        <player color="Blue">
          <control>{if BluePlayer.isCPU then "CPU" else "Player"}</control>
          <resources>{BluePlayer.resources}</resources>
        </player>
        <acting_player>{game.gameState.actingPlayer.toString}</acting_player>
      </game_state>

    //grid-coordinates to String
    def locationData(coords: (Int, Int)) =
      coords._1 + "," + coords._2

    def areaTiles(area: Area) =
      val tileCoords = Buffer[xml.Node]()
      for tileCoord <- area.tiles.map(a => locationData(a.coords)) do
        tileCoords += <tile>{tileCoord}</tile>
      val a = tileCoords.toSeq
      <tiles>
        {a}
      </tiles>

    def baseData(base: Area, color: String) =
      <base color={color}>
        {areaTiles(base)}
        <strength>{base.strength}</strength>
      </base>

    def settlementData(settlements: Vector[Area]) =
      val data = Buffer[xml.Node]()
      for settlement <- settlements do
        val controller = settlement.controller match
          case Some(player) => player.toString
          case None => "None"
        data +=
          {
            <settlement>
              {areaTiles(settlement)}
              <controller>{controller}</controller>
              <strength>{settlement.strength}</strength>
            </settlement>
          }
      data.toSeq

    def troopData(troops: Vector[Troop], player: Player) =
      val playerTroops = game.gameLevel.playerTroops(player)
      val data = Buffer[xml.Node]()
      for troop <- playerTroops do
        val status =
          if troop.exhausted then
            "exhausted"
          else if troop.hasMoved then
            "moved"
          else
            "ready"
        data +={
            <troop>
              <type>{troop.id}</type>
              <location>{locationData(troop.gridCoords)}</location>
              <hp>{troop.stats(Stat.Hp)}</hp>
              <status>{status}</status>
            </troop>}
      data.toSeq

    val areas =
      <areas>
        {baseData(game.gameLevel.areas.head, "Red")}
        {baseData(game.gameLevel.areas(1), "Blue")}
        {settlementData(game.gameLevel.areas.drop(2).toVector)}
      </areas>

    val troops =
      <troops color="Red">
        {troopData(game.gameLevel.troops.toVector, RedPlayer)}
      </troops>
      <troops color="Blue">
        {troopData(game.gameLevel.troops.toVector, BluePlayer)}
      </troops>

    val saveData =
      <save>
        <game_map>{loadedMap}</game_map>
        {gameState}
        {areas}
        {troops}
      </save>

    xml.XML.save("data\\text\\saveFile.xml", saveData)

  //clears given save
  def clearSave(saveName: String) = xml.XML.save("data\\text\\" + saveName, <save></save>)

  //checks if given save is empty
  def nonEmptySave(saveName: String) = xml.XML.loadFile(("data\\text\\" + saveName)).head.text.nonEmpty

end FileManager