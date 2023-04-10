import scala.collection.mutable.Buffer
import scala.io.Source
import scala.xml

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

  //load given save
  def loadSave(saveName: String) =
    val saveFile = xml.XML.loadFile(("data\\text\\" + saveName))
    val game = Game(GameLevel(gameMapReader((saveFile \\ "game_map").text)))
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
    for player <- (saveFile \\ "troops") do
      val color = playerColor(player)
      for troopData <- player \\ "troop" do
        val coords = locationDataToCoords((troopData \\ "location").text)
        //make correct troop
        val troop = (troopData \\ "type").text match
          case "solider" => Solider(color, coords)
          case "tank" => Tank(color, coords)
          case "artillery" => Artillery(color, coords)
          case "apache" => Apache(color, coords)
          case "sniper" => Sniper(color, coords)
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
        case "None" =>
        case "Red" => settlement.updateScore(Some(RedPlayer))
        case "Blue" => settlement.updateScore(Some(BluePlayer))
      //set settlement strength
      settlement.strength = (settlementData \\ "strength").text.toInt
      game.gameLevel.areas += settlement
    //return loaded game
    game

  //save game to saveFile
  def saveGame(game: Game) =
    val gameState =
      <game_state>
        <player color="Red">
          <resources>{RedPlayer.resources}</resources>
        </player>
        <player color="Blue">
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
      val playerTroops = game.gameLevel.troops.filter(_.controller == player)
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
              <hp>{troop.hp}</hp>
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

end FileManager