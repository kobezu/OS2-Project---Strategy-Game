import scala.collection.mutable.Buffer
import javafx.scene.paint.Color
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout.Pane
import scalafx.scene.text.Font
import scalafx.scene.text.Text
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.Color.*
import scalafx.scene.shape.Rectangle
import scala.util.Random

object GameApp extends JFXApp3:
  val fileManager = FileManager()
  val game = fileManager.loadSave("newGame_1.xml")
  val redCpu: Option[AI] = Some(AI(game, RedPlayer))
  val blueCpu: Option[AI] = Some(AI(game, BluePlayer))
  //resolution of tile
  val tileRes = 24
  val scaleBy = 1
  val tileSize = 24 * scaleBy
  //height of info-area
  val tileInfoArea = 48
  val gameInfoArea = 200
  //convert scene coords to grid coords
  def sceneToGridCoords(x:Double, y: Double): (Int, Int) = ((x/tileSize).toInt, (y/tileSize).toInt)
  //convert grid coords to scene coords
  def gridToSceneCoordX(gridCoords: (Int, Int)) = gridCoords._1 * tileSize
  def gridToSceneCoordY(gridCoords: (Int, Int)) = gridCoords._2 * tileSize
  //get tile and troop images
  def getTileImage(tileId: String) = Image(FileInputStream("data\\images\\tiles\\" + tileId + ".png"))
  def getTroopImage(troop: Troop) = Image(FileInputStream("data\\images\\troops\\" + troop.id + "_" + troop.controller.toString.toLowerCase + ".png"))

  //save game when closing GameApp
  override def stopApp() = fileManager.saveGame(game)
  def start(): Unit =
    val mapWidth = tileSize * game.gameLevel.gridWidth
    val mapHeight = tileSize * game.gameLevel.gridHeight

    stage = new JFXApp3.PrimaryStage:
      title = "Game App"
      //makes window size equal to grid size
      width = mapWidth + gameInfoArea + 16
      height = mapHeight + tileInfoArea + 39

    val root = Pane()
    val scene = Scene(parent = root)
    stage.scene = scene

    //initialize branches of root
    val tiles = Pane()
    val troops = Pane()
    val userInterface = Pane()
    val highlights = Pane()
    root.children += tiles
    root.children += troops
    root.children += highlights
    root.children += userInterface
    //initialize branches of highlight
    val focusHL = Pane()
    val areaHL = Pane()
    highlights.children += focusHL
    highlights.children += areaHL
    //initialize branches of userInterface
    val menuUI = Pane()
    val staticUI = Pane()
    val infoUI = Pane()
    userInterface.children += menuUI
    userInterface.children += staticUI
    userInterface.children += infoUI
    //add static UI-elements
    val advanceTurnBtn = AdvanceTurnBtn(mapWidth-300, mapHeight)
    var gameInfo: GameInfo = GameInfo(mapWidth, 0, game.gameState.actingPlayer)
    staticUI.children += advanceTurnBtn
    staticUI.children += gameInfo

    //highlights tile in given grid coords
    def highlightTile(gridCoords: (Int, Int), color: Color) =
      val rectangle = new Rectangle:
        x = gridToSceneCoordX(gridCoords)
        y = gridToSceneCoordY(gridCoords)
        width = tileSize
        height = tileSize
        fill = color.opacity(0.4)
      focusHL.children += rectangle

    //outlines area
    def outlineArea(gridCoords: Vector[(Int, Int)], color: Color) =
      val minX = gridCoords.map(_._1).min
      val maxX = gridCoords.map(_._1).max
      val minY = gridCoords.map(_._2).min
      val maxY = gridCoords.map(_._2).max
      val lineWidth = 2 * scaleBy

      def drawVerticalLine(sceneX: Int, sceneY: Int) =
        val rectangle = new Rectangle:
          x = sceneX
          y = sceneY
          width = lineWidth
          height = tileSize
          fill = color
        rectangle

      def drawHorizontalLine(sceneX: Int, sceneY: Int) =
        val rectangle = new Rectangle:
          x = sceneX
          y = sceneY
          width = tileSize
          height = lineWidth
          fill = color
        rectangle

      for coords <- gridCoords do
        val x = coords._1
        val y = coords._2
        if x == minX then
          areaHL.children += drawVerticalLine(gridToSceneCoordX(coords), gridToSceneCoordY(coords))
        if x == maxX then
          areaHL.children += drawVerticalLine(gridToSceneCoordX(coords) + tileSize - lineWidth, gridToSceneCoordY(coords))
        if y == minY then
          areaHL.children += drawHorizontalLine(gridToSceneCoordX(coords), gridToSceneCoordY(coords))
        if y == maxY then
          areaHL.children += drawHorizontalLine(gridToSceneCoordX(coords), gridToSceneCoordY(coords) + tileSize - lineWidth)

    //refreshes all troop imageView-objects
    def refreshTroopImages() =
      troops.children.clear()
      for troop <- game.gameLevel.troops do
        val imageView = new ImageView()
        imageView.setImage(getTroopImage(troop))
        imageView.setFitWidth(tileSize)
        imageView.setFitHeight(tileSize)
        imageView.setX(gridToSceneCoordX(troop.gridCoords))
        imageView.setY(gridToSceneCoordY(troop.gridCoords))
        troop.imageViewIndex = troops.children.size //saves troop's imageView-index
        troops.children += imageView

    //updates color of area lines according to controller of the area
    def updateLines() =
      for area <- game.gameLevel.areas do
        val controlColor =
          area.controller match
            case Some(player: Player) => player.color
            case None => Gray
        outlineArea(area.tiles.map(_.coords), controlColor)

    def createTroop(troop: Troop) =
      troop.initializeStats()
      game.gameLevel.tileAt(troop.gridCoords).moveTo(troop) // saves troop to it's starting tile
      refreshTroopImages()

    def renderGameLevel() =
      //render tiles in gameLevel
      var currentX = 0
      var currentY = 0
      for row <- game.gameLevel.tileGrid do
        for tile <- row do
          val imageView = new ImageView()
          imageView.setImage(getTileImage(tile.id))
          imageView.setFitWidth(tileSize)
          imageView.setFitHeight(tileSize)
          imageView.setX(currentX)
          imageView.setY(currentY)
          tiles.children += imageView
          currentX += tileSize
        currentY += tileSize
        currentX = 0

      //render troops in gameLevel
      for troop <- game.gameLevel.troops do
        createTroop(troop)

      //render houses to areas
      for area <- game.gameLevel.areas do
        //choose tile randomly
        val tile = area.tiles(Random.nextInt(area.tiles.size-1))
        val imageView = new ImageView()
          imageView.setImage(getTileImage("settlement"))
          imageView.setFitWidth(tileSize)
          imageView.setFitHeight(tileSize)
          imageView.setX(gridToSceneCoordX(tile.coords))
          imageView.setY(gridToSceneCoordY(tile.coords))
          tiles.children += imageView
      //outline areas
      updateLines()

      //movement-range test
      def paintText(gridCoords: (Int, Int), textToPaint: String) =
        val text = new Text(textToPaint):
          font = new Font(10)
          fill = Black
          x = gridToSceneCoordX(gridCoords)
          y = gridToSceneCoordY(gridCoords) +20
        tiles.children += text
      for x <- 0 until  game.gameLevel.gridWidth do
        for y <- 0 until  game.gameLevel.gridHeight do
          paintText((x, y), x + "," + y)
      /*
      val troopType = TroopType.Human
      val startTile = game.gameLevel.tileAt((30,14))
      val costs = Array.fill[Int](game.gameLevel.gridHeight, game.gameLevel.gridWidth)(250)
      costs(startTile.coords._2).update(startTile.coords._1, 0)
      val numbers = game.gameLevel.tileMoveFromCosts(startTile, troopType, costs)
      val route = numbers.zipWithIndex
        .flatMap(row => (row._1.zipWithIndex
        .map(cell => (game.gameLevel.tileAt((cell._2, row._2)), cell._1)))).toVector
      route.foreach(a => paintText(a._1.coords, a._2.toString))
      */
    end renderGameLevel

    //listens for mouse clicks and then does things according to the mouse position and what is current action
    def handleInput() =
      root.onMouseClicked = event => {
        //check if click was inside map
        if event.getY < mapHeight && event.getX < mapWidth then
          val gridCoords = sceneToGridCoords(event.getX, event.getY)
          val clickedTile = game.gameLevel.tileAt(gridCoords)

          game.currentAction match
            //makes clicked tile the focus
            case NoFocus() | TileFocus(_) =>
              focusHL.children.clear()
              infoUI.children.clear()
              highlightTile(gridCoords, Black)
              infoUI.children += TileInfo(mapWidth - 600, mapHeight, clickedTile)
              //display area-info if clicked tile is in area
              game.gameLevel.areas.find(_.tiles.contains(clickedTile)) match
                case Some(area: Area) => infoUI.children += AreaInfo(mapWidth - 800, mapHeight, area)
                case None =>
              val clickedTroop = clickedTile.troop
              //check if clicked tile contains non-exhausted troop acting player controls
              if clickedTroop.nonEmpty then
                infoUI.children += TroopInfo(0, mapHeight, clickedTroop.get)
                if !clickedTroop.get.exhausted && clickedTroop.get.controller == game.gameState.actingPlayer then
                  game.currentAction = TroopFocus(clickedTile)
                  if clickedTroop.get.hasMoved then
                    addPopUp(TroopAttackMenu)
                  else
                    addPopUp(TroopMenu)
              else
                //check if clicked tile is in acting player base
                game.gameLevel.areas.take(2).find(_.tiles.contains(clickedTile)) match
                  case Some(base: Base) =>
                    if base.controller.contains(game.gameState.actingPlayer) then
                      addPopUp(BuildMenu)
                      game.currentAction = BuildTroop(clickedTile)
                    else game.currentAction = TileFocus(clickedTile)
                  case None => game.currentAction = TileFocus(clickedTile)

            //changes current action based on what menu element was clicked
            case TroopFocus(activeTile) =>
              val activeTroop = activeTile.troop.get
              val currentUI =
                if activeTroop.hasMoved then
                  TroopAttackMenu
                else TroopMenu
              currentUI.menuElements.find(_.tryClickMenuElement((event.getX, event.getY))) match
                case Some(menuElement) =>
                  menuUI.children.clear()
                  menuElement.name match
                    case "Move" =>
                      game.currentAction = Moving(activeTile)
                      troopMoveRange(activeTroop).filter(_ != activeTile).foreach(a =>
                        val highlightColor = if a.isPassable then LightBlue else Red
                        highlightTile(a.coords, highlightColor))
                    case "Attack" =>
                      game.currentAction = Attacking(activeTile)
                      //highlight attack range red and enemy troops in range blue
                      for coord <- troopAttackRange(activeTroop) do
                        val tile = game.gameLevel.tileAt(coord)
                        val critical = activeTroop.extraDamage(tile) > 0
                        val color = tile.troop match
                            case None => if critical then Red else Orange
                            case Some(troop: Troop) => if troop.controller == activeTroop.controller then
                              Orange else if critical then Blue else DarkCyan
                        highlightTile(coord, color)
                    case "Wait" =>
                      removeFocus()
                      activeTroop.exhaust()
                case None => removeFocus()

            //moves active troop if the tile clicked is legal for movement
            case Moving(activeTile) =>
              val movingTroop = activeTile.troop.get
              if troopMoveRange(movingTroop).exists(_.coords == gridCoords) && clickedTile.isPassable then
                movingTroop.move(clickedTile) //moves troop
                renderMovement(movingTroop)
              removeFocus()

            case Attacking(activeTile) =>
              val attackingTroop = activeTile.troop.get
              val target = clickedTile.troop
              //check if clicked tile is in range and contains enemy troop
              if troopAttackRange(attackingTroop).contains(gridCoords) && target.nonEmpty && target.get.controller != game.gameState.actingPlayer then
                //attack target and remove it if it dies
                if attackingTroop.attack(target.get) then
                  removeTroop(target.get)
              removeFocus()

            case BuildTroop(activeTile) =>
              BuildMenu.menuElements.find(_.tryClickMenuElement((event.getX, event.getY))) match
                case Some(menuElement) =>
                  buildTroop(menuElement.name, game.gameState.actingPlayer, activeTile)
                  removeFocus()
                case None => removeFocus()
        else
          //check if advance turn was clicked
          if advanceTurnBtn.menuElements.exists(_.tryClickMenuElement((event.getX, event.getY))) then
            removeFocus()
            advanceTurn()
            val cpu = if game.gameState.actingPlayer == RedPlayer then redCpu else blueCpu
            cpu match
              case Some(ai) => computerAct(ai)
              case None =>
        }
      def removeFocus() =
        focusHL.children.clear()
        menuUI.children.clear()
        infoUI.children.clear()
        game.currentAction = NoFocus()

      def addPopUp(menu: Menu) =
        menuUI.children.clear()
        menuUI.children += menu

      def troopMoveRange(troop: Troop) =
        game.gameLevel.tilesAtMovementRange(troop)

      def troopAttackRange(troop: Troop) =
        game.gameLevel.coordsAtRange(troop.gridCoords, troop.stats(Stat.Rng))

      def buildTroop(name: String, owner: Player, tile: Tile) =
        val troopCount = game.gameLevel.troopCount
        val troop =
          name match
            case "Solider" => Solider(owner, tile, troopCount)
            case "Sniper" => Sniper(owner, tile, troopCount)
            case "Tank" => Tank(owner, tile, troopCount)
            case "Artillery" => Artillery(owner, tile, troopCount)
            case "Apache" => Apache(owner, tile, troopCount)
        val endResources = owner.resources - troop.cost
        if endResources >= 0 then
          owner.resources = endResources
          gameInfo.updateResources(owner)
          game.gameLevel.addTroop(troop)
          createTroop(troop)
    end handleInput

    def advanceTurn() =
      game.gameState.advanceTurn()
      gameInfo.updateTurn(game.gameState.actingPlayer.toString)
      updateLines()

    //renders troop's position change
    def renderMovement(movingTroop: Troop) =
      troops.children(movingTroop.imageViewIndex)
        .relocate(gridToSceneCoordX(movingTroop.gridCoords), gridToSceneCoordY(movingTroop.gridCoords))

    //remove troop from game
    def removeTroop(troop: Troop) =
      game.gameLevel.removeTroop(troop)
      refreshTroopImages()

    def computerAct(ai: AI) =
      //build troops
      ai.updateTroops()
      ai.buildTroops()
      refreshTroopImages()
      gameInfo.updateResources(ai.player)
      ai.commitToAreas()
      //act with troops
      for troop <- ai.ownTroops do
        if game.gameLevel.tilesAtMovementRange(troop).nonEmpty then
          ai.move(troop)
          renderMovement(troop)
        ai.attack(troop) match
          case Some(target) =>
            if troop.attack(target) then
              removeTroop(target)
              ai.updateTroops()
          case None =>
      ai.updateWeights()
      //ai.areaWeights.foreach(a => println(a._1.tiles(0).coords.toString() +  a._2))


    renderGameLevel()
    handleInput()
end GameApp
