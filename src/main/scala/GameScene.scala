import javafx.scene.layout.StackPane
import scalafx.scene.layout.Background
import scalafx.scene.paint.Color
import scalafx.scene.Scene
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color.*
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.{Font, Text}
import java.io.FileInputStream
import scala.util.Random

class GameScene(root: Pane, game: Game, winWidth: Double, winHeight: Double) extends Scene(parent = root){
  //resolution of tile
  private val tileSize = 24
  //convert scene coords to grid coords
  def sceneToGridCoords(x:Double, y: Double): (Int, Int) = ((x/tileSize).toInt, (y/tileSize).toInt)
  //convert grid coords to scene coords
  def gridToSceneCoordX(gridCoords: (Int, Int)) = gridCoords._1 * tileSize
  def gridToSceneCoordY(gridCoords: (Int, Int)) = gridCoords._2 * tileSize
  //get tile and troop images
  def getTileImage(tileId: String) = Image(FileInputStream("data\\images\\tiles\\" + tileId + ".png"))
  def getTroopImage(troop: Troop) = Image(FileInputStream("data\\images\\troops\\" + troop.id + "_" + troop.controller.toString.toLowerCase + ".png"))

  private val mapWidth = tileSize * game.gameLevel.gridWidth
  private val mapHeight = tileSize * game.gameLevel.gridHeight

  root.background = Background.fill(LightCyan)
  //initialize branches of root
  private val tiles = Pane()
  private val troops = Pane()
  private val userInterface = Pane()
  private val highlights = Pane()
  root.children += tiles
  root.children += troops
  root.children += highlights
  root.children += userInterface
  //initialize branches of highlight
  private val focusHL = Pane()
  private val areaHL = Pane()
  highlights.children += focusHL
  highlights.children += areaHL
  //initialize branches of userInterface
  private val menuUI = Pane()
  private val staticUI = Pane()
  private val infoUI = Pane()
  userInterface.children += menuUI
  userInterface.children += staticUI
  userInterface.children += infoUI
  //add static UI-elements
  private val advanceTurnBtn = AdvanceTurnBtn(mapWidth-300, mapHeight)
  private var gameInfo: GameInfo = GameInfo(mapWidth, 0, game.gameState.actingPlayer)
  staticUI.children += advanceTurnBtn
  staticUI.children += gameInfo

  //highlights tile in given grid coords
  def highlightTile(gridCoords: (Int, Int), color: Color) =
    val rectangle = new Rectangle:
      x = gridToSceneCoordX(gridCoords)
      y = gridToSceneCoordY(gridCoords)
      width = tileSize
      height = tileSize
      fill.set(color.opacity(0.4))
    focusHL.children += rectangle

  //outlines area
  def outlineArea(gridCoords: Vector[(Int, Int)], color: Color) =
    val minX = gridCoords.map(_._1).min
    val maxX = gridCoords.map(_._1).max
    val minY = gridCoords.map(_._2).min
    val maxY = gridCoords.map(_._2).max
    val lineWidth = 2

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

  //updates color of area lines according to controller of the area
  def updateLines() =
    for area <- game.gameLevel.areas do
      val controlColor =
        area.controller match
          case Some(player: Player) => player.color
          case None => Gray
      outlineArea(area.tiles.map(_.coords), controlColor)

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

    //render building to areas
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
  end renderGameLevel

  //stores current action
  private var currentAction: Action = NoFocus()
  //listens for mouse clicks and then does things according to the mouse position and what is current action
  def handleInput() =
    root.onMouseClicked = event => {
      if game.gameState.winner.isEmpty then
        val actingPlayer = game.gameState.actingPlayer
        //check if click was inside map
        if event.getY < mapHeight && event.getX < mapWidth then
          val gridCoords = sceneToGridCoords(event.getX, event.getY)
          val clickedTile = game.gameLevel.tileAt(gridCoords)

          currentAction match
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
              //check if clicked tile contains non-exhausted troop that acting non-cpu player controls
              val clickedTroop = clickedTile.troop
              if clickedTroop.nonEmpty then
                infoUI.children += TroopInfo(0, mapHeight, clickedTroop.get)
                if !actingPlayer.isCPU && !clickedTroop.get.exhausted && clickedTroop.get.controller == actingPlayer then
                  currentAction = TroopFocus(clickedTile)
                  if clickedTroop.get.hasMoved then
                    addPopUp(TroopAttackMenu)
                  else
                    addPopUp(TroopMenu)
              else
                //check if clicked tile is in acting player base
                game.gameLevel.areas.take(2).find(_.tiles.contains(clickedTile)) match
                  case Some(base: Base) =>
                    if !actingPlayer.isCPU && base.controller.contains(actingPlayer) then
                      addPopUp(BuildMenu)
                      currentAction = BuildTroop(clickedTile)
                    else currentAction = TileFocus(clickedTile)
                  case _ => currentAction = TileFocus(clickedTile)

            //changes current action based on what menu element was clicked
            case TroopFocus(activeTile) =>
              val activeTroop = activeTile.troop.get
              val currentUI =
                if activeTroop.hasMoved then
                  TroopAttackMenu
                else TroopMenu
              currentUI.menuElements.find(_.tryClick((event.getX, event.getY))) match
                case Some(menuElement) =>
                  menuUI.children.clear()
                  menuElement.name match
                    case "Move" =>
                      currentAction = Moving(activeTile)
                      troopMoveRange(activeTroop).filter(_ != activeTile).foreach(a =>
                        val highlightColor = if a.isPassable then LightBlue else Red
                        highlightTile(a.coords, highlightColor))
                    case "Attack" =>
                      currentAction = Attacking(activeTile)
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
              if troopAttackRange(attackingTroop).contains(gridCoords) && target.nonEmpty && target.get.controller != actingPlayer then
                //attack target and remove it if it dies
                if attackingTroop.attack(target.get) then
                  removeTroop(target.get)
              removeFocus()

            case BuildTroop(activeTile) =>
              BuildMenu.menuElements.find(_.tryClick((event.getX, event.getY))) match
                case Some(menuElement) =>
                  buildTroop(menuElement.name, actingPlayer, activeTile)
                  removeFocus()
                case None => removeFocus()
        else
          //check if advance turn was clicked
          if advanceTurnBtn.menuElements.exists(_.tryClick((event.getX, event.getY))) then
            removeFocus()
            if !actingPlayer.isCPU then
              advanceTurn()
            game.gameState.actingPlayer.cpu match
              case Some(ai) =>
                computerAct(ai)
                advanceTurn()
              case None =>
      }
    def removeFocus() =
      focusHL.children.clear()
      menuUI.children.clear()
      infoUI.children.clear()
      currentAction = NoFocus()

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

  //advances turn and makes all relevant things for advancing turn
  def advanceTurn() =
    game.gameState.advanceTurn()
    gameInfo.updateTurn(game.gameState.actingPlayer.toString)
    updateLines()
    game.gameState.winner match
      case Some(winner) => gameEndScreen(winner)
      case None =>

  //renders troop's position change
  def renderMovement(movingTroop: Troop) =
    troops.children(movingTroop.imageViewIndex)
      .relocate(gridToSceneCoordX(movingTroop.gridCoords), gridToSceneCoordY(movingTroop.gridCoords))

  //remove troop from game
  def removeTroop(troop: Troop) =
    game.gameLevel.removeTroop(troop)
    refreshTroopImages()

  //given AI-player does it's turn
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

  //displays game-end screen with given winner
  def gameEndScreen(winner: Player) =
    val bg = new Rectangle:
      width = winWidth
      height = winHeight
      fill.set(Black.opacity(0.8))
    val text = new Text(winner.toString + " is winner!"):
      font = new Font(50)
      fill = winner.color
    val gameEndScreen = new StackPane()
    gameEndScreen.getChildren.addAll(bg, text)
    root.children += gameEndScreen


  renderGameLevel()
  handleInput()

}
